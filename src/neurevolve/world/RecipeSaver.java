package neurevolve.world;

import java.io.IOException;
import java.io.StringWriter;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import neurevolve.organism.Code;
import static neurevolve.organism.Code.toInt;
import neurevolve.organism.Instruction;
import neurevolve.organism.Recipe;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class RecipeSaver {

    private static final DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private final World world;

    public RecipeSaver(World world) {
        this.world = world;
    }

    private class InstructionProcessor implements Instruction.Processor {

        private final Document doc;

        public InstructionProcessor(Document doc) {
            this.doc = doc;
        }

        @Override
        public void process(Instruction instruction, byte... values) {
            switch (instruction) {
                case ADD_NEURON:
                    Element addNeuron = addElement("add_neuron");
                    setInt(addNeuron, "weight", values[0]);
                    break;
                case ADD_LINK:
                    Element link = addElement("add_link");
                    setInt(link, "neuron", values[0]);
                    setInt(link, "weight", values[1]);
                    break;
                case ADD_INPUT:
                    Element input = addElement("add_input");
                    String name = world.describeInput(Code.toInt(values[0]))
                            .toLowerCase().replace(" ", "_");
                    input.setAttribute("input", name);
                    setInt(input, "weight", values[1]);
                    break;
                case ADD_DELAY:
                    Element delay = addElement("add_delay");
                    setInt(delay, "period", values[0]);
                    break;
                case SET_ACTIVITY:
                    Element setActivity = addElement("set_activity");
                    WorldActivity activity = WorldActivity.decode(toInt(values[0]));
                    setActivity.setAttribute("activity", activity.name().toLowerCase());
                    break;
                default:
                    throw new AssertionError(instruction.name());
            }
        }

        @Override
        public void junk(byte value) {
            Element junk = addElement("junk");
            junk.setAttribute("value", String.valueOf(Code.toInt(value)));
        }

        private Element addElement(String name) {
            Element child = doc.createElement(name);
            doc.getDocumentElement().appendChild(child);
            return child;
        }

    }

    public String save(Recipe recipe) {
        try {
            DocumentBuilder builder = BUILDER_FACTORY.newDocumentBuilder();
            Document doc = builder.newDocument();
            Element root = doc.createElement("recipe");
            root.setAttribute("colour", String.valueOf(recipe.getColour()));
            doc.appendChild(root);
            recipe.forEachInstruction(new InstructionProcessor(doc));
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter output = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(output));
            return output.toString();
        } catch (ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(RecipeSaver.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public Recipe load(InputSource source) throws SAXException {
        try {
            DocumentBuilder builder = BUILDER_FACTORY.newDocumentBuilder();
            Document doc = builder.parse(source);
            return processDocument((Element) doc.getFirstChild());
        } catch (ParserConfigurationException | IOException ex) {
            Logger.getLogger(RecipeSaver.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private Recipe processDocument(Element root) throws SAXException {
        if (!root.getNodeName().equals("recipe"))
            throw new SAXException("XML document node must be <recipe>");
        Recipe recipe = new Recipe(getInt(root, "colour"));
        for (Node child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE)
                processInstruction((Element) child, recipe, world);
        }
        return recipe;
    }

    private void processInstruction(Element element, Recipe recipe, World world) throws SAXException {
        switch (element.getNodeName()) {
            case "add_neuron":
                recipe.add(Instruction.ADD_NEURON, getByte(element, "weight"));
                break;
            case "add_link":
                recipe.add(Instruction.ADD_LINK, getByte(element, "neuron"), getByte(element, "weight"));
                break;
            case "add_input":
                OptionalInt code = world.getInputCode(element.getAttribute("input"));
                if (code.isPresent())
                    recipe.add(Instruction.ADD_INPUT, Code.fromInt(code.getAsInt()), getByte(element, "weight"));
                break;
            case "add_delay":
                recipe.add(Instruction.ADD_DELAY, getByte(element, "period"));
                break;
            case "set_activity":
                String activity = element.getAttribute("activity").toUpperCase();
                recipe.add(Instruction.SET_ACTIVITY, WorldActivity.valueOf(activity).code());
                break;
            case "junk":
                recipe.add(getByte(element, "value"));
                break;
            default:
                throw new SAXException("No instruction " + element.getNodeName());
        }
    }

    private byte getByte(Element element, String attribute) throws SAXException {
        return Code.fromInt(getInt(element, attribute));
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

    private void setInt(Element element, String attribute, byte value) {
        element.setAttribute(attribute, String.valueOf(toInt(value)));
    }

}
