package neurevolve.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import neurevolve.organism.Organism;
import neurevolve.world.Population;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;

public class ZoomWindow {

    private static final int PIXEL_SIZE = 20;
    private static final int MARGIN = 2;
    private static final int SCALE = 30;
    private static final int HALF_SCALE = SCALE / 2;
    private static final int SIDE = PIXEL_SIZE * SCALE;
    private static final int MAX_SNAPSHOTS = 50;

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

    @FunctionalInterface
    private interface PositionProcessor {

        void process(int x, int y, int position);
    }

    private void forEachPosition(PositionProcessor processor) {
        for (int x = 0; x < SCALE; x++) {
            for (int y = 0; y < SCALE; y++) {
                int position = space.position(Math.floorMod((centreX + x - HALF_SCALE) , space.getWidth()), Math.floorMod((centreY + y - HALF_SCALE) , space.getHeight()));
                processor.process(x, y, position);
            }
        }
    }

    private class SnapShot {

        private final int resources[][] = new int[SCALE][SCALE];
        private final Organism organims[][] = new Organism[SCALE][SCALE];

        public SnapShot() {
            int[] resourceCopy = world.getResourceCopy();
            forEachPosition((x, y, p) -> resources[x][y] = resourceCopy[p]);
            Population population = world.getPopulationCopy();
            forEachPosition((x, y, p) -> organims[x][y] = population.getOrganism(p));
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
                    g.fillRect(x * PIXEL_SIZE + MARGIN, y * PIXEL_SIZE + MARGIN, PIXEL_SIZE - 2 * MARGIN, PIXEL_SIZE - 2 * MARGIN);
                    if (snapShot.organims[x][y] != null) {
                        g.setColor(Color.red);
                        g.fillOval(x * PIXEL_SIZE + 2 * MARGIN, y * PIXEL_SIZE + 2 * MARGIN, PIXEL_SIZE - 4 * MARGIN, PIXEL_SIZE - 4 * MARGIN);
                    }
                });
            }
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
        tools.add(new JButton(new AbstractAction("|<") {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentSnapShot = 0;
                updatePositionLabel();
                frame.repaint();
            }
        }));
        tools.add(new JButton(new AbstractAction("<") {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentSnapShot = Math.max(0, currentSnapShot - 1);
                updatePositionLabel();
                frame.repaint();
            }
        }));
        tools.addSeparator();
        tools.add(snapShotCountLabel);
        tools.addSeparator();
        tools.add(new JButton(new AbstractAction(">") {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentSnapShot = Math.min(snapShots.size() - 1, currentSnapShot + 1);
                updatePositionLabel();
                frame.repaint();
            }
        }));
        tools.add(new JButton(new AbstractAction(">|") {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentSnapShot = snapShots.size() - 1;
                updatePositionLabel();
                frame.repaint();
            }
        }));
        zoomPanel = new ZoomPanel();
        zoomPanel.setPreferredSize(new Dimension(SIDE, SIDE));
        frame.add(zoomPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.pack();
        world.addTickListener(tickListener);
    }

    private void tick() {
        if (snapShots.size() < MAX_SNAPSHOTS) {
            snapShots.add(new SnapShot());
            updatePositionLabel();
        }
    }

    private void updatePositionLabel() {
        snapShotCountLabel.setText((currentSnapShot + 1) + " of " + snapShots.size());
    }

    public void show() {
        frame.setVisible(true);
    }

    public void hide() {
        world.removeTickListener(tickListener);
        frame.setVisible(false);
    }

}
