package neurevolve.world;

import neurevolve.TestConfiguration;
import neurevolve.TestEnvironment;
import neurevolve.organism.Organism;
import static neurevolve.world.Angle.BACKWARD;
import static neurevolve.world.Angle.FORWARD;
import static neurevolve.world.Angle.LEFT;
import static neurevolve.world.Angle.RIGHT;
import static neurevolve.world.Space.EAST;
import static neurevolve.world.Space.NORTH;
import static neurevolve.world.Space.SOUTH;
import static neurevolve.world.Space.WEST;
import static neurevolve.world.WorldActivity.DIVIDE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class PopulationTest {

    private Space space;
    private Population population;
    private Organism organism;
    private int position;

    @Before
    public void setup() {
        this.space = new Space(10, 10);
        this.population = new Population(space, new TestConfiguration());
        this.organism = new Organism(new TestEnvironment(), 100);
        this.position = space.position(5, 7);
    }

    @Test
    public void testEmpty() {
        assertThat(population.size(), is(0));
    }

    @Test
    public void testAddOrganism() {
        population.addOrganism(organism, position, EAST);
        assertThat(population.size(), is(1));
        assertTrue(population.hasOrganism(position));
        assertThat(population.getOrganism(position), is(organism));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddTwoOrganismsToSamePosition() {
        population.addOrganism(organism, position, EAST);
        population.addOrganism(organism, position, WEST);
    }

    @Test
    public void testRemoveOrganism() {
        population.addOrganism(organism, position, EAST);
        population.removeOrganism(organism);
        assertThat(population.size(), is(0));
        assertFalse(population.hasOrganism(position));
        assertNull(population.getOrganism(position));
    }

    @Test
    public void testGetPosition() {
        population.addOrganism(organism, position, EAST);
        assertThat(population.getPosition(organism), is(position));
        assertThat(population.getPosition(organism, FORWARD), is(space.move(position, EAST)));
        assertThat(population.getPosition(organism, LEFT), is(space.move(position, NORTH)));
        assertThat(population.getPosition(organism, RIGHT), is(space.move(position, SOUTH)));
    }

    @Test
    public void testGetDirection() {
        population.addOrganism(organism, position, EAST);
        assertThat(population.getDirection(organism), is(EAST));
    }

    @Test
    public void testTurn() {
        population.addOrganism(organism, position, EAST);
        population.turn(organism, RIGHT);
        assertThat(population.getDirection(organism), is(SOUTH));
        population.turn(organism, RIGHT);
        assertThat(population.getDirection(organism), is(WEST));
        population.turn(organism, BACKWARD);
        assertThat(population.getDirection(organism), is(EAST));
        population.turn(organism, FORWARD);
        assertThat(population.getDirection(organism), is(EAST));
    }

    @Test
    public void testMove() {
        population.addOrganism(organism, position, EAST);
        population.moveOrganism(organism, 10);
        assertFalse(population.hasOrganism(position));
        assertThat(population.getOrganism(space.move(position, EAST)), is(organism));
        assertThat(organism.getEnergy(), is(90));
    }

    @Test
    public void testNoMoveWithInsufficientEnergy() {
        population.addOrganism(organism, position, EAST);
        population.moveOrganism(organism, 110);
        assertTrue(population.hasOrganism(position));
        assertThat(population.getOrganism(position), is(organism));
        assertThat(organism.getEnergy(), is(100));
    }

    @Test
    public void testActivityCount() {
        population.addOrganism(organism, position, EAST);
        assertThat(population.getActivityCount(organism, DIVIDE), is(0));
        population.incrementActivityCount(organism, DIVIDE);
        assertThat(population.getActivityCount(organism, DIVIDE), is(1));
        population.resetActivityCount(organism);
        assertThat(population.getActivityCount(organism, DIVIDE), is(0));
    }
}
