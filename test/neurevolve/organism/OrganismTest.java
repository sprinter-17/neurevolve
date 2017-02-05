package neurevolve.organism;

import neurevolve.TestEnvironment;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class OrganismTest {

    private final Environment environment = new TestEnvironment();
    private Organism organism;

    @Before
    public void setup() {
        organism = new Organism(environment, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeInitialHealth() {
        organism = new Organism(environment, -1);
    }

    @Test
    public void testDefaultSize() {
        assertThat(organism.getBrain().size(), is(0));
    }

    @Test
    public void testGetEnergy() {
        assertThat(organism.getEnergy(), is(100));
        assertThat(new Organism(environment, 17).getEnergy(), is(17));
    }

    @Test
    public void testReduceEnergy() {
        organism.reduceEnergy(40);
        assertThat(organism.getEnergy(), is(60));
        organism.reduceEnergy(100);
        assertThat(organism.getEnergy(), is(0));
    }

    @Test
    public void testIsDead() {
        assertFalse(organism.isDead());
        organism = new Organism(environment, 0);
        assertTrue(organism.isDead());
    }

    @Test
    public void divide() {
        Recipe recipe = mock(Recipe.class);
        organism = new Organism(environment, 30);
        organism.setRecipe(recipe);
        organism.divide();
        assertThat(organism.getEnergy(), is(15));
        verify(recipe).make(environment, 15);
    }

    @Test
    public void testAge() {
        assertThat(organism.getAge(), is(0));
        organism.activate();
        assertThat(organism.getAge(), is(1));
    }
}
