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
     * @param range the negative and positive limits to the function
     */
    public ActivationFunction(int range) {
        this.range = range;
        values = new int[range * 2 + 1];
        for (int i = 0; i < values.length; i++) {
            double output = range / (1d + Math.pow(Math.E, range - i));
            values[i] = (int) output - range;
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
