package neurevolve.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A <code>Neuron</code> is the unit of calculation and action in the network
 */
public class Neuron {

    private final ActivationFunction function;

    private int value;
    private int threshold = 0;
    private final List<Synapse> inputs = new ArrayList<>();
    private Optional<Activity> activity = Optional.empty();

    private class Synapse {

        private final Input input;
        private final int weight;

        public Synapse(Input input, int weight) {
            this.input = input;
            this.weight = weight;
        }

        public int getValue() {
            return input.getValue() * weight / 10;
        }
    }

    /**
     * Construct a <code>Neuron</code> with a given activation function
     *
     * @param function the activation function to use
     */
    public Neuron(ActivationFunction function) {
        this.function = function;
    }

    /**
     * Get the current value of the neuron, as set at the previous activation.
     *
     * @return the value determined at the previous call to {@link #activate}, or 0 if there has
     * been no activations of the neuron.
     */
    public int getValue() {
        return value;
    }

    /**
     * Set the threshold for the <code>Neuron</code>. At activation, the value is set to
     * <tt>-threshold</tt> before any inputs are added.
     *
     * @param threshold the threshold value
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Add an input to the neuron with a specified weight. The weight of each input determines how
     * it contributes to the value of the neuron. A weight of 100 leads to the full value of the
     * input. A weight of 0 leads to no value.
     *
     * @param input the input to add
     * @param weight the weight of the added input
     */
    public void addInput(Input input, int weight) {
        inputs.add(new Synapse(input, weight));
    }

    /**
     * Set the activity to occur at activation, if the inputs meet the threshold.
     *
     * @param activity the activity to perform if the neuron fires during activation
     */
    public void setActivity(Activity activity) {
        this.activity = Optional.of(activity);
    }

    /**
     * Activate the neuron, calculating its value and firing the activity if the threshold is met.
     * The value is determined by adding the value of all weighted inputs, subtracting the threshold
     * and then applying the activation function. If the resulting value is positive, the activity
     * is fired.
     */
    public void activate() {
        value = inputs.stream().mapToInt(Synapse::getValue).sum() - threshold;
        value = function.apply(value);
        if (value > 0)
            activity.ifPresent(Activity::perform);
    }
}
