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
    private int energy;

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
        this.brain = new Network(environment.getActivationFunction());
        this.energy = initialEnergy;
        setRecipe(new Recipe());
    }

    public final void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    /**
     * Get the current energy of the organism.
     *
     * @return the current energy
     */
    public int getEnergy() {
        return energy;
    }

    public Organism divide() {
        int childEnergy = energy / 2;
        reduceEnergy(childEnergy);
        return recipe.make(environment, childEnergy);
    }

    public void reduceEnergy(int reduction) {
        assert reduction >= 0;
        energy -= reduction;
        if (energy < 0)
            energy = 0;
    }

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

    public void activate() {
        brain.activate();
    }

    public Input getInput(int value) {
        return () -> environment.getInput(value);
    }

    public Activity getActivity(int value) {
        return () -> environment.performActivity(value);
    }
}
