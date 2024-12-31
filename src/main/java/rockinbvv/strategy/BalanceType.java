package rockinbvv.strategy;

import java.security.SecureRandom;

public enum BalanceType implements StrategyFactory {
    ROUND_ROBIN {
        @Override
        public BalanceStrategy createStrategy() {
            return new RoundRobinBalanceStrategy();
        }
    },
    RANDOM {
        @Override
        public BalanceStrategy createStrategy() {
            return new RandomBalanceStrategy(new SecureRandom());
        }
    }
}
