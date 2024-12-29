package rockinbvv;

import java.util.List;

public interface BalanceStrategy {

    ServiceInstance selectInstance(List<ServiceInstance> instances);
}
