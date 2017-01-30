package neurevolve.organism;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.stream.IntStream;
import neurevolve.network.ActivationFunction;

public class Recipe {

    private static final int INITIAL_CAPACITY = 50;

    private final ActivationFunction function;
    private int[] instructions = new int[INITIAL_CAPACITY];
    private int size = 0;

    public Recipe(ActivationFunction function) {
        this.function = function;
    }

    public int size() {
        return size;
    }

    public void add(Instruction instruction) {
        add(instruction.getCode());
    }

    public void add(int value) {
        if (size == instructions.length)
            instructions = Arrays.copyOf(instructions, size * 2);
        instructions[size++] = value;
    }

    public Organism make(int initialHealth) {
        Organism organism = new Organism(function, initialHealth);
        Queue<Integer> values = new ArrayDeque<>();
        IntStream.range(0, size).forEach(i -> values.add(instructions[i]));
        while (!values.isEmpty()) {
            int code = values.remove();
            Instruction.decode(code)
                    .ifPresent(c -> c.complete(organism, values));
        }
        return organism;
    }

}
