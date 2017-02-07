package neurevolve.organism;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>Species</code> is a collection of similar organisms. It is defined by a maximum distance
 * for the recipes of all members of the species to the first member.
 *
 * Species cannot be created directly. Rather they are created by calling {@link #addToSpecies} for
 * each organism in a population.
 */
public class Species {

    private final int maxDistance;
    private final List<Organism> organisms = new ArrayList<>();
    private int largestDistance = 0;

    /**
     * Construct a new species with a single organism.
     *
     * @param maxDistance the maximum distance between all members
     * @param organism an organism to add to the species
     */
    private Species(int maxDistance, Organism organism) {
        this.maxDistance = maxDistance;
        add(organism);
    }

    /**
     * Find the species matching an organism in a list and add the organism to the species. If there
     * is not matching species, create a new species for the organism and add it to the list.
     *
     * @param organism the organism to find a species for
     * @param speciesList the list of species to search
     * @param maxDistance the maximum distance between organisms in species
     * @return the species the organism belongs within
     */
    public static Species addToSpecies(Organism organism, List<Species> speciesList, int maxDistance) {
        for (Species species : speciesList) {
            assert species.maxDistance == maxDistance;
            if (species.includes(organism)) {
                return species;
            }
        }
        Species species = new Species(maxDistance, organism);
        speciesList.add(species);
        return species;
    }

    /**
     * Get the size of a species.
     *
     * @return the member count
     */
    public int size() {
        return organisms.size();
    }

    public int getLargestDistance() {
        return largestDistance;
    }

    /**
     * Check if an organism belongs in this species by checking the distance of its recipe against
     * the first member.
     */
    private boolean includes(Organism organism) {
        int distance = organisms.get(0).getDifference(organism);
        if (distance <= maxDistance) {
            largestDistance = Math.max(largestDistance, distance);
            add(organism);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add an organism to the species
     */
    private void add(Organism organism) {
        organisms.add(organism);
    }

    public String toString() {
        return organisms.get(0).toString();
    }
}
