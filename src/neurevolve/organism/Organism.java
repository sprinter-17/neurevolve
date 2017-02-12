package neurevolve.organism;

import java.util.Optional;
import neurevolve.network.Activity;
import neurevolve.network.Input;
import neurevolve.network.Network;

/**
 * An independent organism existing in the world
 */
public class Organism {

    private static final int MAX_ENERGY = 10000;
    private static long lastID = 0;

    private final long id = lastID++;
    private final Environment environment;
    private final Network brain;
    private Recipe recipe = null;
    private Optional<Organism> parent = Optional.empty();
    private int age;
    private int ageAtSplit = 0;
    private int energy;
    private int descendents = 0;

    /**
     * Construct an organism.
     *
     * @param environment the environment the organism exists within
     * @param initialEnergy the initial energy to assign
     * @param colour the colour of the organism
     * @throws IllegalArgumentException if <tt>initialHealth @lt; 0</tt>
     */
    public Organism(Environment environment, int initialEnergy, int colour) {
        this(environment, new Network(environment::applyActivationFunction), initialEnergy, new Recipe(colour));
    }

    public Organism(Environment environment, int initialEnergy) {
        this(environment, initialEnergy, 0);
    }

    private Organism(Environment environment, Network brain, int initialEnergy, Recipe recipe) {
        if (initialEnergy < 0) {
            throw new IllegalArgumentException("Negative initial energy");
        }
        this.environment = environment;
        this.brain = brain;
        this.energy = initialEnergy;
        this.recipe = recipe;
    }

    /**
     * Copy the current values for the neurons in the organism's brain
     *
     * @return the neuron values
     */
    public int[] copyValues() {
        return brain.copyValues();
    }

    /**
     * Set the recipe used to create the organism.
     *
     * @param recipe the recipe which was used to create the organism
     */
    public final void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public int getColour() {
        return recipe.getColour();
    }

    public int getDescendents() {
        return descendents;
    }

    /**
     * Generate a human-readable representation of the organism
     *
     * @return a string representing the organism
     */
    public String toString() {
        RecipeDescriber describer = describeRecipe();
        StringBuilder description = new StringBuilder();
        description.append("Len ").append(recipe.size());
        if (describer.getJunk() > 0) {
            description.append(" Junk").append(describer.getJunk());
        }
        description.append(" ").append(describer.describe());
        return description.toString();
    }

    /**
     * Generate a description of the organism's recipe.
     *
     * @return the description
     */
    public RecipeDescriber describeRecipe() {
        return new RecipeDescriber(recipe, environment);
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
     * Get the age of the organism
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
        if (age == 0) {
            return 0;
        } else {
            return (float) brain.getTotalActivitySwitches() / age;
        }
    }

    /**
     * Divide the current organism in two, creating a new descendent from the recipe used to create
     * this organism. The energy of this organism is split evenly between itself and its descendent.
     *
     * @param replicator the replicator to use in copying the recipe
     * @return the descendent organism
     */
    public Organism divide(Replicator replicator) {
        ageAtSplit = age;
        int childEnergy = energy / 2;
        incrementDescendents();
        reduceEnergy(childEnergy);
        Organism child = recipe.make(environment, replicator, childEnergy);
        child.parent = Optional.of(this);
        return child;
    }

    private void incrementDescendents() {
        descendents++;
        if (parent.isPresent()) {
            if (parent.get().isDead())
                parent = Optional.empty();
            else
                parent.get().incrementDescendents();
        }
    }

    /**
     * Test if the organism is able to divide. It requires 1 activation for each 50 instructions in
     * its recipe plus the minimum division time passed to the method.
     *
     * @param minDivisionTime the minimum amount of time between divisions
     * @return true if the organism can divide
     */
    public boolean canDivide(int minDivisionTime) {
        return (age - ageAtSplit) >= minDivisionTime + recipe.size() / 50;
    }

    /**
     * Reduce the organism's energy, to a minimum of 0
     *
     * @param reduction the amount by which to reduce the organism's energy
     */
    public void reduceEnergy(int reduction) {
        if (reduction < 0)
            throw new IllegalArgumentException("Negative energy reduction");
        energy -= reduction;
        if (energy < 0) {
            energy = 0;
        }
    }

    /**
     * If organism has enough energy, reduce it by the given amount.
     *
     * @param amount amount of energy to reduce
     * @return <tt>true</tt>, if the energy was reduced, <tt>false</tt> if there was insufficient
     * energy
     */
    public boolean consume(int amount) {
        if (energy < amount) {
            return false;
        }
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
        energy = Math.min(MAX_ENERGY, energy + addition);
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
     * Get the difference between this organism's recipe and another. The difference is defined by
     * the Levenshtein distance between the two recipes.
     *
     * @param other the organism to compare to
     * @return the difference between the recipes
     */
    public int getDifference(Organism other) {
        return recipe.distanceTo(other.recipe);
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
        if (!isDead()) {
            brain.activate();
        }
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        else if (obj == null || getClass() != obj.getClass())
            return false;
        final Organism other = (Organism) obj;
        return other.id == this.id;
    }

}
