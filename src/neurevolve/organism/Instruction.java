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
     * Add a new neuron with a given threshold.
     */
    ADD_NEURON("Add", Instruction::addNeuron, 1),
    /**
     * Add a link to the last neuron from the neuron indexed in the next value and with the weight
     * specified in the following value.
     */
    ADD_LINK("Link", Instruction::addLink, 2),
    /**
     * Add an input to the last neuron
     */
    ADD_INPUT("Input", Instruction::addInput, 2),
    /**
     * Set an activity to perform if the last neuron is activated.
     */
    SET_ACTIVITY("Action", Instruction::setActivity, 1);

    private interface Operation {

        void operate(Organism organism, Queue<Integer> values);
    }

    private final String name;
    private final Operation operation;
    private final int valueCount;

    private Instruction(String name, Operation operation, int valueCount) {
        this.name = name;
        this.operation = operation;
        this.valueCount = valueCount;
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
        if (code < 0)
            return Optional.empty();
        else
            return Optional.of(values()[code % values().length]);
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

    public String toString(RecipePrinter printer, Queue<Integer> values) {
        StringBuilder builder = new StringBuilder();
        switch (this) {
            case ADD_INPUT:
                builder.append(printer.getInput(value(values)));
                builder.append("(").append(value(values)).append(")");
                break;
            case SET_ACTIVITY:
                builder.append(printer.getActivity(value(values)));
                break;
            default:
                builder.append(name);
                builder.append("(");
                for (int i = 0; i < valueCount; i++) {
                    if (i > 0)
                        builder.append(",");
                    builder.append(value(values));
                }
                builder.append(")");
                break;
        }
        return builder.toString();
    }

    private static int value(Queue<Integer> values) {
        if (values.isEmpty())
            return 0;
        else
            return values.remove();
    }
}
