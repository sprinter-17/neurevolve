package neurevolve.network;

/**
 * An input value to a {@link Neuron}
 */
@FunctionalInterface
public interface Input {

    /**
     * Get the value of the input.
     *
     * @return the input's current value.
     */
    public int getValue();
}
