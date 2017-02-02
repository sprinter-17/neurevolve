package neurevolve.organism;

/**
 * A <code>RecipePrinter</code> is used to allow a recipe to generate a human-readable
 * representation of itself without knowing the meaning if the input and activity codes.
 */
public interface RecipePrinter {

    /**
     * Get a human-readable representation of an input code
     *
     * @param code the input code
     * @return text representing the code
     */
    String getInput(int code);

    /**
     * Get a human-readable representation of an activity code
     *
     * @param code the activity code
     * @return text representing the code
     */
    String getActivity(int code);

}
