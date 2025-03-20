package rockinbvv.balancer;


import rockinbvv.ServiceInstance;
import rockinbvv.strategy.BalanceStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * I have picked CopyOnWriteArrayList as for this task we should focus on slow writes since services are not supposed
 * to register to often and fast reads to get them fast and often
 */
public class LimitedLoadBalancer implements LoadBalancer {

    private final int serviceLimit;
    private final BalanceStrategy balanceStrategy;

    /**
     * Constructs a LimitedLoadBalancer with the given service limit and balance strategy.
     *
     * @param serviceLimit    the maximum number of service instances that can be registered
     * @param balanceStrategy the strategy used to select an instance
     */
    public LimitedLoadBalancer(int serviceLimit, BalanceStrategy balanceStrategy) {
        this.serviceLimit = serviceLimit;
        this.balanceStrategy = balanceStrategy;
    }

    private final ConcurrentHashMap<String, ServiceInstance> instances = new ConcurrentHashMap<>();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();

    /**
     * Registers a service instance if the limit has not been reached and the instance is not already registered.
     *
     * @param instance the service instance to register
     * @return true if the instance was successfully registered, false otherwise
     */
    @Override
    public boolean register(ServiceInstance instance) {
        writeLock.lock();
        try {
            if (instances.size() >= serviceLimit || instances.contains(instance)) {
                return false;
            }
            instances.put(instance.getAddress(), instance);
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Retrieves an instance using the balance strategy.
     *
     * @return the selected service instance
     */
    @Override
    public ServiceInstance getInstance() {
        readLock.lock();
        try {
            return balanceStrategy.selectInstance(instances.values().stream().sorted(Comparator.comparing(ServiceInstance::getAddress)).toList());
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a list of all registered service instances.
     *
     * @return a list of all service instances
     */
    @Override
    public List<ServiceInstance> getAllInstances() {
        readLock.lock();
        try {
            return instances.values().stream().sorted(Comparator.comparing(ServiceInstance::getAddress)).toList();
        } finally {
            readLock.unlock();
        }
    }
}
