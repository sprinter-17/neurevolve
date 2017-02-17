package neurevolve.world;

/**
 * Each {@code GroundElement} represents the attributes for each position in the world. This enum is
 * designed to support several element values to be packed into a single {@code int}. Each elements
 * is packed into a given number of bits. It allows values in the range {@code 0} to
 * {@code 2 ^ bits - 1} inclusive;
 */
public enum GroundElement {
    ACID(1),
    WALL(ACID, 1),
    RADIATION(WALL, 2),
    ELEVATION(RADIATION, 8),
    RESOURCES(ELEVATION, 8);

    private final int shift;
    private final int bits;
    private final int max;
    private final int mask;

    private GroundElement(GroundElement previous, int bits) {
        this(previous.shift + previous.bits, bits);
    }

    private GroundElement(int bits) {
        this(0, bits);
    }

    private GroundElement(int shift, int bits) {
        this.shift = shift;
        this.bits = bits;
        this.max = (1 << bits) - 1;
        this.mask = max << shift;
    }

    /**
     * Get the largest allowed value for this element.
     *
     * @return {@code 2 ^ bits - 1}
     */
    public int getMaximum() {
        return max;
    }

    /**
     * Get the value of the element from a packed int.
     *
     * @param input the input to get the value for.
     * @return the value.
     */
    public int get(int input) {
        return (input & mask) >> shift;
    }

    /**
     * Set the value of an element in a packed int.
     *
     * @param input the int to set the value for
     * @param data the value of the element
     * @return the resulting int with the element's value set
     */
    public int set(int input, int data) {
        if (data > max || data < 0) {
            throw new IllegalArgumentException("Data out of range");
        }
        return (input & ~mask) | (data << shift) & mask;
    }

}
