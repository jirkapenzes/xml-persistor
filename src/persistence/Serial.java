package persistence;

/**
 * The <code>Serial</code> interface defines the constants used in the
 * XML representation of objects. It also provides arrays of classes
 * used for mapping primitive types, and primitive arrays.
 *
 * @author Simon M. Lucas <br />
 *         Carlos R. Jaimez Gonzalez
 * @version Serial.java - 1.0
 */
public interface Serial {

    public static final String OBJECT = "object";

    public static final String FIELD = "field";

    public static final String NAME = "name";

    public static final String TYPE = "type";

    public static final String VALUE = "value";

    public static final String ARRAY = "array";

    public static final String ARRAYLIST = "list";

    public static final String ELEMENT_TYPE = "elementType";

    public static final String MAP = "map";

    public static final String KEY_TYPE = "keyType";

    public static final String VALUE_TYPE = "valueType";

    public static final String ENTRY = "entry";

    public static final String KEY = "key";

    public static final String LENGTH = "length";

    public static final String ID = "id";

    public static final String IDREF = "idref";

    public static final String DECLARED = "declaredClass";

    public static final Class[] primitiveArrays =
            new Class[]{int[].class,
                    boolean[].class,
                    byte[].class,
                    short[].class,
                    long[].class,
                    char[].class,
                    float[].class,
                    double[].class,
                    Integer[].class,
                    Boolean[].class,
                    Byte[].class,
                    Short[].class,
                    Long[].class,
                    Character[].class,
                    Float[].class,
                    Double[].class,
                    Class[].class
            };

    public static final String[] primitiveArraysWOX =
            new String[]{
                    "int",
                    "boolean",
                    "byte",
                    "short",
                    "long",
                    "char",
                    "float",
                    "double",
                    "intWrapper",
                    "booleanWrapper",
                    "byteWrapper",
                    "shortWrapper",
                    "longWrapper",
                    "charWrapper",
                    "floatWrapper",
                    "doubleWrapper",
                    "class"
            };

    public static final Class[] primitiveWrappers =
            new Class[]{
                    Integer.class,
                    Boolean.class,
                    Byte.class,
                    Short.class,
                    Long.class,
                    Character.class,
                    Float.class,
                    Double.class
            };

    public static final Class[] primitives =
            new Class[]{
                    int.class,
                    boolean.class,
                    byte.class,
                    short.class,
                    long.class,
                    char.class,
                    float.class,
                    double.class
            };
}
