Small LoadBalancer that can work in two modes(limited by instance amount and unlimited) and two balancing strategies(
random and round-robin).

# Key concepts

1. Load Balancer can register instance, return specific instance according to balance strategy requested, return all
   registered instances
2. Round-Robin balance strategy should return instances "looping" through them
3. Random balance strategy complies uniform distribution
4. Each component is able to work in concurrent environment
5. Tests included

# TODO

1. Cover with tests the following behaviour: LimitedLB + round-robin gets new instance during getting existing ones.
2. For the sake of encapsulation it seems that balancing strategy should be immutable for specific LB instance.
3. Seed SecureRandom in tests to increase test stability
