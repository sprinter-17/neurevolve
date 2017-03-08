package neurevolve.world;

import java.io.StringReader;
import java.util.Random;
import neurevolve.TestConfiguration;
import neurevolve.organism.Code;
import static neurevolve.organism.Code.fromInt;
import neurevolve.organism.Instruction;
import neurevolve.organism.Recipe;
import neurevolve.organism.RecipeDescriber;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RecipeSaverTest {

    private RecipeSaver saver;
    private World world;

    @Before
    public void setup() {
        Space space = new Space(10, 10);
        world = new World(n -> n, space, new TestConfiguration());
        saver = new RecipeSaver(world);
    }

    @Test
    public void testLoadColour() throws SAXException {
        assertThat(load("<recipe colour='242135'></recipe>").getColour(), is(242135));
        assertThat(load("<recipe colour=\"12\"></recipe>").getColour(), is(12));
    }

    @Test
    public void testIgnoresWhitespace() throws SAXException {
        assertThat(load("<recipe colour=\"12\">\n\t   </recipe>").getColour(), is(12));
    }

    @Test
    public void testSaveColour() {
        assertThat(save(new Recipe(324)), is("<recipe colour=\"324\"/>"));
        assertThat(save(new Recipe(15)), is("<recipe colour=\"15\"/>"));
    }

    @Test
    public void testLoadJunk() throws SAXException {
        Recipe recipe = loadRecipe("<junk value='0'/>");
        assertThat(recipe.size(), is(1));
        assertThat(describe(recipe), is(""));
    }

    @Test
    public void testSaveJunk() {
        Recipe recipe = new Recipe(0);
        recipe.add((byte) 0);
        assertThat(save(recipe), is("<recipe colour=\"0\"><junk value=\"0\"/></recipe>"));
    }

    @Test
    public void testLoadNeuron() throws SAXException {
        Recipe recipe = loadRecipe("<add_neuron weight='-4'/>");
        assertThat(recipe.size(), is(2));
        assertThat(describe(recipe), is("N1-4"));
    }

    @Test
    public void testSaveNeuron() {
        Recipe recipe = new Recipe(0);
        recipe.add(Instruction.ADD_NEURON, fromInt(82));
        assertThat(save(recipe), is("<recipe colour=\"0\"><add_neuron weight=\"82\"/></recipe>"));
    }

    @Test
    public void testLoadLink() throws SAXException {
        Recipe recipe = loadRecipe("<add_neuron weight='5'/>"
                + "<add_neuron weight='8'/>"
                + "<add_link neuron='1' weight='3'/>");
        assertThat(describe(recipe), is("N1+5 >N2 | N2+8 <N1+3"));
    }

    @Test
    public void testSaveLink() {
        Recipe recipe = new Recipe(0);
        recipe.add(Instruction.ADD_NEURON, fromInt(-7));
        recipe.add(Instruction.ADD_NEURON, fromInt(98));
        recipe.add(Instruction.ADD_LINK, fromInt(0), fromInt(-7));
        assertThat(save(recipe), is(
                "<recipe colour=\"0\">"
                + "<add_neuron weight=\"-7\"/>"
                + "<add_neuron weight=\"98\"/>"
                + "<add_link neuron=\"0\" weight=\"-7\"/>"
                + "</recipe>"));
    }

    @Test
    public void testLoadInputs() throws SAXException {
        world.addUsedElement(GroundElement.ACID);
        Recipe recipe = loadRecipe("<add_neuron weight=\"5\"/>"
                + "<add_input input='look_acid_far_forward' weight='11'/>"
                + "<add_input input='look_left_wall' weight='6'/>");
        assertThat(describe(recipe), is("N1+5 Look Acid Far Forward+11"));
    }

    @Test
    public void testSaveInputs() throws SAXException {
        Recipe recipe = new Recipe(7);
        recipe.add(Instruction.ADD_NEURON, fromInt(3));
        recipe.add(Instruction.ADD_INPUT, fromInt(world.getInputCode("Own Age").getAsInt()), fromInt(-5));
        assertThat(save(recipe), is(""
                + "<recipe colour=\"7\">"
                + "<add_neuron weight=\"3\"/>"
                + "<add_input input=\"own_age\" weight=\"-5\"/>"
                + "</recipe>"));
    }

    @Test
    public void testLoadDelay() throws SAXException {
        Recipe recipe = loadRecipe("<add_neuron weight='2'/><add_delay period='3'/>");
        assertThat(describe(recipe), is("N1+2d3"));
    }

    @Test
    public void testSaveDelay() {
        Recipe recipe = new Recipe(324);
        recipe.add(Instruction.ADD_NEURON, fromInt(90));
        recipe.add(Instruction.ADD_DELAY, fromInt(3));
        assertThat(save(recipe), is(""
                + "<recipe colour=\"324\">"
                + "<add_neuron weight=\"90\"/>"
                + "<add_delay period=\"3\"/>"
                + "</recipe>"));
    }

    @Test
    public void testLoadActivity() throws SAXException {
        Recipe recipe = loadRecipe("<add_neuron weight='7'/><set_activity activity='eat_here'/>");
        assertThat(recipe.size(), is(4));
        assertThat(describe(recipe), is("N1+7 Eat Here"));
    }

    @Test
    public void testSaveActivity() {
        Recipe recipe = new Recipe(0);
        recipe.add(Instruction.ADD_NEURON, fromInt(-4));
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.TURN_LEFT.code());
        assertThat(save(recipe), is("<recipe colour=\"0\"><add_neuron weight=\"-4\"/><set_activity activity=\"turn_left\"/></recipe>"));
    }

    @Test
    public void testSaveLoadRandomRecipe() throws SAXException {
        for (long l = 0; l < 100; l++) {
            Random random = new Random(l);
            Recipe recipe = new Recipe(random.nextInt(100));
            random.ints(random.nextInt(5) + 5, -100, 100)
                    .mapToObj(Code::fromInt)
                    .forEach(recipe::add);
            assertThat(save(recipe), is(save(load(save(recipe)))));
            assertThat(describe(load(save(recipe))), is(describe(recipe)));
        }
    }

    private String describe(Recipe recipe) {
        RecipeDescriber describer = new RecipeDescriber(recipe, world);
        return describer.toString();
    }

    private Recipe load(String input) throws SAXException {
        return saver.load(new InputSource(new StringReader(input)));
    }

    private Recipe loadRecipe(String input) throws SAXException {
        return load("<recipe colour='0'>" + input + "</recipe>");
    }

    private String save(Recipe recipe) {
        return saver.save(recipe)
                .replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "")
                .replace("\n", "");
    }
}
