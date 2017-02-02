package neurevolve.network;

import java.util.ArrayList;
import java.util.List;

/**
 * A self-contained network of neurons. Neurons may have previous neurons in the network as input.
 * Links to neurons later in the network are not allowed.
 */
public class Network {

    private final ActivationFunction function;
    private final List<Neuron> neurons = new ArrayList<>();

    /**
     * Construct a <code>Network</code> that uses the given {@link ActivationFunction}
     *
     * @param function the function to use to activate the neurons in the network
     */
    public Network(ActivationFunction function) {
        this.function = function;
    }

    /**
     * Get the size of network.
     *
     * @return the number of neurons in the network
     */
    public int size() {
        return neurons.size();
    }

    /**
     * Checks if the network is empty
     *
     * @return <code>true</code> if <tt>size() == 0</tt>
     */
    public boolean isEmpty() {
        return neurons.isEmpty();
    }

    /**
     * Add a new neuron at the end of the network
     */
    public void addNeuron() {
        neurons.add(new Neuron(function));
    }

    /**
     * Set the threshold for the last neuron to the given value
     *
     * @param threshold the threshold for the neuron
     * @throws IllegalStateException if the network is empty
     */
    public void setThreshold(int threshold) {
        lastNeuron().setThreshold(threshold);
    }

    /**
     * Add an input to the last neuron
     *
     * @param input the input to add
     * @param weight the weight of the given input
     * @throws IllegalStateException if the network is empty
     */
    public void addInput(Input input, int weight) {
        lastNeuron().addInput(input, weight);
    }

    /**
     * Add a link from a previous neuron to the last neuron. A link may only be created from a
     * previous neuron to a later neuron to avoid loops in the network.
     *
     * @param from the index of the source neuron
     * @param weight the weight of the link
     * @throws IndexOutOfBoundsException if <tt>from</tt> indexes a non-existent neuron or the last
     * neuron
     * @throws IllegalStateException if the network is empty
     */
    public void addLink(int from, int weight) {
        if (from < 0 || from >= size() - 1)
            throw new IndexOutOfBoundsException("Attempt to link to same or forward neuron");
        lastNeuron().addInput(() -> getValue(from), weight);
    }

    /**
     * Set an activity for the last neuron to fire on activation. A neuron may only have one
     * activity so this method overwrites any earlier activity for the neuron.
     *
     * @param activity the activity to fire
     * @throws IllegalStateException if the network is empty
     */
    public void setActivity(Activity activity) {
        lastNeuron().setActivity(activity);
    }

    /**
     * Activate the network. This activates each neuron in the network in turn.
     */
    public void activate() {
        neurons.forEach(Neuron::activate);
    }

    /**
     * Get the value of a neuron.
     *
     * @param neuron the index of the neuron
     * @return the value of the neuron, set at previous call to {@link #activate} (or 0 if the
     * network has not been activated.
     * @throws IndexOutOfBoundsException if <tt>neuron &lt; 0 || neuron &ge; size</tt>
     */
    public int getValue(int neuron) {
        if (neuron < 0 || neuron >= size())
            throw new IndexOutOfBoundsException("Out of range neuron index");
        return neurons.get(neuron).getValue();
    }

    /**
     * Get the last neuron added to the network
     */
    private Neuron lastNeuron() {
        if (neurons.isEmpty())
            throw new IllegalStateException("Attempt to get last neuron from empty network");
        else
            return neurons.get(neurons.size() - 1);
    }

    /**
     * Get a measure of the total complex activity of the network over its lifetime.
     *
     * @return the total number of times a neuron with an activity has switched form not firing to
     * firing.
     */
    public int getTotalActivitySwitches() {
        return neurons.stream().mapToInt(Neuron::getActivationCount).sum();
    }
}
