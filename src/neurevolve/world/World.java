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
import static neurevolve.world.Angle.FORWARD;

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

    public static final int POSITION_CODE = 0;
    public static final int DIRECTION_CODE = 1;

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

    /**
     * Make a copy of the resources
     *
     * @return a complete copy of the resource array
     */
    public int[] getResourceCopy() {
        return Arrays.copyOf(resources, frame.size());
    }

    /**
     * Make a copy of the population
     *
     * @return a complete copy of the population array
     */
    public Organism[] getPopulationCopy() {
        return Arrays.copyOf(population, frame.size());
    }

    /**
     * Make a copy of the elevations
     *
     * @return a complete copy of the elevation array
     */
    public int[] getElevationCopy() {
        return Arrays.copyOf(elevation, frame.size());
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
     * Set the amount of resource at a position. This method is primarily for testing purposes
     *
     * @param position the position
     * @param amount the amount of resource to set
     */
    protected void setResource(int position, int amount) {
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

    /**
     * Add a hill at a given position, radius and slope. Changes the elevations in an area of the
     * frame. The position is set by the centre and radius and the elevation is set by the slope,
     * with the highest elevation at the centre. The entire area is additionally raised by the cliff
     * amount.
     *
     * @param centre the position of highest elevation
     * @param radius the distance of the bottom of the hill from the centre
     * @param slope the gradient of the hill
     * @param cliff the height of the edge of the hill
     */
    public void addHill(int centre, int radius, int slope, int cliff) {
        frame.forAllPositionsInCircle(centre, radius, (p, d) -> setElevation(p, cliff + (radius - d) * slope));
    }

    /**
     * Get the difference in elevation between an organism's position and an adjacent position
     *
     * @param organism the organism that the slope is relative to
     * @param angle the angle to the position for the slope
     * @return the difference in elevation between the adjacent position and the organism's position
     */
    public int getSlope(Organism organism, Angle angle) {
        return getElevation(getPosition(organism, angle)) - getElevation(getPosition(organism));
    }

    /**
     * Set the elevation of a position. This method is primarily for testing purposes.
     *
     * @param position the position whose elevation will be set
     * @param value the elevation to set the position to
     */
    protected void setElevation(int position, int value) {
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
     * Calculate a position relative to an organism. Given one or more angles, calculates a position
     * by moving from an organism's position according to the angles.
     *
     * @param organism the organism whose position is used
     * @param angles the angles to follow from the organism's position
     * @return the resulting position
     */
    public int getPosition(Organism organism, Angle... angles) {
        int position = organism.getWorldValue(POSITION_CODE);
        for (Angle angle : angles) {
            int direction = angle.add(organism.getWorldValue(DIRECTION_CODE));
            position = frame.move(position, direction);
        }
        return position;
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
     * Get the energy of the organism at a position.
     *
     * @param position the position of the organism
     * @return the organism's energy, or 0 if there is no organism in the given position
     */
    public int getOrganismEnergy(int position) {
        return hasOrganism(position) ? population[position].getEnergy() : 0;
    }

    /**
     * Add an organism to the world.
     *
     * @param organism the organism to place
     * @param position the position to place the organism
     * @param direction the direction the organism will face
     * @throws IllegalArgumentException if the position already has an organism
     */
    public void addOrganism(Organism organism, int position, int direction) {
        if (population[position] != null)
            throw new IllegalArgumentException("Attempt to add two organisms to same position");
        population[position] = organism;
        populationSize++;
        setPosition(organism, position);
        setDirection(organism, direction);
    }

    /**
     * Remove an organism from the world
     */
    private void removeOrganism(Organism organism) {
        int position = getPosition(organism);
        assert population[position] != null;
        population[position] = null;
        populationSize--;
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
                addOrganism(organism, position, random.nextInt(4));
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

    /**
     * Grow all resources in the world according to their temperature. The resources are increased
     * by temp / 100 and a further one each temp % 100 ticks.
     */
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

    /**
     * Process the population. This uses a copy of the population array so that changes that occur
     * during processing do not interfere with the current state.
     */
    private void processPopulation() {
        largestOrganism = null;
        totalComplexity = 0;
        Organism[] populationCode = getPopulationCopy();
        for (int i = 0; i < frame.size(); i++) {
            if (populationCode[i] != null)
                processPosition(i, populationCode[i]);
        }
    }

    /**
     * Process the organism at a given position. Reduce its energy according to temperature
     */
    private void processPosition(int position, Organism organism) {
        reduceEnergyByTemperature(position, organism);
        organism.activate();
        totalComplexity += organism.complexity();
        if (organism.isDead())
            removeOrganism(organism);
        else if (largestOrganism == null || organism.complexity() > largestOrganism.complexity())
            largestOrganism = organism;
    }

    public Organism getLargestOrganism() {
        return largestOrganism;
    }

    public float getAverageComplexity() {
        return (float) totalComplexity / populationSize;
    }

    protected int getPosition(Organism organism) {
        return organism.getWorldValue(POSITION_CODE);
    }

    protected void setPosition(Organism organism, int position) {
        organism.setWorldValue(POSITION_CODE, position);
    }

    protected int getDirection(Organism organism) {
        return organism.getWorldValue(DIRECTION_CODE);
    }

    private void setDirection(Organism organism, int direction) {
        organism.setWorldValue(DIRECTION_CODE, direction);
    }

    protected void turn(Organism organism, Angle angle) {
        setDirection(organism, angle.add(getDirection(organism)));
    }

    /**
     * Move an organism in the direction it is facing. A move consumes energy from the organism in
     * proportion to the difference in elevation of the two positions. If the organism does not have
     * enough energy it doesn't move
     *
     * @param organism the organism to move
     */
    public void moveOrganism(Organism organism) {
        if (!hasOrganism(getPosition(organism, FORWARD))) {
            int slope = Math.max(0, getSlope(organism, FORWARD));
            if (organism.consume(slope)) {
                removeOrganism(organism);
                addOrganism(organism, getPosition(organism, FORWARD), getDirection(organism));
            }
        }
    }

    /**
     * Feed an organism by consuming resources at a position relative to the organism. The amount is
     * added to the organism's energy and taken from the world's resources at that position.
     *
     * @param organism the organism
     * @param angles zero or more angles defining the path to the position from which to consume
     * resources
     */
    public void feedOrganism(Organism organism, Angle... angles) {
        int position = getPosition(organism, angles);
        int amount = Math.min(resources[position], config.getConsumptionRate());
        organism.increaseEnergy(amount);
        resources[position] -= amount;
    }

    /**
     * Split the organism at the given position. The child organism is placed at a random position
     * next to this organism facing in the same direction. The split does consumes as much energy as
     * the size of the recipe being replicated. If it does not have enough energy then it does not
     * split. It also does not split if there are no free positions adjacent to the organism.
     *
     * @param position the position of the organism to split.
     */
    public void splitOrganism(Organism organism) {
        int position = getPosition(organism);
        if (organism.consume(organism.size() * 2))
            openPositionNextTo(position).ifPresent(pos -> addChild(pos, organism));
    }

    private void addChild(int position, Organism parent) {
        Organism child = parent.divide();
        setDirection(child, getDirection(parent));
        addOrganism(child, position, getDirection(parent));
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
    public void killOrganism(Organism killer, Angle angle) {
        int position = getPosition(killer, angle);
        if (hasOrganism(position) && killer.consume(40)) {
            Organism target = population[position];
            resources[position] += target.getEnergy();
            target.reduceEnergy(target.getEnergy());
            removeOrganism(target);
        }
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
