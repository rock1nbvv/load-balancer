package rockinbvv;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UnlimitedLoadBalancer {

    private final List<ServiceInstance> instances = new CopyOnWriteArrayList<>();

    private final BalanceStrategy balanceStrategy;

    public UnlimitedLoadBalancer(BalanceStrategy balanceStrategy) {
        this.balanceStrategy = balanceStrategy;
    }

    public boolean register(ServiceInstance instance) {
        if (instances.contains(instance)) {
            return false;
        }
        return instances.add(instance);
    }

    public ServiceInstance getInstance() {
        return balanceStrategy.selectInstance(instances.stream().toList());
    }

    public List<ServiceInstance> getAllInstances() {
        return instances.stream().toList();
    }
}
