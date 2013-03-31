package persistence;

import org.jdom.Comment;
import org.jdom.Element;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * This is a simple object to XML serializer. The <code>SimpleWriter</code> class
 * implements <code>ObjectWriter</code>. It writes (converts) an object
 * to a JDOM element. The JDOM element represents the object in standard XML
 * defined by WOX. The resulting XML (JDOM element) can be used to be
 * stored in an XML file, or other storage media. For more information about
 * the XML representation please visit: http://woxserializer.sourceforge.net/
 *
 * @author Simon M. Lucas <br />
 *         Carlos R. Jaimez Gonzalez
 * @version SimpleWriter.java - 1.0
 */
public class SimpleWriter extends TypeMapping implements ObjectWriter {

    HashMap map;
    int count;
    boolean writePrimitiveTypes = true;
    boolean doStatic = true;
    boolean doFinal = false;

    public SimpleWriter() {
        map = new HashMap();
        count = 0;

    }

    public Element write(Object ob) {
        Element el;
        if (ob == null) {
            return new Element(OBJECT);
        }

        if (map.get(ob) != null) {
            el = new Element(OBJECT);
            el.setAttribute(IDREF, map.get(ob).toString());
            return el;
        }

        map.put(ob, new Integer(count++));
        if (Util.stringable(ob)) {
            el = new Element(OBJECT);
            String woxType = (String) mapJavaToWOX.get(ob.getClass());
            el.setAttribute(TYPE, woxType);
            el.setAttribute(VALUE, stringify(ob));
        } else if (ob.getClass().isArray()) {
            el = writeArray(ob);
        }
        else if (ob instanceof ArrayList) {
            el = writeArrayList(ob);
        }
        else if (ob instanceof HashMap) {
            el = writeHashMap(ob);
        } else {
            el = new Element(OBJECT);
            el.setAttribute(TYPE, ob.getClass().getName());
            writeFields(ob, el);
        }
        el.setAttribute(ID, map.get(ob).toString());
        return el;
    }

    private Element writeHashMap(Object ob) {
        Element el = new Element(OBJECT);
        el.setAttribute(TYPE, MAP);
        HashMap hashMap = (HashMap) ob;
        Set keys = hashMap.entrySet();
        Iterator it = keys.iterator();
        while (it.hasNext()) {
            Map.Entry entryMap = (Map.Entry) it.next();
            el.addContent(writeMapEntry(entryMap));
        }
        return el;
    }

    private Element writeMapEntry(Object ob) {
        Element el = new Element(OBJECT);
        el.setAttribute(TYPE, ENTRY);
        Map.Entry entry = (Map.Entry) ob;
        el.addContent(writeMapEntryKey(entry.getKey()));
        el.addContent(writeMapEntryKey(entry.getValue()));
        return el;
    }

    private Element writeMapEntryKey(Object ob) {
        return write(ob);
    }

    private Element writeMapEntryValue(Object ob) {
        return write(ob);
    }

    private Element writeArrayList(Object ob) {
        Element el = new Element(OBJECT);
        el.setAttribute(TYPE, ARRAYLIST);
        ArrayList list = (ArrayList) ob;
        ob = list.toArray();

        return writeObjectArrayGeneric(ob, el);
    }

    private Element writeArray(Object ob) {
        if (isPrimitiveArray(ob.getClass())) {
            return writePrimitiveArray(ob);
        } else {
            return writeObjectArray(ob);
        }
    }

    private Element writeObjectArray(Object ob) {
        Element el = new Element(OBJECT);
        el.setAttribute(TYPE, ARRAY);
        return writeObjectArrayGeneric(ob, el);
    }

    private Element writeObjectArrayGeneric(Object ob, Element el) {
        String woxType = (String) mapJavaToWOX.get(ob.getClass().getComponentType());
        if (woxType == null) {
            woxType = (String) mapArrayJavaToWOX.get(ob.getClass().getComponentType());
            if (woxType != null) {
                el.setAttribute(ELEMENT_TYPE, woxType);
            } else {
                if (ob.getClass().getComponentType().getName().equals("java.lang.Object")) {
                    el.setAttribute(ELEMENT_TYPE, "Object");
                }
                else {
                    el.setAttribute(ELEMENT_TYPE, ob.getClass().getComponentType().getName());
                }
            }
        } else {
            el.setAttribute(ELEMENT_TYPE, woxType);
        }
        int len = Array.getLength(ob);
        el.setAttribute(LENGTH, "" + len);
        for (int i = 0; i < len; i++) {
            el.addContent(write(Array.get(ob, i)));
        }
        return el;
    }

    private Element writePrimitiveArray(Object ob) {
        Element el = new Element(OBJECT);
        el.setAttribute(TYPE, ARRAY);
         String woxType = (String) mapJavaToWOX.get(ob.getClass().getComponentType());
        el.setAttribute(ELEMENT_TYPE, woxType);
        int len = Array.getLength(ob);
        if ((ob instanceof byte[]) || (ob instanceof Byte[])) {
            if (ob instanceof Byte[]) {
                Byte[] arrayWrapperByte = (Byte[]) ob;
                byte[] arrayPrimitiveByte = new byte[arrayWrapperByte.length];
                for (int k = 0; k < arrayWrapperByte.length; k++) {
                    arrayPrimitiveByte[k] = arrayWrapperByte[k].byteValue();
                }
                el.setText(byteArrayString(arrayPrimitiveByte, el));
            }
            else {
                el.setText(byteArrayString((byte[]) ob, el));
            }
        } else {
            el.setAttribute(LENGTH, "" + len);
            el.setText(arrayString(ob, len));
        }
        return el;
    }

    private String byteArrayString(byte[] a, Element e) {
        byte[] target = EncodeBase64.encode(a);
        e.setAttribute(LENGTH, "" + target.length);
        String strTarget = new String(target);
        return strTarget;
    }

    private String arrayString(Object ob, int len) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            if (ob instanceof Class[]) {
                Class arrayElement = (Class) Array.get(ob, i);
                if (arrayElement != null) {
                    String woxType = (String) mapJavaToWOX.get(arrayElement);
                    if (woxType != null) {
                        sb.append(woxType);
                    } else {
                        woxType = (String) mapArrayJavaToWOX.get(arrayElement);
                        if (woxType != null) {
                            sb.append(woxType);
                        } else {
                            sb.append(arrayElement.getName());
                        }
                    }
                } else {
                    sb.append("null");
                }
            }
            else if ((ob instanceof char[]) || (ob instanceof Character[])) {
                 Object arrayElement = Array.get(ob, i);
                if (arrayElement != null) {
                    Character myChar = (Character) Array.get(ob, i);
                    sb.append(getUnicodeValue(myChar));
                } else {
                    sb.append("null");
                }
            }
            else {
                Object arrayElement = Array.get(ob, i);
                if (arrayElement != null) {
                    sb.append(arrayElement.toString());
                } else {
                    sb.append("null");
                }
            }
        }
        return sb.toString();
    }

    private void writeFields(Object o, Element parent) {
        Class cl = o.getClass();
        Field[] fields = getFields(cl);
        String name = null;
        for (int i = 0; i < fields.length; i++) {
            if ((doStatic || !Modifier.isStatic(fields[i].getModifiers())) &&
                    (doFinal || !Modifier.isFinal(fields[i].getModifiers())))
                try {
                    fields[i].setAccessible(true);
                    name = fields[i].getName();
                    Object value = fields[i].get(o);
                    Element field = new Element(FIELD);
                    field.setAttribute(NAME, name);
                    if (shadowed(fields, name)) {
                        field.setAttribute(DECLARED, fields[i].getDeclaringClass().getName());
                    }
                    if (fields[i].getType().isPrimitive()) {
                        if (writePrimitiveTypes) {
                            field.setAttribute(TYPE, fields[i].getType().getName());
                        }
                        if (fields[i].getType().getName().equals("char")) {
                            //System.out.println("IT IS A CHAR...");
                            Character myChar = (Character) value;
                            String unicodeValue = getUnicodeValue(myChar);
                            field.setAttribute(VALUE, unicodeValue);
                        }
                        else {
                            field.setAttribute(VALUE, value.toString());
                        }

                    }
                    else if (mapJavaToWOX.get(fields[i].getType()) != null) {
                        String woxType = (String) mapJavaToWOX.get(value.getClass());
                        field.setAttribute(TYPE, woxType);
                        field.setAttribute(VALUE, stringify(value));
                    }
                    else {
                        field.addContent(write(value));

                    }
                    parent.addContent(field);

                } catch (Exception e) {
                    System.out.println("Error on -> " + cl.getName() + ": field ->" + name);
                    parent.addContent(new Comment(e.toString()));
                }
        }
    }

    private static String getUnicodeValue(Character character) {
        int asciiValue = (int) character.charValue();
        String hexValue = Integer.toHexString(asciiValue);
        String unicodeValue = "\\u" + fillWithZeros(hexValue);
        return unicodeValue;
    }

    private static String fillWithZeros(String hexValue) {
        int len = hexValue.length();
        switch (len) {
            case 1:
                return ("000" + hexValue);
            case 2:
                return ("00" + hexValue);
            case 3:
                return ("0" + hexValue);
            default:
                return hexValue;
        }
    }


    private boolean shadowed(Field[] fields, String fieldName) {
        int count = 0;
        for (int i = 0; i < fields.length; i++) {
            if (fieldName.equals(fields[i].getName())) {
                count++;
            }
        }
        return count > 1;
    }

    private static String stringify(Object ob) {
        if (ob instanceof Class) {
            String woxType = (String) mapJavaToWOX.get((Class) ob);
            if (woxType != null) {
                return woxType;
            } else {
                return ((Class) ob).getName();
            }
        }
        else if (ob instanceof Character) {
            return (getUnicodeValue((Character) ob));
        }
        else {
            return ob.toString();
        }
    }

    private static Field[] getFields(Class c) {
        Vector v = new Vector();
        while (!(c == null)) {
            Field[] fields = c.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                v.addElement(fields[i]);
            }
            c = c.getSuperclass();
        }
        Field[] f = new Field[v.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = (Field) v.get(i);
        }
        return f;
    }

    private static Object[] getValues(Object o, Field[] fields) {
        Object[] values = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            try {
                fields[i].setAccessible(true);
                values[i] = fields[i].get(o);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return values;
    }

    private boolean isPrimitiveArray(Class c) {
        for (int i = 0; i < primitiveArrays.length; i++) {
            if (c.equals(primitiveArrays[i])) {
                return true;
            }
        }
        return false;
    }
}
