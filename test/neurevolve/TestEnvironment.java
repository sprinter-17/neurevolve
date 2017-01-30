package neurevolve;

import java.util.HashSet;
import java.util.Set;
import neurevolve.network.ActivationFunction;
import neurevolve.organism.Environment;

public class TestEnvironment implements Environment {

    private Set<Integer> activitiesPerformed = new HashSet<>();

    @Override
    public ActivationFunction getActivationFunction() {
        return n -> n;
    }

    @Override
    public int getInput(int input) {
        return 10 * input;
    }

    @Override
    public void performActivity(int activity) {
        activitiesPerformed.add(activity);
    }

    public boolean isPerformed(int activity) {
        return activitiesPerformed.contains(activity);
    }

}
