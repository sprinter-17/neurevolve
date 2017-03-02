package neurevolve.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import neurevolve.network.ActivationFunction;
import neurevolve.organism.Environment;
import neurevolve.organism.Organism;
import neurevolve.organism.Recipe;
import static neurevolve.world.Angle.FORWARD;
import neurevolve.world.Configuration.Value;
import static neurevolve.world.GroundElement.ACID;
import static neurevolve.world.GroundElement.BODY;
import static neurevolve.world.GroundElement.ELEVATION;
import static neurevolve.world.GroundElement.RADIATION;
import static neurevolve.world.GroundElement.RESOURCES;
import static neurevolve.world.GroundElement.WALL;
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
    private final Configuration config;
    private final int[] positionData;
    private final Random random = new Random();
    private final WorldInput inputs;

    private final List<Runnable> tickListeners = new ArrayList<>();

    private final Time time;
    private WorldStatistics stats;

    private final Population population;
    private final EnumSet<GroundElement> usedElements = EnumSet.of(GroundElement.BODY);

    private final ActivationFunction function;

    /**
     * Construct a world within a frame with a configuration
     *
     * @param function the activation function to use for all organisms in the world
     * @param frame the frame that defines the size of the world
     * @param configuration the configuration of the world
     */
    public World(ActivationFunction function, Space frame, Configuration configuration) {
        this.function = function;
        this.config = configuration;
        this.space = frame;
        this.time = new Time(configuration);
        this.inputs = new WorldInput(this);
        this.population = new Population(space);
        this.positionData = new int[frame.size()];
    }

    public void setUsedElements(EnumSet<GroundElement> elements) {
        this.usedElements.addAll(elements);
        inputs.setUsedElements(usedElements);
    }

    public boolean usesElement(GroundElement element) {
        return usedElements.contains(element);
    }

    public int getInputCodeCount() {
        return inputs.getCodeCount();
    }

    public int[] copyGroundElements() {
        return Arrays.copyOf(positionData, space.size());
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
        setData(position, RESOURCES, amount);
    }

    /**
     * Get the amount of resource at a position
     *
     * @param position the position to check
     * @return the amount of resource at the given position
     */
    public int getResource(int position) {
        return getElementValue(position, RESOURCES);
    }

    public boolean isAcidic(int position) {
        return getElementValue(position, ACID) == 1;
    }

    public void setAcidic(int position, boolean acidic) {
        setData(position, ACID, acidic);
    }

    public int getRadiation(int position) {
        return getElementValue(position, RADIATION);
    }

    public void addRadition(int position, int radiation) {
        radiation += getRadiation(position);
        setData(position, RADIATION, Math.min(radiation, RADIATION.getMaximum()));
    }

    public int getElementValue(int position, GroundElement element) {
        return element.get(positionData[position]);
    }

    private void setData(int position, GroundElement data, int value) {
        positionData[position] = data.set(positionData[position], value);
    }

    private void setData(int position, GroundElement data, boolean value) {
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
     * Get the difference in elevation between an organism's position and another position
     *
     * @param organism the organism that the slope is relative to
     * @param position the position whose elevation to compare
     * @return the difference in elevation between the adjacent position and the organism's position
     */
    public int getSlope(Organism organism, int position) {
        return getElevation(position) - getElevation(getPosition(organism));
    }

    /**
     * Set the elevation of a position. This method is primarily for testing purposes.
     *
     * @param position the position whose elevation will be set
     * @param value the elevation to set the position to
     */
    public void addElevation(int position, int value) {
        value += getElementValue(position, ELEVATION);
        setData(position, ELEVATION, Math.min(value, ELEVATION.getMaximum()));
    }

    /**
     * Get the elevation at a given position
     *
     * @param position the position to get the elevation for
     * @return the elevation at the given position
     */
    public int getElevation(int position) {
        return getElementValue(position, ELEVATION);
    }

    public void setWall(int position, boolean wall) {
        setData(position, WALL, wall);
    }

    public boolean hasWall(int position) {
        return getElementValue(position, WALL) == 1;
    }

    public boolean isEmpty(int position) {
        return getElementValue(position, WALL) == 0 && getElementValue(position, BODY) == 0;
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
        if (!hasOrganism(position)) {
            return -100;
        } else {
            int differences = organism.getColour() ^ population.getOrganism(position).getColour();
            return (int) IntStream.range(0, 24)
                    .filter(b -> (differences & (1 << b)) != 0)
                    .count() - 1;
        }
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
        if (!isEmpty(position)) {
            throw new IllegalArgumentException("Attempt to add organism in non-empty position");
        }
        population.addOrganism(organism, position, direction);
        if (isAcidic(position)) {
            organism.reduceEnergy(50);
        }
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
        halfLives();
        processPopulation();
        tickListeners.stream().collect(Collectors.toList()).forEach(Runnable::run);
    }

    public WorldStatistics getStats() {
        return stats;
    }

    private void seedOrganisms() {
        Recipe recipe = config.getSeedRecipe().replicate((instructions, size, c) -> {
            Recipe copy = new Recipe(random.nextInt(1 << 24));
            for (int i = 0; i < size; i++) {
                copy.add(instructions[i]);
            }
            return copy;
        });
        if (population.size() < config.getValue(Value.SEED_COUNT)) {
            int position = random.nextInt(space.size());
            if (!population.hasOrganism(position) && isEmpty(position)) {
                population.addOrganism(new Organism(this, config.getValue(Value.INITIAL_ENERGY), recipe),
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
                int growthPeriod = 100;
                while (temp >= growthPeriod) {
                    addResources(i, 1);
                    temp -= growthPeriod;
                }
                if (getTime() % (growthPeriod - temp) == 0) {
                    addResources(i, 1);
                }
            }
        }
    }

    private void halfLives() {
        Arrays.stream(GroundElement.values())
                .forEach(this::halfLife);
    }

    private void halfLife(GroundElement element) {
        int halfLife = config.getHalfLife(element);
        if (halfLife > 0 && halfLife < 1000) {
            for (int i = 0; i < space.size(); i++) {
                int value = getElementValue(i, element);
                if (value > 0 && (halfLife == 1 || random.nextInt(halfLife) == 0))
                    setData(i, element, value - 1);
            }
        }
    }

    public void addResources(int position, int amount) {
        int resources = getElementValue(position, RESOURCES) + amount;
        resources = Math.min(resources, RESOURCES.getMaximum());
        resources = Math.max(resources, 0);
        setData(position, RESOURCES, resources);
    }

    /**
     * Feed an organism by consuming resources at a position relative to the organism. The amount is
     * added to the organism's energy and taken from the world's resources at that position.
     *
     * @param organism the organism
     * @param angles zero or more angles defining the path to the position from which to consume
     * resources
     * @return true if the organism was fed
     */
    public boolean feedOrganism(Organism organism, Angle... angles) {
        int position = getPosition(organism, angles);
        if (isEmpty(position)) {
            int consumption = config.getValue(Value.CONSUMPTION_RATE);
            int amount = Math.min(getResource(position), consumption);
            int maxEnergy = config.getValue(Value.MAX_ENERGY);
            amount = Math.min(amount, maxEnergy - organism.getEnergy());
            organism.increaseEnergy(amount);
            addResources(position, -amount);
            return true;
        }
        return false;
    }

    /**
     * Move an organism forward, if it has sufficient energy to climb the slope and there is no
     * organism, wall or body in front.
     *
     * @param organism the organism to move
     * @return true if the organism moved
     */
    public boolean moveOrganism(Organism organism) {
        int position = population.getPosition(organism, FORWARD);
        if (isEmpty(position) && !population.hasOrganism(position)) {
            int slope = Math.max(0, getSlope(organism, position));
            if (population.moveOrganism(organism, slope)) {
                if (getRadiation(population.getPosition(organism)) > 0) {
                    splitToAnyOpenPosition(0, organism);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Turn an organism's direction through an angle
     *
     * @param organism the organism to turn
     * @param angle the angle to turn the organism by
     * @return true, always
     */
    public boolean turnOrganism(Organism organism, Angle angle) {
        population.turn(organism, angle);
        return true;
    }

    /**
     * Split and organism. The child organism is placed at a random position next to this organism
     * facing in the same direction. The split consumes as much energy as the size of the recipe
     * being replicated. If it does not have enough energy then it does not split. It also does not
     * split if there are no free positions adjacent to the organism.
     *
     * @param organism the organism to split
     * @return true if the organism split
     */
    public boolean splitOrganism(Organism organism) {
        return splitToAnyOpenPosition(config.getValue(Value.MIN_SPLIT_TIME), organism);
    }

    private boolean splitToAnyOpenPosition(int minTime, Organism parent) {
        if (parent.canDivide(minTime) && parent.getEnergy() >= config.getValue(Value.MIN_SPLIT_ENERGY)) {
            OptionalInt position = openPositionNextTo(getPosition(parent));
            if (position.isPresent()) {
                splitTo(parent, position.getAsInt());
            }
        }
        return false;
    }

    private void splitTo(Organism parent, int position) {
        Organism child = parent.divide(mutator(position));
        addOrganism(child, position, population.getDirection(parent));
    }

    private Mutator mutator(int position) {
        int mutationRate = config.getValue(Value.NORMAL_MUTATION_RATE)
                + getRadiation(position) * config.getValue(Value.RADIATION_MUTATION_RATE);
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
                .filter(pos -> !hasOrganism(pos) && isEmpty(pos))
                .findFirst();
    }

    /**
     * Process the population. This uses a copy of the population array so that changes that occur
     * during processing do not interfere with the current state.
     */
    public void processPopulation() {
        stats = new WorldStatistics(time);
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
        if (isAcidic(position))
            organism.reduceEnergy(config.getValue(Value.ACID_TOXICITY));
        organism.reduceEnergy(config.getValue(Value.BASE_COST));
        organism.reduceEnergy(organism.size() * config.getValue(Value.SIZE_RATE) / 10);
        organism.reduceEnergy(organism.getAge() * config.getValue(Value.AGING_RATE) / 100);
        population.resetActivityCount(organism);
        organism.activate();
        stats.add(organism);
        if (organism.isDead()) {
            removeOrganism(organism);
            setData(position, BODY, true);
        }
    }

    /**
     * Reduce the energy of an organism by the temperature at its position only if it is negative
     */
    private void reduceEnergyByTemperature(int position, Organism organism) {
        int temp = getTemperature(position);
        if (temp < 0) {
            organism.reduceEnergy(-temp);
        }
    }

    /**
     * Attack an organism at a given angle. If the attacked organism does not have greater energy
     * then it dies and its energy is transfered to the attacker.
     *
     * @param attacker the organism that is the source of the attack
     * @param angle the angle to the target organism
     * @return true if the organism attacked (even if unsuccessfully)
     */
    public boolean attackOrganism(Organism attacker, Angle angle) {
        int position = getPosition(attacker, angle);
        if (hasOrganism(position)) {
            Organism target = population.getOrganism(position);
            if (attacker.getEnergy() >= target.getEnergy()) {
                attacker.increaseEnergy(target.getEnergy());
                target.reduceEnergy(target.getEnergy());
            }
            return true;
        }
        return false;
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
        return space.scaleByLatitude(position, config.getValue(Value.MIN_TEMP), config.getValue(Value.MAX_TEMP));
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
        return inputs.getValue(organism, input);
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
        int cost = config.getActivityCost(activity);
        int count = population.getActivityCount(organism, activity);
        cost = cost * (100 + count * config.getActivityFactor(activity)) / 100;
        if (organism.hasEnergy(cost)) {
            if (activity.perform(this, organism)) {
                organism.reduceEnergy(cost);
                population.incrementActivityCount(organism, activity);
            }
        } else {
            organism.reduceEnergy(cost / 2);
        }
    }

    @Override
    public String describeInput(int input) {
        return inputs.getName(input);
    }

    public OptionalInt getInputCode(String name) {
        return inputs.getCode(name);
    }

    @Override
    public String describeActivity(int activity) {
        return WorldActivity.describe(activity);
    }

}
