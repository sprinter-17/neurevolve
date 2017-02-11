package neurevolve;

import neurevolve.organism.Recipe;
import neurevolve.organism.Replicator;

public class TestReplicator implements Replicator {

    @Override
    public Recipe copyInstructions(int[] instructions, int size, int colour) {
        Recipe recipe = new Recipe(colour);
        for (int i = 0; i < size; i++) {
            recipe.add(instructions[i]);
        }
        return recipe;
    }

}
