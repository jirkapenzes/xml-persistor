package persistence;

/**
 * The <code>EncodeBase64</code> class provides methods to encode and decode byte arrays
 * to and from base64 encoding.
 *
 * @author Carlos R. Jaimez Gonzalez <br />
 *         Simon M. Lucas
 * @version EncodeBase64.java - 1.0
 */
public class EncodeBase64 {

    private final static int MAX_LINE_LENGTH = 76;
    private final static byte EQUALS_CHAR = (byte) '=';
    private final static byte NEW_LINE_CHAR = (byte) '\n';

    private final static byte[] TABLE64 = {
            (byte) 'A', (byte) 'B', (byte) 'C', (byte) 'D', (byte) 'E', (byte) 'F',
            (byte) 'G', (byte) 'H', (byte) 'I', (byte) 'J', (byte) 'K', (byte) 'L',
            (byte) 'M', (byte) 'N', (byte) 'O', (byte) 'P', (byte) 'Q', (byte) 'R',
            (byte) 'S', (byte) 'T', (byte) 'U', (byte) 'V', (byte) 'W', (byte) 'X',
            (byte) 'Y', (byte) 'Z',
            (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f',
            (byte) 'g', (byte) 'h', (byte) 'i', (byte) 'j', (byte) 'k', (byte) 'l',
            (byte) 'm', (byte) 'n', (byte) 'o', (byte) 'p', (byte) 'q', (byte) 'r',
            (byte) 's', (byte) 't', (byte) 'u', (byte) 'v', (byte) 'w', (byte) 'x',
            (byte) 'y', (byte) 'z',
            (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5',
            (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) '+', (byte) '/'};

    private final static byte[] DECODE_TABLE64 = {
            -9, -9, -9, -9, -9, -9, -9, -9, -9,
            -5, -5,
            -9, -9,
            -5,
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9,
            -9, -9, -9, -9, -9,
            -5,
            -9, -9, -9, -9, -9, -9, -9, -9, -9, -9,
            62,
            -9, -9, -9,
            63,
            52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
            -9, -9, -9,
            -1,
            -9, -9, -9,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13,
            14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
            -9, -9, -9, -9, -9, -9,
            26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38,
            39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51,
            -9, -9, -9, -9
    };

    private final static byte WHITE_SPACE_ENC = -5;
    private final static byte EQUALS_SIGN_ENC = -1;

    public EncodeBase64() {
    }

    public static byte[] encode(byte[] source) {

        int len = source.length;
        int off = 0;
        int options = 0;

        int newLen = len * 4 / 3;
        int padding = 0;
        if ((len % 3) == 1) {
            padding = 3;
        }
        if ((len % 3) == 2) {
            padding = 2;
        }

        int breaks = newLen / 76;
        int finalSize = newLen + padding + breaks;
        byte[] encodedArray = new byte[finalSize];

        int i = 0;
        int j = 0;
        int len2 = len - 2;
        int lineLength = 0;

        for (i = 0; i < len2; i += 3, j += 4) {
            encode3Bytes(source, i, 3, encodedArray, j);
            lineLength += 4;
            if (lineLength == MAX_LINE_LENGTH) {
                encodedArray[j + 4] = NEW_LINE_CHAR;
                j++;
                lineLength = 0;
            }
        }

        if (i < len) {
            encode3Bytes(source, i, len - i, encodedArray, j);
            j += 4;
        }

        return encodedArray;

    }

    private static void encode3Bytes(byte[] source, int sourceOff, int numBytes,
                                     byte[] target, int targetOff) {
        int result = (numBytes > 0 ? ((source[sourceOff] << 24) >>> 8) : 0)
                | (numBytes > 1 ? ((source[sourceOff + 1] << 24) >>> 16) : 0)
                | (numBytes > 2 ? ((source[sourceOff + 2] << 24) >>> 24) : 0);

        switch (numBytes) {
            case 3:
                target[targetOff] = TABLE64[(result >>> 18)];
                target[targetOff + 1] = TABLE64[(result >>> 12) & 0x3f];
                target[targetOff + 2] = TABLE64[(result >>> 6) & 0x3f];
                target[targetOff + 3] = TABLE64[(result) & 0x3f];
                break;
            case 2:
                target[targetOff] = TABLE64[(result >>> 18)];
                target[targetOff + 1] = TABLE64[(result >>> 12) & 0x3f];
                target[targetOff + 2] = TABLE64[(result >>> 6) & 0x3f];
                target[targetOff + 3] = EQUALS_CHAR;
                break;
            case 1:
                target[targetOff] = TABLE64[(result >>> 18)];
                target[targetOff + 1] = TABLE64[(result >>> 12) & 0x3f];
                target[targetOff + 2] = EQUALS_CHAR;
                target[targetOff + 3] = EQUALS_CHAR;
                break;
        }
    }


    public static byte[] decode(byte[] source) {
        int len = source.length;
        int len34 = len * 3 / 4;
        byte[] targetArray = new byte[len34]; //maximum size
        int targetIndex = 0;
        byte[] aux = new byte[4];
        int auxPos = 0;
        int i = 0;
        byte sbiCrop = 0;
        byte sbiDecode = 0;

        for (i = 0; i < len; i++) {
            sbiCrop = (byte) (source[i] & 0x7f);
            sbiDecode = DECODE_TABLE64[sbiCrop];
            if (sbiDecode >= WHITE_SPACE_ENC) {
                if (sbiDecode >= EQUALS_SIGN_ENC) {
                    aux[auxPos++] = sbiCrop;
                    if (auxPos > 3) {
                        targetIndex += decode4Bytes(aux, 0, targetArray, targetIndex);
                        auxPos = 0;

                        if (sbiCrop == EQUALS_CHAR)
                            break;
                    }
                }
            } else {
                System.err.println("Bad Base64 input character at " + i + ": " + source[i] + "(decimal)");
                return null;
            }
        }
        byte[] out = new byte[targetIndex];
        System.arraycopy(targetArray, 0, out, 0, targetIndex);
        return out;
    }

    private static int decode4Bytes(byte[] source, int sourceOff,
                                    byte[] target, int targetOff) {

        if (source[sourceOff + 2] == EQUALS_CHAR) {
            int result = ((DECODE_TABLE64[source[sourceOff]] & 0xFF) << 18)
                    | ((DECODE_TABLE64[source[sourceOff + 1]] & 0xFF) << 12);
            target[targetOff] = (byte) (result >>> 16);
            return 1;
        } else if (source[sourceOff + 3] == EQUALS_CHAR) {
            int result = ((DECODE_TABLE64[source[sourceOff]] & 0xFF) << 18)
                    | ((DECODE_TABLE64[source[sourceOff + 1]] & 0xFF) << 12)
                    | ((DECODE_TABLE64[source[sourceOff + 2]] & 0xFF) << 6);
            target[targetOff] = (byte) (result >>> 16);
            target[targetOff + 1] = (byte) (result >>> 8);
            return 2;
        } else {
            try {
                int result = ((DECODE_TABLE64[source[sourceOff]] & 0xFF) << 18)
                        | ((DECODE_TABLE64[source[sourceOff + 1]] & 0xFF) << 12)
                        | ((DECODE_TABLE64[source[sourceOff + 2]] & 0xFF) << 6)
                        | ((DECODE_TABLE64[source[sourceOff + 3]] & 0xFF));
                target[targetOff] = (byte) (result >> 16);
                target[targetOff + 1] = (byte) (result >> 8);
                target[targetOff + 2] = (byte) (result);
                return 3;

            } catch (Exception e) {
                System.out.println("" + source[sourceOff] + ": " + (DECODE_TABLE64[source[sourceOff]]));
                System.out.println("" + source[sourceOff + 1] + ": " + (DECODE_TABLE64[source[sourceOff + 1]]));
                System.out.println("" + source[sourceOff + 2] + ": " + (DECODE_TABLE64[source[sourceOff + 2]]));
                System.out.println("" + source[sourceOff + 3] + ": " + (DECODE_TABLE64[source[sourceOff + 3]]));
                return -1;
            }
        }
    }
}