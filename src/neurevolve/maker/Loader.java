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

public class Loader {

    private static final DocumentBuilderFactory FACTORY = DocumentBuilderFactory.newInstance();
    private WorldMaker maker;
    private String name;
    private Optional<String> description = Optional.empty();

    public String getName() {
        return name;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public void load(WorldMaker maker, String name, InputSource input) throws SAXException {
        try {
            DocumentBuilder builder = FACTORY.newDocumentBuilder();
            Document doc = builder.parse(input);
            this.maker = maker;
            this.name = name;
            processWorld((Element) doc.getFirstChild());
        } catch (ParserConfigurationException | IOException ex) {
            Logger.getLogger(Loader.class.getName()).log(Level.SEVERE, "XML parsing error", ex);
        }
    }

    private void processWorld(Element element) throws SAXException {
        if (!element.getNodeName().equals("world"))
            throw new SAXException("XML document node must be <world>");
        if (element.hasAttribute("name"))
            name = element.getAttribute("name");
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                processWorldElement((Element) child);
        }
    }

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
