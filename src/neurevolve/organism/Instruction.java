package neurevolve.organism;

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
    ADD_NEURON(Instruction::addNeuron, 1, 1),
    /**
     * Add a link to the last neuron from the neuron indexed in the next value and with the weight
     * specified in the following value. If no neuron has been added or if the next value does not
     * index an existing neuron then this instruction is ignored.
     */
    ADD_LINK(Instruction::addLink, 2, 4),
    /**
     * Add an input to the last neuron. The code for the input is given in the next value.
     */
    ADD_INPUT(Instruction::addInput, 2, 7),
    /**
     * Add a delay to the last neuron. The amount to delay is given in the next value.
     */
    ADD_DELAY(Instruction::addDelay, 1, 8),
    /**
     * Set an activity to perform if the last neuron is activated. The code for the activity is
     * given in the next value.
     */
    SET_ACTIVITY(Instruction::setActivity, 1, 9);

    @FunctionalInterface
    public interface Processor {

        void process(Instruction instruction, int... values);
    }

    private interface Operation {

        void operate(Organism organism, int... values);
    }

    private final Operation operation;
    private final int valueCount;
    private final int code;

    private Instruction(Operation operation, int valueCount, int code) {
        this.operation = operation;
        this.valueCount = valueCount;
        this.code = code;
    }

    /**
     * Get the integer code for this instruction.
     *
     * @return the <code>int</code> code for this instruction
     */
    public int getCode() {
        return code - 1;
    }

    /**
     * Get the <code>Instruction</code> represented by a given code. All codes are able to be
     * converted to an instruction.
     *
     * @param code the code to convert to an <code>Instruction</code>
     * @return the instruction
     */
    public static Instruction decode(int code) {
        code = Math.floorMod(code, SET_ACTIVITY.code);
        for (Instruction instruction : values()) {
            if (code < instruction.code)
                return instruction;
        }
        throw new IllegalStateException("No instruction for code " + code);
    }

    public int getValueCount() {
        return valueCount;
    }

    /**
     * Perform the operation related to this instruction on the given organism using the given
     * values. If there are not enough values left in the queue, then<tt>0</tt> is used as the
     * value.
     *
     * @param organism the organism to perform the related operation on
     * @param values the values to use in performing the operation
     */
    public void complete(Organism organism, int... values) {
        operation.operate(organism, values);
    }

    /**
     * Add a neuron to the organism's network
     */
    private static void addNeuron(Organism organism, int... values) {
        assert values.length == 1;
        organism.getBrain().addNeuron();
        organism.getBrain().setThreshold(values[0]);
    }

    /**
     * Add a link to the last neuron in the organism's network
     */
    private static void addLink(Organism organism, int... values) {
        assert values.length == 2;
        if (organism.getBrain().size() > 1) {
            int from = Math.floorMod(values[0], organism.getBrain().size() - 1);
            int weight = values[1];
            organism.getBrain().addLink(from, weight);
        }
    }

    /**
     * Add an input to the last neuron in the organism's network.
     */
    private static void addInput(Organism organism, int... values) {
        assert values.length == 2;
        if (!organism.getBrain().isEmpty()) {
            Input input = organism.getInput(values[0]);
            int weight = values[1];
            organism.getBrain().addInput(input, weight);
        }
    }

    private static void addDelay(Organism organism, int... values) {
        assert values.length == 1;
        if (!organism.getBrain().isEmpty()) {
            int delay = values[0];
            if (delay > 0)
                organism.getBrain().addDelay(delay);
        }
    }

    /**
     * Set an activity for the last neuron in the organism's network
     */
    private static void setActivity(Organism organism, int... values) {
        assert values.length == 1;
        if (!organism.getBrain().isEmpty()) {
            Activity activity = organism.getActivity(values[0]);
            organism.getBrain().setActivity(activity);
        }
    }
}
