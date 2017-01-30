package neurevolve.organism;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class RecipeTest {

    private Recipe recipe;

    @Before
    public void setup() {
        recipe = new Recipe(n -> n);
    }

    @Test
    public void testSizeWhenEmpty() {
        assertThat(recipe.size(), is(0));
    }

    @Test
    public void testMakeDefaultOrganism() {
        assertThat(recipe.make(100).getHealth(), is(100));
        assertThat(recipe.make(20).getHealth(), is(20));
        assertThat(recipe.make(50).size(), is(0));
    }

    @Test
    public void testAddInstruction() {
        recipe.add(Instruction.ADD_NEURON);
        assertThat(recipe.size(), is(1));
    }

    @Test
    public void testSimpleRecipe() {
        recipe.add(Instruction.ADD_NEURON);
        assertThat(recipe.make(100).size(), is(1));
    }

    @Test
    public void testComplexRecipe() {
        recipe.add(Instruction.ADD_NEURON);
        recipe.add(Instruction.SET_THRESHOLD);
        recipe.add(-18);
        recipe.add(Instruction.ADD_NEURON);
        recipe.add(Instruction.ADD_LINK);
        recipe.add(0);
        recipe.add(50);
        Organism organism = recipe.make(400);
        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(1), is(9));
    }

    @Test
    public void testExpansion() {
        for (int i = 0; i < 10000; i++) {
            assertThat(recipe.size(), is(i));
            recipe.add(Instruction.ADD_NEURON);
        }
    }

}
