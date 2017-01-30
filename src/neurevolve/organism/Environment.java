package neurevolve.organism;

import neurevolve.network.ActivationFunction;

public interface Environment {

    public ActivationFunction getActivationFunction();

    public int getInput(int input);

    public void performActivity(int activity);

    public Recipe copyInstructions(int[] instructions, int size);
}
