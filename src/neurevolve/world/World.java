package neurevolve.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    private final int width;
    private final int height;
    private final int[] resources;
    private final int[] elevation;
    private final Organism[] population;
    private final Random random = new Random();

    private int mutationRate = 0;
    private int time = 0;
    private int populationSize = 0;
    private int yearLength = 1;
    private int minTemp = 0;
    private int maxTemp = 0;
    private int tempVariation = 0;

    private final ActivationFunction function;
    private Position currentPosition;
    private Organism largestOrganism;
    private int totalComplexity;

    /**
     * Construct a world with the provide width and height
     *
     * @param function the activation function to use for all organisms in the world
     * @param width the width of the world
     * @param height the height of the world;
     */
    public World(ActivationFunction function, int width, int height) {
        this.function = function;
        this.width = width;
        this.height = height;
        int size = width * height;
        this.resources = new int[size];
        this.elevation = new int[size];
        this.population = new Organism[size];
    }

    /**
     * Set the mutation rate for the world. The mutation rate determines the likelihood of
     * transcription errors when copying a recipe. A mutation rate of 0 means that no errors occur.
     * A mutation rate of 1000 means that errors occur on every transcription.
     *
     * @param mutationRate the rate of mutation (range 0-1000)
     */
    public void setMutationRate(int mutationRate) {
        this.mutationRate = mutationRate;
    }

    /**
     * Get the total number of positions in the world
     *
     * @return <tt>width * height</tt>
     */
    public int size() {
        return width * height;
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
    public int getResource(Position position) {
        return resources[position.toIndex(width, height)];
    }

    /**
     * Get the difference in elevation between a position and an adjacent position
     *
     * @param position the starting position
     * @param dir the direction to the adjacent position
     * @return the difference in elevation
     */
    public int getSlope(Position position, Direction dir) {
        return getElevation(dir.move(position)) - getElevation(position);
    }

    /**
     * Set the elevation of a position
     *
     * @param position the position whose elevation will be set
     * @param value the elevation to set the position to
     */
    public void setElevation(Position position, int value) {
        elevation[position.toIndex(width, height)] = value;
    }

    /**
     * Get the elevation at a given position
     *
     * @param position the position to get the elevation for
     * @return the elevation at the given position
     */
    protected int getElevation(Position position) {
        return elevation[position.toIndex(width, height)];
    }

    public boolean hasOrganism(Position position) {
        return hasOrganism(position.toIndex(width, height));
    }

    private boolean hasOrganism(int position) {
        return population[position] != null;
    }

    private Organism getOrganism(Position position) {
        return getOrganism(position.toIndex(width, height));
    }

    private Organism getOrganism(int position) {
        return population[position];
    }

    public void addOrganism(Position position, Organism organism) {
        if (hasOrganism(position))
            throw new IllegalArgumentException("Attempt to add two organisms to same position");
        addOrganism(position.toIndex(width, height), organism);
    }

    private void addOrganism(int position, Organism organism) {
        if (population[position] == null) {
            population[position] = organism;
            populationSize++;
        }
    }

    private void removeOrganism(int position) {
        if (population[position] != null) {
            population[position] = null;
            populationSize--;
        }
    }

    public void seed(Recipe recipe, int count) {
        for (int i = 0; i < Math.min(count, size()); i++) {
            int position = random.ints(0, size())
                    .filter(p -> !hasOrganism(p))
                    .findAny().orElseThrow(IllegalStateException::new);
            addOrganism(position, recipe.make(this, 1000));
        }
    }

    public void setTemperatureRange(int minTemp, int maxTemp) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public void tick() {
        growResources();
        processPopulation();
        time++;
    }

    private void growResources() {
        for (int i = 0; i < width * height; i++) {
            int temp = getTemperature(Position.fromIndex(i, width, height));
            if (temp > 0)
                resources[i] += temp;
        }
    }

    private void processPopulation() {
        largestOrganism = null;
        totalComplexity = 0;
        Organism[] previous = Arrays.copyOf(population, size());
        for (int i = 0; i < size(); i++) {
            if (previous[i] != null)
                processPosition(i, previous[i]);
        }
    }

    private void processPosition(int position, Organism organism) {
        setCurrentPosition(Position.fromIndex(position, width, height));
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

    protected void setCurrentPosition(Position position) {
        this.currentPosition = position;
    }

    public void moveOrganism(Position position, Direction direction) {
        if (hasOrganism(position) && !hasOrganism(direction.move(position))) {
            int index = position.toIndex(width, height);
            Organism organism = population[index];
            removeOrganism(index);
            addOrganism(direction.move(position), organism);
        }
    }

    public void feedOrganism(Position position, int amount) {
        if (hasOrganism(position)) {
            if (amount > getResource(position))
                amount = getResource(position);
            getOrganism(position).increaseEnergy(amount);
            resources[position.toIndex(width, height)] -= amount;
        }
    }

    public void splitOrganism(Position position) {
        if (hasOrganism(position)) {
            Organism organism = getOrganism(position);
            if (organism.getEnergy() > 10)
                openPositionNextTo(position)
                        .ifPresent(pos -> addOrganism(pos, organism.divide()));
        }
    }

    private Optional<Position> openPositionNextTo(Position position) {
        List<Direction> directions = Arrays.asList(Direction.values());
        Collections.shuffle(directions);
        return directions.stream()
                .map(dir -> dir.move(position))
                .filter(pos -> !hasOrganism(pos))
                .findFirst();
    }

    private void reduceEnergyByTemperature(Position position, Organism organism) {
        int temp = getTemperature(position);
        if (temp < 0)
            organism.reduceEnergy(-temp);
    }

    public void killOrganism(Position position) {
        if (!hasOrganism(position))
            throw new IllegalArgumentException("Attempt to kill organism in empty position");
        int index = position.toIndex(width, height);
        Organism organism = population[index];
        resources[index] += organism.getEnergy();
        organism.reduceEnergy(organism.getEnergy());
        population[index] = null;
    }

    public int getTime() {
        return time;
    }

    public void setYear(int length, int tempVariation) {
        this.yearLength = length;
        this.tempVariation = tempVariation;
    }

    public int getTemperature(Position position) {
        return getLatitudeTemp(position) - getElevation(position) + getSeasonTemp();
    }

    private int getLatitudeTemp(Position position) {
        int distance = Math.abs(position.latitude(height));
        return maxTemp - 2 * (maxTemp - minTemp) * distance / height;
    }

    private int getSeasonTemp() {
        int timeOfYear = time % yearLength;
        int timeFromMidYear = Math.abs(yearLength / 2 - timeOfYear);
        return tempVariation * 2 * timeFromMidYear / yearLength;
    }

    @Override
    public int applyActivationFunction(int input) {
        return function.apply(input);
    }

    @Override
    public int getInput(int input) {
        return WorldInput.getValue(input, this, currentPosition, getOrganism(currentPosition));
    }

    @Override
    public void performActivity(int activity) {
        if (hasOrganism(currentPosition))
            WorldActivity.perform(activity, this, currentPosition, getOrganism(currentPosition));
    }

    @Override
    public Recipe copyInstructions(int[] instructions, int size) {
        Recipe copy = new Recipe();
        int pos = 0;
        while (pos < size) {
            if (pos >= 0) {
                int value = instructions[pos];
                if (mutationRate > 0 && random.nextInt(1000 / mutationRate) == 0)
                    value += random.nextInt(11) - 5;
                copy.add(value);
            }
            if (mutationRate > 0 && random.nextInt(1000 / mutationRate) == 0)
                pos += random.nextInt(3);
            else if (mutationRate > 0 && random.nextInt(1000 / mutationRate) == 0)
                pos -= random.nextInt(3);
            else
                pos++;
        }
        return copy;
    }
}
