package neurevolve.organism;

public class Code {

    public static final byte ZERO = (byte) 0;

    private static final int NEGATIVE_MASK = 1 << 7;

    public static byte mod(byte code, int div) {
        return (byte) (abs(code) % div);
    }

    public static byte abs(byte code) {
        return (byte) (code & ~NEGATIVE_MASK);
    }

    public static byte fromInt(int code) {
        int val = Math.floorMod(Math.abs(code), NEGATIVE_MASK);
        if (code < 0)
            return (byte) (val | NEGATIVE_MASK);
        else
            return (byte) val;
    }

    public static int toInt(byte code) {
        return (code & NEGATIVE_MASK) == NEGATIVE_MASK ? -abs(code) : abs(code);
    }
}
