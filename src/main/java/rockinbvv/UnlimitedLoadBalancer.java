package rockinbvv;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class UnlimitedLoadBalancer {

    private final List<ServiceInstance> instances = new CopyOnWriteArrayList<>();

    private final Map<BalanceType, BalanceStrategy> strategyMap = Map.ofEntries(
            Map.entry(BalanceType.ROUND_ROBIN, new RoundRobinBalanceStrategy()),
            Map.entry(BalanceType.RANDOM, new RandomBalanceStrategy())
    );

    public boolean register(ServiceInstance instance) {
        if (instances.contains(instance)) {
            return false;
        }
        return instances.add(instance);
    }

    public ServiceInstance getInstance(BalanceType type) {
        BalanceStrategy balanceStrategy = strategyMap.get(type);
        return balanceStrategy.selectInstance(instances.stream().toList());
    }

    public List<ServiceInstance> getAllInstances() {
        return instances.stream().toList();
    }
}
