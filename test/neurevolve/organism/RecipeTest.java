package neurevolve.organism;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import neurevolve.TestEnvironment;
import neurevolve.network.Neuron;
import static neurevolve.organism.Code.fromInt;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class RecipeTest {

    private final Environment environment = new TestEnvironment();
    private Recipe recipe;
    private final List<Gene> genes = new ArrayList<>();

    private class Gene {

        private Instruction instruction;
        private byte[] values;
    }

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
        assertThat(new Organism(environment, 100, recipe).getEnergy(), is(100));
        assertThat(new Organism(environment, 20, recipe).getEnergy(), is(20));
        assertThat(new Organism(environment, 20, recipe).getBrain().size(), is(0));
    }

    @Test
    public void testAddInstruction() {
        recipe.add(Instruction.ADD_NEURON);
        assertThat(recipe.size(), is(1));
    }

    @Test
    public void testSimpleRecipe() {
        recipe.add(Instruction.ADD_NEURON, fromInt(5));
        assertThat(new Organism(environment, 100, recipe).getBrain().size(), is(1));
    }

    @Test
    public void testGetZeroGenes() {
        getGenes();
        assertTrue(genes.isEmpty());
    }

    @Test
    public void testGetZeroGenesWhenInstructionMissingValues() {
        recipe.add(Instruction.ADD_NEURON);
        getGenes();
        assertTrue(genes.isEmpty());
    }

    @Test
    public void testGetGene() {
        recipe.add(Instruction.ADD_NEURON, fromInt(7));
        getGenes();
        assertThat(genes.get(0).instruction, is(Instruction.ADD_NEURON));
        assertThat(genes.get(0).values[0], is(fromInt(7)));
        assertThat(genes.get(0).values.length, is(1));
    }

    @Test
    public void testGetTwoGenes() {
        recipe.add(Instruction.ADD_LINK, fromInt(5), fromInt(7));
        recipe.add(Instruction.ADD_DELAY, fromInt(4));
        getGenes();
        assertThat(genes.size(), is(2));
        assertThat(genes.get(0).instruction, is(Instruction.ADD_LINK));
        assertThat(genes.get(1).instruction, is(Instruction.ADD_DELAY));
    }

    @Test
    public void testComplexRecipe() {
        recipe.add(Instruction.ADD_NEURON, fromInt(-18));
        recipe.add(Instruction.ADD_NEURON, Code.ZERO);
        recipe.add(Instruction.ADD_LINK, Code.ZERO, fromInt(5 * Neuron.WEIGHT_DIVISOR));
        Organism organism = new Organism(environment, 400, recipe);
        organism.getBrain().activate();
        assertThat(organism.getBrain().getValue(1), is(90));
    }

    @Test
    public void testJunkRecipe() {
        recipe.add(fromInt(9));
        Organism organism = new Organism(environment, 100, recipe);
        assertThat(organism.getBrain().size(), is(0));
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
        random.ints(100, -100, 100).mapToObj(i -> (byte) i).forEach(recipe::add);
        assertThat(recipe.distanceTo(recipe), is(0));
    }

    @Test
    public void testSingleInsertionDistance() {
        recipe.add(fromInt(5));
        assertThat(recipe.distanceTo(new Recipe(0)), is(5));
    }

    @Test
    public void testSingleDeletionDistance() {
        recipe.add(fromInt(5));
        assertThat(new Recipe(0).distanceTo(recipe), is(5));
    }

    @Test
    public void testSingleSubstituteDistance() {
        Recipe other = new Recipe(0);
        recipe.add(fromInt(5));
        recipe.add(fromInt(17));
        other.add(fromInt(-2));
        other.add(fromInt(17));
        assertThat(recipe.distanceTo(other), is(7));
    }

    @Test
    public void testMultipleSubstituteDistance() {
        Recipe other = new Recipe(0);
        recipe.add(fromInt(7));
        other.add(fromInt(9));
        recipe.add(fromInt(12));
        other.add(fromInt(10));
        assertThat(recipe.distanceTo(other), is(4));
    }

    @Test
    public void testComplexInsertionDistance() {
        Recipe other = new Recipe(0);
        recipe.add(fromInt(65));
        recipe.add(fromInt(-4));
        recipe.add(fromInt(17));
        other.add(fromInt(65));
        other.add(fromInt(17));
        assertThat(recipe.distanceTo(other), is(4));
    }

    @Test
    public void testInverseDistance() {
        Recipe other = new Recipe(0);
        recipe.add(fromInt(5));
        other.add(fromInt(-5));
        assertThat(recipe.distanceTo(other), is(10));
    }

    private void getGenes() {
        genes.clear();
        recipe.forEachInstruction((i, v) -> {
            Gene gene = new Gene();
            gene.instruction = i;
            gene.values = v;
            genes.add(gene);
        });
    }
}
