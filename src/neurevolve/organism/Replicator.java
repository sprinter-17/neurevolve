package neurevolve.organism;

/**
 * A <code>Replicator</code> is an object that copies a recipe. It is passed to a recipe when it
 * creates an organism to control the creation of the new organism's recipe. The replication
 * behaviour is separated to allow for transcription errors dependent on the organism's environment.
 */
@FunctionalInterface
public interface Replicator {

    /**
     * Copy a set of instructions in a {@link Recipe} during organism's division. The copy may
     * contain various errors or alterations based on context.
     *
     * @param instructions the set of recipe instructions to copy
     * @param size the number of instructions in the recipe
     * @param colour the colour of the recipe
     * @return a new recipe containing the copied instructions
     */
    public Recipe copyInstructions(byte[] instructions, int size, int colour);

}
