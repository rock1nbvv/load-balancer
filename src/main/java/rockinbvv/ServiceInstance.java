package rockinbvv;

/**
 * Represents instances that are registered in load balancer<br>
 * <b>address</b> - represents unique identifier of specific instance (e.g. 111.22.33.44)<br>
 * So that hashCode()/equals() contract fully relies on comparing address fields of instances
 */
public class ServiceInstance {

    @Deprecated
    private final Long id;
    private final String address;

    public ServiceInstance(Long id, String address) {
        this.id = id;
        this.address = address;
    }

    public Long getId() {
        return this.id;
    }

    public String getAddress() {
        return this.address;
    }

    @Override
    public int hashCode() {
        return this.address.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ServiceInstance comparedWith) {
            return comparedWith.getAddress().equals(this.address);
        }
        return false;
    }
}
