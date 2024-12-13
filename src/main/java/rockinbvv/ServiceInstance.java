package rockinbvv;


public class ServiceInstance {

    Long id;
    String address;

    public ServiceInstance(Long id, String address) {
        this.id = id;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }
}
