package neurevolve;

import neurevolve.world.Configuration;

public class TestConfiguration extends Configuration {

    public TestConfiguration() {
        setValue(Value.YEAR_LENGTH, 1);
        setValue(Value.TEMP_VARIATION, 0);
        setValue(Value.MIN_TEMP, 0);
        setValue(Value.MAX_TEMP, 0);
        setValue(Value.SEED_COUNT, 0);
        setValue(Value.BASE_COST, 0);
        setValue(Value.MIN_SPLIT_TIME, 0);
    }

}
