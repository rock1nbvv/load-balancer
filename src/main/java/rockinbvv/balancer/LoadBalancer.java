package rockinbvv.balancer;

import rockinbvv.ServiceInstance;

import java.util.List;

public interface LoadBalancer {

    boolean register(ServiceInstance instance);

    ServiceInstance getInstance();

    List<ServiceInstance> getAllInstances();
}
