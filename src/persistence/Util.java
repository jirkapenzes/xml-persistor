package persistence;

import sun.reflect.ReflectionFactory;

import java.lang.reflect.Constructor;
import java.security.AccessController;

/**
 * The <code>Util</code> class provides static methods that are used by
 * <code>SimpleReader</code> and <code>SimpleWriter</code>. The methods of
 * this class are used by the serialization and de-serialization processes.
 *
 * @author Carlos R. Jaimez Gonzalez <br />
 *         Simon M. Lucas
 * @version Util.java - 1.0
 */
public class Util implements Serial {

    private static final ReflectionFactory reflFactory = (ReflectionFactory)
            AccessController.doPrivileged(
                    new ReflectionFactory.GetReflectionFactoryAction());

    public static Constructor forceDefaultConstructor(Class cl) throws Exception {
        Constructor cons = Object.class.getDeclaredConstructor(new Class[0]);
        cons = reflFactory.newConstructorForSerialization(cl, cons);
        cons.setAccessible(true);
        return cons;
    }

    public static boolean stringable(Object o) {
        boolean val = (o instanceof Byte) ||
                (o instanceof Short) ||
                (o instanceof Integer) ||
                (o instanceof Long) ||
                (o instanceof Float) ||
                (o instanceof Double) ||
                (o instanceof Character) ||
                (o instanceof Boolean) ||
                (o instanceof Class) ||
                (o instanceof String);
        return val;
    }

    public static boolean stringable(Class type) {
        boolean val = (Byte.class.isAssignableFrom(type)) ||
                (Double.class.isAssignableFrom(type)) ||
                (Float.class.isAssignableFrom(type)) ||
                (Integer.class.isAssignableFrom(type)) ||
                (Long.class.isAssignableFrom(type)) ||
                (Short.class.isAssignableFrom(type)) ||
                (Boolean.class.isAssignableFrom(type)) ||
                (Character.class.isAssignableFrom(type)) ||
                (Class.class.equals(type)) ||
                (String.class.equals(type));
        return val;
    }

    public static boolean stringable(String name) {
        try {
            Class realDataType = (Class) TypeMapping.mapWOXToJava.get(name);
            if (realDataType != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean primitive(Class type) {
        for (int i = 0; i < primitives.length; i++) {
            if (primitives[i].equals(type)) {
                return true;
            }
        }
        return false;
    }

}
