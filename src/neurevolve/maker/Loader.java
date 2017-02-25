package neurevolve.maker;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import neurevolve.maker.WorldMaker.Shape;
import neurevolve.maker.WorldMaker.Timing;
import neurevolve.maker.WorldMaker.Type;
import neurevolve.world.GroundElement;
import neurevolve.world.Time.Season;
import neurevolve.world.WorldActivity;
import neurevolve.world.WorldConfiguration;
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
 * {@code <world [name='name']>}
 * {@code    <description>Here is a description</description>}
 * {@code    <configuration>}
 * {@code        <config-element>...}
 * {@code    </configuration>}
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
 * <p>
 * The supported configuration elements are:</p>
 * <table style='border:1px solid black'>
 * <caption>Configuration Elements</caption>
 * <tr><th align='left' style='width:50%'>Element</th><th align='left'>Description</th></tr>
 * <tr><td>{@code <temperature_range min='n' max='x'/>}</td><td>Sets the range of temperatures from
 * the horizontal edges (min) to the centre (max)</td></tr>
 * <tr><td>{@code <mutation_rate normal='n' [radiation='r']/>}</td><td>Sets the mutation rate for
 * non-radiated (normal) and, optionally, radiated (radiation) positions.</td></tr>
 * <tr><td>{@code <year length='l' variation='v'/>}</td><td>Set the year for the world with a length
 * of {@code l} and a temperature variation from {@code -v} (in winter) to {@code +v} (in
 * summer).</td></tr>
 * <tr><td>{@code <seed count='c' energy='e'/>}</td><td>Set the population level below which
 * organisms are generated to {@code c}. When creating seed organisms, set their initial energy
 * level to {@code e}</td></tr>
 * <tr><td>{@code <activity_cost [activity='name'] cost='c'/ [factor='f']>}</td><td>Set the cost of
 * performing activity with name {@code "name"} (ignoring case) once to {@code c}. Each time the
 * activity is performed subsequently within a single activation, the cost increases by a factor of
 * {@code f/100}. If an activity is not supplied then the cost and factor become the default for
 * activities that do not have an explicit cost and factor supplied. If no default is supplied then
 * the default cost is 1 and the default factor is 50.</td></tr>
 * <tr><td>{@code <time_costs base='b' age='a' size='s'/>}</td><td>Set the amount of energy used by
 * organisms in each tick to be the sum of the base amount, the age amount for each 100 activations
 * and the size amount for each 10 instructions in the recipe.</td></tr>
 * <tr><td>{@code <minimum_split_time period='p'/>}</td><td>Set the minimum number of activations
 * between divisions to {@code p}</td></tr>
 * <tr><td>{@code <consumption_rate rate='r'/>}</td><td>Set the maximum amount of resource an
 * organism can convert to energy in a single eat activity.</td></tr>
 * <tr><td>{@code <half_life element='e' period='p/>}</td><td>Set the period over which element
 * {@code e} will, on average, reduce by 1. All elements are supported: acid, wall, radiation,
 * elevation or resource.</td></tr>
 * </table>
 *
 * <p>
 * The supported timing, type and shape elements are:</p>
 *
 * <table style='border:1px solid black;width:100%'>
 * <caption>Timing Elements</caption>
 * <tr><th align='left' style='width:50%'>Element</th><th align='left'>Description</th></tr>
 * <tr><td>{@code <at_start>}</td><td>specifies an element to be placed when the world is
 * created</td></tr>
 * <tr><td>{@code <with_period period='n'>}</td><td>specifies an element to be placed every
 * {@code n} ticks</td></tr>
 * <tr><td>{@code <in_season season='summer|winter|sprint|autumn'>}</td><td>species an element to be
 * placed every tick in the season with the given name</td></tr>
 * </table>
 *
 * <br>
 *
 * <table style='border:1px solid black;width:100%'>
 * <caption>Type Elements</caption>
 * <tr><th align='left' style='width:50%'>Element</th><th align='left'>Description</th></tr>
 * <tr><td>{@code <acid>}</td><td>places acid</td></tr>
 * <tr><td>{@code <wall>}</td><td>place a wall</td></tr>
 * <tr><td>{@code <radiation amount='n'>}</td><td>add radiation of strength {@code n} (up to
 * 3)</td></tr>
 * <tr><td>{@code <add_resources amount='n'}</td><td>add {@code n} resources (up to 255)</td></tr>
 * <tr><td>{@code <elevation amount='n'>}</td><td>add {@code n} elevation (up to 255)</td></tr>
 * </table>
 *
 * <br>
 *
 * <table style='border:1px solid black;width:100%'>
 * <caption>Shape Elements</caption>
 * <tr><th align='left' style='width:50%'>Element</th><th align='left'>Description</th></tr>
 * <tr><td>{@code <everywhere>}</td><td>place at every position in the space</td></tr>
 * <tr><td>{@code <horizontal_edges width='w'>}</td><td>place bands of width {@code w} along the top
 * and bottom edges</td></tr>
 * <tr><td>{@code <vertical_edges width='w'>}</td><td>place bands of width {@code w} along the left
 * and right edges</td></tr>
 * <tr><td>{@code <horizontal_dividers count='n' width='w' gap='g'>}</td><td>place {@code n}
 * horizontal bands of width {@code w} with gaps of {@code g%} on the left and right ends</td></tr>
 * <tr><td>{@code <vertical_dividers count='n' width='w' gap='g'>}</td><td>place {@code n} vertical
 * bands of width {@code w} with gaps of {@code g%} on the top and bottom ends</td></tr>
 * <tr><td>{@code <pools count='n' radius='r'>}</td><td>randomly place {@code n} circles of radius
 * {@code r}. The number of pools is scaled relative to the size of the space.</td></tr>
 * <tr><td>{@code <maze cell='c' edge='e'>}</td><td>place walls of thickness {@code e} to form a
 * randomly generated maze in which each cell in the maze has a width and height of
 * {@code c}</td></tr>
 * </table>
 */
public class Loader {

    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    private WorldMaker maker;
    private WorldConfiguration config;
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
     * @param config the configuration for the world
     * @param name the name of the world to construct
     * @param input the XML input source
     * @throws SAXException if there are errors parsing the input source
     */
    public void load(WorldMaker maker, WorldConfiguration config,
            String name, InputSource input) throws SAXException {
        try {
            DocumentBuilder builder = FACTORY.newDocumentBuilder();
            Document doc = builder.parse(input);
            this.maker = maker;
            this.config = config;
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
        processChildren(element, this::processWorldElement);
    }

    /**
     * Process a world element
     */
    private void processWorldElement(Element element) throws SAXException {
        switch (element.getNodeName()) {
            case "description":
                description = Optional.of(element.getTextContent());
                break;
            case "configuration":
                processChildren(element, this::processConfiguration);
                break;
            default:
                processTiming(element);
                break;
        }
    }

    private void processConfiguration(Element element) throws SAXException {
        switch (element.getNodeName()) {
            case "temperature_range":
                config.setTemperatureRange(getInt(element, "min"), getInt(element, "max"));
                break;
            case "mutation_rate":
                config.setNormalMutationRate(getInt(element, "normal"));
                if (element.hasAttribute("radiation"))
                    config.setRadiatedMutationRate(getInt(element, "radiation"));
                break;
            case "year":
                config.setYear(getInt(element, "length"), getInt(element, "variation"));
                break;
            case "seed":
                config.setSeed(getInt(element, "count"), getInt(element, "energy"));
                break;
            case "activity_cost":
                if (element.hasAttribute("activity")) {
                    String activityName = element.getAttribute("activity");
                    WorldActivity activity = WorldActivity.withName(activityName)
                            .orElseThrow(() -> new SAXException("Illegal activity " + activityName));
                    config.setActivityCost(activity, getInt(element, "cost"));
                    if (element.hasAttribute("factor"))
                        config.setActivityFactor(activity, getInt(element, "factor"));
                } else {
                    config.setDefaultActivityCost(getInt(element, "cost"));
                    if (element.hasAttribute("factor"))
                        config.setDefaultActivityFactor(getInt(element, "factor"));
                }
                break;
            case "activation_cost":
                config.setBaseCost(getInt(element, "base"));
                config.setAgeCost(getInt(element, "age"));
                config.setSizeCost(getInt(element, "size"));
                break;
            case "minimum_split_time":
                config.setMinimumSplitTime(getInt(element, "period"));
                break;
            case "consumption_rate":
                config.setConsumptionRate(getInt(element, "rate"));
                break;
            case "half_life":
                String groundName = element.getAttribute("element");
                GroundElement ground = Arrays.stream(GroundElement.values())
                        .filter(e -> e.name().equalsIgnoreCase(groundName))
                        .findAny()
                        .orElseThrow(() -> new SAXException("No half life element " + groundName));
                config.setHalfLife(ground, getInt(element, "period"));
                break;
            default:
                throw new SAXException("Illegal configuration: " + element.getNodeName());
            /*
        CONSUMPTION_RATE("Consumption Rate", 50),
             */
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
            case "in_season":
                if (!element.hasAttribute("season"))
                    throw new SAXException("No season attribute for timing in_season");
                String seasonName = element.getAttribute("season");
                Season season = Arrays.stream(Season.values())
                        .filter(s -> s.getName().equalsIgnoreCase(seasonName))
                        .findAny()
                        .orElseThrow(() -> new SAXException("No season " + seasonName));
                timing = maker.duringSeason(season);
                break;
            default:
                throw new SAXException("Illegal timing: " + element.getNodeName());
        }
        processChildren(element, el -> processType(timing, el));
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
        processChildren(element, el -> processShape(timing, type, el));
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
     * Simple functional interface to define a process to use with children of an element
     */
    @FunctionalInterface
    private interface ChildProcessor {

        void processChild(Element child) throws SAXException;
    }

    /**
     * process all child elements of an element
     */
    private void processChildren(Element parent, ChildProcessor process) throws SAXException {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                process.processChild((Element) child);
        }
    }

    /**
     * Get a mandatory integer attribute from an element
     */
    private int getInt(Element element, String attribute) throws SAXException {
        if (!element.hasAttribute(attribute))
            throw new SAXException("Element " + element.getNodeName()
                    + " must have attribute " + attribute);
        try {
            return Integer.valueOf(element.getAttribute(attribute));
        } catch (NumberFormatException ex) {
            throw new SAXException("Attribute " + attribute + " is not a number");
        }
    }
}
