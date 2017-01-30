package neurevolve.network;

/**
 * A function that is used to map values from the input to a {@link Neuron} to the value of that
 * {@link Neuron}.
 */
@FunctionalInterface
public interface ActivationFunction {

    public int apply(int input);
}
