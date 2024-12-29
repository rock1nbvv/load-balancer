package rockinbvv;

import java.security.SecureRandom;
import java.util.List;

/**
 * Due to SecureRandom picks instances uniformly
 */
public class RandomBalanceStrategy implements BalanceStrategy {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public ServiceInstance selectInstance(List<ServiceInstance> instances) {
        int idx = secureRandom.nextInt(0, instances.size());
        return instances.get(idx);
    }
}
