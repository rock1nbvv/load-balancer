package rockinbvv.strategy;

import rockinbvv.ServiceInstance;

import java.util.List;

public interface BalanceStrategy {

    ServiceInstance selectInstance(List<ServiceInstance> instances);
}
