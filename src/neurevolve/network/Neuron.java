package neurevolve.network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A <code>Neuron</code> is the unit of calculation and action in the network. It functions by
 * summing a set of weighted input values and comparing the result to a threshold. If the inputs are
 * equal to or greater than the threshold then the neuron is considered activated. It can have a
 * single activity associated with it which is performed when the neuron is activated.
 */
public class Neuron {

    public static final int WEIGHT_DIVISOR = 10;
    private final ActivationFunction function;

    private int threshold = 0;
    private int switches = 0;
    private final List<Synapse> inputs = new ArrayList<>();
    private Optional<Activity> activity = Optional.empty();
    private int[] values = new int[1];
    private int valueIndex = 0;

    /**
     * A <code>Synapse</code> represents a weighted input to the neuron
     */
    private class Synapse {

        private final Input input;
        private final int weight;

        public Synapse(Input input, int weight) {
            this.input = input;
            this.weight = weight;
        }

        public int getValue() {
            return input.getValue() * weight / WEIGHT_DIVISOR;
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
        return values[valueIndex];
    }

    /**
     * Get a measure of the complex activity of the neuron.
     *
     * @return the number of times the neuron has switched from inactivity to performing an
     * activity.
     */
    public int getActivationCount() {
        return switches;
    }

    /**
     * Set the threshold for the <code>Neuron</code>. At activation, the value is set to
     * <tt>-threshold</tt> before any inputs are added. This method will override any previous
     * thresholds.
     *
     * @param threshold the threshold value
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    /**
     * Add an input to the neuron with a specified weight. The weight of each input determines how
     * it contributes to the value of the neuron. A weight of 10 leads to the full value of the
     * input. A weight of 0 leads to no value. A weight of -10 leads to negative full value.
     *
     * @param input the input to add
     * @param weight the weight of the added input
     */
    public void addInput(Input input, int weight) {
        inputs.add(new Synapse(input, weight));
    }

    /**
     * Adds a delay between activation and the value returned by {@link #getValue}. For each delay
     * amount, one activation occurs before the current value is returned. If <code>addDelay</code>
     * is called multiple times the delays are added together.
     *
     * @param delay the number of activations before a value is available
     */
    public void addDelay(int delay) {
        values = Arrays.copyOf(values, values.length + delay);
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
     * and then applying the activation function. If the resulting value is not negative, the
     * activity is fired.
     */
    public void activate() {
        int previous = getValue();
        int value = calculateValue();
        storeValue(value);
        performActivity(previous);
    }

    private int calculateValue() {
        int value = inputs.stream().mapToInt(Synapse::getValue).sum() - threshold;
        value = function.apply(value);
        return value;
    }

    private void storeValue(int value) {
        values[valueIndex] = value;
        valueIndex = (valueIndex + 1) % values.length;
    }

    private void performActivity(int previous) {
        if (getValue() >= 0 && activity.isPresent()) {
            if (previous < 0)
                switches++;
            activity.get().perform();
        }
    }

}
