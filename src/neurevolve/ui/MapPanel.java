package neurevolve.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Optional;
import javax.swing.JPanel;
import neurevolve.organism.Organism;
import neurevolve.organism.Species;
import neurevolve.world.Configuration;
import neurevolve.world.Ground;
import static neurevolve.world.GroundElement.ACID;
import static neurevolve.world.GroundElement.BODY;
import static neurevolve.world.GroundElement.ELEVATION;
import static neurevolve.world.GroundElement.RADIATION;
import static neurevolve.world.GroundElement.RESOURCES;
import static neurevolve.world.GroundElement.WALL;
import neurevolve.world.Population;
import neurevolve.world.Space;
import neurevolve.world.World;

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
    private final Configuration config;
    private final BufferedImage image;
    private final int[] pixels;

    private Optional<Species> selectedSpecies = Optional.empty();

    /**
     * Construct a <code>MapPanel</code>
     *
     * @param world the world to display in the panel
     * @param space the space the world occupies
     * @param config the configuration for the world
     */
    public MapPanel(World world, Space space, Configuration config) {
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

    public void selectSpecies(Species species) {
        this.selectedSpecies = Optional.ofNullable(species);
    }

    /**
     * Redraw the world. Copies the resources, population and elevation in order to ensure that the
     * display represents a snapshot of the world.
     */
    private void redraw() {
        Population population = world.getPopulationCopy();
        Ground ground = world.copyGround();
        ground.forEach((p, d) -> redraw(population, p, d));
        repaint();
    }

    private void redraw(Population population, int pos, int ground) {
        if (WALL.get(ground) == 1) {
            pixels[pos] = Color.DARK_GRAY.getRGB();
        } else if (population.hasOrganism(pos)) {
            Organism organism = population.getOrganism(pos);
            if (selectedSpecies.isPresent() && selectedSpecies.get().matches(organism))
                pixels[pos] = Color.WHITE.getRGB();
            else
                pixels[pos] = populationColour(config, population.getOrganism(pos)) | 255 << 24;
        } else if (BODY.get(ground) == 1) {
            pixels[pos] = bodyColour(config);
        } else {
            pixels[pos] = convertToColour(config, ground);
            pixels[pos] |= 255 << 24;
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(image, 0, 0, this);
    }

    public static int wallColour() {
        return Color.DARK_GRAY.getRGB();
    }

    /**
     * Convert resources, organism and elevation for a position to a colour to display
     *
     * @param config the configuration for the world
     * @param ground the packed data containing the ground elements
     * @return a colour, in RGB format, representing the status of the position
     */
    public static int convertToColour(Configuration config, int ground) {
        if (WALL.get(ground) > 0)
            return wallColour();
        else
            return resourceColour(config, RESOURCES.get(ground))
                    | elevationColour(config, ELEVATION.get(ground))
                    | acidColour(config, ACID.get(ground) == 1)
                    | radiationColour(config, RADIATION.get(ground));
    }

    /**
     * Elevation shows in blue with intensity determined by height.
     *
     * @param config the configuration for the world
     * @param elevation the height of the position
     * @return a colour, in RGB format, representing the height of the position
     */
    private static int elevationColour(Configuration config, int elevation) {
        return elevation << BLUE_SHIFT;
    }

    /**
     * Resources show as green with intensity determined by number of resources.
     *
     * @param config the configuration of the world
     * @param resource the number of resources
     * @return a colour, in RGB format, representing the number of resources
     */
    private static int resourceColour(Configuration config, int resource) {
        return resource << GREEN_SHIFT;
    }

    private static int acidColour(Configuration config, boolean acid) {
        return acid ? 172 << GREEN_SHIFT | 172 << RED_SHIFT : 0;
    }

    private static int radiationColour(Configuration config, int radiation) {
        return radiation * 64 << RED_SHIFT;
    }

    /**
     * An organism shows as a red pixel.
     *
     * @param config the configuration of the world
     * @param organism an organism, or null if none
     * @return a colour, in RGB format, representing the organism, if any
     */
    public static int populationColour(Configuration config, Organism organism) {
        return organism == null ? 0 : organism.getColour();
    }

    public static int bodyColour(Configuration config) {
        return Color.LIGHT_GRAY.getRGB();
    }
}
