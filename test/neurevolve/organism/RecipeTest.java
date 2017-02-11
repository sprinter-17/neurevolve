package neurevolve.organism;

import java.util.Random;
import neurevolve.TestEnvironment;
import neurevolve.TestReplicator;
import neurevolve.network.Neuron;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class RecipeTest {

    private Environment environment = new TestEnvironment();
    private Replicator replicator = new TestReplicator();
    private Recipe recipe;

    @Before
    public void setup() {
        recipe = new Recipe(0);
    }

    @Test
    public void testColour() {
        assertThat(new Recipe(1000).getColour(), is(1000));
    }

    @Test
    public void testSizeWhenEmpty() {
        assertThat(recipe.size(), is(0));
    }

    @Test
    public void testMakeDefaultOrganism() {
        assertThat(recipe.make(environment, replicator, 100).getEnergy(), is(100));
        assertThat(recipe.make(environment, replicator, 20).getEnergy(), is(20));
        assertThat(recipe.make(environment, replicator, 50).getBrain().size(), is(0));
    }

    @Test
    public void testAddInstruction() {
        recipe.add(Instruction.ADD_NEURON);
        assertThat(recipe.size(), is(1));
    }

    @Test
    public void testSimpleRecipe() {
        recipe.add(Instruction.ADD_NEURON);
        assertThat(recipe.make(environment, replicator, 100).getBrain().size(), is(1));
    }

    @Test
    public void testComplexRecipe() {
        recipe.add(Instruction.ADD_NEURON, -18);
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_LINK, 0, 5 * Neuron.WEIGHT_DIVISOR);
        Organism organism = recipe.make(environment, replicator, 400);
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
        assertThat(recipe.distanceTo(new Recipe(0)), is(5));
    }

    @Test
    public void testSingleDeletionDistance() {
        recipe.add(5);
        assertThat(new Recipe(0).distanceTo(recipe), is(5));
    }

    @Test
    public void testSingleSubstituteDistance() {
        Recipe other = new Recipe(0);
        recipe.add(5);
        recipe.add(17);
        other.add(-2);
        other.add(17);
        assertThat(recipe.distanceTo(other), is(7));
    }

    @Test
    public void testMultipleSubstituteDistance() {
        Recipe other = new Recipe(0);
        recipe.add(7);
        other.add(9);
        recipe.add(12);
        other.add(10);
        assertThat(recipe.distanceTo(other), is(4));
    }

    @Test
    public void testComplexInsertionDistance() {
        Recipe other = new Recipe(0);
        recipe.add(65);
        recipe.add(-4);
        recipe.add(17);
        other.add(65);
        other.add(17);
        assertThat(recipe.distanceTo(other), is(4));
    }

    @Test
    public void testInverseDistance() {
        Recipe other = new Recipe(0);
        recipe.add(5);
        other.add(-5);
        assertThat(recipe.distanceTo(other), is(10));
    }

}
