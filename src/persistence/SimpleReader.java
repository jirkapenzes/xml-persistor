package persistence;

import org.jdom.Element;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This is a simple XML to object de-serializer. The <code>SimpleReader</code> class
 * implements <code>ObjectReader</code>. It reads an object from a JDOM element
 * and puts it back to a live Java object. The XML representation of the
 * object is a standard WOX representation. For more information about
 * the XML representation please visit: http://woxserializer.sourceforge.net/
 *
 * @author Simon M. Lucas <br />
 *         Carlos R. Jaimez Gonzalez
 * @version SimpleReader.java - 1.0
 */
public class SimpleReader extends TypeMapping implements ObjectReader {

    HashMap map;
    static HashMap primitivesMap;

    static {
        primitivesMap = new HashMap();
        primitivesMap.put("float", float.class);
        primitivesMap.put("double", double.class);
        primitivesMap.put("boolean", boolean.class);
        primitivesMap.put("char", char.class);
        primitivesMap.put("int", int.class);
        primitivesMap.put("short", short.class);
        primitivesMap.put("long", long.class);
        primitivesMap.put("byte", byte.class);
    }

    public SimpleReader() {
        map = new HashMap();
    }

    public Object read(Element xob) {
        if (empty(xob)) {
            return null;
        } else if (reference(xob)) {
            return map.get(xob.getAttributeValue(IDREF));
        }

        Object ob = null;
        String id = xob.getAttributeValue(ID);

        if (isPrimitiveArray(xob)) {
            ob = readPrimitiveArray(xob, id);
        } else if (isArray(xob)) {
            ob = readObjectArray(xob, id);
        } else if (isArrayList(xob)) {
            ob = readArrayList(xob, id);
        } else if (isHashMap(xob)) {
            System.out.println("readHashMap: " + xob.getAttributeValue(TYPE));
            ob = readHashMap(xob, id);
        } else if (Util.stringable(xob.getAttributeValue(TYPE))) {
            ob = readStringObject(xob, id);
        } else {
            ob = readObject(xob, id);
        }
        return ob;
    }

    private boolean empty(Element xob) {
        return !xob.getAttributes().iterator().hasNext() &&
                !xob.getContent().iterator().hasNext();
    }

    private boolean reference(Element xob) {
        boolean ret = xob.getAttribute(IDREF) != null;
        return ret;
    }

    private boolean isPrimitiveArray(Element xob) {
        if (!xob.getAttributeValue(TYPE).equals(ARRAY)) {
            return false;
        }
        String arrayType = xob.getAttributeValue(ELEMENT_TYPE);
        for (int i = 0; i < primitiveArraysWOX.length; i++) {
            if (primitiveArraysWOX[i].equals(arrayType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isArray(Element xob) {
        return xob.getAttributeValue(TYPE).equals(ARRAY);
    }

    private boolean isArrayList(Element xob) {
        return xob.getAttributeValue(TYPE).equals(ARRAYLIST);
    }

    private boolean isHashMap(Element xob) {
        return xob.getAttributeValue(TYPE).equals(MAP);
    }

    private Object readPrimitiveArray(Element xob, Object id) {
        try {
            Class type = (Class) mapWOXToJava.get(xob.getAttributeValue(ELEMENT_TYPE));
            Class wrapperType = getWrapperType(type);

            Constructor cons = null;
            if ((!type.equals(char.class)) && (!type.equals(Character.class)) &&
                    (!type.equals(Class.class))) {
                cons = wrapperType.getDeclaredConstructor(new Class[]{String.class});
            }

            Object[] args = new Object[1];
            int len = Integer.parseInt(xob.getAttributeValue(LENGTH));
            Object array = Array.newInstance(type, len);

            if ((type.equals(byte.class)) || (type.equals(Byte.class))) {
                Object byteArray = readByteArray(xob);
                if (type.equals(Byte.class)) {
                    byte[] arrayPrimitiveByte = (byte[]) byteArray;
                    Byte[] arrayWrapperByte = new Byte[arrayPrimitiveByte.length];
                    for (int k = 0; k < arrayPrimitiveByte.length; k++) {
                        arrayWrapperByte[k] = new Byte(arrayPrimitiveByte[k]);
                    }
                    map.put(id, arrayWrapperByte);
                    return arrayWrapperByte;
                }
                else {
                    map.put(id, byteArray);
                    return byteArray;
                }
            }

            if (type.equals(char.class)) {
                Object charArray = readCharArray((char[]) array, xob);
                map.put(id, charArray);
                return charArray;
            }

            if (type.equals(Character.class)) {
                Object charArray = readCharArray((Character[]) array, xob);
                map.put(id, charArray);
                return charArray;
            }

            if (type.equals(Class.class)) {
                Object classArray = readClassArray((Class[]) array, xob);
                map.put(id, classArray);
                return classArray;
            }

            StringTokenizer st = new StringTokenizer(xob.getText());
            int index = 0;
            while (st.hasMoreTokens()) {
                String s = st.nextToken();
                if (s.equals("null")) {
                    Array.set(array, index++, null);
                }
                else {
                    args[0] = s;
                    Object value = cons.newInstance(args);
                    Array.set(array, index++, value);
                }
            }
            map.put(id, array);
            return array;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Class getPrimitiveType(String name) {
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].getName().equals(name)) {
                return primitives[i];
            }
        }
        return null;
    }

    private Class getWrapperType(Class type) {
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].equals(type)) {
                return primitiveWrappers[i];
            }
        }
        return type;
    }

    private Class getWrapperType(String type) {
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].getName().equals(type)) {
                return primitiveWrappers[i];
            }
        }
        return null;
    }

    private Object readIntArray(int[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            a[index++] = Integer.parseInt(s);
        }
        return a;
    }

    private Object readIntArray(Integer[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            //check for null values because this is a wrapper
            if (s.equals("null")) {
                a[index++] = null;
            } else {
                a[index++] = new Integer(s);
            }
        }
        return a;
    }

    private Object readByteArray(Element xob) {
        String strByte = xob.getText();
        byte[] encodedArray = strByte.getBytes();
        byte[] decodedArray = EncodeBase64.decode(encodedArray);
        return decodedArray;
    }

    private Object readCharArray(char[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            int decimalValue = getDecimalValue(s);
            a[index++] = (char) decimalValue;
        }
        return a;
    }

    private Object readCharArray(Character[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.equals("null")) {
                a[index++] = null;
            } else {
                int decimalValue = getDecimalValue(s);
                a[index++] = new Character((char) decimalValue);
            }
        }
        return a;
    }

    private Object readClassArray(Class[] a, Element xob) {
        StringTokenizer st = new StringTokenizer(xob.getText());
        int index = 0;
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (s.equals("null")) {
                a[index++] = null;
            } else {
                Class javaClass = (Class) mapWOXToJava.get(s);
                if (javaClass == null) {
                    javaClass = (Class) mapArrayWOXToJava.get(s);
                    if (javaClass == null) {
                        try {
                            a[index++] = Class.forName(s);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        a[index++] = javaClass;
                    }
                } else {
                    a[index++] = javaClass;
                }

            }
        }
        return a;
    }

    private Object readHashMap(Element xob, Object id) {
        System.out.println("Reading a HashMap...");
        HashMap newHashMap = new HashMap();
        List children = xob.getChildren();
        int index = 0;
        for (Iterator i = children.iterator(); i.hasNext(); ) {
            Element entryElement = (Element) i.next();
            List entryChildren = entryElement.getChildren();
            Object key = read((Element) entryChildren.get(0));
            Object value = read((Element) entryChildren.get(1));
            newHashMap.put(key, value);
        }
        map.put(id, newHashMap);
        return newHashMap;
    }

    private Object readArrayList(Element xob, Object id) {
        Object array = readObjectArrayGeneric(xob, id);
        ArrayList list = new ArrayList();
        for (int i = 0; i < Array.getLength(array); i++) {
            list.add(Array.get(array, i));
        }
        map.put(id, list);
        return list;
    }

    private Object readObjectArray(Element xob, Object id) {
        Object array = readObjectArrayGeneric(xob, id);
        map.put(id, array);
        return array;
    }

    private Object readObjectArrayGeneric(Element xob, Object id) {
        try {
            String arrayTypeName = xob.getAttributeValue(ELEMENT_TYPE);
            int len = Integer.parseInt(xob.getAttributeValue(LENGTH));
            Class componentType = getObjectArrayComponentType(arrayTypeName);
            Object array = Array.newInstance(componentType, len);
            List children = xob.getChildren();
            int index = 0;
            for (Iterator i = children.iterator(); i.hasNext(); ) {
                Object childArray = read((Element) i.next());
                Array.set(array, index++, childArray);
            }
            return array;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Class getObjectArrayComponentType(String arrayTypeName) throws Exception {
        Class javaClass = (Class) mapWOXToJava.get(arrayTypeName);
        if (javaClass == null) {
            javaClass = (Class) mapArrayWOXToJava.get(arrayTypeName);
            if (javaClass == null) {
                if (arrayTypeName.equals("Object")) {
                    arrayTypeName = "java.lang.Object";
                }
                return Class.forName(arrayTypeName);
            } else {
                return javaClass;
            }
        } else {
            return javaClass;
        }
    }

    private Object readStringObject(Element xob, Object id) {
        try {
            Class type = (Class) TypeMapping.mapWOXToJava.get(xob.getAttributeValue(TYPE));

            if (type.equals(Class.class)) {
                Class javaClass = (Class) mapWOXToJava.get(xob.getAttributeValue(VALUE));
                if (javaClass == null) {
                    javaClass = (Class) mapArrayWOXToJava.get(xob.getAttributeValue(VALUE));
                    if (javaClass == null) {
                        Object obClass = Class.forName(xob.getAttributeValue(VALUE));
                        map.put(id, obClass);
                        return obClass;
                    }
                    else {
                        map.put(id, javaClass);
                        return javaClass;
                    }
                }
                else {
                    map.put(id, javaClass);
                    return javaClass;
                }
            }
            else {
                if (type.equals(char.class)) {
                    int decimalValue = getDecimalValue(xob.getAttributeValue(VALUE));
                    Character charObject = new Character((char) decimalValue);
                    map.put(id, charObject);
                    return charObject;
                }
                else {
                    Object ob = this.makeWrapper(type, xob.getAttributeValue(VALUE));
                    map.put(id, ob);
                    return ob;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int getDecimalValue(String unicodeValue) {
        String unicodeModified = unicodeValue.substring(2, unicodeValue.length());
        int decimalValue = Integer.parseInt(unicodeModified, 16);
        return decimalValue;
    }

    private Object readObject(Element xob, Object id) {
        try {
            Class type = Class.forName(xob.getAttributeValue(TYPE));
            Constructor cons = Util.forceDefaultConstructor(type);
            cons.setAccessible(true);
            Object ob = makeObject(cons, new Object[0], id);
            boolean bbb = ob instanceof Method;
            setFields(ob, xob);
            return ob;
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            return null;
        }
    }

    private void setFields(Object ob, Element xob) {
        Class type = ob.getClass();
        for (Iterator i = xob.getChildren().iterator(); i.hasNext(); ) {
            Element fe = (Element) i.next();
            String name = fe.getAttributeValue(NAME);
            String declaredType = fe.getAttributeValue(DECLARED);
            try {
                Class declaringType;
                if (declaredType != null) {
                    declaringType = Class.forName(declaredType);
                } else {
                    declaringType = type;
                }
                Field field = getField(declaringType, name);
                field.setAccessible(true);
                Object value = null;
                if (Util.primitive(field.getType())) {
                    if (fe.getAttributeValue(TYPE).equals("char")) {
                        int decimalValue = getDecimalValue(fe.getAttributeValue(VALUE));
                        Character charObject = new Character((char) decimalValue);
                        value = charObject;
                    } else {
                        value = makeWrapper(field.getType(), fe.getAttributeValue(VALUE));
                    }
                }
                else if (mapWOXToJava.get(fe.getAttributeValue(TYPE)) != null) {
                    Class typeWrapper = (Class) TypeMapping.mapWOXToJava.get(fe.getAttributeValue(TYPE));
                    if (typeWrapper.equals(Character.class)) {
                        int decimalValue = getDecimalValue(fe.getAttributeValue(VALUE));
                        Character charObject = new Character((char) decimalValue);
                        value = charObject;
                    }
                    else {
                        Class[] st = {String.class};
                        Constructor cons = typeWrapper.getDeclaredConstructor(st);
                        cons.setAccessible(true);
                        value = cons.newInstance(new String[]{fe.getAttributeValue(VALUE)});
                    }

                } else {
                    Element child = (Element) fe.getChildren().iterator().next();
                    value = read(child);
                }
                field.set(ob, value);
            } catch (Exception e) {
            }
        }

    }

    private Object makeObject(Constructor cons, Object[] args, Object key) throws Exception {
        cons.setAccessible(true);
        Object value = cons.newInstance(args);
        map.put(key, value);
        return value;
    }

    private Object makeWrapper(Class type, String value) throws Exception {
        Class wrapperType = getWrapperType(type);
        Constructor cons = wrapperType.getDeclaredConstructor(new Class[]{String.class});
        return cons.newInstance(new Object[]{value});
    }

    private Field getField(Class type, String name) throws Exception {
        if (type == null) {
            return null;
        }
        try {
            return type.getDeclaredField(name);
        } catch (Exception e) {
            return getField(type.getSuperclass(), name);
        }
    }

    private void print(Constructor[] cons) {
        for (int i = 0; i < cons.length; i++) {
        }
    }


    private Class getComponentType(String type) {
        for (int i = 0; i < primitiveArrays.length; i++) {
            if (primitiveArrays[i].getName().equals(type)) {
                return primitives[i];
            }
        }
        return null;
    }

    private Class getArrayType(String type) {
        for (int i = 0; i < primitiveArrays.length; i++) {
            if (primitiveArrays[i].getName().equals(type)) {
                return primitiveArrays[i];
            }
        }
        return null;
    }
}
