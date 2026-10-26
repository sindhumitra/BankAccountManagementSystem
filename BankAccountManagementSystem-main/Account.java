import java.time.LocalDate;

// ABSTRACTION: Abstract base class representing a bank account
public abstract class Account {
    // ENCAPSULATION: Private fields accessible only through getters
    private final int accountNo;
    private final int customerId;
    private final int branchId;
    private double balance;
    private final LocalDate openedOn;

    public Account(int accountNo, int customerId, int branchId, double balance, LocalDate openedOn) {
        this.accountNo = accountNo;
        this.customerId = customerId;
        this.branchId = branchId;
        this.balance = balance;
        this.openedOn = openedOn;
    }

    // Getters for encapsulation
    public int getAccountNo() {
        return accountNo;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getBranchId() {
        return branchId;
    }

    public double getBalance() {
        return balance;
    }

    public LocalDate getOpenedOn() {
        return openedOn;
    }

    protected void setBalance(double balance) {
        this.balance = balance;
    }

    // Common concrete method for deposit
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }
        this.balance += amount;
    }

    // ABSTRACTION: Abstract methods to be implemented by concrete subclasses
    // POLYMORPHISM: Dynamic dispatch enforced in SavingsAccount and CurrentAccount
    public abstract void withdraw(double amount) throws InsufficientFundsException;
    public abstract double calculateInterest();
    public abstract String getType();
}
