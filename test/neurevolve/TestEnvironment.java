package neurevolve;

import java.util.HashSet;
import java.util.Set;
import neurevolve.organism.Environment;
import neurevolve.organism.Organism;

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
    public String describeInput(int input) {
        return "Input" + input;
    }

    @Override
    public String describeActivity(int activity) {
        return "Activity" + activity;
    }

}
