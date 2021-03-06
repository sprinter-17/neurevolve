package neurevolve.organism;

/**
 * An interface between an organism and the world. This interface supplies all the services required
 * to allow the organism to interact with the world.
 */
public interface Environment {

    /**
     * Apply the environment's {@link neurevolve.network.ActivationFunction}
     *
     * @param input input value
     * @return activation value
     */
    public int applyActivationFunction(int input);

    /**
     * Get an input value from the environment
     *
     * @param organism the organism containing the neuron requesting the value
     * @param input code for the input value to retrieve
     * @return value of input for the given organism in this environment
     */
    public int getInput(Organism organism, int input);

    public String describeInput(int input);

    /**
     * Perform an activity in the environment
     *
     * @param organism the organism performing the activity
     * @param activity code for the activity to perform
     */
    public void performActivity(Organism organism, int activity);

    public String describeActivity(int activity);

}
