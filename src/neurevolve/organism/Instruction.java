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
    ADD_NEURON("+", Instruction::addNeuron, 1),
    /**
     * Add a link to the last neuron from the neuron indexed in the next value and with the weight
     * specified in the following value. If no neuron has been added or if the next value does not
     * index an existing neuron then this instruction is ignored.
     */
    ADD_LINK("^", Instruction::addLink, 2),
    /**
     * Add an input to the last neuron. The code for the input is given in the next value.
     */
    ADD_INPUT("<-", Instruction::addInput, 2),
    /**
     * Set an activity to perform if the last neuron is activated. The code for the activity is
     * given in the next value.
     */
    SET_ACTIVITY("->", Instruction::setActivity, 1);

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
            int weight = value(values);
            if (from >= 0 && from < organism.getBrain().size() - 1)
                organism.getBrain().addLink(from, weight);
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
     * Generate a human-readable representation of the instruction
     *
     * @param printer the object to use to print input and activities for this instruction
     * @param values the queue of values
     * @return a human-readable string representing the instruction
     */
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
