package neurevolve;

import neurevolve.world.Configuration;

public class TestConfiguration extends Configuration {

    public TestConfiguration() {
        setYear(1, 0);
        setTemperatureRange(0, 0);
        setSeed(0, 0);
        setValue(Value.BASE_COST, 0);
        setValue(Value.MIN_SPLIT_TIME, 0);
    }

}
