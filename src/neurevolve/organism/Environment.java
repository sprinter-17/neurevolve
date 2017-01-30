package neurevolve.organism;

import neurevolve.network.ActivationFunction;

/**
 * An interface between and organism and the environment it exists within
 */
public interface Environment {

    public ActivationFunction getActivationFunction();

    public int getInput(int input);

    public void performActivity(int activity);

    public Recipe copyInstructions(int[] instructions, int size);
}
