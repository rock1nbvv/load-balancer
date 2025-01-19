package rockinbvv;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import rockinbvv.balancer.UnlimitedLoadBalancer;
import rockinbvv.strategy.BalanceType;
import rockinbvv.strategy.RandomBalanceStrategy;
import rockinbvv.strategy.StrategyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UnlimitedGetServiceTest {

    UnlimitedLoadBalancer loadBalancer;

    @Test
    void getInstanceRandomTest() throws Exception {
        List<ServiceInstance> serviceInstances = List.of(new ServiceInstance("service 1"), new ServiceInstance("service 2"));

        StrategyFactory strategyFactory = mockStrategyFactory(serviceInstances);

        loadBalancer = new UnlimitedLoadBalancer(strategyFactory.createStrategy());

        loadBalancer.register(serviceInstances.get(0));
        loadBalancer.register(serviceInstances.get(1));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);
        CompletableFuture<List<ServiceInstance>> t1ResultFuture = new CompletableFuture<>();
        CompletableFuture<List<ServiceInstance>> t2ResultFuture = new CompletableFuture<>();

        new ServiceGetThread("t1", startLatch, endLatch, 3, loadBalancer, t1ResultFuture).start();
        new ServiceGetThread("t2", startLatch, endLatch, 3, loadBalancer, t2ResultFuture).start();
        startLatch.countDown();

        endLatch.await();
        Assertions.assertThat(
                        t1ResultFuture.get().stream()
                                .collect(Collectors.groupingBy(ServiceInstance::getAddress))
                                .entrySet()
                )
                .as("Thread 1 should get more than 1 unique instance")
                .hasSizeGreaterThan(1);

        Assertions.assertThat(
                        t2ResultFuture.get().stream()
                                .collect(Collectors.groupingBy(ServiceInstance::getAddress))
                                .entrySet()
                )
                .as("Thread 2 should get more than 1 unique instance")
                .hasSizeGreaterThan(1);
    }

    private StrategyFactory mockStrategyFactory(List<ServiceInstance> serviceInstances) {
        AtomicInteger t1Counter = new AtomicInteger();
        AtomicInteger t2Counter = new AtomicInteger(1);

        RandomBalanceStrategy randomBalanceStrategy = mock(RandomBalanceStrategy.class);
        when(randomBalanceStrategy.selectInstance(anyList())).then(invocation -> {
            if (Thread.currentThread().getName().equals("t1")) {
                return serviceInstances.get(t1Counter.getAndIncrement() % 2);
            } else {
                return serviceInstances.get(t2Counter.getAndIncrement() % 2);
            }
        });

        StrategyFactory strategyFactory = mock(StrategyFactory.class);
        when(strategyFactory.createStrategy()).thenReturn(
                randomBalanceStrategy
        );
        return strategyFactory;
    }

    @Test
    void getInstanceRoundRobinTest() throws InterruptedException, ExecutionException {
        loadBalancer = new UnlimitedLoadBalancer(BalanceType.ROUND_ROBIN.createStrategy());
        loadBalancer.register(new ServiceInstance("service 1"));
        loadBalancer.register(new ServiceInstance("service 2"));
        loadBalancer.register(new ServiceInstance("service 3"));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(1);
        CompletableFuture<List<ServiceInstance>> t1ResultFuture = new CompletableFuture<>();
        CompletableFuture<List<ServiceInstance>> t2ResultFuture = new CompletableFuture<>();
        new ServiceGetThread("t1", startLatch, endLatch, 3, loadBalancer, t1ResultFuture).start();
        new ServiceGetThread("t2", startLatch, endLatch, 2, loadBalancer, t2ResultFuture).start();
        startLatch.countDown();

        endLatch.await();

        Assertions.assertWith(
                Stream.concat(
                                t1ResultFuture.get().stream(),
                                t2ResultFuture.get().stream()
                        )
                        .collect(Collectors.toMap(
                                ServiceInstance::getAddress,
                                i -> 1,
                                Math::addExact
                        )),
                countById -> {
                    Assertions.assertThat(countById).hasSize(3);
                    Assertions.assertThat(countById.get("service 1")).isEqualTo(2);
                    Assertions.assertThat(countById.get("service 2")).isEqualTo(2);
                    Assertions.assertThat(countById.get("service 3")).isEqualTo(1);
                }
        );
    }

    public static class ServiceGetThread extends Thread {
        private final CountDownLatch startLatch;
        private final CountDownLatch endLatch;
        private final int iterationCount;
        private final UnlimitedLoadBalancer loadBalancer;
        private final CompletableFuture<List<ServiceInstance>> resultFuture;

        public ServiceGetThread(String name, CountDownLatch startLatch, CountDownLatch endLatch, int iterationCount, UnlimitedLoadBalancer loadBalancer, CompletableFuture<List<ServiceInstance>> resultFuture) {
            super(name);
            this.startLatch = startLatch;
            this.endLatch = endLatch;
            this.iterationCount = iterationCount;
            this.loadBalancer = loadBalancer;
            this.resultFuture = resultFuture;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            List<ServiceInstance> instances = new ArrayList<>();
            for (int i = 0; i < iterationCount; i++) {
                instances.add(loadBalancer.getInstance());
            }
            resultFuture.complete(instances);
            endLatch.countDown();
        }
    }
}
