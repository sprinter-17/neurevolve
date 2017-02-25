package neurevolve.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import neurevolve.organism.Organism;
import static neurevolve.world.Angle.FORWARD;

/**
 * A <code>Population</code> represents a set of organisms in a {@link Space}.
 */
public class Population {

    private final Space space;
    private final Organism[] organisms;
    private final Map<Organism, OrganismInfo> info = new HashMap<>();

    private class OrganismInfo {

        private final int position;
        private int direction;
        private EnumMap<WorldActivity, Integer> activityCount = new EnumMap<>(WorldActivity.class);

        public OrganismInfo(int position, int direction) {
            this.position = position;
            this.direction = direction;
        }

        public OrganismInfo copy() {
            return new OrganismInfo(position, direction);
        }
    }

    /**
     * Construct a new population.
     *
     * @param space the space the population will live within.
     */
    public Population(Space space) {
        this.space = space;
        organisms = new Organism[space.size()];
    }

    /**
     * Copy the population to allow operations that won't impact on the copy.
     *
     * @return a copy of the population
     */
    public synchronized Population copy() {
        Population copy = new Population(space);
        System.arraycopy(organisms, 0, copy.organisms, 0, space.size());
        info.forEach((o, i) -> copy.info.put(o, i.copy()));
        return copy;
    }

    /**
     * Get the size of the population.
     *
     * @return the number of organisms that have been added
     */
    public int size() {
        return info.size();
    }

    /**
     * Divides a random sample of the population up into species. A species is defined by all
     * organisms in the group having similar recipes. The distance between the recipes of the
     * organisms in the group are all less than a given amount. To increase performance each new
     * organism is tested against the first 5 members of the group.
     *
     * @param sampleSize the maximum number of organisms to sample
     * @param maxDistance the maximum distance of the recipes of all organisms in a species
     * @return a list of lists of organisms, with each list defining a species
     */
    public List<List<Organism>> getSpecies(int sampleSize, int maxDistance) {
        List<List<Organism>> populations = new ArrayList<>();
        List<Organism> sample = new ArrayList<>(info.keySet());
        Collections.shuffle(sample);
        sample.stream().limit(sampleSize).forEach((org) -> addToPopulation(org, populations, maxDistance));
        return populations;
    }

    private void addToPopulation(Organism organism, List<List<Organism>> populations, int maxDistance) {
        Optional<List<Organism>> population = populations.stream()
                .filter(pop -> pop.stream()
                        .limit(5)
                        .allMatch(o -> organism.getDifference(o) < maxDistance))
                .findFirst();
        if (!population.isPresent()) {
            population = Optional.of(new ArrayList<>());
            populations.add(population.get());
        }
        population.get().add(organism);
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
    public synchronized void addOrganism(Organism organism, int position, int direction) {
        if (hasOrganism(position))
            throw new IllegalArgumentException("Attempt to add two organisms to same position");
        organisms[position] = organism;
        info.put(organism, new OrganismInfo(position, direction));
    }

    /**
     * Remove an organism from the population
     *
     * @param organism the organism to remove
     */
    public synchronized void removeOrganism(Organism organism) {
        int position = getPosition(organism);
        assert hasOrganism(position);
        organisms[position] = null;
        info.remove(organism);
    }

    /**
     * Get a position relative to an organism
     *
     * @param organism the organism whose position the result is relative to
     * @param angles zero or more angles that form the steps to the resulting position
     * @return the resulting position
     */
    protected int getPosition(Organism organism, Angle... angles) {
        int position = info.get(organism).position;
        for (Angle angle : angles) {
            int direction = angle.add(info.get(organism).direction);
            position = space.move(position, direction);
        }
        return position;
    }

    protected int getActivityCount(Organism organism, WorldActivity activity) {
        return info.get(organism).activityCount.getOrDefault(activity, 0);
    }

    protected void incrementActivityCount(Organism organism, WorldActivity activity) {
        info.get(organism).activityCount.merge(activity, 1, (n, i) -> n + i);
    }

    protected void resetActivityCount(Organism organism) {
        info.get(organism).activityCount.clear();
    }

    /**
     * Get the direction an organism is facing
     *
     * @param organism the organism to get the direction for
     * @return the direction
     */
    protected int getDirection(Organism organism) {
        return info.get(organism).direction;
    }

    /**
     * Change an organism's direction by a given angle
     *
     * @param organism the organism whose direction to change
     * @param angle the angle to add to the current direction
     */
    protected void turn(Organism organism, Angle angle) {
        info.get(organism).direction = angle.add(getDirection(organism));
    }

    /**
     * Move an organism in the direction it is facing. A move consumes energy and if the organism
     * does not have enough energy it doesn't move
     *
     * @param organism the organism to move
     * @param energyCost the cost in energy for the move
     * @return true if the organism moved
     */
    public boolean moveOrganism(Organism organism, int energyCost) {
        int position = getPosition(organism, FORWARD);
        int direction = getDirection(organism);
        if (hasOrganism(position))
            throw new IllegalStateException("Attempt to move organism to position with organism");
        if (organism.hasEnergy(energyCost)) {
            organism.reduceEnergy(energyCost);
            removeOrganism(organism);
            addOrganism(organism, position, direction);
            return true;
        }
        return false;
    }

}
