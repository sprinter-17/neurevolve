package neurevolve.organism;

import java.util.ArrayList;
import java.util.List;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpeciesTest {

    private Recipe recipe;
    private RecipeDescriber describer;
    private Species species;

    @Before
    public void setup() {
        describer = mock(RecipeDescriber.class);
        recipe = mock(Recipe.class);
        when(recipe.matches(recipe)).thenReturn(true);
        species = new Species(makeOrganism(describer, 17, 94, 9));
    }

    @Test
    public void testNew() {
        assertThat(species.getSize(), is(1));
        assertThat(species.getMaxAge(), is(94));
        assertThat(species.getAverageAge(), is(94f));
        assertThat(species.getColour(), is(17));
        assertThat(species.getComplexity(), is(9));
        assertThat(species.describeRecipe(), is(describer));
    }

    @Test
    public void testMatch() {
        assertTrue(species.matches(makeOrganism(describer, 17, 9, 8)));
        assertFalse(species.matches(makeOrganism(describer, 18, 9, 8)));
        recipe = mock(Recipe.class);
        assertFalse(species.matches(makeOrganism(describer, 17, 9, 8)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddNonMatchingOrganism() {
        species.add(makeOrganism(describer, 18, 9, 8));
    }

    @Test
    public void testAddOrganism() {
        species.add(makeOrganism(describer, 17, 100, 12));
        assertThat(species.getSize(), is(2));
        assertThat(species.getMaxAge(), is(100));
        assertThat(species.getAverageAge(), is(97f));
        assertThat(species.getComplexity(), is(12));
    }

    @Test
    public void testAddToSpeciesList() {
        List<Species> list = new ArrayList<>();
        Species.addToSpecies(makeOrganism(describer, 7, 0, 0), list);
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getSize(), is(1));
        Species.addToSpecies(makeOrganism(describer, 7, 0, 0), list);
        assertThat(list.size(), is(1));
        assertThat(list.get(0).getSize(), is(2));
        Species.addToSpecies(makeOrganism(describer, 8, 0, 0), list);
        assertThat(list.size(), is(2));
        assertThat(list.get(0).getSize(), is(2));
        assertThat(list.get(1).getSize(), is(1));
    }

    private Organism makeOrganism(RecipeDescriber describer, int colour, int age, int complexity) {
        Organism organism = mock(Organism.class);
        when(organism.getColour()).thenReturn(colour);
        when(organism.getAge()).thenReturn(age);
        when(organism.complexity()).thenReturn(complexity);
        when(organism.getRecipe()).thenReturn(recipe);
        when(organism.describeRecipe()).thenReturn(describer);
        return organism;
    }

}
