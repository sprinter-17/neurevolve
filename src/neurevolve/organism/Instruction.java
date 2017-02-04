package neurevolve.organism;

import java.util.Queue;
import neurevolve.network.Activity;
import neurevolve.network.Input;

/**
 * The possible instructions that can be included in a {@link Recipe}. The instruction is executed
 * using the {@link #complete} method.
 */
public enum Instruction {
    /**
     * Add a new neuron with a given threshold.
     */
    ADD_NEURON(Instruction::addNeuron),
    /**
     * Add a link to the last neuron from the neuron indexed in the next value and with the weight
     * specified in the following value. If no neuron has been added or if the next value does not
     * index an existing neuron then this instruction is ignored.
     */
    ADD_LINK(Instruction::addLink),
    /**
     * Add an input to the last neuron. The code for the input is given in the next value.
     */
    ADD_INPUT(Instruction::addInput),
    /**
     * Add a delay to the last neuron. The amount to delay is given in the next value.
     */
    ADD_DELAY(Instruction::addDelay),
    /**
     * Set an activity to perform if the last neuron is activated. The code for the activity is
     * given in the next value.
     */
    SET_ACTIVITY(Instruction::setActivity);

    private interface Operation {

        void operate(Organism organism, Queue<Integer> values);
    }

    private final Operation operation;

    private Instruction(Operation operation) {
        this.operation = operation;
    }

    /**
     * Get the integer code for this instruction.
     *
     * @return the <code>int</code> code for this instruction
     */
    public int getCode() {
        return ordinal();
    }

    /**
     * Get the <code>Instruction</code> represented by a given code. All codes are able to be
     * converted to an instruction.
     *
     * @param code the code to convert to an <code>Instruction</code>
     * @return the instruction
     */
    public static Instruction decode(int code) {
        final int count = values().length;
        return values()[((code % count) + count) % count];
    }

    /**
     * Perform the operation related to this instruction on the given organism using the given
     * values. If there are not enough values left in the queue, then<tt>0</tt> is used as the
     * value.
     *
     * @param organism the organism to perform the related operation on
     * @param values the values to use in performing the operation
     */
    public void complete(Organism organism, Queue<Integer> values) {
        operation.operate(organism, values);
    }

    /**
     * Add a neuron to the organism's network
     */
    private static void addNeuron(Organism organism, Queue<Integer> values) {
        organism.getBrain().addNeuron();
        int threshold = value(values);
        organism.getBrain().setThreshold(threshold);
    }

    /**
     * Add a link to the last neuron in the organism's network
     */
    private static void addLink(Organism organism, Queue<Integer> values) {
        if (!organism.getBrain().isEmpty()) {
            int from = value(values);
            if (from >= 0 && from < organism.getBrain().size() - 1) {
                int weight = value(values);
                organism.getBrain().addLink(from, weight);
            }
        }
    }

    /**
     * Add an input to the last neuron in the organism's network.
     */
    private static void addInput(Organism organism, Queue<Integer> values) {
        if (!organism.getBrain().isEmpty()) {
            Input input = organism.getInput(value(values));
            int weight = value(values);
            organism.getBrain().addInput(input, weight);
        }
    }

    private static void addDelay(Organism organism, Queue<Integer> values) {
        if (!organism.getBrain().isEmpty()) {
            int delay = value(values);
            if (delay > 0)
                organism.getBrain().addDelay(delay);
        }
    }

    /**
     * Set an activity for the last neuron in the organism's network
     */
    private static void setActivity(Organism organism, Queue<Integer> values) {
        if (!organism.getBrain().isEmpty()) {
            Activity activity = organism.getActivity(value(values));
            organism.getBrain().setActivity(activity);
        }
    }

    /**
     * Get the next value from the queue, if any
     */
    private static int value(Queue<Integer> values) {
        if (values.isEmpty())
            return 0;
        else
            return values.remove();
    }
}
