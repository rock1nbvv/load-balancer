package rockinbvv;

import java.util.function.Supplier;

public enum BalanceType {
    ROUND_ROBIN(RoundRobinBalanceStrategy::new),
    RANDOM(RandomBalanceStrategy::new);

    private final Supplier<BalanceStrategy> balanceStrategy;

    BalanceType(Supplier<BalanceStrategy> balanceStrategy) {
        this.balanceStrategy = balanceStrategy;
    }

    public BalanceStrategy getStrategy() {
        return balanceStrategy.get();
    }
}
