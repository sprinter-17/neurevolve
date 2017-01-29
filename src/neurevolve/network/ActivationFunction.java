package neurevolve.network;

/**
 * The function used to calculate the input for a {@link Neuron} based on a provided value.
 *
 * @author simon
 */
public class ActivationFunction {

    private final int range;
    private final int[] values;

    /**
     * Construct an <code>ActivationFunction</code> that generates values in a given range
     *
     * @param range the negative and positive limits to the function. The function generates values
     * in the inclusive range <tt>[-range, +range]</tt> which has a total size of
     * <tt>range * 2 + 2</tt>.
     */
    public ActivationFunction(int range) {
        this.range = range;
        values = new int[range * 2 + 1];
        for (int i = 0; i < values.length; i++) {
            values[i] = (int) (2 * range / (1 + Math.pow(Math.E, range - i)) - range);
        }
    }

    /**
     * Generate an output value from an input value using the function
     *
     * @param input input value
     * @return output value
     */
    public int apply(int input) {
        if (input <= -range)
            return values[0];
        else if (input >= +range)
            return values[values.length - 1];
        else
            return values[input + range];
    }

}
