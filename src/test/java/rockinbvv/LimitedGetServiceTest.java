package rockinbvv;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class LimitedGetServiceTest {

    LimitedLoadBalancer loadBalancer;

    @Test
    void getInstanceRandomTest() throws Exception {
        /*
        Supposed to be unstable on low iterations as we are basically testing random.
        Possible solution is to seed SecureRandom.
        This particular config passed on 10k times
         */

        RandomBalanceStrategy randomBalanceStrategy = new RandomBalanceStrategy();
        //seed random to increase test stability
        ReflectionUtils.setBalanceStrategyFiled(randomBalanceStrategy, "secureRandom", new SecureRandom(SecureRandom.getSeed(123)));

        loadBalancer = new LimitedLoadBalancer(10, randomBalanceStrategy);

        int iterationCount = 15;

        loadBalancer.register(new ServiceInstance("service 1"));
        loadBalancer.register(new ServiceInstance("service 2"));

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);
        CompletableFuture<List<ServiceInstance>> t1ResultFuture = new CompletableFuture<>();
        CompletableFuture<List<ServiceInstance>> t2ResultFuture = new CompletableFuture<>();

        new ServiceGetThread("t1", startLatch, endLatch, iterationCount, loadBalancer, t1ResultFuture).start();
        new ServiceGetThread("t2", startLatch, endLatch, iterationCount, loadBalancer, t2ResultFuture).start();
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

    @Test
    void getInstanceRoundRobinTest() throws InterruptedException, ExecutionException {
        loadBalancer = new LimitedLoadBalancer(10, new RoundRobinBalanceStrategy());
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
                countByAddress -> {
                    Assertions.assertThat(countByAddress).hasSize(3);
                    Assertions.assertThat(countByAddress.get("service 1")).isEqualTo(2);
                    Assertions.assertThat(countByAddress.get("service 2")).isEqualTo(2);
                    Assertions.assertThat(countByAddress.get("service 3")).isEqualTo(1);
                }
        );
    }

    public static class ServiceGetThread extends Thread {
        private final CountDownLatch startLatch;
        private final CountDownLatch endLatch;
        private final int iterationCount;
        private final LimitedLoadBalancer loadBalancer;
        private final CompletableFuture<List<ServiceInstance>> resultFuture;

        public ServiceGetThread(String name, CountDownLatch startLatch, CountDownLatch endLatch, int iterationCount, LimitedLoadBalancer loadBalancer, CompletableFuture<List<ServiceInstance>> resultFuture) {
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
