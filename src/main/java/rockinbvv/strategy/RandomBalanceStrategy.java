package rockinbvv.strategy;

import rockinbvv.ServiceInstance;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * RandomGenerator should comply uniform distribution
 */
public class RandomBalanceStrategy implements BalanceStrategy {

    private final RandomGenerator randomGenerator;

    public RandomBalanceStrategy(RandomGenerator randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    @Override
    public ServiceInstance selectInstance(List<ServiceInstance> instances) {
        int idx = randomGenerator.nextInt(0, instances.size());
        return instances.get(idx);
    }
}
