package neurevolve.maker;

import java.io.StringReader;
import neurevolve.maker.WorldMaker.Shape;
import neurevolve.maker.WorldMaker.Timing;
import neurevolve.maker.WorldMaker.Type;
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

    private void loadStartElement(String xml) throws SAXException {
        loadWorld("<at_start>" + xml + "</at_start>");
    }

    private void loadWorld(String xml) throws SAXException {
        load("<world>" + xml + "</world>");
    }

    private void load(String xml) throws SAXException {
        loader.load(maker, "Fred", new InputSource(new StringReader(xml)));
    }

}
