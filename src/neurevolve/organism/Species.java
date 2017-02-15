package neurevolve.organism;

import java.util.List;
import java.util.Optional;

/**
 * A {@code Species} represents a group of organisms with identical colour and recipe. Each species
 * records summary information about the group.
 */
public class Species {

    private final int colour;
    private final Recipe recipe;
    private final RecipeDescriber recipeDescriber;
    private int maxAge = 0;
    private float totalAge = 0f;
    private int size = 0;
    private int complexity = 0;
    private int[] complexityRanges;

    /**
     * Construct a Species from a prototype organism. The colour and recipe of the species is set
     * from the organism.
     *
     * @param organism the prototype organism.
     */
    public Species(Organism organism) {
        this.colour = organism.getColour();
        this.recipe = organism.getRecipe();
        this.recipeDescriber = organism.describeRecipe();
        add(organism);
    }

    /**
     * @return the species' colour
     */
    public int getColour() {
        return colour;
    }

    /**
     * @return the number of organism's in the species
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the complexity of the most complex organism in the species.
     */
    public int getComplexity() {
        return complexity;
    }

    /**
     * @return the age of the oldest organism in the species
     */
    public int getMaxAge() {
        return maxAge;
    }

    /**
     * @return the average age of organisms in the species
     */
    public float getAverageAge() {
        return totalAge / size;
    }

    /**
     * @return describe the species' recipe
     */
    public RecipeDescriber describeRecipe() {
        return recipeDescriber;
    }

    /**
     * @return the ranges of the neurons of the most complex organism in the species, or null if all
     * species have complexity 0
     */
    public int[] getRanges() {
        return complexityRanges;
    }

    /**
     * Check if an organism is a member of this species
     *
     * @param organism the organism to check
     * @return true if the organism's colour and recipe matches this species
     */
    public boolean matches(Organism organism) {
        return colour == organism.getColour() && recipe.matches(organism.getRecipe());
    }

    /**
     * Add an organism to the species
     *
     * @param member the organism to add.
     * @throws IllegalArgumentException if the organism does not match the species
     */
    public void add(Organism member) {
        if (!matches(member))
            throw new IllegalArgumentException("Attempt to add a non-matching organism to a species");
        size++;
        maxAge = Math.max(maxAge, member.getAge());
        totalAge += member.getAge();
        if (member.complexity() > complexity) {
            complexity = member.complexity();
            complexityRanges = member.copyRanges();
        }
    }

    /**
     * Find the species in a list for an organism. If no such species is in the list then one is
     * created and added to the list.
     *
     * @param organism the organism to find a species for
     * @param speciesList a list of species to check
     * @return the species for the organism
     */
    public static Species addToSpecies(Organism organism, List<Species> speciesList) {
        Optional<Species> match = speciesList.stream()
                .filter(s -> s.matches(organism)).findAny();
        if (match.isPresent()) {
            match.get().add(organism);
            return match.get();
        } else {
            Species species = new Species(organism);
            speciesList.add(species);
            return species;
        }
    }
}
