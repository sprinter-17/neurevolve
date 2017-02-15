package neurevolve.ui;

import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_BUTT;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import neurevolve.organism.RecipeDescriber;
import neurevolve.organism.Species;
import neurevolve.world.WorldInput;
import static neurevolve.world.WorldInput.AGE;
import static neurevolve.world.WorldInput.OWN_ENERGY;
import static neurevolve.world.WorldInput.TEMPERATURE;

/**
 * A {@code NetworkPanel} is used to visually display an organism's or species's network. It can be
 * used within another frame to allow a selected species or organism to be shown.
 */
public class NetworkPanel extends JPanel {

    private static final Color POSITIVE_COLOUR = Color.GREEN.darker();
    private static final Color NEGATIVE_COLOUR = Color.RED.darker();

    private static final int INPUT_IMAGE_WIDTH = 140;
    private static final int INDENT = 18;
    private static final int GAP = 5;
    private static final int NEURON_WIDTH = 100;
    private static final int NEURON_HEIGHT = 35;

    private BufferedImage networkImage;
    private BufferedImage inputImage;

    private EnumMap<WorldInput, Point> inputPositions = new EnumMap<>(WorldInput.class);

    private Species species = null;
    private final List<RecipeDescriber.Neuron> neurons = new ArrayList<>();
    private int[] ranges;

    private int offsetX = 0;
    private int offsetY = 0;

    private int dragX = 0;
    private int dragY = 0;

    private class Dragger extends MouseInputAdapter {

        private int clickX = 0;
        private int clickY = 0;

        @Override
        public void mousePressed(MouseEvent e) {
            clickX = e.getX();
            clickY = e.getY();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            dragX = e.getX() - clickX;
            dragY = e.getY() - clickY;
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            offsetX += dragX;
            offsetY += dragY;
            dragX = 0;
            dragY = 0;
            repaint();
        }

    }

    /**
     * Construct a network panel
     */
    public NetworkPanel() {
        super(null);
        Dragger dragger = new Dragger();
        addMouseListener(dragger);
        addMouseMotionListener(dragger);
        drawInputImage();
    }

    /**
     * Draw a fixed image representing the various inputs to neurons
     */
    private void drawInputImage() {
        inputImage = new BufferedImage(INPUT_IMAGE_WIDTH, 1000, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = inputImage.createGraphics();
        g.setColor(new Color(242, 217, 230));
        g.fillRect(0, 0, INPUT_IMAGE_WIDTH, 1000);
        g.setColor(Color.pink.darker());
        int y = 25;

        int height = 40;
        drawInputName(g, "Own Age", y);
        drawDot(g, here(y), AGE);

        y += height;
        drawInputName(g, "Own Energy", y);
        drawDot(g, here(y), OWN_ENERGY);

        y += height;
        drawInputName(g, "Temperature", y);
        drawDot(g, here(y), TEMPERATURE);

        y += height;
        drawInputName(g, "Resource", y);
        drawDot(g, here(y), WorldInput.LOOK_RESOURCE_HERE);
        drawLine(g, y, forward(y), WorldInput.LOOK_RESOURCE_FORWARD);
        drawLine(g, y, farForward(y), WorldInput.LOOK_RESOURCE_FAR_FORWARD);
        drawLine(g, y, left(y), WorldInput.LOOK_RESOURCE_LEFT);
        drawLine(g, y, right(y), WorldInput.LOOK_RESOURCE_RIGHT);

        y += height;
        drawInputName(g, "Slope", y);
        drawLine(g, y, forward(y), WorldInput.LOOK_SLOPE_FORWARD);
        drawLine(g, y, left(y), WorldInput.LOOK_SLOPE_LEFT);
        drawLine(g, y, right(y), WorldInput.LOOK_SLOPE_RIGHT);

        y += height;
        drawInputName(g, "Wall", y);
        drawLine(g, y, forward(y), WorldInput.LOOK_WALL_FORWARD);
        drawLine(g, y, farForward(y), WorldInput.LOOK_WALL_FAR_FORWARD);
        drawLine(g, y, left(y), WorldInput.LOOK_WALL_FORWARD);
        drawLine(g, y, right(y), WorldInput.LOOK_WALL_RIGHT);

        y += height;
        drawInputName(g, "Radiation", y);
        drawDot(g, here(y), WorldInput.LOOK_RADIATION_HERE);
        drawLine(g, y, forward(y), WorldInput.LOOK_RADIATION_FORWARD);
        drawLine(g, y, farForward(y), WorldInput.LOOK_RADIATION_FAR_FORWARD);
        drawLine(g, y, left(y), WorldInput.LOOK_RADIATION_LEFT);
        drawLine(g, y, right(y), WorldInput.LOOK_RADIATION_RIGHT);

        y += height;
        drawInputName(g, "Other Colour", y);
        drawLine(g, y, forward(y), WorldInput.LOOK_ORGANISM_FORWARD);
        drawLine(g, y, farForward(y), WorldInput.LOOK_ORGANISM_FAR_FORWARD);
        drawLine(g, y, left(y), WorldInput.LOOK_ORGANISM_LEFT);
        drawLine(g, y, right(y), WorldInput.LOOK_ORGANISM_RIGHT);

        y += height;
        drawInputName(g, "Other Energy", y);
        drawLine(g, y, forward(y), WorldInput.LOOK_ORGANISM_ENERGY_FORWARD);
        drawLine(g, y, farForward(y), WorldInput.LOOK_ORGANISM_ENERGY_FAR_FORWARD);
    }

    /**
     * Calculate the point representing input from the current position
     */
    private Point here(int y) {
        return new Point(86, y - 4);
    }

    /**
     * Calculate the point representing input from the forward position
     */
    private Point forward(int y) {
        return new Point(here(y).x + 16, here(y).y);
    }

    /**
     * Calculate the point representing input from the far forward position
     */
    private Point farForward(int y) {
        return new Point(here(y).x + 32, here(y).y);
    }

    /**
     * Calculate the point representing input from the left position
     */
    private Point left(int y) {
        return new Point(here(y).x, here(y).y - 12);
    }

    /**
     * Calculate the point representing input from the right position
     */
    private Point right(int y) {
        return new Point(here(y).x, here(y).y + 12);
    }

    /**
     * Draw a line and dot representing an input
     */
    private void drawLine(Graphics2D g, int y, Point to, WorldInput input) {
        g.drawLine(here(y).x, here(y).y, to.x, to.y);
        drawDot(g, to, input);
    }

    /**
     * Draw a dot representing an input
     */
    private void drawDot(Graphics2D g, Point p, WorldInput input) {
        g.fillOval(p.x - 4, p.y - 4, 8, 8);
        inputPositions.put(input, p);
    }

    /**
     * Draw a string for the name of the input
     */
    private void drawInputName(Graphics2D g, String name, int y) {
        g.drawString(name, 80 - g.getFontMetrics().stringWidth(name), y);
    }

    /**
     * Display a species in the network panel.
     *
     * @param species the species to display
     */
    public void showSpecies(Species species) {
        this.species = species;
        neurons.clear();
        RecipeDescriber describer = species.describeRecipe();
        for (int i = 0; i < describer.getSize(); i++) {
            neurons.add(describer.getNeuron(i));
        }
        ranges = species.getRanges();
        networkImage = new BufferedImage(500, 1000, BufferedImage.TYPE_INT_RGB);
        offsetX = 0;
        offsetY = 0;
        paintNetwork(networkImage.createGraphics());
        repaint();
    }

    /**
     * Clear the network panel
     */
    public void clear() {
        species = null;
        repaint();
    }

    /**
     * An {@code Indenter} is used to locate each Neuron by moving a consistent distance
     */
    private class Indenter {

        private int x = INDENT;
        private int y = GAP;
        private int i;

        private void next() {
            x += INDENT;
            y += GAP + NEURON_HEIGHT;
            i++;
        }
    }

    /**
     * Paint the input, network and links
     *
     * @param g the {@code Graphics} to paint within
     */
    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        if (species != null) {
            g.drawImage(networkImage, INPUT_IMAGE_WIDTH + offsetX + dragX, offsetY + dragY, this);
            g.drawImage(inputImage, 0, 0, this);
            paintInputs((Graphics2D) g);
        }
    }

    /**
     * Paint the links from the input image to the network image
     */
    private void paintInputs(Graphics2D g) {
        Indenter indenter = new Indenter();
        neurons.stream().forEach((neuron) -> {
            neuron.forEachInput((i, w) -> {
                WorldInput input = WorldInput.decode(i);
                if (inputPositions.containsKey(input)) {
                    Point start = inputPositions.get(input);
                    if (w < 0) {
                        g.setColor(NEGATIVE_COLOUR);
                    } else if (w > 0) {
                        g.setColor(POSITIVE_COLOUR);
                    } else {
                        g.setColor(Color.DARK_GRAY);
                    }
                    g.setStroke(new BasicStroke(Math.min(5, Math.abs(w) / 2)));
                    g.drawLine(start.x, start.y, INPUT_IMAGE_WIDTH + offsetX + dragX + indenter.x,
                            offsetY + dragY + indenter.y + NEURON_HEIGHT / 2);
                }
            });
            indenter.next();
        });
    }

    /**
     * Paint the network into a graphics
     */
    private void paintNetwork(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 500, 1000);
        if (species != null) {
            g.setColor(Color.BLACK);
            Indenter indenter = new Indenter();
            for (RecipeDescriber.Neuron neuron : neurons) {
                Color colour = ranges != null && ranges[indenter.i] > 0 ? Color.CYAN : Color.LIGHT_GRAY;
                if (neuron.hasActivity()) {
                    paintBox(g, colour, indenter.x, indenter.y, NEURON_WIDTH);
                    paintActivity(g, neuron.getActivityName(), indenter.x + 10, indenter.y + NEURON_HEIGHT - 10);
                } else {
                    paintBox(g, colour, indenter.x, indenter.y, INDENT);
                }
                Map<Integer, Integer> synapses = new HashMap<>();
                neuron.forEachLink(synapses::put);
                int height = synapses.values().stream()
                        .mapToInt(n -> Math.min(5, Math.abs(n)) + 1).sum();
                int link = NEURON_HEIGHT / 2 + height / 2;
                for (int n : synapses.keySet()) {
                    paintLink(g, n * INDENT + 5, indenter.x - 1, n * (NEURON_HEIGHT + GAP) + 1,
                            indenter.y + link, synapses.get(n));
                    link -= 1 + synapses.get(n);
                }
                paintThreshold(g, neuron.getThreshold(), indenter.x, indenter.y);
                indenter.next();
            }
        }
    }

    /**
     * Paint a box representing a neuron
     */
    private void paintBox(Graphics2D g, Color color, int x, int y, int width) {
        g.setColor(color);
        g.fillRect(x, y, width, NEURON_HEIGHT);
        g.setColor(Color.LIGHT_GRAY.darker());
        g.drawRect(x, y, width, NEURON_HEIGHT);
        g.setColor(Color.LIGHT_GRAY.brighter());
        g.drawRect(x + 1, y + 1, width - 2, NEURON_HEIGHT - 2);
    }

    /**
     * Paint a string for a Neuron's activity
     */
    private void paintActivity(Graphics2D g, String text, int x, int y) {
        g.setColor(Color.BLACK);
        g.drawString(text, x, y);
    }

    /**
     * Paint a link from one neuron to another
     */
    private void paintLink(Graphics2D g, int fromX, int toX, int fromY, int toY, int weight) {
        g.setColor(Color.BLACK);
        g.drawLine(fromX, fromY, fromX, toY);
        if (weight < 0) {
            g.setColor(NEGATIVE_COLOUR);
        } else if (weight > 0) {
            g.setColor(POSITIVE_COLOUR);
        }
        g.setStroke(new BasicStroke(Math.min(5, Math.abs(weight)), CAP_BUTT, CAP_BUTT));
        g.drawLine(fromX, toY, toX, toY);
        g.setStroke(new BasicStroke(1));
    }

    /**
     * Paint a visual display of a neuron's threshold
     */
    private void paintThreshold(Graphics2D g, int threshold, int x, int y) {
        if (threshold < 0) {
            threshold = Math.min(10, -threshold);
            g.setColor(POSITIVE_COLOUR);
            g.fillRect(x - threshold - 1, y, threshold, NEURON_HEIGHT);
        } else if (threshold > 0) {
            threshold = Math.min(10, threshold);
            g.setColor(NEGATIVE_COLOUR);
            g.fillRect(x - threshold - 1, y, threshold, NEURON_HEIGHT);
        }
    }

}
