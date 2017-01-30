package neurevolve.network;

/**
 * A function that can be used as an activation function in a {@link Network}. The defining feature
 * of a sigmoid is that, at its upper and lower limits it has a gradient approaching zero. A cached
 * array generated at construction is used to make the {@link #apply} method as efficient as
 * possible.
 */
public class SigmoidFunction implements ActivationFunction {

    private final int range;
    private final int[] values;

    /**
     * Construct a function that generates values in a given range
     *
     * @param range the negative and positive limits to the function. The function generates values
     * in the inclusive range <tt>[-range, +range]</tt> which has a total size of
     * <tt>range * 2 + 2</tt>.
     */
    public SigmoidFunction(int range) {
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
