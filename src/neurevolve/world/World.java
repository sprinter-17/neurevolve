package neurevolve.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import neurevolve.network.ActivationFunction;
import neurevolve.organism.Environment;
import neurevolve.organism.Organism;
import static neurevolve.world.Angle.*;
import neurevolve.world.Configuration.Value;
import static neurevolve.world.GroundElement.*;
import static neurevolve.world.Space.*;

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
    private final Ground ground;
    private final Population population;
    private final WorldInput inputs;

    private final ActivationFunction function;

    /**
     * Construct a world within a frame with a configuration
     *
     * @param function the activation function to use for all organisms in the world
     * @param space the frame that defines the size of the world
     * @param configuration the configuration of the world
     */
    public World(ActivationFunction function, Space space, Configuration configuration) {
        this.function = function;
        this.config = configuration;
        this.space = space;
        this.inputs = new WorldInput(this);
        this.population = new Population(space, configuration);
        this.ground = new Ground(space.size());
    }

    /**
     * Add a set of ground elements to be used as inputs.
     *
     * @param elements the set to add.
     */
    public void addUsedElements(EnumSet<GroundElement> elements) {
        inputs.addUsedElements(elements);
    }

    /**
     * Check if a ground element is available as an input.
     *
     * @param element the element to check
     * @return true if the given element is available as an input.
     */
    public boolean usesElement(GroundElement element) {
        return inputs.usesElement(element);
    }

    /**
     * Get the number of input codes
     *
     * @return the total number of input codes.
     */
    public int getInputCodeCount() {
        return inputs.getCodeCount();
    }

    /**
     * Makes a copy of the ground elements.
     *
     * @return the copy
     */
    public Ground copyGround() {
        return ground.copy();
    }

    /**
     * Make a copy of the population
     *
     * @return a complete copy of the population array
     */
    public Population getPopulationCopy() {
        return population.copy();
    }

    /**
     * Get the direction an organism is currently facing.
     *
     * @param organism the organism who direction to get.
     * @return the direction.
     */
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

    /**
     * Get the value of a ground element.
     *
     * @param position the position of the element
     * @param element the element to retrieve
     * @return the value of the given element at the given position
     */
    public int getElementValue(int position, GroundElement element) {
        return ground.getElementValue(position, element);
    }

    /**
     * Check if an element is present.
     *
     * @param position the position of the element
     * @param element the element to check
     * @return true, if the given element has a non-zero value at the given position.
     */
    public boolean hasElement(int position, GroundElement element) {
        return getElementValue(position, element) > 0;
    }

    /**
     * Add to the value of an element.
     *
     * @param position the position of the element
     * @param element the element to increase the value
     * @param value the amount to increase the given element's value by at the given position
     */
    public void addElementValue(int position, GroundElement element, int value) {
        ground.addElementValue(position, element, value);
    }

    public void decrementElementValue(int position, GroundElement element) {
        ground.substractElementValue(position, element, 1);
    }

    public void resetActivityCount(Organism organism) {
        population.resetActivityCount(organism);
    }

    /**
     * Get the difference in elevation between an organism's position and another position
     *
     * @param organism the organism that the slope is relative to
     * @param position the position whose elevation to compare
     * @return the difference in elevation between the adjacent position and the organism's position
     */
    public int getSlope(Organism organism, int position) {
        return getElementValue(position, ELEVATION) - getElementValue(getPosition(organism), ELEVATION);
    }

    /**
     * Check if a position has any element that prevents an organism being added.
     *
     * @param position the position to check
     * @return true, if the position does not have a wall or body
     */
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
     * @return the organism's energy, or -100 if there is no organism in the given position
     */
    public int getOrganismEnergy(int position) {
        return hasOrganism(position)
                ? population.getOrganism(position).getEnergy()
                : -100;
    }

    /**
     * Get the difference in colour between an organism and another organism at a position
     *
     * @param organism the organism whose colour to compare
     * @param position the position to check
     * @return the colour difference between the organisms, or -100 if there is no organism in the
     * given position
     */
    public int getColourDifference(Organism organism, int position) {
        return hasOrganism(position)
                ? organism.getColourDifference(population.getOrganism(position))
                : -100;
    }

    /**
     * Add an organism to the world.
     *
     * @param organism the organism to place
     * @param position the position to place the organism
     * @param direction the direction the organism will face
     * @throws IllegalArgumentException if the position is not empty or already has an organism
     */
    public void addOrganism(Organism organism, int position, int direction) {
        if (!isEmpty(position) || hasOrganism(position)) {
            throw new IllegalArgumentException("Attempt to add organism in non-empty position");
        }
        population.addOrganism(organism, position, direction);
    }

    /**
     * Remove an organism from the world
     */
    public void removeOrganism(Organism organism) {
        population.removeOrganism(organism);
    }

    /**
     * Get all current organisms from a copy of population.
     *
     * @return a stream of the current organisms.
     */
    public Stream<Organism> getOrganisms() {
        Population copy = population.copy();
        return allPositions()
                .filter(copy::hasOrganism)
                .mapToObj(copy::getOrganism);
    }

    public IntStream allPositions() {
        return IntStream.range(0, space.size());
    }

    public int getSpaceSize() {
        return space.size();
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
            consumeResources(position, organism);
            return true;
        }
        return false;
    }

    private void consumeResources(int position, Organism organism) {
        int consumption = config.getValue(Value.CONSUMPTION_RATE);
        int amount = Math.min(getElementValue(position, RESOURCES), consumption);
        int maxEnergy = config.getValue(Value.MAX_ENERGY);
        amount = Math.min(amount, maxEnergy - organism.getEnergy());
        amount = Math.max(amount, 0);
        organism.increaseEnergy(amount);
        ground.substractElementValue(position, RESOURCES, amount);
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
                if (getElementValue(position, RADIATION) > 0) {
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
                + getElementValue(position, RADIATION) * config.getValue(Value.RADIATION_MUTATION_RATE);
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
     * Attack an organism at a given angle. If the attacked organism does not have greater energy
     * then it dies and its energy is transfered to the attacker.
     *
     * @param attacker the organism that is the source of the attack
     * @param angle the angle to the target organism
     * @return true if the organism attacked (even if unsuccessfully)
     */
    public boolean attackOrganism(Organism attacker, Angle angle) {
        return population.attack(attacker, angle);
    }

    /**
     * Get the temperature of a position. Calculated based on latitude, elevation and season.
     *
     * @param position the position to retrieve the temperature for
     * @return the temperature
     */
    public int getTemperature(int position) {
        return getLatitudeTemp(position) - getElementValue(position, ELEVATION);
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
        population.perform(this, organism, code);
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
