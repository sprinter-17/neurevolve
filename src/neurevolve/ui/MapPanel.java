package neurevolve.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JPanel;
import javax.swing.Timer;
import neurevolve.organism.Organism;
import neurevolve.world.Frame;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;
import neurevolve.world.WorldInput;

public class MapPanel extends JPanel {

    private final World world;
    private final WorldConfiguration config;
    private final Timer timer;
    private final BufferedImage image;
    private final int[] pixels;

    public MapPanel(World world, Frame frame, WorldConfiguration config) {
        this.world = world;
        this.config = config;
        setPreferredSize(new Dimension(frame.getWidth(), frame.getHeight()));
        image = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        timer = new Timer(100, this::redraw);
        timer.start();
    }

    private void redraw(ActionEvent ev) {
        int[] resources = world.getResourceCopy();
        Organism[] population = world.getPopulationCopy();
        int[] elevation = world.getElevationCopy();
        for (int i = 0; i < resources.length; i++) {
            pixels[i] = WorldInput.convertToColour(config, resources[i], population[i], elevation[i]);
            pixels[i] |= 255 << 24;
        }
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }
}
