package rockinbvv;


import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * I have picked CopyOnWriteArrayList as for this task we should focus on slow writes since services are not supposed
 * to register to often and fast reads to get them fast and often
 */
public class LimitedLoadBalancer {

    private final int serviceLimit;
    private final BalanceStrategy balanceStrategy;

    public LimitedLoadBalancer(int serviceLimit, BalanceStrategy balanceStrategy) {
        this.serviceLimit = serviceLimit;
        this.balanceStrategy = balanceStrategy;
    }

    private final List<ServiceInstance> instances = new CopyOnWriteArrayList<>();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock r = readWriteLock.readLock();
    private final Lock w = readWriteLock.writeLock();

    public boolean register(ServiceInstance instance) {
        w.lock();
        try {
            if (instances.size() >= serviceLimit || instances.contains(instance)) {
                return false;
            }
            return instances.add(instance);
        } finally {
            w.unlock();
        }
    }

    public ServiceInstance getInstance() {
        r.lock();
        try {
            return balanceStrategy.selectInstance(Collections.unmodifiableList(instances));
        } finally {
            r.unlock();
        }
    }

    public List<ServiceInstance> getAllInstances() {
        r.lock();
        try {
            return instances.stream().toList();
        } finally {
            r.unlock();
        }
    }
}
