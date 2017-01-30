package neurevolve.network;

/**
 * A potential action to take when a neuron is activated.
 */
@FunctionalInterface
public interface Activity {

    /**
     * Called when a neuron with this activity is activated.
     */
    void perform();
}
