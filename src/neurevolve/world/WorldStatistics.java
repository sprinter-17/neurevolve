package neurevolve.world;

import neurevolve.organism.Organism;

public class WorldStatistics {

    private final Time time;

    private int population = 0;
    private int totalComplexity = 0;
    private int totalAge = 0;
    private int totalSize = 0;
    private int totalDescendents = 0;
    private int totalEnergy = 0;

    public WorldStatistics(Time time) {
        this.time = time;
    }

    public void add(Organism organism) {
        population++;
        totalComplexity += organism.complexity();
        totalAge += organism.getAge();
        totalSize += organism.size();
        totalEnergy += organism.getEnergy();
        totalDescendents += organism.getDescendents();
    }

    public int getTime() {
        return time.getTime();
    }

    public boolean isEndOfYear() {
        return time.timeOfYear() == 0;
    }

    public int getYear() {
        return time.getYear();
    }

    public float getPopulation() {
        return population;
    }

    public float getAverageComplexity() {
        return getAverage(totalComplexity);
    }

    public float getAverageAge() {
        return getAverage(totalAge);
    }

    public float getAverageDescendents() {
        return getAverage(totalDescendents);
    }

    public float getAverageSize() {
        return getAverage(totalSize);
    }

    public float getAverageEnergy() {
        return getAverage(totalEnergy);
    }

    private float getAverage(float total) {
        if (population == 0) {
            return 0f;
        } else {
            return total / population;
        }
    }

}
