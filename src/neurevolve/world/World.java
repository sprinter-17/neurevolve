package neurevolve.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import neurevolve.network.ActivationFunction;
import neurevolve.organism.Environment;
import neurevolve.organism.Instruction;
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
    private final int[] positionData;
    private final Random random = new Random();

    private final List<Runnable> tickListeners = new ArrayList<>();

    private final Time time;
    private final Population population;

    private final ActivationFunction function;

    private Organism mostComplexOrganism;
    private float totalComplexity;

    protected static enum Data {
        ACID(1),
        WALL(ACID, 1),
        RADIATION(WALL, 2),
        ELEVATION(RADIATION, 8),
        RESOURCES(ELEVATION, 8);

        private final int shift;
        private final int bits;
        private final int max;
        private final int mask;

        private Data(Data previous, int bits) {
            this(previous.shift + previous.bits, bits);
        }

        private Data(int bits) {
            this(0, bits);
        }

        private Data(int shift, int bits) {
            this.shift = shift;
            this.bits = bits;
            this.max = (1 << bits) - 1;
            this.mask = max << shift;
        }

        public int get(int input) {
            return (input & mask) >> shift;
        }

        public int set(int input, int data) {
            if (data > max || data < 0)
                throw new IllegalArgumentException("Data out of range");
            return (input & ~mask) | (data << shift) & mask;
        }

    }

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
        this.positionData = new int[frame.size()];
    }

    /**
     * Make a copy of the resources
     *
     * @return a complete copy of the resource array
     */
    public int[] getResourceCopy() {
        int[] resources = new int[space.size()];
        IntStream.range(0, space.size())
                .forEach(i -> resources[i] = Data.RESOURCES.get(positionData[i]));
        return resources;
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
        int[] elevations = new int[space.size()];
        IntStream.range(0, space.size())
                .forEach(i -> elevations[i] = getData(i, Data.ELEVATION));
        return elevations;
    }

    public boolean[] getAcidCopy() {
        boolean[] acid = new boolean[space.size()];
        IntStream.range(0, space.size())
                .forEach(i -> acid[i] = getData(i, Data.ACID) == 1);
        return acid;
    }

    public int[] getRadiationCopy() {
        int[] radiation = new int[space.size()];
        forEachPosition(i -> radiation[i] = getData(i, Data.RADIATION));
        return radiation;
    }

    private void forEachPosition(IntConsumer action) {
        IntStream.range(0, space.size()).forEach(action);
    }

    /**
     * Get the total number of organisms in the world
     *
     * @return the organism count
     */
    public int getPopulationSize() {
        return population.size();
    }

    public void addResourcesEverywhere(int amount) {
        IntStream.range(0, space.size())
                .forEach(pos -> addResources(pos, amount));
    }

    /**
     * Set the amount of resource at a position. This method is primarily for testing purposes
     *
     * @param position the position
     * @param amount the amount of resource to set
     */
    protected void setResource(int position, int amount) {
        setData(position, Data.RESOURCES, amount);
    }

    /**
     * Get the amount of resource at a position
     *
     * @param position the position to check
     * @return the amount of resource at the given position
     */
    public int getResource(int position) {
        return getData(position, Data.RESOURCES);
    }

    public boolean isAcidic(int position) {
        return getData(position, Data.ACID) == 1;
    }

    public void setAcidic(int position, boolean acidic) {
        setData(position, Data.ACID, acidic);
    }

    public int getRadiation(int position) {
        return getData(position, Data.RADIATION);
    }

    public void addRadition(int position, int radiation) {
        radiation += getRadiation(position);
        setData(position, Data.RADIATION, Math.min(radiation, Data.RADIATION.max));
    }

    private int getData(int position, Data data) {
        return data.get(positionData[position]);
    }

    private void setData(int position, Data data, int value) {
        positionData[position] = data.set(positionData[position], value);
    }

    private void setData(int position, Data data, boolean value) {
        setData(position, data, value ? 1 : 0);
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
                (p, d) -> addElevation(p, cliff + (radius - d) * slope));
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
    public void addElevation(int position, int value) {
        value += getData(position, Data.ELEVATION);
        setData(position, Data.ELEVATION, Math.min(value, Data.ELEVATION.max));
    }

    /**
     * Get the elevation at a given position
     *
     * @param position the position to get the elevation for
     * @return the elevation at the given position
     */
    public int getElevation(int position) {
        return getData(position, Data.ELEVATION);
    }

    public void setWall(int position, boolean wall) {
        setData(position, Data.WALL, wall);
    }

    public boolean hasWall(int position) {
        return getData(position, Data.WALL) == 1;
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

    public int getColourDifference(Organism organism, int position) {
        if (!hasOrganism(position))
            return -100;
        else
            return organism.getColour() ^ population.getOrganism(position).getColour();
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
        if (hasWall(position))
            throw new IllegalArgumentException("Attempt to add organism in wall");
        population.addOrganism(organism, position, direction);
        if (isAcidic(position))
            organism.reduceEnergy(50);
    }

    /**
     * Remove an organism from the world
     */
    private void removeOrganism(Organism organism) {
        population.removeOrganism(organism);
    }

    public Stream<Organism> getOrganisms() {
        Population copy = population.copy();
        return IntStream.range(0, space.size())
                .filter(copy::hasOrganism)
                .mapToObj(copy::getOrganism);
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
        seedOrganisms();
        growResources();
        processPopulation();
        tickListeners.stream().collect(Collectors.toList()).forEach(Runnable::run);
    }

    private void seedOrganisms() {
        Recipe recipe = new Recipe(random.nextInt(1 << 24));
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.EAT_HERE.ordinal());
        recipe.add(Instruction.ADD_NEURON, 0);
        recipe.add(Instruction.SET_ACTIVITY, WorldActivity.DIVIDE.ordinal());
        if (population.size() < config.getSeedCount()) {
            int position = random.nextInt(space.size());
            if (!population.hasOrganism(position) && !hasWall(position)) {
                population.addOrganism(recipe.make(this, new Mutator(0),
                        config.getInitialEnergy()),
                        position, random.nextInt(4));
            }
        }
    }

    /**
     * Grow all resources in the world according to their temperature. The resources are increased
     * by temp / 100 and a further one each temp % 100 ticks.
     */
    private void growResources() {
        for (int i = 0; i < space.size(); i++) {
            int temp = getTemperature(i);
            if (temp > 0) {
                int growthPeriod = 500 / config.getGrowthRate();
                while (temp >= growthPeriod) {
                    addResources(i, 1);
                    temp -= growthPeriod;
                }
                if (getTime() % (growthPeriod - temp) == 0)
                    addResources(i, 1);
            }
        }
    }

    public void addResources(int position, int amount) {
        int resources = getData(position, Data.RESOURCES) + amount;
        resources = Math.min(resources, Data.RESOURCES.max);
        resources = Math.max(resources, 0);
        setData(position, Data.RESOURCES, resources);
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
        int amount = Math.min(getResource(position), config.getConsumptionRate());
        organism.increaseEnergy(amount);
        addResources(position, -amount);
    }

    /**
     * Move an organism forward, if it has sufficient energy to climb the slope and there is not
     * organism in front.
     *
     * @param organism the organism to move
     */
    public void moveOrganism(Organism organism) {
        int slope = Math.max(0, getSlope(organism, FORWARD));
        if (!hasWall(population.getPosition(organism, FORWARD))) {
            population.moveOrganism(organism, slope);
            if (getRadiation(population.getPosition(organism)) > 0)
                splitToAnyOpenPosition(0, organism);
        }
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
        splitToAnyOpenPosition(config.getMinimumSplitTime(), organism);
    }

    private void splitToAnyOpenPosition(int minTime, Organism parent) {
        if (parent.canDivide(minTime))
            openPositionNextTo(getPosition(parent)).ifPresent(pos -> splitTo(parent, pos));
    }

    private void splitTo(Organism parent, int position) {
        Organism child = parent.divide(mutator(position));
        addOrganism(child, position, population.getDirection(parent));
    }

    private Mutator mutator(int position) {
        int mutationRate = config.getNormalMutationRate()
                + getRadiation(position) * config.getRadiatedMutationRate();
        return new Mutator(mutationRate);
    }

    /**
     * Get an adjacent position that does not have an organism, or OptionalInt.empty() if all
     * adjacent positions have an organism.
     */
    private OptionalInt openPositionNextTo(int position) {
        final List<Integer> directions = Arrays.asList(EAST, WEST, NORTH, SOUTH);
        Collections.shuffle(directions);
        return directions.stream()
                .mapToInt(dir -> space.move(position, dir))
                .filter(pos -> !hasOrganism(pos) && !hasWall(pos))
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
        organism.reduceEnergy(config.getBaseCost());
        organism.reduceEnergy(organism.size() * config.getSizeRate() / 10);
        organism.reduceEnergy(organism.getAge() * config.getAgingRate() / 100);
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
        int activityCount = population.getActivityCount(organism, activity);
        int cost = config.getActivityCost(activity) * activityCount;
        if (organism.consume(cost)) {
            activity.perform(this, organism);
            population.incrementActivityCount(organism, activity);
        }
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
