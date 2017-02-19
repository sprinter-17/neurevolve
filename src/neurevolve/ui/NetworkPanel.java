package neurevolve.ui;

import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_BUTT;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.MouseInputAdapter;
import neurevolve.organism.RecipeDescriber;
import neurevolve.organism.RecipeDescriber.Neuron;
import neurevolve.organism.Species;
import neurevolve.world.World;

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

    private static final Point FORWARD = new Point(16, 0);
    private static final Point LEFT = new Point(0, -8);
    private static final Point RIGHT = new Point(0, +8);

    private final World world;
    private final Map<Integer, Point> inputPositions = new HashMap<>();
    private final Map<Integer, Point> neuronPositions = new HashMap<>();
    private final List<Neuron> neurons = new ArrayList<>();
    private final JButton showOrHideInactiveButton;

    private BufferedImage networkImage;
    private BufferedImage inputImage;

    private Species species = null;
    private int[] ranges;

    private Point offset = new Point(0, 0);
    private Point drag = new Point(0, 0);

    private boolean showInactiveNeurons = false;

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
            drag.x = e.getX() - clickX;
            drag.y = e.getY() - clickY;
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            offset.x += drag.x;
            offset.y += drag.y;
            drag = new Point(0, 0);
            repaint();
        }
    }

    /**
     * Construct a network panel
     *
     * @param world the world the network's organism is in
     */
    public NetworkPanel(World world) {
        super(new BorderLayout());
        this.world = world;
        Dragger dragger = new Dragger();
        addMouseListener(dragger);
        addMouseMotionListener(dragger);
        drawInputImage();
        JToolBar toolBar = new JToolBar();
        showOrHideInactiveButton = new JButton(showAllNeurons());
        toolBar.add(showOrHideInactiveButton);
        add(toolBar, BorderLayout.NORTH);
        add(new ImagePanel(), BorderLayout.CENTER);
        setPreferredSize(new Dimension(INPUT_IMAGE_WIDTH * 2, 500));
    }

    private Action showAllNeurons() {
        return new AbstractAction("Show All Neurons") {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOrHideInactiveButton.setAction(hideInactiveNeurons());
                showInactiveNeurons = true;
                paintNetwork();
            }
        };
    }

    private Action hideInactiveNeurons() {
        return new AbstractAction("Hide Inactive Neurons") {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOrHideInactiveButton.setAction(showAllNeurons());
                showInactiveNeurons = false;
                paintNetwork();
            }
        };
    }

    private class ImagePanel extends JPanel {

        @Override
        public void paint(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            if (species != null) {
                g.drawImage(networkImage, INPUT_IMAGE_WIDTH + offset.x + drag.x,
                        offset.y + drag.y, this);
                g.drawImage(inputImage, 0, 0, this);
                paintInputs((Graphics2D) g);
            }
        }

        /**
         * Paint the links from the input image to the network image
         */
        private void paintInputs(Graphics2D g) {
            neuronPositions.forEach((n, p) -> paintNeuronInputs(g, n, p));
        }

        private void paintNeuronInputs(Graphics2D g, int neuron, Point point) {
            int maxX = getWidth() - INPUT_IMAGE_WIDTH;
            int maxY = getHeight();
            Point p = new Point(offset.x + drag.x + point.x,
                    offset.y + drag.y + point.y + NEURON_HEIGHT / 2);
            if (p.x > 0 && p.x < maxX && p.y > 0 && p.y < maxY) {
                neurons.get(neuron).forEachInput((i, w) -> paintSingleInput(g, i, w, p));
            }
        }

        private void paintSingleInput(Graphics2D g, int inputCode, int weight, Point point) {
            inputCode = world.getInputCode(world.describeInput(inputCode));
            if (inputPositions.containsKey(inputCode)) {
                Point start = inputPositions.get(inputCode);
                if (weight < 0) {
                    g.setColor(NEGATIVE_COLOUR);
                } else if (weight > 0) {
                    g.setColor(POSITIVE_COLOUR);
                } else {
                    g.setColor(Color.DARK_GRAY);
                }
                g.setStroke(new BasicStroke(Math.min(5, Math.abs(weight) / 4)));
                g.drawLine(start.x, start.y, INPUT_IMAGE_WIDTH + point.x, point.y);
            } else {
                System.out.println("No position for input code " + inputCode);
            }
        }
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
        int code;

        int height = 44;
        code = world.getInputCode("Own Age");
        drawInputName(g, "Own Age", y);
        drawDot(g, here(y), code);

        y += 20;
        code = world.getInputCode("Own Energy");
        drawInputName(g, "Own Energy", y);
        drawDot(g, here(y), code);

        y += 20;
        code = world.getInputCode("Temperature Here");
        drawInputName(g, "Temperature", y);
        drawDot(g, here(y), code);

        y += 30;
        drawInputName(g, "Slope", y);
        drawLine(g, y, world.getInputCode("Look Slope Forward"), FORWARD);
        drawLine(g, y, world.getInputCode("Look Slope Left"), LEFT);
        drawLine(g, y, world.getInputCode("Look Slope Right"), RIGHT);

        y += height;
        drawVisualInput(g, "Resources", y);

        y += height;
        drawVisualInput(g, "Acid", y);

        y += height;
        drawVisualInput(g, "Other Colour", y);

        y += height;
        drawVisualInput(g, "Other Energy", y);

        y += height;
        drawVisualInput(g, "Wall", y);

        y += height;
        drawVisualInput(g, "Radiation", y);

        y += height;
        drawVisualInput(g, "Body", y);
    }

    /**
     * Draw a string for the name of the input
     */
    private void drawInputName(Graphics2D g, String name, int y) {
        g.drawString(name, 80 - g.getFontMetrics().stringWidth(name), y);
    }

    /**
     * Draw a dot representing an input
     */
    private void drawDot(Graphics2D g, Point p, int input) {
        g.fillOval(p.x - 4, p.y - 4, 8, 8);
        inputPositions.put(input, p);
    }

    private void drawVisualInput(Graphics2D g, String name, int y) {
        drawInputName(g, name, y);
        drawLine(g, y, world.getInputCode("Look " + name + " Here"));
        drawLine(g, y, world.getInputCode("Look " + name + " Forward"), FORWARD);
        drawLine(g, y, world.getInputCode("Look " + name + " Far Forward"), FORWARD, FORWARD);
        drawLine(g, y, world.getInputCode("Look " + name + " Left"), LEFT);
        drawLine(g, y, world.getInputCode("Look " + name + " Forward Left"), FORWARD, LEFT);
        drawLine(g, y, world.getInputCode("Look " + name + " Far Left"), LEFT, LEFT);
        drawLine(g, y, world.getInputCode("Look " + name + " Right"), RIGHT);
        drawLine(g, y, world.getInputCode("Look " + name + " Forward Right"), FORWARD, RIGHT);
        drawLine(g, y, world.getInputCode("Look " + name + " Far Right"), RIGHT, RIGHT);
    }

    /**
     * Draw a line and dot representing an input
     */
    private void drawLine(Graphics2D g, int y, int input, Point... adjustments) {
        Point from = here(y);
        Point to = here(y);
        for (Point adjustment : adjustments) {
            to.x += adjustment.x;
            to.y += adjustment.y;
        }
        if (from.x != to.x || from.y != to.y)
            g.drawLine(from.x, from.y, to.x, to.y);
        drawDot(g, to, input);
    }

    private Point here(int y) {
        return new Point(86, y - 4);
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
        paintNetwork();
    }

    /**
     * Clear the network panel
     */
    public void clear() {
        species = null;
        repaint();
    }

    /**
     * Paint the network into a graphics
     */
    private void paintNetwork() {
        resetDrag();
        calculateNeuronPositions();
        Graphics2D g = createImage();
        paintBackground(g);
        neuronPositions.forEach((n, p) -> paintNeuron(g, n, p));
        repaint();
    }

    private void resetDrag() {
        offset = new Point(0, 0);
    }

    private void calculateNeuronPositions() {
        neuronPositions.clear();
        if (species != null) {
            Point point = new Point(INDENT, GAP);
            for (int n = 0; n < neurons.size(); n++) {
                if (showInactiveNeurons || !neurons.get(n).isInactive()) {
                    neuronPositions.put(n, point);
                    point = new Point(point.x + INDENT, point.y + NEURON_HEIGHT + GAP);
                }
            }
        }
    }

    private Graphics2D createImage() {
        Point lastPoint = neuronPositions.values().stream()
                .max(Comparator.comparingDouble(Point::getY))
                .orElse(new Point(0, 0));
        int width = lastPoint.x + NEURON_WIDTH + 5;
        int height = lastPoint.y + NEURON_HEIGHT + 5;
        networkImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        return networkImage.createGraphics();
    }

    private void paintBackground(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, networkImage.getWidth(), networkImage.getHeight());
    }

    private void paintNeuron(Graphics2D g, int neuron, Point point) {
        paintNeuronBox(g, neuron, point);
        paintLinks(g, neuron, point);
        paintThreshold(g, neuron, point);
    }

    private void paintNeuronBox(Graphics2D g, int neuron, Point point) {
        Color colour = ranges != null && ranges[neuron] > 0 ? Color.CYAN : Color.LIGHT_GRAY;
        if (neurons.get(neuron).hasActivity()) {
            paintBox(g, colour, point.x, point.y, NEURON_WIDTH);
            paintActivity(g, neurons.get(neuron).getActivityName(),
                    point.x + 10, point.y + NEURON_HEIGHT - 10);
        } else {
            paintBox(g, colour, point.x, point.y, INDENT);
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

    private void paintLinks(Graphics2D g, int neuron, Point point) {
        Map<Integer, Integer> synapses = new HashMap<>();
        neurons.get(neuron).forEachLink(synapses::put);
        int height = synapses.values().stream()
                .mapToInt(n -> Math.min(5, Math.abs(n)) + 1).sum();
        int linkSpace = NEURON_HEIGHT / 2 + height / 2;
        for (int n : synapses.keySet()) {
            paintLink(g, n, neuron, linkSpace, synapses.get(n));
            linkSpace -= 1 + synapses.get(n);
        }
    }

    /**
     * Paint a link from one neuron to another
     */
    private void paintLink(Graphics2D g, int from, int to, int linkSpace, int weight) {
        int fromX = neuronPositions.get(from).x + 5;
        int fromY = neuronPositions.get(from).y + NEURON_HEIGHT + 1;
        int toX = neuronPositions.get(to).x - 1;
        int toY = neuronPositions.get(to).y + linkSpace;
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
    private void paintThreshold(Graphics2D g, int neuron, Point point) {
        int threshold = neurons.get(neuron).getThreshold();
        if (threshold < 0) {
            threshold = Math.min(10, -threshold);
            g.setColor(POSITIVE_COLOUR);
            g.fillRect(point.x - threshold - 1, point.y, threshold, NEURON_HEIGHT);
        } else if (threshold > 0) {
            threshold = Math.min(10, threshold);
            g.setColor(NEGATIVE_COLOUR);
            g.fillRect(point.x - threshold - 1, point.y, threshold, NEURON_HEIGHT);
        }
    }
}
