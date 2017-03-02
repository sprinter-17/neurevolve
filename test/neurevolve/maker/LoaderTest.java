package neurevolve.maker;

import java.io.StringReader;
import neurevolve.maker.WorldMaker.Shape;
import neurevolve.maker.WorldMaker.Timing;
import neurevolve.maker.WorldMaker.Type;
import neurevolve.world.Configuration;
import neurevolve.world.Configuration.Value;
import static neurevolve.world.GroundElement.ACID;
import neurevolve.world.Time;
import neurevolve.world.WorldActivity;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class LoaderTest {

    private WorldMaker maker;
    private Configuration config;
    private Loader loader;
    private Timing atStart;
    private Type acid;
    private Type wall;
    private Shape everywhere;
    private Shape horizontalEdges;
    private Shape verticalDivider;

    @Before
    public void setup() {
        atStart = mock(Timing.class);
        acid = mock(Type.class);
        wall = mock(Type.class);
        everywhere = mock(Shape.class);
        horizontalEdges = mock(Shape.class);
        verticalDivider = mock(Shape.class);
        maker = mock(WorldMaker.class);
        when(maker.atStart()).thenReturn(atStart);
        when(maker.acid()).thenReturn(acid);
        when(maker.wall()).thenReturn(wall);
        when(maker.everywhere()).thenReturn(everywhere);
        when(maker.horizontalEdges(anyInt())).thenReturn(horizontalEdges);
        when(maker.verticalDividers(anyInt(), anyInt(), anyInt())).thenReturn(verticalDivider);

        config = mock(Configuration.class);
        loader = new Loader();
    }

    @Test(expected = SAXException.class)
    public void testIllegalDocNode() throws SAXException {
        load("<fred/>");
    }

    @Test(expected = SAXException.class)
    public void testIllegalTimingNode() throws SAXException {
        loadWorld("<bad_timing/>");
    }

    @Test(expected = SAXException.class)
    public void testIllegalTypeNode() throws SAXException {
        loadWorld("<at_start><fred/></at_start>");
    }

    @Test(expected = SAXException.class)
    public void testIllegalShapeChildNode() throws SAXException {
        loadWorld("<at_start><wall><fred/></wall></at_start>");
    }

    @Test
    public void testName() throws SAXException {
        load("<world/>");
        assertThat(loader.getName(), is("Fred"));
        load("<world name='Foo'/>");
        assertThat(loader.getName(), is("Foo"));
    }

    @Test
    public void testDescription() throws SAXException {
        load("<world><description>Foobar.</description></world>");
        assertThat(loader.getDescription().get(), is("Foobar."));
    }

    @Test
    public void testEmptyMaker() throws SAXException {
        loadWorld("");
        verify(maker, never()).add(any(), any(), any());
    }

    @Test
    public void testIgnoreWhitespace() throws SAXException {
        loadWorld(" \t\n");
        verify(maker, never()).add(any(), any(), any());
    }

    @Test(expected = SAXException.class)
    public void testIllegalConfigElement() throws SAXException {
        loadConfig("<fred/>");
    }

    @Test
    public void testTemperatureRange() throws SAXException {
        loadConfig("<temperature_range min='80' max='100'/>");
        verify(config).setTemperatureRange(80, 100);
    }

    @Test
    public void testMutationRate() throws SAXException {
        loadConfig("<mutation_rate normal='20' radiation='40'/>");
        verify(config).setNormalMutationRate(20);
        verify(config).setRadiatedMutationRate(40);
    }

    @Test
    public void testYear() throws SAXException {
        loadConfig("<year length='800' variation='10'/>");
        verify(config).setYear(800, 10);
    }

    @Test
    public void testSeed() throws SAXException {
        loadConfig("<seed count='100' energy='400'/>");
        verify(config).setSeed(100, 400);
    }

    @Test(expected = SAXException.class)
    public void testIllegalActivityCost() throws SAXException {
        loadConfig("<activity_cost activity='fred' cost='234'/>");
    }

    @Test
    public void testActivityCost() throws SAXException {
        loadConfig("<activity_cost activity='eat here' cost='6' factor='75'/>");
        verify(config).setActivityCost(WorldActivity.EAT_HERE, 6);
        verify(config).setActivityFactor(WorldActivity.EAT_HERE, 75);
    }

    @Test
    public void testActivationCost() throws SAXException {
        loadConfig("<activation_cost base='5' size='7' age='4'/>");
        verify(config).setValue(Value.BASE_COST, 5);
        verify(config).setValue(Value.SIZE_RATE, 7);
        verify(config).setValue(Value.AGING_RATE, 4);
    }

    @Test
    public void testDefaultActivityCost() throws SAXException {
        loadConfig("<activity_cost cost='7' factor='0'/>");
        verify(config).setDefaultActivityCost(7);
        verify(config).setDefaultActivityFactor(0);
    }

    @Test
    public void testMinimumSplitTime() throws SAXException {
        loadConfig("<minimum_split_time period='7'/>");
        verify(config).setMinimumSplitTime(7);
    }

    @Test
    public void testConsumptionRate() throws SAXException {
        loadConfig("<consumption_rate rate='80'/>");
        verify(config).setConsumptionRate(80);
    }

    @Test
    public void testHalfLife() throws SAXException {
        loadConfig("<half_life element='acid' period='37'/>");
        verify(config).setHalfLife(ACID, 37);
    }

    @Test
    public void testStartAcidEverywhere() throws SAXException {
        loadStartElement("<acid><everywhere/></acid>");
        verify(maker).atStart();
        verify(maker).acid();
        verify(maker).everywhere();
        verify(maker).add(atStart, acid, everywhere);
    }

    @Test
    public void testAddResources() throws SAXException {
        Type addResource = mock(Type.class);
        when(maker.addResources(27)).thenReturn(addResource);
        loadStartElement("<add_resources amount='27'><everywhere/></add_resources>");
        verify(maker).add(atStart, addResource, everywhere);
    }

    @Test
    public void testWallEdges() throws SAXException {
        loadStartElement("<wall><horizontal_edges width='30'/></wall>");
        verify(maker).wall();
        verify(maker).horizontalEdges(30);
        verify(maker).add(atStart, wall, horizontalEdges);
    }

    @Test
    public void testDividers() throws SAXException {
        loadStartElement("<acid><vertical_dividers count='4' width='10' gap='71'/></acid>");
        verify(maker).verticalDividers(4, 10, 71);
        verify(maker).add(atStart, acid, verticalDivider);
    }

    @Test
    public void testPools() throws SAXException {
        Shape pools = mock(Shape.class);
        when(maker.pools(5, 12)).thenReturn(pools);
        loadStartElement("<acid><pools count='5' radius='12'/></acid>");
        verify(maker).add(atStart, acid, pools);
    }

    @Test
    public void testMaze() throws SAXException {
        Shape maze = mock(Shape.class);
        when(maker.maze(17, 21)).thenReturn(maze);
        loadStartElement("<wall><maze cell='17' edge='21'/></wall>");
        verify(maker).add(atStart, wall, maze);
    }

    @Test
    public void testSeasonal() throws SAXException {
        Timing winter = mock(Timing.class);
        when(maker.duringSeason(Time.Season.WINTER)).thenReturn(winter);
        loadWorld("<in_season season='winter'><acid><everywhere/></acid></in_season>");
        verify(maker).add(winter, acid, everywhere);
    }

    private void loadStartElement(String xml) throws SAXException {
        loadWorld("<at_start>" + xml + "</at_start>");
    }

    private void loadWorld(String xml) throws SAXException {
        load("<world>" + xml + "</world>");
    }

    private void loadConfig(String xml) throws SAXException {
        load("<world><configuration>" + xml + "</configuration></world>");
    }

    private void load(String xml) throws SAXException {
        loader.load(maker, config, "Fred", new InputSource(new StringReader(xml)));
    }

}
