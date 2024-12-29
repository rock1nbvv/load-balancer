package rockinbvv;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class LimitedRegisterTest {

    @Test
    public void registerDuplicateTest() {
        LimitedLoadBalancer loadBalancer = new LimitedLoadBalancer(10, new RoundRobinBalanceStrategy());
        ServiceInstance testInstance = new ServiceInstance(1L, "0.0.0.0");

        assertThat(loadBalancer.register(testInstance)).isEqualTo(true);
        assertThat(loadBalancer.register(testInstance)).isEqualTo(false);
        assertThat(loadBalancer.getAllInstances()).hasSize(1);
    }

    @Test
    public void registerTest() throws Exception {
        int threadCount = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        LimitedLoadBalancer loadBalancer = new LimitedLoadBalancer(10, new RoundRobinBalanceStrategy());

        IntStream.range(0, threadCount)
                .mapToObj(i -> new RegisterThread("t" + i, 20, loadBalancer, startLatch, endLatch))
                .forEach(Thread::start);

        startLatch.countDown();
        endLatch.await();

        assertThat(loadBalancer.getAllInstances()).hasSize(10);
    }

    public static class RegisterThread extends Thread {

        private static final AtomicLong instanceId = new AtomicLong(1);

        private final int registrationCount;
        private final LimitedLoadBalancer loadBalancer;
        private final CountDownLatch startLatch;
        private final CountDownLatch endLatch;

        public RegisterThread(String name, int registrationCount, LimitedLoadBalancer loadBalancer, CountDownLatch startLatch, CountDownLatch endLatch) {
            super(name);
            this.registrationCount = registrationCount;
            this.loadBalancer = loadBalancer;
            this.startLatch = startLatch;
            this.endLatch = endLatch;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            try {
                for (int i = 0; i < registrationCount; i++) {
                    if (interrupted()) {
                        return;
                    }
                    try {
                        loadBalancer.register(new ServiceInstance(instanceId.getAndIncrement(), "service" + " " + i + " " + getName()));
                    } catch (Exception e) {
                        //nothing to do
                    }
                }
            } finally {
                endLatch.countDown();
            }
        }
    }
}
