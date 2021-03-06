package neurevolve.world;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import neurevolve.organism.Recipe;
import neurevolve.organism.Replicator;

class Mutator implements Replicator {

    private static final int MAX_RATE = 300;

    private final Random random = new Random();
    private final int mutationRate;
    private int mutationCount = 0;
    private int size = 0;

    Mutator(final int mutationRate) {
        this.mutationRate = Math.min(MAX_RATE, mutationRate);
    }

    @Override
    public Recipe copyInstructions(byte[] instructions, int size, int colour) {
        this.mutationCount = 0;
        this.size = size;
        List<Byte> copy = new LinkedList<>();
        for (int pos = 0; pos < size; pos += advance()) {
            if (pos >= 0) {
                copy.add(copy(instructions[pos]));
            }
        }
        Recipe recipe = new Recipe(colour(colour));
        copy.forEach(recipe::add);
        return recipe;
    }

    private boolean mutate() {
        boolean mutated = mutationRate > 0 && random.nextInt(MAX_RATE * size / mutationRate) == 0;
        if (mutated) {
            mutationCount++;
        }
        return mutated;
    }

    private int advance() {
        if (mutate()) {
            return random.nextInt(17) - 8;
        } else {
            return 1;
        }
    }

    private byte copy(byte code) {
        if (mutate()) {
            code ^= 1 << random.nextInt(8);
        }
        return code;
    }

    private int colour(int colour) {
        if (mutationCount > 0) {
            colour ^= 1 << random.nextInt(24);
        }
        return colour;
    }

}
