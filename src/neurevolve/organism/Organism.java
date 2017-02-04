package neurevolve.organism;

import neurevolve.network.Activity;
import neurevolve.network.Input;
import neurevolve.network.Network;

/**
 * An independent organism existing in the world
 */
public class Organism {

    private final Environment environment;
    private final Network brain;
    private Recipe recipe = null;
    private int age;
    private int ageAtSplit = 0;
    private int energy;
    private final int[] worldValues = new int[2];

    /**
     * Construct an organism.
     *
     * @param environment the environment the organism exists within
     * @param initialEnergy the initial energy to assign
     * @throws IllegalArgumentException if <tt>initialHealth @lt; 0</tt>
     */
    public Organism(Environment environment, int initialEnergy) {
        if (initialEnergy < 0)
            throw new IllegalArgumentException("Negative initial energy");
        this.environment = environment;
        this.brain = new Network(environment::applyActivationFunction);
        this.energy = initialEnergy;
        setRecipe(new Recipe());
    }

    /**
     * Set the recipe used to create the organism.
     *
     * @param recipe the recipe which was used to create the organism
     */
    public final void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    /**
     * Generate a human-readable representation of the organism
     *
     * @return a string representing the organism
     */
    public String toString() {
        RecipeDescriber describer = new RecipeDescriber(recipe, environment);
        return describer.describe();
    }

    /**
     * Get the current energy of the organism.
     *
     * @return the current energy
     */
    public int getEnergy() {
        return energy;
    }

    /**
     * Get the age of the organim
     *
     * @return the number of activations that have occurred on this organism
     */
    public int getAge() {
        return age;
    }

    /**
     * Get the size of the organism, as defined by the number of neurons in its brain.
     *
     * @return the number of neurons in the network associated with this organism
     */
    public int size() {
        return brain.size();
    }

    /**
     * Get a measure of the complexity of the organism.
     *
     * @return the total number of activity switches of the associated network per ticks.
     */
    public float complexity() {
        if (age == 0)
            return 0;
        else
            return (float) brain.getTotalActivitySwitches() / age;
    }

    /**
     * Divide the current organism in two, creating a new descendent from the recipe used to create
     * this organism. The energy of this organism is split evenly between itself and its descendent.
     *
     * @return the descendent organism
     */
    public Organism divide() {
        ageAtSplit = age;
        int childEnergy = energy / 2;
        reduceEnergy(childEnergy);
        return recipe.make(environment, childEnergy);
    }

    /**
     * Get the time since the organism last split.
     *
     * @return the number of activations since the organism split
     */
    public int getTimeSinceLastSplit() {
        return age - ageAtSplit;
    }

    /**
     * Reduce the organism's energy, to a minimum of 0
     *
     * @param reduction the amount by which to reduce the organism's energy
     */
    public void reduceEnergy(int reduction) {
        assert reduction >= 0;
        energy -= reduction;
        if (energy < 0)
            energy = 0;
    }

    /**
     * If organism has enough energy, reduce it by the given amount.
     *
     * @param amount amount of energy to reduce
     * @return <tt>true</tt>, if the energy was reduced, <tt>false</tt> if there was insufficient
     * energy
     */
    public boolean consume(int amount) {
        if (energy < amount)
            return false;
        reduceEnergy(amount);
        return true;
    }

    /**
     * Increase the organism's energy
     *
     * @param addition the amount by which to increase the organism's energy
     */
    public void increaseEnergy(int addition) {
        assert addition >= 0;
        energy += addition;
    }

    /**
     * Returns true if the organism is dead.
     *
     * @return true if <tt>energy &le; 0</tt>
     */
    public boolean isDead() {
        return energy <= 0;
    }

    /**
     * Get the {@link Network} associated with this organism
     *
     * @return the associated network
     */
    protected Network getBrain() {
        return brain;
    }

    /**
     * Activate the organism by using energy relative to its size and age then activating the
     * associated network.
     */
    public void activate() {
        age++;
        if (!isDead())
            brain.activate();
    }

    /**
     * Get an input value from the environment
     *
     * @param value the code for the type of input to retrieve
     * @return the resulting input
     */
    public Input getInput(int value) {
        return () -> environment.getInput(this, value);
    }

    /**
     * Get an activity to perform in the environment
     *
     * @param value the code for the type of activity to perform
     * @return the resulting activity
     */
    public Activity getActivity(int value) {
        return () -> environment.performActivity(this, value);
    }

    public void setWorldValue(int code, int value) {
        worldValues[code] = value;
    }

    public int getWorldValue(int code) {
        return worldValues[code];
    }
}
