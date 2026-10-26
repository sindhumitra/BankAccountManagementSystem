// ENCAPSULATION: Domain model representing a customer
public class Customer {
    private int customerId;
    private String name;
    private String phone;
    private String email;
    private String address;

    public Customer(int customerId, String name, String phone, String email, String address) {
        this.customerId = customerId;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    public Customer(String name, String phone, String email, String address) {
        this(0, name, phone, email, address);
    }

    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }

    @Override
    public String toString() {
        return "Customer #" + customerId + " | Name: " + name + " | Phone: " + phone + " | Email: " + email + " | Address: " + address;
    }
}
