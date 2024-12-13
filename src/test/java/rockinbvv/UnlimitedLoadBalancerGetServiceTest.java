package rockinbvv;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class UnlimitedLoadBalancerGetServiceTest {

    UnlimitedLoadBalancer loadBalancer;

    @BeforeEach
    void beforeEach() {
        loadBalancer = new UnlimitedLoadBalancer();
    }

    //supposed to be unstable on low iterations
    @Test
    void test_getService_Random() throws InterruptedException, ExecutionException {
        loadBalancer.register(new ServiceInstance(1L, "service 1"));
        loadBalancer.register(new ServiceInstance(2L, "service 2"));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);
        CompletableFuture<List<ServiceInstance>> t1ResultFuture = new CompletableFuture<>();
        CompletableFuture<List<ServiceInstance>> t2ResultFuture = new CompletableFuture<>();

        new ServiceGetThread("t1", startLatch, endLatch, 10, BalanceType.RANDOM, loadBalancer, t1ResultFuture).start();
        new ServiceGetThread("t2", startLatch, endLatch, 10, BalanceType.RANDOM, loadBalancer, t2ResultFuture).start();
        startLatch.countDown();

        endLatch.await();
        Assertions.assertThat(
                        t1ResultFuture.get().stream()
                                .collect(Collectors.groupingBy(ServiceInstance::getId))
                                .entrySet()
                )
                .as("Thread 1 should get more than 1 unique instance")
                .hasSizeGreaterThan(1);

        Assertions.assertThat(
                        t2ResultFuture.get().stream()
                                .collect(Collectors.groupingBy(ServiceInstance::getId))
                                .entrySet()
                )
                .as("Thread 2 should get more than 1 unique instance")
                .hasSizeGreaterThan(1);
    }

    @Test
    void test_getService_RoundRobin() throws InterruptedException, ExecutionException {
        loadBalancer.register(new ServiceInstance(1L, "service 1"));
        loadBalancer.register(new ServiceInstance(2L, "service 2"));
        loadBalancer.register(new ServiceInstance(3L, "service 3"));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(1);
        CompletableFuture<List<ServiceInstance>> t1ResultFuture = new CompletableFuture<>();
        CompletableFuture<List<ServiceInstance>> t2ResultFuture = new CompletableFuture<>();
        new ServiceGetThread("t1", startLatch, endLatch, 3, BalanceType.ROUND_ROBIN, loadBalancer, t1ResultFuture).start();
        new ServiceGetThread("t2", startLatch, endLatch, 2, BalanceType.ROUND_ROBIN, loadBalancer, t2ResultFuture).start();
        startLatch.countDown();

        endLatch.await();

        Assertions.assertWith(
                Stream.concat(
                                t1ResultFuture.get().stream(),
                                t2ResultFuture.get().stream()
                        )
                        .collect(Collectors.toMap(
                                ServiceInstance::getId,
                                i -> 1,
                                Math::addExact
                        )),
                countById -> {
                    Assertions.assertThat(countById).hasSize(3);
                    Assertions.assertThat(countById.get(1L)).isEqualTo(2);
                    Assertions.assertThat(countById.get(2L)).isEqualTo(2);
                    Assertions.assertThat(countById.get(3L)).isEqualTo(1);
                }
        );
    }

    public static class ServiceGetThread extends Thread {
        private final CountDownLatch startLatch;
        private final CountDownLatch endLatch;
        private final int iterationCount;
        private final BalanceType balanceType;
        private final UnlimitedLoadBalancer loadBalancer;
        private final CompletableFuture<List<ServiceInstance>> resultFuture;

        public ServiceGetThread(String name, CountDownLatch startLatch, CountDownLatch endLatch, int iterationCount, BalanceType balanceType, UnlimitedLoadBalancer loadBalancer, CompletableFuture<List<ServiceInstance>> resultFuture) {
            super(name);
            this.startLatch = startLatch;
            this.endLatch = endLatch;
            this.iterationCount = iterationCount;
            this.balanceType = balanceType;
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
                instances.add(loadBalancer.getService(balanceType));
            }
            resultFuture.complete(instances);
            endLatch.countDown();
        }
    }
}
