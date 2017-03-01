package neurevolve.organism;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.stream.IntStream;
import static neurevolve.organism.Code.abs;
import static neurevolve.organism.Code.toInt;

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
    private byte[] instructions = new byte[INITIAL_CAPACITY];
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
    public void add(Instruction instruction, byte... values) {
        add(instruction.getCode());
        for (byte value : values) {
            add(value);
        }
    }

    /**
     * Add a new value at the end of the recipe. This value will be used by the last instruction.
     *
     * @param value the value to add
     */
    public void add(byte value) {
        expandIfNecessary();
        instructions[size++] = value;
    }

    private void expandIfNecessary() {
        if (size == instructions.length)
            instructions = Arrays.copyOf(instructions, size * EXPANSION_FACTOR);
    }

    public void forEachInstruction(Instruction.Processor processor) {
        int i = 0;
        while (i < size) {
            byte code = instructions[i++];
            Instruction instruction = Instruction.decode(code);
            int valueCount = instruction.getValueCount();
            if (i + valueCount <= size) {
                byte[] values = Arrays.copyOfRange(instructions, i, i += valueCount);
                processor.process(instruction, values);
            } else {
                processor.junk(code);
            }
        }
    }

    public Recipe replicate(Replicator replicator) {
        return replicator.copyInstructions(instructions, size, colour);
    }

    /**
     * Convert the values to queue
     *
     * @return the instructions in a queue
     */
    protected Queue<Byte> instructionInQueue() {
        Queue<Byte> values = new ArrayDeque<>();
        IntStream.range(0, size).forEach(i -> values.add(instructions[i]));
        return values;
    }

    public boolean matches(Recipe other) {
        return this.colour == other.colour
                && this.size == other.size
                && IntStream.range(0, size)
                .allMatch(i -> this.instructions[i] == other.instructions[i]);
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
            distance[i][0] = 0;
            for (int j = 0; j < i; j++) {
                distance[i][0] += toInt(abs(this.instructions[j]));
            }
        }
        for (int j = 0; j <= other.size; j++) {
            distance[0][j] = 0;
            for (int i = 0; i < j; i++) {
                distance[0][j] += toInt(abs(other.instructions[i]));
            }
        }
        for (int i = 1; i <= this.size; i++) {
            for (int j = 1; j <= other.size; j++) {
                int val1 = toInt(this.instructions[i - 1]);
                int val2 = toInt(other.instructions[j - 1]);
                int cost = distance[i - 1][j - 1] + Math.abs(val1 - val2);
                cost = Math.min(cost, distance[i][j - 1] + Math.abs(val2));
                cost = Math.min(cost, distance[i - 1][j] + Math.abs(val1));
                distance[i][j] = cost;
            }
        }
        return distance[this.size][other.size];
    }

}
