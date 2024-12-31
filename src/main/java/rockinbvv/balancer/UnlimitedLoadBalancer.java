package rockinbvv.balancer;

import rockinbvv.ServiceInstance;
import rockinbvv.strategy.BalanceStrategy;
import rockinbvv.strategy.BalanceType;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UnlimitedLoadBalancer implements LoadBalancer {

    private final List<ServiceInstance> instances = new CopyOnWriteArrayList<>();

    private final BalanceStrategy balanceStrategy;

    public UnlimitedLoadBalancer(BalanceStrategy balanceStrategy) {
        this.balanceStrategy = balanceStrategy;
    }

    @Override
    public boolean register(ServiceInstance instance) {
        if (instances.contains(instance)) {
            return false;
        }
        return instances.add(instance);
    }

    @Override
    public ServiceInstance getInstance() {
        return balanceStrategy.selectInstance(instances.stream().toList());
    }

    @Override
    public List<ServiceInstance> getAllInstances() {
        return instances.stream().toList();
    }
}
