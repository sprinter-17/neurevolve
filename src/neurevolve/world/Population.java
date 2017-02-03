package neurevolve.world;

import java.util.Arrays;
import neurevolve.organism.Organism;
import static neurevolve.world.Angle.FORWARD;

/**
 * A <code>Population</code> represents a set of organisms in a {@link Space}.
 */
public class Population {

    private static final int POSITION_CODE = 0;
    private static final int DIRECTION_CODE = 1;

    private final Space space;
    private final Organism[] organisms;
    private int size = 0;

    /**
     * Construct a new population.
     *
     * @param space the space the population will live within.
     */
    public Population(Space space) {
        this(space, new Organism[space.size()]);
    }

    private Population(Space space, Organism[] organisms) {
        this.space = space;
        this.organisms = organisms;
    }

    /**
     * Copy the population to allow operations that won't impact on the copy.
     *
     * @return a copy of the population
     */
    public Population copy() {
        return new Population(space, Arrays.copyOf(organisms, space.size()));
    }

    /**
     * Get the size of the population.
     *
     * @return the number of organisms that have been added
     */
    public int size() {
        return size;
    }

    /**
     * Check if a position has an organism
     *
     * @param position the position to check
     * @return true if the position has an organism
     */
    public boolean hasOrganism(int position) {
        return getOrganism(position) != null;
    }

    /**
     * Get the organism in a position
     *
     * @param position the position to get the organism for
     * @return the organism in the given position, or <tt>null</tt> if there is no organism
     */
    public Organism getOrganism(int position) {
        return organisms[position];
    }

    /**
     * Add an organism to the population
     *
     * @param organism the organism to add
     * @param position the position to add the organism at
     * @param direction the direction the organism is facing
     * @throws IllegalArgumentException if the position already has an organism in it
     */
    public void addOrganism(Organism organism, int position, int direction) {
        if (hasOrganism(position))
            throw new IllegalArgumentException("Attempt to add two organisms to same position");
        organisms[position] = organism;
        setPosition(organism, position);
        setDirection(organism, direction);
        size++;
    }

    private void setPosition(Organism organism, int position) {
        organism.setWorldValue(POSITION_CODE, position);
    }

    private void setDirection(Organism organism, int direction) {
        organism.setWorldValue(DIRECTION_CODE, direction);
    }

    /**
     * Remove an organism from the population
     *
     * @param organism the organism to remove
     */
    public void removeOrganism(Organism organism) {
        int position = getPosition(organism);
        assert hasOrganism(position);
        organisms[position] = null;
        size--;
    }

    /**
     * Get a position relative to an organism
     *
     * @param organism the organism whose position the result is relative to
     * @param angles zero or more angles that form the steps to the resulting position
     * @return the resulting position
     */
    protected int getPosition(Organism organism, Angle... angles) {
        int position = organism.getWorldValue(POSITION_CODE);
        for (Angle angle : angles) {
            int direction = angle.add(organism.getWorldValue(DIRECTION_CODE));
            position = space.move(position, direction);
        }
        return position;
    }

    /**
     * Get the direction an organism is facing
     *
     * @param organism the organism to get the direction for
     * @return the direction
     */
    protected int getDirection(Organism organism) {
        return organism.getWorldValue(DIRECTION_CODE);
    }

    /**
     * Change an organism's direction by a given angle
     *
     * @param organism the organism whose direction to change
     * @param angle the angle to add to the current direction
     */
    protected void turn(Organism organism, Angle angle) {
        setDirection(organism, angle.add(getDirection(organism)));
    }

    /**
     * Move an organism in the direction it is facing. A move consumes energy and if the organism
     * does not have enough energy it doesn't move
     *
     * @param organism the organism to move
     * @param energyCost the cost in energy for the move
     */
    public void moveOrganism(Organism organism, int energyCost) {
        int position = getPosition(organism, FORWARD);
        if (!hasOrganism(position)) {
            if (organism.consume(energyCost)) {
                removeOrganism(organism);
                addOrganism(organism, position, getDirection(organism));
            }
        }
    }

}
