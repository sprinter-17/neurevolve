package neurevolve.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import neurevolve.network.ActivationFunction;
import neurevolve.organism.Environment;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import static neurevolve.world.Angle.FORWARD;
import static neurevolve.world.Space.EAST;
import static neurevolve.world.Space.NORTH;
import static neurevolve.world.Space.SOUTH;
import static neurevolve.world.Space.WEST;

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

    private final Space space;
    private final WorldConfiguration config;
    private final int[] resources;
    private final int[] elevation;
    private final Random random = new Random();

    private final List<Runnable> tickListeners = new ArrayList<>();

    private final Time time;
    private final Population population;

    private final ActivationFunction function;

    private Organism mostComplexOrganism;
    private float totalComplexity;
    private int tickDelay;

    /**
     * Construct a world within a frame with a configuration
     *
     * @param function the activation function to use for all organisms in the world
     * @param frame the frame that defines the size of the world
     * @param configuration the configuration of the world
     */
    public World(ActivationFunction function, Space frame, WorldConfiguration configuration) {
        this.function = function;
        this.config = configuration;
        this.space = frame;
        this.time = new Time(configuration);
        this.population = new Population(space);
        this.resources = new int[frame.size()];
        this.elevation = new int[frame.size()];
        this.tickDelay = 1;
    }

    /**
     * Make a copy of the resources
     *
     * @return a complete copy of the resource array
     */
    public int[] getResourceCopy() {
        return Arrays.copyOf(resources, space.size());
    }

    /**
     * Make a copy of the population
     *
     * @return a complete copy of the population array
     */
    public Population getPopulationCopy() {
        return population.copy();
    }

    public int getOrganismDirection(Organism organism) {
        return population.getDirection(organism);
    }

    /**
     * Make a copy of the elevations
     *
     * @return a complete copy of the elevation array
     */
    public int[] getElevationCopy() {
        return Arrays.copyOf(elevation, space.size());
    }

    /**
     * Get the total number of organisms in the world
     *
     * @return the organism count
     */
    public int getPopulationSize() {
        return population.size();
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
        space.forAllPositionsInCircle(centre, radius,
                (p, d) -> setElevation(p, getElevation(p) + cliff + (radius - d) * slope));
    }

    public void addHills(int hillCount, int radius, int elevation) {
        for (int i = 0; i < hillCount; i++) {
            int position = random.nextInt(space.size());
            addHill(position, random.nextInt(radius) + 1, random.nextInt(elevation / radius) + 1, random.nextInt(10));
        }
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
        elevation[position] = Math.min(255, value);
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
        return population.getPosition(organism, angles);
    }

    /**
     * Check if there is an organism in a given position
     *
     * @param position the position to check
     * @return true if there is an organism in the position
     */
    public boolean hasOrganism(int position) {
        return population.hasOrganism(position);
    }

    /**
     * Get the energy of the organism at a position.
     *
     * @param position the position of the organism
     * @return the organism's energy, or 0 if there is no organism in the given position
     */
    public int getOrganismEnergy(int position) {
        return hasOrganism(position) ? population.getOrganism(position).getEnergy() : 0;
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
        population.addOrganism(organism, position, direction);
    }

    /**
     * Remove an organism from the world
     */
    private void removeOrganism(Organism organism) {
        population.removeOrganism(organism);
    }

    /**
     * Seed the world with organisms constructed from a recipe until the population size reaches a
     * given level. The organisms will be placed randomly within the frame.
     *
     * @param recipe the recipe to use to construct the organisms
     * @param energy the starting energy for the new organisms
     * @param count the number of organism to add
     * @throws IllegalArgumentException if <tt>count &gt; space.size()</tt>
     */
    public void seed(Recipe recipe, int energy, int count) {
        if (count > space.size())
            throw new IllegalArgumentException("Attempt to seed the world with too many organisms");
        IntStream.range(0, space.size())
                .filter(this::hasOrganism)
                .mapToObj(population::getOrganism)
                .forEach(this::removeOrganism);
        while (population.size() < count) {
            int position = random.nextInt(space.size());
            if (!population.hasOrganism(position)) {
                population.addOrganism(recipe.make(this, energy), position, random.nextInt(4));
            }
        }
    }

    public void addTickListener(Runnable listner) {
        tickListeners.add(listner);
    }

    public void removeTickListener(Runnable listener) {
        tickListeners.remove(listener);
    }

    /**
     * Advance the world 1 time unit. Add resources at all positions based on the temperature.
     * Process all organisms.
     */
    public void tick() {
        time.tick();
        growResources();
        processPopulation();
        tickListeners.stream().collect(Collectors.toList()).forEach(Runnable::run);
    }

    /**
     * Grow all resources in the world according to their temperature. The resources are increased
     * by temp / 100 and a further one each temp % 100 ticks.
     */
    private void growResources() {
        for (int i = 0; i < space.size(); i++) {
            int temp = getTemperature(i);
            while (temp >= 100) {
                addResources(i, 1);
                temp -= 100;
            }
            if (time.getTime() % (100 - temp) == 0)
                addResources(i, 1);
        }
    }

    private void addResources(int position, int amount) {
        resources[position] = Math.min(config.getMaxResources(), resources[position] + amount);
    }

    public Organism getMostComplexOrganism() {
        return mostComplexOrganism;
    }

    public float getAverageComplexity() {
        if (population.size() == 0)
            return 0f;
        else
            return totalComplexity / population.size();
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
     * Move an organism forward, if it has sufficient energy to climb the slope and there is not
     * organism in front.
     *
     * @param organism the organism to move
     */
    public void moveOrganism(Organism organism) {
        int slope = Math.max(0, getSlope(organism, FORWARD));
        population.moveOrganism(organism, slope);
    }

    /**
     * Turn an organism's direction through an angle
     *
     * @param organism the organism to turn
     * @param angle the angle to turn the organism by
     */
    public void turnOrganism(Organism organism, Angle angle) {
        population.turn(organism, angle);
    }

    /**
     * Split and organism. The child organism is placed at a random position next to this organism
     * facing in the same direction. The split consumes as much energy as the size of the recipe
     * being replicated. If it does not have enough energy then it does not split. It also does not
     * split if there are no free positions adjacent to the organism.
     *
     * @param organism the organism to split
     */
    public void splitOrganism(Organism organism) {
        if (organism.canDivide())
            openPositionNextTo(getPosition(organism))
                    .ifPresent(pos -> addOrganism(organism.divide(), pos, population.getDirection(organism)));
    }

    /**
     * Get an adjacent position that does not have an organism, or OptionalInt.empty() if all
     * adjacent positions have an organism.
     */
    private OptionalInt openPositionNextTo(int position) {
        List<Integer> directions = Arrays.asList(EAST, WEST, NORTH, SOUTH);
        Collections.shuffle(directions);
        return directions.stream()
                .mapToInt(dir -> space.move(position, dir))
                .filter(pos -> !hasOrganism(pos))
                .findFirst();
    }

    /**
     * Process the population. This uses a copy of the population array so that changes that occur
     * during processing do not interfere with the current state.
     */
    public void processPopulation() {
        mostComplexOrganism = null;
        totalComplexity = 0;
        Population copy = population.copy();
        IntStream.range(0, space.size())
                .filter(copy::hasOrganism)
                .forEach(i -> processPosition(i, copy.getOrganism(i)));
    }

    /**
     * Process the organism at a given position. Reduce its energy according to temperature
     */
    private void processPosition(int position, Organism organism) {
        reduceEnergyByTemperature(position, organism);
        organism.reduceEnergy(organism.size() * config.getSizeRate() / 100);
        organism.reduceEnergy(organism.getAge() * config.getAgingRate() / 1000);
        population.resetActivityCount(organism);
        organism.activate();
        totalComplexity += organism.complexity();
        if (organism.isDead())
            removeOrganism(organism);
        else if (mostComplexOrganism == null || organism.complexity() > mostComplexOrganism.complexity())
            mostComplexOrganism = organism;
    }

    /**
     * Reduce the energy of an organism by the temperature at its position only if it is negative
     */
    private void reduceEnergyByTemperature(int position, Organism organism) {
        int temp = getTemperature(position);
        if (temp < 0)
            organism.reduceEnergy(-temp);
    }

    /**
     * Attack an organism at a given angle. This activity costs 20 energy. The organism with less
     * current energy dies. The energy of that organism becomes resources at their current position.
     *
     * @param attacker the organism that is the source of the attack
     * @param angle the angle to the target organism
     */
    public void attackOrganism(Organism attacker, Angle angle) {
        int position = getPosition(attacker, angle);
        if (hasOrganism(position)) {
            Organism target = population.getOrganism(position);
            if (attacker.getEnergy() >= target.getEnergy()) {
                addResources(getPosition(target), target.getEnergy() / 2);
                attacker.increaseEnergy(target.getEnergy() / 2);
                target.reduceEnergy(target.getEnergy());
            }
        }
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
     * Get the temperature of a position. Calculated based on latitude, elevation and season.
     *
     * @param position the position to retrieve the temperature for
     * @return the temperature
     */
    public int getTemperature(int position) {
        return getLatitudeTemp(position) - getElevation(position) + time.getSeasonalTemp();
    }

    private int getLatitudeTemp(int position) {
        return space.scaleByLatitude(position, config.getMinTemp(), config.getMaxTemp());
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
     * @param organism the organism to perform the activity
     * @param code the code for the activity to perform, as defined by {@link WorldActivity#decode}.
     */
    @Override
    public void performActivity(Organism organism, int code) {
        WorldActivity activity = WorldActivity.decode(code);
        int cost = config.getActivityCost(activity) * (population.getActivityCount(organism, activity) + 1);
        if (organism.consume(cost)) {
            activity.perform(this, organism);
            population.incrementActivityCount(organism, activity);
        }
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

    @Override
    public String describeInput(int input) {
        return WorldInput.describe(input);
    }

    @Override
    public String describeActivity(int activity) {
        return WorldActivity.describe(activity);
    }

}
