package rockinbvv;

import java.util.List;

public interface BalanceStrategy {

    BalanceType getType();

    ServiceInstance selectInstance(List<ServiceInstance> instances);
}
