package neurevolve.network;

/**
 * A function that is used to map values from the input to a {@link Neuron} to the value of that
 * {@link Neuron}.
 */
@FunctionalInterface
public interface ActivationFunction {

    /**
     * Apply the function.
     *
     * @param input the input value to the function
     * @return the output value
     */
    public int apply(int input);
}
