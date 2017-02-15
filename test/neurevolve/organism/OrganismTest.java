package neurevolve.organism;

import neurevolve.TestEnvironment;
import neurevolve.TestReplicator;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrganismTest {

    private final Environment environment = new TestEnvironment();
    private Organism organism;

    @Before
    public void setup() {
        organism = makeOrganism();
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
    public void testDivide() {
        organism = makeOrganism();
        Organism child = organism.divide(new TestReplicator());
        assertThat(organism.getEnergy(), is(50));
        assertNotSame(child, organism);
    }

    @Test
    public void testDescendents() {
        organism = makeOrganism();
        assertThat(organism.getDescendents(), is(0));
        Organism child = organism.divide(new TestReplicator());
        assertThat(organism.getDescendents(), is(1));
        assertThat(child.getDescendents(), is(0));
        Organism grandchild = child.divide(new TestReplicator());
        assertThat(organism.getDescendents(), is(2));
        assertThat(child.getDescendents(), is(1));
        assertThat(grandchild.getDescendents(), is(0));
    }

    private Organism makeOrganism() {
        Recipe recipe = mock(Recipe.class);
        when(recipe.replicate(any())).thenReturn(recipe);
        Organism child = new Organism(environment, 100, recipe);
        return child;
    }

    @Test
    public void testAge() {
        assertThat(organism.getAge(), is(0));
        organism.activate();
        assertThat(organism.getAge(), is(1));
    }
}
