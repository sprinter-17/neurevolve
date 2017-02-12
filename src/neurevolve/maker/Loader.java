package neurevolve.maker;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import neurevolve.maker.WorldMaker.Shape;
import neurevolve.maker.WorldMaker.Timing;
import neurevolve.maker.WorldMaker.Type;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class reads XML files and updates {@link WorldMaker} and
 * {@link neurevolve.world.WorldConfiguration} objects based on the information in the file. The
 * information designates elements to add to the world both when it is created and as it progresses.
 *
 * The structure of the file is as follows:
 * <pre>
 * {@code <world>}
 * {@code    <description>Here is a description</description>}
 * {@code    <timing-element>...}
 * {@code        <type-element>...}
 * {@code            <shape-element/>...}
 * {@code        </type-element>}
 * {@code    </timing-element>}
 * {@code </world>}
 * </pre>
 *
 * All elements, including description, are optional.
 *
 * <table style='border:1px solid black'>
 * <tr>
 * <th align='left'>Category</th><th align='left'>Element</th><th align='left'>Description</th>
 * </tr>
 * <tr>
 * <td>Timing</td>
 * <td>{@code <at_start>}</td>
 * <td>specifies an element to be placed when the world is created</td>
 * </tr>
 * <tr>
 * <td>Timing</td>
 * <td>{@code <with_period period='n'>}</td>
 * <td>specifies an element to be placed every {@code n} ticks</td>
 * </tr>
 * <tr>
 * <td>Type</td>
 * <td>{@code <acid>}</td>
 * <td>places acid</td>
 * </tr>
 * <tr>
 * <td>Type</td>
 * <td>{@code <wall>}</td>
 * <td>place a wall</td>
 * </tr>
 * <tr>
 * <td>Type</td>
 * <td>{@code <radiation amount='n'>}</td>
 * <td>add radiation of strength {@code n} (up to 3)</td>
 * </tr>
 * <tr>
 * <td>Type</td>
 * <td>{@code <add_resources amount='n'}</td>
 * <td>add {@code n} resources (up to 255)</td>
 * </tr>
 * <tr>
 * <td>Type</td>
 * <td>{@code <elevation amount='n'>}</td>
 * <td>add {@code n} elevation (up to 255)</td>
 * </tr>
 * <tr>
 * <td>Shape</td>
 * <td>{@code <everywhere>}</td>
 * <td>place at every position in the space</td>
 * </tr>
 * <tr>
 * <td>Shape</td>
 * <td>{@code <horizontal_edges width='w'>}</td>
 * <td>place bands of width {@code w} along the top and bottom edges</td>
 * </tr>
 * <tr>
 * <td>Shape</td>
 * <td>{@code <vertical_edges width='w'>}</td>
 * <td>place bands of width {@code w} along the left and right edges</td>
 * </tr>
 * <tr>
 * <td>Shape</td>
 * <td>{@code <horizontal_dividers count='n' width='w' gap='g'>}</td>
 * <td>place {@code n} horizontal bands of width {@code w} with gaps of {@code g} on the left and
 * right ends</td>
 * </tr>
 * <tr>
 * <td>Shape</td>
 * <td>{@code <vertical_dividers count='n' width='w' gap='g'>}</td>
 * <td>place {@code n} vertical bands of width {@code w} with gaps of {@code g} on the top and
 * bottom ends</td>
 * </tr>
 * <tr>
 * <td>Shape</td>
 * <td>{@code <pools count='n' radius='r'>}</td>
 * <td>randomly place {@code n} circles of radius {@code r}</td>
 * </tr>
 * <tr>
 * <td>Shape</td>
 * <td>{@code <maze cell='c' edge='e'>}</td>
 * <td>place walls of thickness {@code e} to form a randomly generated maze in which each cell in
 * the maze has a width and height of {@code c}</td>
 * </tr>
 * </table>
 */
public class Loader {

    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    private WorldMaker maker;
    private String name;
    private Optional<String> description = Optional.empty();

    /**
     * @return the name of the world built by this loader
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description of the worlds
     */
    public Optional<String> getDescription() {
        return description;
    }

    /**
     * Load a world from a XML input source.
     *
     * @param maker the world maker to configures from the source
     * @param name the name of the world to construct
     * @param input the XML input source
     * @throws SAXException if there are errors parsing the input source
     */
    public void load(WorldMaker maker, String name, InputSource input) throws SAXException {
        try {
            DocumentBuilder builder = FACTORY.newDocumentBuilder();
            Document doc = builder.parse(input);
            this.maker = maker;
            this.name = name;
            processDocument((Element) doc.getFirstChild());
        } catch (ParserConfigurationException | IOException ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, "XML parsing error", ex);
        }
    }

    /**
     * Process the document node
     */
    private void processDocument(Element element) throws SAXException {
        if (!element.getNodeName().equals("world"))
            throw new SAXException("XML document node must be <world>");
        if (element.hasAttribute("name"))
            name = element.getAttribute("name");
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                processWorldElement((Element) child);
        }
    }

    /**
     * Process a world element
     */
    private void processWorldElement(Element element) throws SAXException {
        switch (element.getNodeName()) {
            case "description":
                description = Optional.of(element.getTextContent());
                break;
            default:
                processTiming(element);
                break;
        }
    }

    /**
     * Process a timing element
     */
    private void processTiming(Element element) throws SAXException {
        Timing timing;
        switch (element.getNodeName()) {
            case "at_start":
                timing = maker.atStart();
                break;
            case "with_period":
                timing = maker.withPeriod(getInt(element, "period"));
                break;
            default:
                throw new SAXException("Illegal timing: " + element.getNodeName());
        }
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                processType(timing, (Element) child);
        }
    }

    /**
     * Process a type element
     */
    private void processType(Timing timing, Element element) throws SAXException {
        Type type;
        switch (element.getNodeName()) {
            case "acid":
                type = maker.acid();
                break;
            case "wall":
                type = maker.wall();
                break;
            case "radiation":
                type = maker.radiation(getInt(element, "amount"));
                break;
            case "add_resources":
                type = maker.addResources(getInt(element, "amount"));
                break;
            case "elevation":
                type = maker.elevation(getInt(element, "amount"));
                break;
            default:
                throw new SAXException("Illegal type " + element.getNodeName());
        }
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                processShape(timing, type, (Element) child);
        }
    }

    /**
     * Process a shape element
     */
    private void processShape(Timing timing, Type type, Element element) throws SAXException {
        Shape shape;
        switch (element.getNodeName()) {
            case "everywhere":
                shape = maker.everywhere();
                break;
            case "horizontal_edges":
                shape = maker.horizontalEdges(getInt(element, "width"));
                break;
            case "vertical_edges":
                shape = maker.verticalEdges(getInt(element, "width"));
                break;
            case "horizontal_dividers":
                shape = maker.horizontalDividers(getInt(element, "count"),
                        getInt(element, "width"), getInt(element, "gap"));
                break;
            case "vertical_dividers":
                shape = maker.verticalDividers(getInt(element, "count"),
                        getInt(element, "width"), getInt(element, "gap"));
                break;
            case "pools":
                shape = maker.pools(getInt(element, "count"), getInt(element, "radius"));
                break;
            case "maze":
                shape = maker.maze(getInt(element, "cell"), getInt(element, "edge"));
                break;
            default:
                throw new SAXException("Illegal element: " + element.getNodeName());
        }
        maker.add(timing, type, shape);
    }

    /**
     * Get a mandatory integer attribute from an element
     */
    private int getInt(Element element, String attribute) throws SAXException {
        if (!element.hasAttribute(attribute))
            throw new SAXException("Element " + element.getNodeName() + " must have attribute " + attribute);
        try {
            return Integer.valueOf(element.getAttribute(attribute));
        } catch (NumberFormatException ex) {
            throw new SAXException("Attribute " + attribute + " is not a number");
        }
    }
}
