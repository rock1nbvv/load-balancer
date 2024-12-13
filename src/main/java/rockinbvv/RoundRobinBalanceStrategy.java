package rockinbvv;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class RoundRobinBalanceStrategy implements BalanceStrategy {

    private final AtomicLong requestCount = new AtomicLong();

    @Override
    public BalanceType getType() {
        return BalanceType.ROUND_ROBIN;
    }

    @Override
    public ServiceInstance selectInstance(List<ServiceInstance> instances) {
        long request = requestCount.getAndIncrement();
        return instances.get((int) (request % instances.size()));
    }
}
