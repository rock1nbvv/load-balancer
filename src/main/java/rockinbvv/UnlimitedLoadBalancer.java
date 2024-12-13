package rockinbvv;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class UnlimitedLoadBalancer {
    private final List<ServiceInstance> instances = new CopyOnWriteArrayList<>();
    private final Map<BalanceType, BalanceStrategy> strategyMap = Map.ofEntries(
            Map.entry(BalanceType.ROUND_ROBIN, new RoundRobinBalanceStrategy()),
            Map.entry(BalanceType.RANDOM, new RandomBalanceStrategy())
    );

    public boolean register(ServiceInstance instance) {
        Optional<ServiceInstance> duplicateInstance = instances.stream().filter(inst -> inst.getAddress().equals(instance.getAddress())).findFirst();
        if (duplicateInstance.isEmpty()) {
            return instances.add(instance);
        }
        return false;
    }

    public ServiceInstance getService(BalanceType type) {
        BalanceStrategy balanceStrategy = strategyMap.get(type);
        return balanceStrategy.selectInstance(instances.stream().toList());
    }

    public List<ServiceInstance> getAllInstances() {
        return instances.stream().toList();
    }
}
