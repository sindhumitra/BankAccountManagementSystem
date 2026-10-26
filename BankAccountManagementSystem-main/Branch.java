// ENCAPSULATION: Domain model representing a bank branch
public class Branch {
    private int branchId;
    private String branchName;
    private String ifscCode;
    private String city;

    public Branch(int branchId, String branchName, String ifscCode, String city) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.ifscCode = ifscCode;
        this.city = city;
    }

    public Branch(String branchName, String ifscCode, String city) {
        this(0, branchName, ifscCode, city);
    }

    public int getBranchId() { return branchId; }
    public String getBranchName() { return branchName; }
    public String getIfscCode() { return ifscCode; }
    public String getCity() { return city; }

    @Override
    public String toString() {
        return "Branch #" + branchId + " | Name: " + branchName + " | IFSC: " + ifscCode + " | City: " + city;
    }
}
