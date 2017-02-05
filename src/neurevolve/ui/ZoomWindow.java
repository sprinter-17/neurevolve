package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.stream.Collectors;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import neurevolve.organism.Organism;
import neurevolve.organism.RecipeDescriber;
import neurevolve.world.Population;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;

public class ZoomWindow {

    private static final int PIXEL_SIZE = 20;
    private static final int MARGIN1 = 2;
    private static final int MARGIN2 = 4;
    private static final int SCALE = 30;
    private static final int HALF_SCALE = SCALE / 2;
    private static final int SIDE = PIXEL_SIZE * SCALE;
    private static final int NETWORK_PANEL_WIDTH = 210;
    private static final int NETWORK_PANEL_VALUE = 170;
    private static final int TEXT_HEIGHT = 17;
    private static final int MAX_SNAPSHOTS = 100;
    private static final Color POSITIVE = new Color(40, 120, 20);
    private static final Color NEGATIVE = new Color(190, 30, 70);

    private final World world;
    private final Space space;
    private final WorldConfiguration config;
    private final int centreX;
    private final int centreY;
    private final JFrame frame;
    private final ZoomPanel zoomPanel;
    private final int[][] elevations = new int[SCALE][SCALE];
    private final List<SnapShot> snapShots = new ArrayList<>();
    private final Runnable tickListener = this::tick;
    private final JLabel snapShotCountLabel = new JLabel("0");
    private int currentSnapShot = 0;
    private Organism currentOrganism = null;

    @FunctionalInterface
    private interface PositionProcessor {

        void process(int x, int y, int position);
    }

    private void forEachPosition(PositionProcessor processor) {
        for (int x = 0; x < SCALE; x++) {
            for (int y = 0; y < SCALE; y++) {
                int position = space.position(
                        Math.floorMod(centreX + x - HALF_SCALE, space.getWidth()),
                        Math.floorMod(centreY + y - HALF_SCALE, space.getHeight()));
                processor.process(x, y, position);
            }
        }
    }

    private class NetworkSnapShot {

        private final Organism organism;
        private final int direction;
        private final int energy;
        private final int[] values;

        public NetworkSnapShot(Organism organism) {
            this.organism = organism;
            direction = organism.getWorldValue(Population.DIRECTION_CODE);
            energy = organism.getEnergy();
            values = organism.copyValues();
        }
    }

    private NetworkSnapShot getNetwork(Organism organism) {
        if (organism == null || snapShots.isEmpty())
            return null;
        return snapShots.get(currentSnapShot).networks.stream()
                .filter(n -> n.organism == organism)
                .findAny().orElse(null);
    }

    private class SnapShot {

        private final int resources[][] = new int[SCALE][SCALE];
        private final Organism organisms[][] = new Organism[SCALE][SCALE];
        private final List<NetworkSnapShot> networks = new ArrayList<>();

        public SnapShot() {
            int[] resourceCopy = world.getResourceCopy();
            Population population = world.getPopulationCopy();
            forEachPosition((x, y, p) -> process(x, y, resourceCopy[p], population.getOrganism(p)));
        }

        private void process(int x, int y, int resource, Organism organism) {
            resources[x][y] = resource;
            if (organism != null) {
                organisms[x][y] = organism;
                networks.add(new NetworkSnapShot(organism));
            }
        }
    }

    private class ZoomPanel extends JPanel {

        @Override
        public void paint(Graphics g) {
            if (snapShots.isEmpty()) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, SIDE, SIDE);
            } else {
                SnapShot snapShot = snapShots.get(currentSnapShot);
                forEachPosition((x, y, p) -> {
                    g.setColor(new Color(MapPanel.elevationColour(config, elevations[x][y])));
                    g.fillRect(x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
                    g.setColor(new Color(MapPanel.resourceColour(config, snapShot.resources[x][y])));
                    g.fillRect(x * PIXEL_SIZE + MARGIN1, y * PIXEL_SIZE + MARGIN1, PIXEL_SIZE - 2 * MARGIN1, PIXEL_SIZE - 2 * MARGIN1);
                    Organism organism = snapShot.organisms[x][y];
                    if (organism != null) {
                        g.setColor(Color.RED);
                        g.fillOval(x * PIXEL_SIZE + MARGIN2, y * PIXEL_SIZE + MARGIN2, PIXEL_SIZE - 2 * MARGIN2, PIXEL_SIZE - 2 * MARGIN2);
                        int direction = getNetwork(organism).direction;
                        g.setColor(Color.BLACK);
                        g.fillArc(x * PIXEL_SIZE + MARGIN2, y * PIXEL_SIZE + MARGIN2, PIXEL_SIZE - 2 * MARGIN2, PIXEL_SIZE - 2 * MARGIN2, -90 * direction - 45, 90);
                        if (organism == currentOrganism) {
                            g.setColor(Color.YELLOW);
                            g.drawRect(x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE - 1, PIXEL_SIZE - 1);
                        }
                    }
                });
            }
        }
    }

    private class NetworkPanel extends JPanel {

        private int line = 1;
        private int firstNeuronLine;
        private RecipeDescriber describer = null;
        private final Map<Integer, String> toolTips = new HashMap<>();

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            NetworkSnapShot network = getNetwork(currentOrganism);
            if (network == null) {
                g.setColor(Color.GRAY);
                g.fillRect(0, 0, NETWORK_PANEL_WIDTH, SIDE);
            } else {
                line = 1;
                describer = network.organism.describeRecipe();
                g.setColor(Color.BLACK);
                g.drawString("Length of recipe", 10, line * TEXT_HEIGHT);
                g.drawString(String.valueOf(describer.getLength()), NETWORK_PANEL_VALUE - 35, line * TEXT_HEIGHT);
                line++;
                g.drawString("Amount of junk", 10, line * TEXT_HEIGHT);
                g.drawString(String.valueOf(describer.getJunk()), NETWORK_PANEL_VALUE - 35, line * TEXT_HEIGHT);
                line++;
                g.drawString("Energy", 10, line * TEXT_HEIGHT);
                g.drawString(String.valueOf(network.energy), NETWORK_PANEL_VALUE - 35, line * TEXT_HEIGHT);
                line++;
                line++;
                firstNeuronLine = line;
                toolTips.clear();
                describer.getNeuronDescriptions()
                        .filter(RecipeDescriber.NeuronDescription::isNotJunk)
                        .forEach(desc -> {
                            g.setColor(Color.BLACK);
                            g.drawString(desc.getNeuronDescription(), 10, line * TEXT_HEIGHT);
                            int value = network.values[line - firstNeuronLine];
                            g.setColor(value >= 0 ? POSITIVE : NEGATIVE);
                            g.drawString(String.valueOf(Math.abs(value)), NETWORK_PANEL_VALUE, line * TEXT_HEIGHT);
                            toolTips.put(line, getInputOutputs(desc));
                            line++;
                        });
            }
        }

        private String getInputOutputs(RecipeDescriber.NeuronDescription neuronDescriber) {
            if (neuronDescriber.getInputDescriptions().count() == 0 && neuronDescriber.getOutputDescriptions().count() == 0)
                return "None";
            else
                return neuronDescriber.getInputDescriptions().collect(Collectors.joining(" "))
                        + neuronDescriber.getOutputDescriptions().collect(Collectors.joining(" "));
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            int mouseLine = 1 + event.getY() / TEXT_HEIGHT;
            return toolTips.getOrDefault(mouseLine, "None");
        }

    }

    public ZoomWindow(World world, Space space, WorldConfiguration config, int centreX, int centreY) {
        this.world = world;
        this.space = space;
        this.config = config;
        this.centreX = centreX;
        this.centreY = centreY;
        int[] worldElevations = world.getElevationCopy();
        forEachPosition((x, y, p) -> elevations[x][y] = worldElevations[p]);
        this.frame = new JFrame("Zoom @" + centreX + "," + centreY);
        JToolBar tools = new JToolBar();
        tools.setBorder(BorderFactory.createEtchedBorder());
        frame.add(tools, BorderLayout.NORTH);
        tools.add(new JButton(new AbstractAction("Close") {
            @Override
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        }));
        tools.addSeparator();
        tools.add(movementButton("|<", () -> 0));
        tools.add(movementButton("<<", () -> currentSnapShot - 10));
        tools.add(movementButton("<", () -> currentSnapShot - 1));
        tools.addSeparator();
        snapShotCountLabel.setPreferredSize(new Dimension(200, 5));
        tools.add(snapShotCountLabel);
        tools.addSeparator();
        tools.add(movementButton(">", () -> currentSnapShot + 1));
        tools.add(movementButton(">>", () -> currentSnapShot + 10));
        tools.add(movementButton(">|", () -> snapShots.size() - 1));
        zoomPanel = new ZoomPanel();
        zoomPanel.setPreferredSize(new Dimension(SIDE, SIDE));
        frame.add(zoomPanel, BorderLayout.CENTER);

        NetworkPanel networkDisplay = new NetworkPanel();
        networkDisplay.setPreferredSize(new Dimension(NETWORK_PANEL_WIDTH, SIDE));
        zoomPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!snapShots.isEmpty()) {
                    SnapShot snapShot = snapShots.get(currentSnapShot);
                    int x = e.getX() / PIXEL_SIZE;
                    int y = e.getY() / PIXEL_SIZE;
                    if (x < SIDE && y < SIDE) {
                        currentOrganism = snapShot.organisms[x][y];
                        System.out.println(x + "," + y + ":" + currentOrganism.toString());
                    }
                    networkDisplay.repaint();
                    zoomPanel.repaint();
                }
            }
        });

        frame.add(networkDisplay, BorderLayout.EAST);
        networkDisplay.setToolTipText("Network information");
        frame.setLocationRelativeTo(null);
        frame.pack();
    }

    private JButton movementButton(String text, IntSupplier position) {
        return new JButton(new AbstractAction(text) {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveTo(position.getAsInt());
            }
        });
    }

    private void moveTo(int snapShot) {
        currentSnapShot = snapShot;
        if (currentSnapShot < 0)
            currentSnapShot = 0;
        if (currentSnapShot >= snapShots.size())
            currentSnapShot = snapShots.size() - 1;
        updatePositionLabel();
        frame.repaint();
    }

    private void tick() {
        if (snapShots.size() < MAX_SNAPSHOTS) {
            snapShots.add(new SnapShot());
            updatePositionLabel();
            frame.repaint();
        }
    }

    private void updatePositionLabel() {
        snapShotCountLabel.setText((currentSnapShot + 1) + " of " + snapShots.size());
    }

    public void show() {
        world.addTickListener(tickListener);
        frame.setVisible(true);
    }

    public void hide() {
        world.removeTickListener(tickListener);
        frame.setVisible(false);
    }

}
