package neurevolve.organism;

import java.util.ArrayList;
import java.util.List;

/**
 * A <code>DistanceSpecies</code> is a collection of similar organisms. It is defined by a maximum distance
 for the recipes of all members of the species to the first member.

 DistanceSpecies cannot be created directly. Rather they are created by calling {@link #addToSpecies} for
 * each organism in a population.
 */
public class DistanceSpecies {

    private final int maxDistance;
    private final List<Organism> organisms = new ArrayList<>();
    private final RecipeDescriber recipeDescriber;
    private int largestDistance = 0;

    /**
     * Construct a new species with a single organism. This organism becomes the archetype for the
     * species. All organisms tested for membership of the species are compared to this organism.
     *
     * @param maxDistance the maximum distance between all members
     * @param organism an organism to add to the species
     */
    private DistanceSpecies(int maxDistance, Organism organism) {
        this.maxDistance = maxDistance;
        recipeDescriber = organism.describeRecipe();
        add(organism);
    }

    /**
     * Find the species matching an organism in a list and add the organism to the species. If there
     * is not matching species, create a new species using the organism as the archetype and add the
     * new species to the list.
     *
     * @param organism the organism to find a species for
     * @param speciesList the list of species to search
     * @param maxDistance the maximum distance between organisms in species
     * @return the species the organism belongs within
     */
    public static DistanceSpecies addToSpecies(Organism organism, List<DistanceSpecies> speciesList, int maxDistance) {
        for (DistanceSpecies species : speciesList) {
            assert species.maxDistance == maxDistance;
            if (species.includes(organism)) {
                return species;
            }
        }
        DistanceSpecies species = new DistanceSpecies(maxDistance, organism);
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

    /**
     * Get the maximum distance between the recipe of the first organism and any other organism in
     * the species.
     *
     * @return the largest distance
     */
    public int getLargestDistance() {
        return largestDistance;
    }

    /**
     * Get a describer for the first organism's recipe.
     *
     * @return a recipe describer for the species
     */
    public RecipeDescriber getRecipeDescriber() {
        return recipeDescriber;
    }

    /**
     * Check if an organism belongs in this species by checking the distance of its recipe against
     * the archetype member. If it belongs then add it and return true. Otherwise return false
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
}
