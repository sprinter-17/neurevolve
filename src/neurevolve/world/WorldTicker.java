package neurevolve.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import static neurevolve.world.Configuration.Value.ACID_TOXICITY;
import static neurevolve.world.Configuration.Value.MAX_ENERGY;
import static neurevolve.world.GroundElement.ACID;
import static neurevolve.world.GroundElement.BODY;
import static neurevolve.world.GroundElement.RESOURCES;

public class WorldTicker {

    private final World world;
    private final Configuration config;
    private final Time time;
    private final List<Runnable> tickListeners = new ArrayList<>();
    private final Random random = new Random();
    private WorldStatistics stats;

    public WorldTicker(World world, Configuration config) {
        this.world = world;
        this.config = config;
        this.time = new Time(config);
    }

    /**
     * Add a {@code Runnable} to call after each tick.
     *
     * @param listner the {@code Runnable} to call
     */
    public void addTickListener(Runnable listner) {
        tickListeners.add(listner);
    }

    /**
     * Remove a previously added {@code Runnable}
     *
     * @param listener the {@code Runnable} to remove
     */
    public void removeTickListener(Runnable listener) {
        tickListeners.remove(listener);
    }

    public WorldStatistics getStats() {
        return stats;
    }

    /**
     * Get the current world time.
     *
     * @return the number of ticks for the world.
     */
    public int getTime() {
        return time.getTime();
    }

    /**
     * Get the name of the current season as defined by the year length
     *
     * @return the name of the season
     */
    public String getSeasonName() {
        return time.getSeasonName();
    }

    /**
     * Advance the world 1 time unit. Add resources at all positions based on the temperature.
     * Process all organisms.
     */
    public void tick() {
        time.tick();
        seedOrganisms();
        growResources();
        halfLives();
        processPopulation();
        tickListeners.stream().collect(Collectors.toList()).forEach(Runnable::run);
    }

    private void seedOrganisms() {
        if (world.getPopulationSize() < config.getValue(Configuration.Value.SEED_COUNT))
            addSeedOrganism(createSeedRecipe());
    }

    private Recipe createSeedRecipe() {
        return config.getSeedRecipe()
                .replicate(this::replicateWithRandomColour);
    }

    private Recipe replicateWithRandomColour(byte[] instructions, int size, int colour) {
        Recipe copy = new Recipe(random.nextInt(1 << 24));
        IntStream.range(0, size)
                .forEach(i -> copy.add(instructions[i]));
        return copy;
    }

    private void addSeedOrganism(Recipe recipe) {
        int position = random.nextInt(world.getSpaceSize());
        if (!world.hasOrganism(position) && world.isEmpty(position)) {
            world.addOrganism(new Organism(world, config.getValue(Configuration.Value.INITIAL_ENERGY), recipe),
                    position, random.nextInt(4));
        }
    }

    /**
     * Grow all resources in the world according to their temperature. The resources are increased
     * by temp / 100 and a further one each temp % 100 ticks.
     */
    private void growResources() {
        world.allPositions().forEach(this::growResourcesAtPosition);
    }

    private void growResourcesAtPosition(int position) {
        int resources = getResourcesForTemperature(getTemperature(position));
        world.addElementValue(position, RESOURCES, resources);
    }

    public int getTemperature(int position) {
        return world.getTemperature(position) + time.getSeasonalTemp();
    }

    private int getResourcesForTemperature(int temp) {
        int resources = 0;
        int growthPeriod = 100;
        while (temp >= growthPeriod) {
            resources++;
            temp -= growthPeriod;
        }
        if (getTime() % (growthPeriod - temp) == 0) {
            resources++;
        }
        return resources;
    }

    private void halfLives() {
        Arrays.stream(GroundElement.values())
                .forEach(this::halfLife);
    }

    private void halfLife(GroundElement element) {
        int halfLife = config.getHalfLife(element);
        if (halfLife > 0 && halfLife < 1000) {
            world.allPositions()
                    .filter(i -> random.nextInt(halfLife) == 0)
                    .forEach(p -> world.decrementElementValue(p, element));
        }
    }

    /**
     * Process the population. This uses a copy of the population array so that changes that occur
     * during processing do not interfere with the current state.
     */
    public void processPopulation() {
        stats = new WorldStatistics(time);
        Population copy = world.getPopulationCopy();
        world.allPositions()
                .filter(copy::hasOrganism)
                .forEach(i -> processPosition(i, copy.getOrganism(i)));
    }

    /**
     * Process the organism at a given position. Reduce its energy according to temperature
     */
    private void processPosition(int position, Organism organism) {
        adjustEnergy(position, organism);
        world.resetActivityCount(organism);
        organism.activate();
        if (organism.isDead()) {
            world.removeOrganism(organism);
            world.addElementValue(position, BODY, 1);
        } else {
            stats.add(organism);
        }
    }

    private void adjustEnergy(int position, Organism organism) {
        if (getTemperature(position) < 0)
            organism.reduceEnergy(-getTemperature(position));
        if (organism.getEnergy() > config.getValue(MAX_ENERGY))
            organism.reduceEnergy(organism.getEnergy() - config.getValue(MAX_ENERGY));
        organism.reduceEnergy(config.getValue(ACID_TOXICITY) * world.getElementValue(position, ACID));
        organism.reduceEnergy(config.getValue(Configuration.Value.BASE_COST));
        organism.reduceEnergy(organism.size() * config.getValue(Configuration.Value.SIZE_RATE) / 10);
        organism.reduceEnergy(organism.getAge() * config.getValue(Configuration.Value.AGING_RATE) / 100);
    }
}
