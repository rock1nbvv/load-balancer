package rockinbvv.balancer;

import rockinbvv.ServiceInstance;
import rockinbvv.strategy.BalanceStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class UnlimitedLoadBalancer implements LoadBalancer {

    private final ConcurrentHashMap<String, ServiceInstance> instances = new ConcurrentHashMap<>();

    private final BalanceStrategy balanceStrategy;

    public UnlimitedLoadBalancer(BalanceStrategy balanceStrategy) {
        this.balanceStrategy = balanceStrategy;
    }

    @Override
    public boolean register(ServiceInstance instance) {
        if (instances.contains(instance)) {
            return false;
        }
        instances.put(instance.getAddress(), instance);
        return true;
    }

    @Override
    public ServiceInstance getInstance() {
        return balanceStrategy.selectInstance(instances.values().stream().sorted(Comparator.comparing(ServiceInstance::getAddress)).toList());
    }

    @Override
    public List<ServiceInstance> getAllInstances() {
        return instances.values().stream().sorted(Comparator.comparing(ServiceInstance::getAddress)).toList();
    }
}
