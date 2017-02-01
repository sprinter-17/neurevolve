package neurevolve.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import neurevolve.network.ActivationFunction;
import neurevolve.organism.Environment;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;

/**
 * A <code>World</code> represents the two dimensional environment that a set of organisms exist
 * within. Each position in the world has:
 * <ul>
 * <li>Zero or one organisms</li>
 * <li>A fixed elevation, representing the amount of energy required to move to that position</li>
 * <li>A variable temperature, representing the amount of energy deducted for organisms at that
 * position. The temperature depends on the position, elevation and time</li>
 * <li>A variable value that represents the amount of resources available for organisms</li>
 * </ul>
 */
public class World implements Environment {

    private final Frame frame;
    private final WorldConfiguration config;
    private final int[] resources;
    private final int[] elevation;
    private final Organism[] population;
    private final Random random = new Random();

    private int time = 0;
    private int populationSize = 0;

    private final ActivationFunction function;

    private Organism largestOrganism;
    private int totalComplexity;

    /**
     * Construct a world within a frame with a configuration
     *
     * @param function the activation function to use for all organisms in the world
     * @param frame the frame that defines the size of the world
     * @param configuration the configuration of the world
     */
    public World(ActivationFunction function, Frame frame, WorldConfiguration configuration) {
        this.function = function;
        this.config = configuration;
        this.frame = frame;
        this.resources = new int[frame.size()];
        this.elevation = new int[frame.size()];
        this.population = new Organism[frame.size()];
    }

    public Frame getFrame() {
        return frame;
    }

    public WorldConfiguration getConfig() {
        return config;
    }

    public int[] getResourceCopy() {
        return Arrays.copyOf(resources, frame.size());
    }

    public Organism[] getPopulationCopy() {
        return Arrays.copyOf(population, frame.size());
    }

    /**
     * Get the total number of organisms in the world
     *
     * @return the organism count
     */
    public int getPopulationSize() {
        return populationSize;
    }

    public void setResource(int position, int amount) {
        resources[position] = amount;
    }

    /**
     * Get the amount of resource at a position
     *
     * @param position the position to check
     * @return the amount of resource at the given position
     */
    public int getResource(int position) {
        return resources[position];
    }

    public int getResource(int position, int direction) {
        return getResource(frame.move(position, direction));
    }

    /**
     * Get the difference in elevation between a position and an adjacent position
     *
     * @param position the starting position
     * @param dir the direction to the adjacent position
     * @return the difference in elevation
     */
    public int getSlope(int position, int direction) {
        return getElevation(frame.move(position, direction)) - getElevation(position);
    }

    /**
     * Set the elevation of a position
     *
     * @param position the position whose elevation will be set
     * @param value the elevation to set the position to
     */
    public void setElevation(int position, int value) {
        elevation[position] = value;
    }

    /**
     * Get the elevation at a given position
     *
     * @param position the position to get the elevation for
     * @return the elevation at the given position
     */
    protected int getElevation(int position) {
        return elevation[position];
    }

    /**
     * Check if there is an organism in a given position
     *
     * @param position the position to check
     * @return true if there is an organism in the position
     */
    public boolean hasOrganism(int position) {
        return population[position] != null;
    }

    /**
     * Add an organism to the world.
     *
     * @param position the position to place the organism
     * @param organism the organism to place
     * @throws IllegalArgumentException if the position already has an organism
     */
    public void addOrganism(int position, Organism organism) {
        if (population[position] != null)
            throw new IllegalArgumentException("Attempt to add two organisms to same position");
        population[position] = organism;
        populationSize++;
        organism.setPosition(position);
    }

    /**
     * Remove an organism (if any) from a position
     */
    private void removeOrganism(int position) {
        if (population[position] != null) {
            population[position] = null;
            populationSize--;
        }
    }

    /**
     * Seed the world with a number of organisms constructed from a recipe. The organisms will be
     * placed randomly within the frame.
     *
     * @param recipe the recipe to use to construct the organisms
     * @param count the number of organism to add
     */
    public void seed(Recipe recipe, int count) {
        while (populationSize < count) {
            int position = random.nextInt(frame.size());
            if (!hasOrganism(position)) {
                Organism organism = recipe.make(this, 1000);
                addOrganism(position, organism);
                organism.setDirection(random.nextInt(4));
            }
        }
    }

    /**
     * Advance the world 1 time unit. Add resources at all positions based on the temperature.
     * Process all organisms.
     */
    public void tick() {
        time++;
        growResources();
        processPopulation();
    }

    private void growResources() {
        int maxResources = config.getMaxResources();
        for (int i = 0; i < frame.size(); i++) {
            int temp = getTemperature(i);
            while (temp >= 100) {
                resources[i]++;
                temp -= 100;
            }
            if (time % (100 - temp) == 0)
                resources[i]++;
            if (resources[i] > maxResources)
                resources[i] = maxResources;
        }
    }

    private void processPopulation() {
        largestOrganism = null;
        totalComplexity = 0;
        Organism[] previous = Arrays.copyOf(population, frame.size());
        for (int i = 0; i < frame.size(); i++) {
            if (previous[i] != null)
                processPosition(i, previous[i]);
        }
    }

    private void processPosition(int position, Organism organism) {
        reduceEnergyByTemperature(position, organism);
        organism.activate();
        totalComplexity += organism.complexity();
        if (organism.isDead())
            removeOrganism(position);
        else if (largestOrganism == null || organism.complexity() > largestOrganism.complexity())
            largestOrganism = organism;
    }

    public Organism getLargestOrganism() {
        return largestOrganism;
    }

    public float getAverageComplexity() {
        return (float) totalComplexity / populationSize;
    }

    /**
     * Move an organism in a position (if any) in a direction. A move consumes energy from the
     * organism in proportion to the difference in elevation of the two positions. If the organism
     * does not have enough energy it doesn't move
     *
     * @param position the position of the organism
     * @param direction the direction to move the organism
     */
    public void moveOrganism(Organism organism) {
        int position = organism.getPosition();
        int direction = organism.getDirection();
        if (!hasOrganism(frame.move(position, direction))) {
            int slope = Math.max(0, getSlope(position, direction));
            if (organism.consume(slope)) {
                removeOrganism(position);
                addOrganism(frame.move(position, direction), organism);
            }
        }
    }

    /**
     * Feed the organism at the given position by a given amount. The amount is added to the
     * organism's energy and taken from the world's resources at that position.
     *
     * @param position the position of the organism to feed
     * @param amount the amount of resources to take from the world and add to the organism's energy
     */
    public void feedOrganism(Organism organism) {
        int position = organism.getPosition();
        int amount = Math.min(resources[position], config.getConsumptionRate());
        organism.increaseEnergy(amount);
        resources[position] -= amount;
    }

    /**
     * Split the organism at the given position. The child organism is placed at a random position
     * next to this organism. The split does consumes as much energy as the size of the recipe being
     * replicated. If it does not have enough energy then it does not split. It also does not split
     * if there are no free positions adjacent to the organism.
     *
     * @param position the position of the organism to split.
     */
    public void splitOrganism(Organism organism) {
        int position = organism.getPosition();
        if (organism.consume(organism.size()))
            openPositionNextTo(position).ifPresent(pos -> addChild(pos, organism));
    }

    private void addChild(int position, Organism parent) {
        Organism child = parent.divide();
        child.setDirection(random.nextInt(4));
        addOrganism(position, child);
    }

    private OptionalInt openPositionNextTo(int position) {
        List<Integer> directions = Arrays.asList(0, 1, 2, 3);
        Collections.shuffle(directions);
        return directions.stream()
                .mapToInt(dir -> frame.move(position, dir))
                .filter(pos -> !hasOrganism(pos))
                .findFirst();
    }

    private void reduceEnergyByTemperature(int position, Organism organism) {
        int temp = getTemperature(position);
        if (temp < 0)
            organism.reduceEnergy(-temp);
    }

    /**
     * Kill and remove the organism at a given position. The current energy of the organism becomes
     * resources at the organism's position.
     *
     * @param position the position of the organism
     */
    public void killOrganism(int position) {
        if (!hasOrganism(position))
            throw new IllegalArgumentException("Attempt to kill organism in empty position");
        Organism organism = population[position];
        resources[position] += organism.getEnergy();
        organism.reduceEnergy(organism.getEnergy());
        population[position] = null;
    }

    /**
     * Get the current world time.
     *
     * @return the number of ticks for the world.
     */
    public int getTime() {
        return time;
    }

    /**
     * Get the name of the current season as defined by the year length
     *
     * @return the name of the season
     */
    public String getSeasonName() {
        final String[] seasons = {"Spring", "Summer", "Autumn", "Winter"};
        return seasons[4 * (time % config.getYearLength()) / config.getYearLength()];
    }

    /**
     * Get the temperature of a position. Calculated based on latitude, elevation and season.
     *
     * @param position the position to retrieve the temperature for
     * @return the temperature
     */
    public int getTemperature(int position) {
        return getLatitudeTemp(position) - getElevation(position) + getSeasonTemp();
    }

    private int getLatitudeTemp(int position) {
        return frame.scaleByLatitude(position, config.getMinTemp(), config.getMaxTemp());
    }

    private int getSeasonTemp() {
        int timeOfYear = time % config.getYearLength();
        int timeFromMidYear = Math.abs(config.getYearLength() / 2 - timeOfYear);
        return config.getTempVariation() * 2 * timeFromMidYear / config.getYearLength();
    }

    /**
     * Apply the world's activation function
     *
     * @param input the input value to the function
     * @return the output value from the function
     */
    @Override
    public int applyActivationFunction(int input) {
        return function.apply(input);
    }

    /**
     * Get an input for an organism
     *
     * @param organism the organism to get input for
     * @param input the code for the input value, as defined by {@link WorldInput#decode}
     * @return the value for the code
     */
    @Override
    public int getInput(Organism organism, int input) {
        return WorldInput.getValue(input, this, organism);
    }

    /**
     * Perform an activity by an organism
     *
     * @param activity the code for the activity to perform, as defined by
     * {@link WorldActivity#decode}.
     * @param organism the organism to perform the activity
     */
    @Override
    public void performActivity(Organism organism, int activity) {
        WorldActivity.perform(activity, this, organism);
    }

    /**
     * Copy a set of instructions within a recipe. The copy is designed to be imperfect to simulate
     * transcription errors. Three types of errors can occur:
     * <ul>
     * <li>Copy errors, in which an instruction is copied with a random variation from its value
     * <li>Deletion errors, in which a set of 1-3 codes are skipped
     * <li>Duplication errors, in which a set of 1-3 codes is repeated
     * </ul>
     * The rate at which errors occur is set by {@link WorldConfiguration#setMutationRate(int)}
     *
     * @param instructions the instructions to copy
     * @param size the number of instructions
     * @return the copied instructions, with errors
     */
    @Override
    public Recipe copyInstructions(int[] instructions, int size) {
        Recipe recipe = new Recipe();
        for (int pos = 0; pos < size; pos += advance()) {
            if (pos >= 0)
                recipe.add(copy(instructions[pos]));
        }
        return recipe;
    }

    private int advance() {
        if (mutate())
            return random.nextInt(7) - 3;
        else
            return 1;
    }

    private int copy(int code) {
        if (mutate())
            code += random.nextInt(11) - 5;
        return code;
    }

    private boolean mutate() {
        return config.getMutationRate() > 0
                && random.nextInt(1000 / config.getMutationRate()) == 0;
    }

}
