package neurevolve.organism;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class OrganismTest {

    private Organism organism;

    @Before
    public void setup() {
        organism = new Organism(n -> n, 100);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeInitialHealth() {
        organism = new Organism(n -> n, -1);
    }

    @Test
    public void testDefaultSize() {
        assertThat(organism.size(), is(0));
    }

    @Test
    public void testSize() {
        organism.getBrain().addNeuron();
        assertThat(organism.size(), is(1));
    }

    @Test
    public void testGetHealth() {
        assertThat(organism.getHealth(), is(100));
        assertThat(new Organism(n -> n, 17).getHealth(), is(17));
    }

    @Test
    public void testIsDead() {
        assertFalse(organism.isDead());
        organism = new Organism(n -> n, 0);
        assertTrue(organism.isDead());
    }

}
