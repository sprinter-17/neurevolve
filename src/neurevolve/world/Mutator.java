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
    public Recipe copyInstructions(int[] instructions, int size, int colour) {
        this.mutationCount = 0;
        this.size = size;
        List<Integer> copy = new LinkedList<>();
        for (int pos = 0; pos < size; pos += advance()) {
            if (pos >= 0)
                copy.add(copy(instructions[pos]));
        }
        Recipe recipe = new Recipe(colour(colour));
        copy.forEach(recipe::add);
        return recipe;
    }

    private boolean mutate() {
        boolean mutated = mutationRate > 0 && random.nextInt(MAX_RATE * size / mutationRate) == 0;
        if (mutated)
            mutationCount++;
        return mutated;
    }

    private int advance() {
        if (mutate())
            return random.nextInt(7) - 3;
        else
            return 1;
    }

    private int copy(int code) {
        if (mutate())
            code += random.nextInt(11) - 5;
        return code;
    }

    private int colour(int colour) {
        for (int i = 0; i < mutationCount; i++) {
            colour ^= 1 << random.nextInt(24);
        }
        return colour;
    }

}
