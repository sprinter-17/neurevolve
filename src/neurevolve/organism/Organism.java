package neurevolve.organism;

import neurevolve.network.ActivationFunction;
import neurevolve.network.Network;

public class Organism {

    private final Network brain;
    private int health;

    public Organism(ActivationFunction function, int initialHealth) {
        if (initialHealth < 0)
            throw new IllegalArgumentException("Negative initial health");
        this.brain = new Network(function);
        this.health = initialHealth;
    }

    public int size() {
        return brain.size();
    }

    public int getHealth() {
        return health;
    }

    public boolean isDead() {
        return health <= 0;
    }

    protected Network getBrain() {
        return brain;
    }
}
