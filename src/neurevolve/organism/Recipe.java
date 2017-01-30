package neurevolve.organism;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.stream.IntStream;

/**
 * A <code>Recipe</code> is a list of instructions that can be used to build a new organism. The
 * instructions are contains in an array of integers which also contain the values for the
 * instructions. Any number of instructions and values can be added, with the array automatically
 * expanding when required.
 */
public class Recipe {

    private static final int INITIAL_CAPACITY = 50;
    private static final int EXPANSION_FACTOR = 3;

    private int[] instructions = new int[INITIAL_CAPACITY];
    private int size = 0;

    /**
     * Construct a new <code>Recipe</code> with a given activation function
     *
     * @param function the activation function to use with any organisms built by this recipe
     */
    public Recipe() {
    }

    /**
     * Get the total number of instructions and values in the recipe
     *
     * @return the number of instructions and values
     */
    public int size() {
        return size;
    }

    /**
     * Add a new instruction at the end of the recipe
     *
     * @param instruction the instruction to add
     */
    public void add(Instruction instruction) {
        add(instruction.getCode());
    }

    /**
     * Add a new value at the end of the recipe. This value will be used by the last instruction.
     *
     * @param value the value to add
     */
    public void add(int value) {
        expandIfNecessary();
        instructions[size++] = value;
    }

    private void expandIfNecessary() {
        if (size == instructions.length)
            instructions = Arrays.copyOf(instructions, size * EXPANSION_FACTOR);
    }

    /**
     * Make a new organism using this recipe.
     *
     * @param environment the environment the organism will exist within
     * @param initialHealth the initial health of the constructed organism
     * @return the constructed organism
     */
    public Organism make(Environment environment, int initialHealth) {
        Organism organism = new Organism(environment, initialHealth);
        makeCopy(environment).applyTo(organism);
        return organism;
    }

    private Recipe makeCopy(Environment environment) {
        Recipe copy = new Recipe();
        copy.instructions = Arrays.copyOf(instructions, instructions.length);
        copy.size = size;
        return copy;
    }

    private void applyTo(Organism organism) {
        Queue<Integer> values = new ArrayDeque<>();
        IntStream.range(0, size).forEach(i -> values.add(instructions[i]));
        while (!values.isEmpty()) {
            int code = values.remove();
            Instruction.decode(code)
                    .ifPresent(c -> c.complete(organism, values));
        }
        organism.setRecipe(this);
    }
}