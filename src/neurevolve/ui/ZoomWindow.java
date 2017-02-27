package neurevolve.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.IntSupplier;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import neurevolve.organism.Organism;
import neurevolve.organism.RecipeDescriber;
import neurevolve.world.GroundElement;
import neurevolve.world.Population;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;

public class ZoomWindow {

    private static final int PIXEL_SIZE = 20;
    private static final int MARGIN1 = 2;
    private static final int MARGIN2 = 3;
    private static final int SCALE = 30;
    private static final int HALF_SCALE = SCALE / 2;
    private static final int SIDE = PIXEL_SIZE * SCALE;
    private static final int MAX_SNAPSHOTS = 100;

    private final World world;
    private final Space space;
    private final WorldConfiguration config;
    private final int centreX;
    private final int centreY;
    private final JFrame frame;
    private final ZoomPanel zoomPanel = new ZoomPanel();
    private final OrganismPanel organismPanel;
    private final List<SnapShot> snapShots = new ArrayList<>();
    private final Runnable tickListener = this::tick;
    private final JLabel snapShotCountLabel = new JLabel("0");
    private int currentSnapShot = 0;
    private Optional<OrganismSnapShot> currentOrganism = Optional.empty();

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

    private class OrganismSnapShot {

        private final int x;
        private final int y;
        private final long id;
        private final int colour;
        private final int age;
        private final int direction;
        private final int energy;
        private final int descendents;
        private final int[] values;
        private final RecipeDescriber recipe;
        private final OptionalLong parent;

        public OrganismSnapShot(Organism organism, int x, int y) {
            this.x = x;
            this.y = y;
            this.id = organism.getID();
            colour = organism.getColour();
            age = organism.getAge();
            direction = world.getOrganismDirection(organism);
            energy = organism.getEnergy();
            descendents = organism.getDescendents();
            values = organism.copyValues();
            recipe = organism.describeRecipe();
            parent = organism.getParent().map(Organism::getID)
                    .map(OptionalLong::of).orElse(OptionalLong.empty());
        }

        public Point2D.Float topLeft() {
            return new Point2D.Float(x * PIXEL_SIZE, y * PIXEL_SIZE);
        }

        public Point2D.Float centre() {
            return new Point2D.Float(x * PIXEL_SIZE + PIXEL_SIZE / 2, y * PIXEL_SIZE + PIXEL_SIZE / 2);
        }
    }

    private Optional<OrganismSnapShot> getOrganism(long id) {
        if (snapShots.isEmpty())
            return Optional.empty();
        else
            return snapShots.get(currentSnapShot).getOrganism(id);
    }

    private class SnapShot {

        private final int ground[][] = new int[SCALE][SCALE];
        private final List<OrganismSnapShot> organisms = new ArrayList<>();

        public SnapShot() {
            int[] groundCopy = world.copyGroundElements();
            Population population = world.getPopulationCopy();
            forEachPosition((x, y, p) -> process(x, y, groundCopy[p], population.getOrganism(p)));
        }

        private void process(int x, int y, int groundElement, Organism organism) {
            ground[x][y] = groundElement;
            if (organism != null) {
                organisms.add(new OrganismSnapShot(organism, x, y));
            }
        }

        public Optional<OrganismSnapShot> getOrganism(long id) {
            return organisms.stream().filter(o -> o.id == id).findAny();
        }
    }

    private class ZoomPanel extends JPanel {

        @Override
        public void paint(Graphics gr) {
            Graphics2D g = (Graphics2D) gr;

            if (snapShots.isEmpty()) {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, SIDE, SIDE);
            } else {
                SnapShot snapShot = snapShots.get(currentSnapShot);
                paintGround(g, snapShot);
                paintOrganisms(g, snapShot);
                paintParentage(g, snapShot);
                paintPath(g, currentOrganism);
            }
        }

        private void paintGround(Graphics2D g, SnapShot snapShot) {
            forEachPosition((x, y, p) -> {
                g.setColor(new Color(MapPanel.convertToColour(config, snapShot.ground[x][y])));
                g.fillRect(x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
                if (GroundElement.BODY.get(snapShot.ground[x][y]) > 0) {
                    g.setColor(Color.WHITE);
                    g.fillOval(x * PIXEL_SIZE, y * PIXEL_SIZE, PIXEL_SIZE, PIXEL_SIZE);
                    g.setColor(new Color(MapPanel.bodyColour(config)));
                    g.fillOval(x * PIXEL_SIZE + 2, y * PIXEL_SIZE + 2, PIXEL_SIZE - 4, PIXEL_SIZE - 4);
                }
            });
        }

        private void paintOrganisms(Graphics2D g, SnapShot snapShot) {
            snapShot.organisms.forEach(organism -> {
                int direction = organism.direction;
                Point point = new Point(
                        organism.x * PIXEL_SIZE + MARGIN1,
                        organism.y * PIXEL_SIZE + MARGIN1);
                g.setColor(organism.age == 0 ? Color.GREEN : Color.WHITE);
                g.fillArc(point.x, point.y, PIXEL_SIZE - 2 * MARGIN1, PIXEL_SIZE - 2 * MARGIN1,
                        90 * direction + 45, 270);
                g.setColor(new Color(organism.colour));
                g.fillArc(point.x + 1, point.y + 1, PIXEL_SIZE - 2 * MARGIN2, PIXEL_SIZE - 2 * MARGIN2, 90 * direction + 45, 270);
                if (currentOrganism.isPresent() && currentOrganism.get().id == organism.id) {
                    g.setColor(Color.YELLOW);
                    g.drawRect(organism.x * PIXEL_SIZE,
                            organism.y * PIXEL_SIZE, PIXEL_SIZE - 1, PIXEL_SIZE - 1);
                }
            });
        }

        private void paintParentage(Graphics2D g, SnapShot snapShot) {
            snapShot.organisms.stream()
                    .filter(o -> o.age == 0)
                    .filter(o -> o.parent.isPresent())
                    .forEach(child -> paintParentage(g, snapShot, child));
        }

        private void paintParentage(Graphics2D g, SnapShot snapShot, OrganismSnapShot child) {
            Optional<OrganismSnapShot> parent = snapShot.getOrganism(child.parent.getAsLong());
            if (parent.isPresent()) {
                g.setColor(Color.BLUE);
                g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND,
                        0, new float[]{1.0f, 3.0f}, 0.25f));
                g.draw(new Line2D.Float(child.centre(), parent.get().centre()));
            }
        }

        private void paintPath(Graphics2D g, Optional<OrganismSnapShot> current) {
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.CAP_ROUND,
                    0, new float[]{1.0f, 3.0f}, 0.25f));
            for (int s = currentSnapShot - 1; s >= 0 && current.isPresent(); s--) {
                Optional<OrganismSnapShot> previous = snapShots.get(s).getOrganism(current.get().id);
                if (previous.isPresent()) {
                    g.draw(new Line2D.Float(current.get().centre(), previous.get().centre()));
                }
                current = previous;
            }
        }
    }

    private class OrganismPanel extends JPanel {

        private final NetworkPanel networkDisplay = new NetworkPanel(world);
        private final JPanel dataDisplay = new JPanel(new GridLayout(0, 2, 5, 5));
        private final JLabel age = new JLabel();
        private final JLabel energy = new JLabel();
        private final JLabel descendents = new JLabel();

        public OrganismPanel() {
            super(new BorderLayout());
            addDataDisplay();
            addNetworkDisplay();
            update();
        }

        private void addDataDisplay() {
            add(dataDisplay, BorderLayout.NORTH);
            dataDisplay.add(new JLabel("Age"));
            dataDisplay.add(age);
            dataDisplay.add(new JLabel("Energy"));
            dataDisplay.add(energy);
            dataDisplay.add(new JLabel("Descendents"));
            dataDisplay.add(descendents);
            dataDisplay.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEtchedBorder(),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        }

        private void addNetworkDisplay() {
            add(networkDisplay, BorderLayout.CENTER);
            networkDisplay.setPreferredSize(new Dimension(400, 500));
        }

        private void update() {
            if (currentOrganism.isPresent() && getOrganism(currentOrganism.get().id).isPresent()) {
                OrganismSnapShot organism = getOrganism(currentOrganism.get().id).get();
                networkDisplay.showRecipe(organism.recipe, organism.values);
                age.setText(String.valueOf(organism.age));
                energy.setText(String.valueOf(organism.energy));
                descendents.setText(String.valueOf(organism.descendents));
            } else {
                networkDisplay.clear();
                age.setText("-");
                energy.setText("-");
                descendents.setText("-");
            }
        }
    }

    public ZoomWindow(World world, Space space, WorldConfiguration config, int centreX, int centreY) {
        this.world = world;
        this.space = space;
        this.config = config;
        this.centreX = centreX;
        this.centreY = centreY;
        this.frame = new JFrame("Zoom @" + centreX + "," + centreY);
        addTools();

        addZoomPanel();

        organismPanel = new OrganismPanel();
        organismPanel.setPreferredSize(new Dimension(600, SIDE));
        frame.add(organismPanel, BorderLayout.CENTER);

        frame.setLocationRelativeTo(null);
        frame.pack();

    }

    private void addZoomPanel() {
        zoomPanel.setPreferredSize(new Dimension(SIDE, SIDE));
        frame.add(zoomPanel, BorderLayout.WEST);
        zoomPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!snapShots.isEmpty()) {
                    SnapShot snapShot = snapShots.get(currentSnapShot);
                    int x = e.getX() / PIXEL_SIZE;
                    int y = e.getY() / PIXEL_SIZE;
                    if (x < SIDE && y < SIDE) {
                        currentOrganism = snapShot.organisms.stream()
                                .filter(o -> o.x == x && o.y == y)
                                .findAny();
                        organismPanel.update();
                    }
                    zoomPanel.repaint();
                }
            }
        });
    }

    private void addTools() {
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
        if (currentSnapShot < 0) {
            currentSnapShot = 0;
        }
        if (currentSnapShot >= snapShots.size()) {
            currentSnapShot = snapShots.size() - 1;
        }
        if (currentOrganism.isPresent())
            currentOrganism = getOrganism(currentOrganism.get().id);
        organismPanel.update();
        updatePositionLabel();
        frame.repaint();
    }

    private void tick() {
        if (snapShots.size() < MAX_SNAPSHOTS) {
            snapShots.add(new SnapShot());
            updatePositionLabel();
            if (snapShots.size() == 1)
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
