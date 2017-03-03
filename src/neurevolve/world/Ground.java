package neurevolve.world;

import java.util.Arrays;

public class Ground {

    private final int[] elements;

    @FunctionalInterface
    public interface Process {

        public void accept(int position, int ground);
    }

    public Ground(int size) {
        this(new int[size]);
    }

    private Ground(int[] elements) {
        this.elements = elements;
    }

    public Ground copy() {
        return new Ground(Arrays.copyOf(elements, elements.length));
    }

    public int getTotalValue(int position) {
        return elements[position];
    }

    public int getElementValue(int position, GroundElement element) {
        if (position < 0 || position >= elements.length)
            throw new IllegalArgumentException("Illegal position");
        return element.get(elements[position]);
    }

    public void addElementValue(int position, GroundElement element, int change) {
        if (change < 0)
            throw new IllegalArgumentException("Adding negative element value");
        int value = Math.min(element.getMaximum(), getElementValue(position, element) + change);
        changeElementValue(position, element, value);
    }

    public void substractElementValue(int position, GroundElement element, int change) {
        if (change < 0)
            throw new IllegalArgumentException("Substracting negative element value");
        int value = Math.max(0, getElementValue(position, element) - change);
        changeElementValue(position, element, value);
    }

    private void changeElementValue(int position, GroundElement element, int value) {
        if (position < 0 || position >= elements.length)
            throw new IllegalArgumentException("Illegal position");
        elements[position] = element.set(elements[position], value);
    }

    public void forEach(Process process) {
        for (int p = 0; p < elements.length; p++) {
            process.accept(p, elements[p]);
        }
    }

}
