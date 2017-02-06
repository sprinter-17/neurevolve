package neurevolve.organism;

import java.util.Random;
import neurevolve.TestEnvironment;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class RecipeTest {

    private Environment environment = new TestEnvironment();
    private Recipe recipe;

    @Before
    public void setup() {
        recipe = new Recipe();
    }

    @Test
    public void testSizeWhenEmpty() {
        assertThat(recipe.size(), is(0));
    }

    @Test
    public void testMakeDefaultOrganism() {
        assertThat(recipe.make(environment, 100).getEnergy(), is(100));
        assertThat(recipe.make(environment, 20).getEnergy(), is(20));
        assertThat(recipe.make(environment, 50).getBrain().size(), is(0));
    }

    @Test
    public void testAddInstruction() {
        recipe.add(Instruction.ADD_NEURON);
        assertThat(recipe.size(), is(1));
    }

    @Test
    public void testSimpleRecipe() {
        recipe.add(Instruction.ADD_NEURON);
        assertThat(recipe.make(environment, 100).getBrain().size(), is(1));
    }

    @Test
    public void testComplexRecipe() {
        recipe.add(Instruction.ADD_NEURON, -18);
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_LINK, 0, 5);
        Organism organism = recipe.make(environment, 400);
        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(1), is(90));
    }

    @Test
    public void testExpansion() {
        for (int i = 0; i < 10000; i++) {
            assertThat(recipe.size(), is(i));
            recipe.add(Instruction.ADD_NEURON);
        }
    }

    @Test
    public void testDistanceToSelf() {
        Random random = new Random();
        random.ints(100, -100, 100).forEach(recipe::add);
        assertThat(recipe.distanceTo(recipe), is(0));
    }

    @Test
    public void testSingleInsertionDistance() {
        recipe.add(5);
        assertThat(recipe.distanceTo(new Recipe()), is(5));
    }

    @Test
    public void testSingleDeletionDistance() {
        recipe.add(5);
        assertThat(new Recipe().distanceTo(recipe), is(5));
    }

    @Test
    public void testSingleSubstituteDistance() {
        Recipe other = new Recipe();
        recipe.add(5);
        recipe.add(17);
        other.add(-2);
        other.add(17);
        assertThat(recipe.distanceTo(other), is(7));
    }

    @Test
    public void testMultipleSubstituteDistance() {
        Recipe other = new Recipe();
        recipe.add(7);
        other.add(9);
        recipe.add(12);
        other.add(10);
        assertThat(recipe.distanceTo(other), is(4));
    }

    @Test
    public void testComplexInsertionDistance() {
        Recipe other = new Recipe();
        recipe.add(65);
        recipe.add(-4);
        recipe.add(17);
        other.add(65);
        other.add(17);
        assertThat(recipe.distanceTo(other), is(4));
    }

}
