package neurevolve.organism;

/**
 * An interface between an organism and the world. This interface supplies all the services required
 * to allow the organism to interact with the world.
 */
public interface Environment {

    /**
     * Apply the environment's {@link ActivationFunction}
     *
     * @param input input value
     * @return activation value
     */
    public int applyActivationFunction(int input);

    /**
     * Get an input value from the environment
     *
     * @param input code for the input value to retrieve
     * @return value of input in the environment
     */
    public int getInput(int input);

    /**
     * Perform an activity in the environment
     *
     * @param activity code for the activity to perform
     */
    public void performActivity(int activity);

    /**
     * Copy a set of instructions in a {@link Recipe} during organism's division. This is part of
     * the environment because the transcription errors will be dependent on mutation rate and other
     * context.
     *
     * @param instructions the set of recipe instructions to copy
     * @param size the number of instructions in the recipe
     * @return a new copy of the instructions, including possible transcription errors.
     */
    public Recipe copyInstructions(int[] instructions, int size);
}
