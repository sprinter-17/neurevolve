package neurevolve.organism;

import java.util.Optional;
import java.util.Queue;

public enum Instruction {
    ADD_NEURON(Instruction::addNeuron),
    SET_THRESHOLD(Instruction::setThreshold),
    ADD_LINK(Instruction::addLink),;

    public interface Operation {

        void operate(Organism organism, Queue<Integer> values);
    }

    private final Operation operation;

    private Instruction(Operation operation) {
        this.operation = operation;
    }

    public int getCode() {
        return ordinal();
    }

    public static Optional<Instruction> decode(int code) {
        if (code < 0 || code > values().length)
            return Optional.empty();
        else
            return Optional.of(values()[code]);
    }

    public void complete(Organism organism, Queue<Integer> values) {
        operation.operate(organism, values);
    }

    private static void addNeuron(Organism organism, Queue<Integer> values) {
        organism.getBrain().addNeuron();
    }

    private static void setThreshold(Organism organism, Queue<Integer> values) {
        int threshold = value(values);
        organism.getBrain().setThreshold(threshold);
    }

    private static void addLink(Organism organism, Queue<Integer> values) {
        int from = value(values);
        int weight = value(values);
        if (from >= 0 && from < organism.size() - 1)
            organism.getBrain().addLink(from, weight);
    }

    private static int value(Queue<Integer> values) {
        if (values.isEmpty())
            return 0;
        else
            return values.remove();
    }
}
