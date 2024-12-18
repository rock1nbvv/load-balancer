package rockinbvv;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class UnlimitedLoadBalancerRegisterTest {

    @Test
    public void test_register_duplicate() {
        ServiceInstance testInstance = new ServiceInstance(1L, "0.0.0.0");
        UnlimitedLoadBalancer loadBalancer = new UnlimitedLoadBalancer();

        assertThat(loadBalancer.register(testInstance)).isEqualTo(true);
        assertThat(loadBalancer.register(testInstance)).isEqualTo(false);
        assertThat(loadBalancer.getAllInstances()).hasSize(1);
    }

    @Test
    public void test_register_concurrent() throws Exception {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        UnlimitedLoadBalancer loadBalancer = new UnlimitedLoadBalancer();

        new RegisterThread("t1", loadBalancer, startLatch, endLatch).start();
        new RegisterThread("t2", loadBalancer, startLatch, endLatch).start();

        startLatch.countDown();
        endLatch.await();

        assertThat(loadBalancer.getAllInstances()).hasSize(20);
    }

    public static class RegisterThread extends Thread {

        private static final AtomicLong instanceId = new AtomicLong(1);

        private final UnlimitedLoadBalancer loadBalancer;
        private final CountDownLatch startLatch;
        private final CountDownLatch endLatch;

        public RegisterThread(String name, UnlimitedLoadBalancer loadBalancer, CountDownLatch startLatch, CountDownLatch endLatch) {
            super(name);
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
                for (int i = 0; i < 10; i++) {
                    if (interrupted()) {
                        return;
                    }
                    loadBalancer.register(new ServiceInstance(instanceId.getAndIncrement(), "service" + i + " " + getName()));
                }
            } finally {
                endLatch.countDown();
            }
        }
    }
}
