package neurevolve;

import java.util.HashSet;
import java.util.Set;
import neurevolve.organism.Environment;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;

public class TestEnvironment implements Environment {

    private final Set<Integer> activitiesPerformed = new HashSet<>();

    @Override
    public int applyActivationFunction(int input) {
        return input;
    }

    @Override
    public int getInput(Organism organism, int input) {
        return 10 * input;
    }

    @Override
    public void performActivity(Organism organism, int activity) {
        activitiesPerformed.add(activity);
    }

    public boolean isPerformed(int activity) {
        return activitiesPerformed.contains(activity);
    }

    @Override
    public Recipe copyInstructions(int[] instructions, int size) {
        Recipe recipe = new Recipe();
        for (int i = 0; i < size; i++) {
            recipe.add(instructions[i]);
        }
        return recipe;
    }

}
