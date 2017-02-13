package neurevolve.ui;

import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_BUTT;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import neurevolve.organism.RecipeDescriber.NeuronDescription;

public class NetworkPanel extends JPanel {

    private static final Color POSITIVE_COLOUR = Color.GREEN.darker();
    private static final Color NEGATIVE_COLOUR = Color.RED.darker();

    private static final int INDENT = 30;
    private static final int GAP = 10;
    private static final int NEURON_WIDTH = 100;
    private static final int NEURON_HEIGHT = 35;

    private BufferedImage image;
    private Species species = null;
    private List<NeuronDescription> neurons;
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

    public NetworkPanel() {
        super(null);
        Dragger dragger = new Dragger();
        addMouseListener(dragger);
        addMouseMotionListener(dragger);
    }

    public void showSpecies(Species species) {
        this.species = species;
        neurons = species.describeRecipe()
                .getNeuronDescriptions()
                .collect(Collectors.toList());
        ranges = species.getRanges();
        image = new BufferedImage(500, 1000, BufferedImage.TYPE_INT_RGB);
        offsetX = 0;
        offsetY = 0;
        paintNetwork(image.createGraphics());
        repaint();
    }

    public void clear() {
        species = null;
        repaint();
    }

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

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(image, offsetX + dragX, offsetY + dragY, this);
    }

    public void paintNetwork(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 500, 1000);
        if (species != null) {
            g.setColor(Color.BLACK);
            Indenter indenter = new Indenter();
            for (NeuronDescription neuron : neurons) {
                if (neuron.getActivity().isPresent()) {
                    paintBox(g, indenter.x, indenter.y, NEURON_WIDTH);
                    paintString(g, neuron.getActivity().get(), indenter.x + 10, indenter.y + NEURON_HEIGHT - 10);
                } else {
                    paintBox(g, indenter.x, indenter.y, INDENT);
                }
                Map<Integer, Integer> synapses = new HashMap<>();
                neuron.forEachSynapse(synapses::put);
                int height = synapses.values().stream().mapToInt(n -> Math.min(5, Math.abs(n)) + 1).sum();
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

    private void paintLink(Graphics2D g, int fromX, int toX, int fromY, int toY, int weight) {
        g.setColor(Color.BLACK);
        g.drawLine(fromX, fromY, fromX, toY);
        if (weight < 0)
            g.setColor(NEGATIVE_COLOUR);
        else if (weight > 0)
            g.setColor(POSITIVE_COLOUR);
        g.setStroke(new BasicStroke(Math.min(5, Math.abs(weight)), CAP_BUTT, CAP_BUTT));
        g.drawLine(fromX, toY, toX, toY);
        g.setStroke(new BasicStroke(1));
    }

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

    private void paintBox(Graphics2D g, int x, int y, int width) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y, width, NEURON_HEIGHT);
        g.setColor(Color.LIGHT_GRAY.darker());
        g.drawRect(x, y, width, NEURON_HEIGHT);
        g.setColor(Color.LIGHT_GRAY.brighter());
        g.drawRect(x + 1, y + 1, width - 2, NEURON_HEIGHT - 2);
    }

    private void paintString(Graphics2D g, String text, int x, int y) {
        g.setColor(Color.BLACK);
        g.drawString(text, x, y);
    }
}
