package neurevolve.ui;

import java.util.List;
import neurevolve.organism.Environment;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import neurevolve.organism.RecipeDescriber;

public class Species {

    private final int colour;
    private final Recipe recipe;
    private final Environment environment;
    private int maxAge = 0;
    private float totalAge = 0f;
    private int size = 0;
    private int complexity = 0;
    private int[] complexityRanges;

    public Species(Organism organism) {
        this.colour = organism.getColour();
        this.recipe = organism.getRecipe();
        this.environment = organism.getEnvironment();
    }

    public int getColour() {
        return colour;
    }

    public int getSize() {
        return size;
    }

    public int getComplexity() {
        return complexity;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public float getAverageAge() {
        return totalAge / size;
    }

    public RecipeDescriber describeRecipe() {
        return new RecipeDescriber(recipe, environment);
    }

    public int[] getRanges() {
        return complexityRanges;
    }

    public static Species addToSpecies(Organism organism, List<Species> speciesList) {
        Species species = speciesList.stream().filter(s -> s.recipe.matches(organism.getRecipe()))
                .findAny().orElseGet(() -> {
                    Species add = new Species(organism);
                    speciesList.add(add);
                    return add;
                });
        species.size++;
        species.totalAge += organism.getAge();
        if (organism.getAge() > species.maxAge) {
            species.maxAge = organism.getAge();
        }
        if (organism.complexity() > species.complexity) {
            species.complexity = organism.complexity();
            species.complexityRanges = organism.copyRanges();
        }
        return species;
    }
}
