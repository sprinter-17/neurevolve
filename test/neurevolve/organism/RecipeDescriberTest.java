package neurevolve.organism;

import neurevolve.TestEnvironment;
import static neurevolve.organism.Code.fromInt;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class RecipeDescriberTest {

    private Recipe recipe;

    @Before
    public void setup() {
        recipe = new Recipe(0);
    }

    @Test
    public void testEmptyRecipe() {
        assertThat(describe(), is(""));
    }

    @Test
    public void testJunkLink() {
        recipe.add(Instruction.ADD_LINK);
        assertThat(describe(), is(""));
    }

    @Test
    public void testJunkInput() {
        recipe.add(Instruction.ADD_INPUT);
        assertThat(describe(), is(""));
    }

    @Test
    public void testJunkActivity() {
        recipe.add(Instruction.SET_ACTIVITY);
        assertThat(describe(), is(""));
    }

    @Test
    public void testAddNeuron() {
        recipe.add(Instruction.ADD_NEURON, fromInt(17));
        assertThat(describe(), is("N1+17"));
    }

    @Test
    public void testAddInput() {
        recipe.add(Instruction.ADD_NEURON, fromInt(0));
        recipe.add(Instruction.ADD_INPUT, fromInt(2), fromInt(12));
        assertThat(describe(), is("N1+0 Input2+12"));
    }

    @Test
    public void testDelay() {
        recipe.add(Instruction.ADD_NEURON, fromInt(0));
        recipe.add(Instruction.ADD_DELAY, fromInt(5));
        assertThat(describe(), is("N1+0d5"));
    }

    @Test
    public void testDelayWithActivity() {
        recipe.add(Instruction.ADD_NEURON, fromInt(0));
        recipe.add(Instruction.ADD_DELAY, fromInt(5));
        recipe.add(Instruction.SET_ACTIVITY, fromInt(4));
        assertThat(describe(), is("N1+0d5 Activity4"));

    }

    @Test
    public void testSetActivity() {
        recipe.add(Instruction.ADD_NEURON, fromInt(0));
        recipe.add(Instruction.SET_ACTIVITY, fromInt(7));
        assertThat(describe(), is("N1+0 Activity7"));
    }

    @Test
    public void testLinkToNegativeNeuron() {
        recipe.add(Instruction.ADD_NEURON, Code.ZERO);
        recipe.add(Instruction.ADD_LINK, fromInt(-2));
        assertThat(describe(), is("N1+0"));
    }

    @Test
    public void testLinkToSelf() {
        recipe.add(Instruction.ADD_NEURON, Code.ZERO);
        recipe.add(Instruction.ADD_LINK, Code.ZERO);
        assertThat(describe(), is("N1+0"));
    }

    @Test
    public void testAddLink() {
        recipe.add(Instruction.ADD_NEURON, Code.ZERO);
        recipe.add(Instruction.ADD_NEURON, Code.ZERO);
        recipe.add(Instruction.ADD_LINK, Code.ZERO, Code.ZERO);
        recipe.add(Instruction.ADD_NEURON, Code.ZERO);
        recipe.add(Instruction.ADD_LINK, fromInt(1), fromInt(+4));
        recipe.add(Instruction.ADD_LINK, Code.ZERO, fromInt(-17));
        assertThat(describe(), is("N1+0 >N2 >N3 | N2+0 <N1+0 >N3 | N3+0 <N1-17 <N2+4"));
    }

    @Test
    public void testUseless() {
        recipe.add(Instruction.ADD_NEURON, Code.ZERO);
        assertTrue(describer().getNeuron(0).isInactive());
        recipe.add(Instruction.ADD_NEURON, Code.ZERO);
        assertTrue(describer().getNeuron(1).isInactive());
        recipe.add(Instruction.SET_ACTIVITY, fromInt(5));
        assertTrue(describer().getNeuron(0).isInactive());
        assertFalse(describer().getNeuron(1).isInactive());
        recipe.add(Instruction.ADD_LINK, Code.ZERO, fromInt(5));
        assertFalse(describer().getNeuron(0).isInactive());
    }

    private String describe() {
        return describer().toString();
    }

    private RecipeDescriber describer() {
        return new RecipeDescriber(recipe, new TestEnvironment());
    }
}
