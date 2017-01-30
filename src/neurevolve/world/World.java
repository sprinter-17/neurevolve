package neurevolve.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
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
public class World {

    private final int width;
    private final int height;
    private final int[] resources;
    private final int[] elevation;
    private final Map<Integer, Organism> population = new HashMap<>();
    private final Map<Integer, Organism> newOrganisms = new HashMap<>();
    private final List<Integer> graveyard = new ArrayList<>();

    private int time = 0;
    private int yearLength = 1;
    private int minTemp = 0;
    private int maxTemp = 0;
    private int tempVariation = 0;

    /**
     * Construct a world with the provide width and height
     *
     * @param width the width of the world
     * @param height the height of the world;
     */
    public World(int width, int height) {
        this.width = width;
        this.height = height;
        int size = width * height;
        this.resources = new int[size];
        this.elevation = new int[size];
    }

    /**
     * Get the total number of positions in the world
     *
     * @return <tt>width * height</tt>
     */
    public int size() {
        return width * height;
    }

    public int getPopulationSize() {
        return population.size() + newOrganisms.size();
    }

    public int getResource(Position position) {
        return resources[position.toIndex(width, height)];
    }

    public int getSlope(Position position, Direction dir) {
        return getElevation(dir.move(position)) - getElevation(position);
    }

    public void setElevation(Position position, int value) {
        elevation[position.toIndex(width, height)] = value;
    }

    protected int getElevation(Position position) {
        return elevation[position.toIndex(width, height)];
    }

    public boolean hasOrganism(Position position) {
        return hasCurrentOrganism(position) || hasNewOrganism(position);
    }

    private boolean hasCurrentOrganism(Position position) {
        int index = position.toIndex(width, height);
        return population.containsKey(index);
    }

    private boolean hasNewOrganism(Position position) {
        int index = position.toIndex(width, height);
        return newOrganisms.containsKey(index);
    }

    private Organism getOrganism(Position position) {
        return population.get(position.toIndex(width, height));
    }

    public void addOrganism(Position position, Organism organism) {
        if (hasOrganism(position))
            throw new IllegalArgumentException("Attempt to add two organisms to same position");
        newOrganisms.put(position.toIndex(width, height), organism);
    }

    public void seed(Environment environment, Recipe recipe, int count) {
        Random random = new Random();
        for (int i = 0; i < Math.min(count, size()); i++) {
            Position position = random.ints(0, size())
                    .mapToObj(p -> Position.fromIndex(p, width, height))
                    .filter(p -> !hasOrganism(p))
                    .findAny().orElseThrow(IllegalStateException::new);
            addOrganism(position, recipe.make(environment, 1000));
        }
    }

    public void setTemperatureRange(int minTemp, int maxTemp) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public void tick(WorldEnvironment environment) {
        moveNewOrganisms();
        growResources();
        processPopulation(environment);
        time++;
    }

    private void moveNewOrganisms() {
        population.putAll(newOrganisms);
        newOrganisms.clear();
    }

    private void growResources() {
        for (int i = 0; i < width * height; i++) {
            resources[i] += getTemperature(Position.fromIndex(i, width, height));
        }
    }

    private void processPopulation(WorldEnvironment environment) {
        graveyard.clear();
        population.forEach((i, o) -> processOrganism(i, o, environment));
        population.keySet().removeAll(graveyard);
    }

    private void processOrganism(int index, Organism organism, WorldEnvironment environment) {
        Position position = Position.fromIndex(index, width, height);
        reduceEnergyByTemperature(position, organism);
        environment.setContext(position, organism);
        organism.activate();
        if (organism.isDead())
            graveyard.add(index);
    }

    public void moveOrganism(Position position, Direction direction) {
        if (hasCurrentOrganism(position) && !hasOrganism(direction.move(position))) {
            Organism organism = population.remove(position.toIndex(width, height));
            addOrganism(direction.move(position), organism);
        }
    }

    public void feedOrganism(Position position, int amount) {
        if (hasCurrentOrganism(position)) {
            if (amount > getResource(position))
                amount = getResource(position);
            getOrganism(position).increaseEnergy(amount);
            resources[position.toIndex(width, height)] -= amount;
        }
    }

    public void splitOrganism(Position position) {
        if (hasCurrentOrganism(position)) {
            Organism organism = getOrganism(position);
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
        if (!hasCurrentOrganism(position))
            throw new IllegalArgumentException("Attempt to kill organism in empty position");
        int index = position.toIndex(width, height);
        Organism organism = population.get(index);
        resources[index] += organism.getEnergy();
        organism.reduceEnergy(organism.getEnergy());
        population.remove(index);
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
}
