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

    private final int colour;
    private int[] instructions = new int[INITIAL_CAPACITY];
    private int size = 0;

    public Recipe(int colour) {
        this.colour = colour;
    }

    public int getColour() {
        return colour;
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
     * Add a new instruction at the end of the recipe along with zero or more associated values
     *
     * @param instruction the instruction to add
     * @param values zero or more values to add following the instruction
     */
    public void add(Instruction instruction, int... values) {
        add(instruction.getCode());
        for (int value : values) {
            add(value);
        }
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
     * Make a new organism using a copy of the recipe. The replicator used to copy the recipe may
     * introduce errors prior to construction.
     *
     * @param environment the environment the organism will exist within
     * @param replicator the object to use to copy the recipe before constructing the organism
     * @param initialEnergy the initial energy of the constructed organism
     * @return the constructed organism
     */
    public Organism make(Environment environment, Replicator replicator, int initialEnergy) {
        Organism organism = new Organism(environment, initialEnergy, colour);
        replicator.copyInstructions(instructions, size, colour).applyTo(organism);
        return organism;
    }

    private void applyTo(Organism organism) {
        Queue<Integer> values = instructionInQueue();
        while (!values.isEmpty()) {
            int code = values.remove();
            Instruction.decode(code).complete(organism, values);
        }
        organism.setRecipe(this);
    }

    /**
     * Convert the values to queue
     *
     * @return the instructions in a queue
     */
    protected Queue<Integer> instructionInQueue() {
        Queue<Integer> values = new ArrayDeque<>();
        IntStream.range(0, size).forEach(i -> values.add(instructions[i]));
        return values;
    }

    /**
     * Calculate the distance between this recipe and another. The distance is defined as the
     * minimum total changes required to go from one recipe to the other. A change is increasing or
     * decreasing a single value by one.
     *
     * @param other the recipe to compare
     * @return the distance to the other recipe
     */
    public int distanceTo(Recipe other) {
        // This algorithm is from https://en.wikipedia.org/wiki/Levenshtein_distance
        int distance[][] = new int[this.size + 1][other.size + 1];
        for (int i = 0; i <= this.size; i++) {
            distance[i][0] = Arrays.stream(this.instructions).limit(i).map(Math::abs).sum();
        }
        for (int j = 0; j <= other.size; j++) {
            distance[0][j] = Arrays.stream(other.instructions).limit(j).map(Math::abs).sum();
        }
        for (int i = 1; i <= this.size; i++) {
            for (int j = 1; j <= other.size; j++) {
                int val1 = this.instructions[i - 1];
                int val2 = other.instructions[j - 1];
                int cost = distance[i - 1][j - 1] + Math.abs(val1 - val2);
                cost = Math.min(cost, distance[i][j - 1] + Math.abs(val2));
                cost = Math.min(cost, distance[i - 1][j] + Math.abs(val1));
                distance[i][j] = cost;
            }
        }
        return distance[this.size][other.size];
    }

}
