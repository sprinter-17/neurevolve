package neurevolve.organism;

import java.util.Optional;
import java.util.Queue;
import neurevolve.network.Activity;
import neurevolve.network.Input;

/**
 * The possible instructions that can be included in a {@link Recipe}. The instruction is executed
 * using the {@link #complete} method.
 */
public enum Instruction {
    /**
     * Add a new neuron
     */
    ADD_NEURON(Instruction::addNeuron),
    /**
     * Add a link to the last neuron from the neuron indexed in the next value and with the weight
     * specified in the following value.
     */
    ADD_LINK(Instruction::addLink),
    ADD_INPUT(Instruction::addInput),
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
     * Get the <code>Instruction</code> represented by a given code, if any
     *
     * @param code the code to convert to an <code>Instruction</code>
     * @return the instruction, or <tt>Optional.empty()</tt> if there is no instruction for the code
     */
    public static Optional<Instruction> decode(int code) {
        if (code < 0 || code >= values().length)
            return Optional.empty();
        else
            return Optional.of(values()[code]);
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

    private static void addNeuron(Organism organism, Queue<Integer> values) {
        organism.getBrain().addNeuron();
        int threshold = value(values);
        organism.getBrain().setThreshold(threshold);
    }

    private static void addLink(Organism organism, Queue<Integer> values) {
        if (!organism.getBrain().isEmpty()) {
            int from = value(values);
            int weight = value(values);
            if (from >= 0 && from < organism.getBrain().size() - 1)
                organism.getBrain().addLink(from, weight);
        }
    }

    private static void addInput(Organism organism, Queue<Integer> values) {
        if (!organism.getBrain().isEmpty()) {
            Input input = organism.getInput(value(values));
            int weight = value(values);
            organism.getBrain().addInput(input, weight);
        }
    }

    private static void setActivity(Organism organism, Queue<Integer> values) {
        if (!organism.getBrain().isEmpty()) {
            Activity activity = organism.getActivity(value(values));
            organism.getBrain().setActivity(activity);
        }
    }

    private static int value(Queue<Integer> values) {
        if (values.isEmpty())
            return 0;
        else
            return values.remove();
    }
}
