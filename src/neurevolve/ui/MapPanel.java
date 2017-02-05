package neurevolve.ui;

import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JPanel;
import neurevolve.organism.Organism;
import neurevolve.world.Population;
import neurevolve.world.Space;
import neurevolve.world.World;
import neurevolve.world.WorldConfiguration;

/**
 * A <code>MapPanel</code> displays a with one pixel per position in the world's space. The colour
 * of the pixel indicates the state of the world:
 * <ul>
 * <li>green: resources</li>
 * <li>blue: elevation</li>
 * <li>red: organism</li>
 * </ul>
 */
public class MapPanel extends JPanel {

    private static final int BLUE_SHIFT = 0;
    private static final int GREEN_SHIFT = 8;
    private static final int RED_SHIFT = 16;

    private final World world;
    private final WorldConfiguration config;
    private final BufferedImage image;
    private final int[] pixels;

    /**
     * Construct a <code>MapPanel</code>
     *
     * @param world the world to display in the panel
     * @param space the space the world occupies
     * @param config the configuration for the world
     */
    public MapPanel(World world, Space space, WorldConfiguration config) {
        this.world = world;
        this.config = config;
        image = new BufferedImage(space.getWidth(), space.getHeight(), BufferedImage.TYPE_INT_ARGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        world.addTickListener(this::redraw);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ZoomWindow zoom = new ZoomWindow(world, space, config, e.getX(), e.getY());
                zoom.show();
            }
        });
    }

    /**
     * Redraw the world. Copies the resources, population and elevation in order to ensure that the
     * display represents a snapshot of the world.
     */
    private void redraw() {
        int[] resources = world.getResourceCopy();
        Population population = world.getPopulationCopy();
        int[] elevation = world.getElevationCopy();
        for (int i = 0; i < resources.length; i++) {
            pixels[i] = convertToColour(config, resources[i], population.getOrganism(i), elevation[i]);
            pixels[i] |= 255 << 24;
        }
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(image, 0, 0, this);
    }

    /**
     * Convert resources, organism and elevation for a position to a colour to display
     */
    public static int convertToColour(WorldConfiguration config, int resources, Organism organism, int elevation) {
        return resourceColour(config, resources)
                | populationColour(config, organism)
                | elevationColour(config, elevation);
    }

    /**
     * Elevation shows in blue with intensity determined by height.
     */
    public static int elevationColour(WorldConfiguration config, int elevation) {
        return elevation << BLUE_SHIFT;
    }

    /**
     * Resources show as green with intensity determined by number of resources.
     */
    public static int resourceColour(WorldConfiguration config, int resource) {
        return (resource * 255 / config.getMaxResources()) << GREEN_SHIFT;
    }

    /**
     * An organism shows as a red pixel.
     */
    public static int populationColour(WorldConfiguration config, Organism organism) {
        return organism == null ? 0 : 200 << RED_SHIFT;
    }

}
