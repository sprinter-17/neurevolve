package neurevolve.organism;

import neurevolve.TestEnvironment;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class RecipeDescriberTest {

    private Recipe recipe;

    @Before
    public void setup() {
        recipe = new Recipe();
    }

    @Test
    public void testEmptyRecipe() {
        assertThat(describe(), is("Len0"));
    }

    @Test
    public void testJunkLink() {
        recipe.add(Instruction.ADD_LINK);
        assertThat(describe(), is("Len1"));
    }

    @Test
    public void testJunkInput() {
        recipe.add(Instruction.ADD_INPUT);
        assertThat(describe(), is("Len1"));
    }

    @Test
    public void testJunkActivity() {
        recipe.add(Instruction.SET_ACTIVITY);
        assertThat(describe(), is("Len1"));
    }

    @Test
    public void testAddNeuron() {
        recipe.add(Instruction.ADD_NEURON, 17);
        assertThat(describe(), is("Len2 | N1+17"));
    }

    @Test
    public void testAddInput() {
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_INPUT, 2, 12);
        assertThat(describe(), is("Len5 | N1+0 Input2+12"));
    }

    @Test
    public void testDelay() {
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_DELAY, 5);
        assertThat(describe(), is("Len4 | N1+0d5"));
    }

    @Test
    public void testSetActivity() {
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.SET_ACTIVITY, 7);
        assertThat(describe(), is("Len4 | N1+0 Activity7"));
    }

    @Test
    public void testLinkToNegativeNeuron() {
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_LINK, -2);
        assertThat(describe(), is("Len4 | N1+0"));
    }

    @Test
    public void testLinkToSelf() {
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_LINK, 0);
        assertThat(describe(), is("Len4 | N1+0"));
    }

    @Test
    public void testAddLink() {
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_LINK, 0, 0);
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.ADD_LINK, 1, +4);
        recipe.add(Instruction.ADD_LINK, 0, -17);
        assertThat(describe(), is("Len15 | N1+0 | N2+0 ^N1+0 | N3+0 ^N1-17 ^N2+4"));
    }

    private String describe() {
        RecipeDescriber describer = new RecipeDescriber(recipe, new TestEnvironment());
        return describer.describe();
    }

}
