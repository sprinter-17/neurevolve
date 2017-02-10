package neurevolve.organism;

import java.util.ArrayList;
import java.util.List;
import neurevolve.TestEnvironment;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class SpeciesTest {

    private final Environment environment = new TestEnvironment();
    private final List<Species> speciesList = new ArrayList<>();

    @Before
    public void setup() {
        speciesList.clear();
    }

    @Test
    public void testSpeciesWithOneMember() {
        Species.addToSpecies(makeOrganism(5), speciesList, 0);
        assertThat(speciesList.size(), is(1));
        assertThat(speciesList.get(0).size(), is(1));
    }

    @Test
    public void testTwoSpecies() {
        Species.addToSpecies(makeOrganism(5), speciesList, 0);
        Species.addToSpecies(makeOrganism(-5), speciesList, 0);
        assertThat(speciesList.size(), is(2));
        assertThat(speciesList.get(0).size(), is(1));
        assertThat(speciesList.get(1).size(), is(1));
    }

    @Test
    public void testSpeciesWithTwoMembers() {
        Species.addToSpecies(makeOrganism(5), speciesList, 0);
        Species.addToSpecies(makeOrganism(5), speciesList, 0);
        assertThat(speciesList.size(), is(1));
        assertThat(speciesList.get(0).size(), is(2));
    }

    @Test
    public void testSpeciesByDistance() {
        Species.addToSpecies(makeOrganism(5), speciesList, 2);
        Species.addToSpecies(makeOrganism(7), speciesList, 2);
        Species.addToSpecies(makeOrganism(9), speciesList, 2);
        Species.addToSpecies(makeOrganism(11), speciesList, 2);
        assertThat(speciesList.size(), is(2));
        assertThat(speciesList.get(0).size(), is(2));
    }

    private Organism makeOrganism(int instruction) {
        Recipe recipe = new Recipe(0);
        recipe.add(instruction);
        return recipe.make(environment, 100);
    }

}
