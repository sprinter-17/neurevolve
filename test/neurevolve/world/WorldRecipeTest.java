package neurevolve.world;

import java.util.EnumSet;
import neurevolve.organism.Code;
import neurevolve.organism.Instruction;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import neurevolve.organism.RecipeDescriber;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WorldRecipeTest {

    private Space space;
    private Configuration config;
    private World world;

    @Before
    public void setup() {
        space = new Space(10, 10);
        config = new Configuration();
        world = new World(n -> n, space, config);
    }

    @Test
    public void testAllElements() {
        testElement(GroundElement.ACID, "Acid");
        testElement(GroundElement.BODY, "Body");
        testElement(GroundElement.RADIATION, "Radiation");
        testElement(GroundElement.RESOURCES, "Resources");
        testElement(GroundElement.WALL, "Wall");
        testElement(GroundElement.ELEVATION, "Slope");
    }

    private void testElement(GroundElement element, String name) {
        world = new World(n -> n, space, config);
        world.addUsedElements(EnumSet.allOf(GroundElement.class));
        Recipe recipe = inputRecipe("Look " + name + " Forward");
        RecipeDescriber description = new RecipeDescriber(recipe, world);
        assertThat(description.toString(), is("N1+1 Turn Left Look " + name + " Forward+1"));
        Organism organism = new Organism(world, 100, recipe);
        world.addOrganism(organism, 55, Space.EAST);
        organism.activate();
        assertTrue("Do not activate without " + name, world.getOrganismDirection(organism) == Space.EAST);
        world.addElementValue(56, element, 100);
        organism.activate();
        assertTrue("Activate with " + name, world.getOrganismDirection(organism) == Space.NORTH);
    }

    private Recipe inputRecipe(String input) {
        byte code = Code.fromInt(world.getInputCode(input).getAsInt());
        Recipe recipe = new Recipe(100);
        recipe.add(Instruction.ADD_NEURON, Code.fromInt(1));
        recipe.add(Instruction.ADD_INPUT, code, Code.fromInt(1));
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.TURN_LEFT.code());
        return recipe;
    }

}
