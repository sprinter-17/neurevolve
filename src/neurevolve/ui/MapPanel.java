package neurevolve.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JPanel;
import javax.swing.Timer;
import neurevolve.organism.Organism;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;

public class MapPanel extends JPanel {

    private final World world;
    private final WorldConfiguration config;
    private final Timer timer;
    private final BufferedImage image;
    private final int[] pixels;

    public MapPanel(World world, Space frame, WorldConfiguration config) {
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
            pixels[i] = convertToColour(config, resources[i], population[i], elevation[i]);
            pixels[i] |= 255 << 24;
        }
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, this);
    }

    private int convertToColour(WorldConfiguration config, int resources, Organism organism, int elevation) {
        return resourceColour(config, resources)
                | populationColour(config, organism)
                | elevationColour(config, elevation);
    }

    private int resourceColour(WorldConfiguration config, int resource) {
        return (resource * 255 / config.getMaxResources()) << 8;
    }

    private int populationColour(WorldConfiguration config, Organism organism) {
        return organism == null ? 0 : 200 << 16;
    }

    private int elevationColour(WorldConfiguration config, int elevation) {
        return elevation * 255 / config.getMaxElevation();
    }

}
