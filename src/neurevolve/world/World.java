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

    private int currentPosition;
    private Organism currentOrganism;

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

    /**
     * Get the position of coordinates within the frame.
     *
     * @param x the horizontal distance from the left edge of the frame
     * @param y the vertical distance from the bottom edge of the frame
     * @return the position
     */
    public int position(int x, int y) {
        return frame.position(x, y);
    }

    /**
     * Calculate a new position in a given direction.
     *
     * @param position the starting position
     * @param direction the direction to move
     * @return the position in the given direction from the starting position.
     */
    public int move(int position, Frame.Direction direction) {
        return frame.move(position, direction);
    }

    /**
     * Get the total number of organisms in the world
     *
     * @return the organism count
     */
    public int getPopulationSize() {
        return populationSize;
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

    /**
     * Get the difference in elevation between a position and an adjacent position
     *
     * @param position the starting position
     * @param dir the direction to the adjacent position
     * @return the difference in elevation
     */
    public int getSlope(int position, Frame.Direction dir) {
        return getElevation(frame.move(position, dir)) - getElevation(position);
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
     * Get the organism in a given position.
     */
    private Organism getOrganism(int position) {
        return population[position];
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
        for (int i = 0; i < Math.min(count, frame.size()); i++) {
            int position = random.ints(0, frame.size())
                    .filter(p -> !hasOrganism(p))
                    .findAny().orElseThrow(IllegalStateException::new);
            addOrganism(position, recipe.make(this, 1000));
        }
    }

    public void tick() {
        growResources();
        processPopulation();
        time++;
    }

    private void growResources() {
        for (int i = 0; i < frame.size(); i++) {
            int temp = getTemperature(i);
            if (temp > 0)
                resources[i] += temp;
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
        currentPosition = position;
        currentOrganism = organism;

        reduceEnergyByTemperature(currentPosition, organism);
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

    protected void setCurrentPosition(int position) {
        this.currentPosition = position;
    }

    /**
     * Move an organism in a position (if any) in a direction. A move consumes energy from the
     * organism in proportion to the difference in elevation of the two positions. If the organism
     * does not have enough energy it doesn't move
     *
     * @param position the position of the organism
     * @param direction the direction to move the organism
     */
    public void moveOrganism(int position, Frame.Direction direction) {
        if (hasOrganism(position) && !hasOrganism(frame.move(position, direction))) {
            Organism organism = population[position];
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
    public void feedOrganism(int position) {
        if (hasOrganism(position)) {
            int amount = Math.min(resources[position], config.getConsumptionRate());
            getOrganism(position).increaseEnergy(amount);
            resources[position] -= amount;
        }
    }

    /**
     * Split the organism at the given position. The child organism is placed at a random position
     * next to this organism. The split does consumes as much energy as the size of the recipe being
     * replicated. If it does not have enough energy then it does not split. It also does not split
     * if there are no free positions adjacent to the organism.
     *
     * @param position the position of the organism to split.
     */
    public void splitOrganism(int position) {
        if (hasOrganism(position)) {
            Organism organism = getOrganism(position);
            if (organism.consume(organism.size()))
                openPositionNextTo(position)
                        .ifPresent(pos -> addOrganism(pos, organism.divide()));
        }
    }

    private OptionalInt openPositionNextTo(int position) {
        List<Frame.Direction> directions = Arrays.asList(Frame.Direction.values());
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

    public int getTime() {
        return time;
    }

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
     * Get the input for a given code
     *
     * @param input the code for the input value, as defined by {@link WorldInput#decode}
     * @return the value for the code
     */
    @Override
    public int getInput(int input) {
        return WorldInput.getValue(input, this, currentPosition, currentOrganism);
    }

    /**
     * Perform the activity for a given code
     *
     * @param activity the code for the activity to perform, as defined by
     * {@link WorldActivity#decode}.
     */
    @Override
    public void performActivity(int activity) {
        WorldActivity.perform(activity, this, currentPosition, currentOrganism);
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
