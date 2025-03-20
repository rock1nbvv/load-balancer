# Load Balancer

A small concurrent Load Balancer that can operate in two modes (limited by instance amount and unlimited) and supports
two balancing strategies: Random and Round-Robin.

## Key Concepts

1. **Load Balancer Functionalities**:
    - Register a service instance.
    - Return a specific instance according to the requested balance strategy.
    - Return all registered instances.

2. **Balancing Strategies**:
    - **Round-Robin**: Returns instances by looping through them in a sequential manner.
    - **Random**: Returns instances based on a uniform random distribution.

4. **Testing**:
    - Junit tests are included to ensure correctness of the functionality the load balancer and strategies.
