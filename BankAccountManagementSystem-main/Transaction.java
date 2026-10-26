import java.sql.Timestamp;

// ENCAPSULATION: Domain model representing a financial transaction
public class Transaction {
    private int txnId;
    private int accountNo;
    private String txnType;
    private double amount;
    private Timestamp txnDate;
    private double balanceAfter;

    public Transaction(int txnId, int accountNo, String txnType, double amount, Timestamp txnDate, double balanceAfter) {
        this.txnId = txnId;
        this.accountNo = accountNo;
        this.txnType = txnType;
        this.amount = amount;
        this.txnDate = txnDate;
        this.balanceAfter = balanceAfter;
    }

    public int getTxnId() { return txnId; }
    public int getAccountNo() { return accountNo; }
    public String getTxnType() { return txnType; }
    public double getAmount() { return amount; }
    public Timestamp getTxnDate() { return txnDate; }
    public double getBalanceAfter() { return balanceAfter; }

    @Override
    public String toString() {
        return String.format("Txn #%d | Acc: %d | Type: %-12s | Amount: Rs. %10.2f | Date: %s | Bal After: Rs. %10.2f",
                txnId, accountNo, txnType, amount, txnDate, balanceAfter);
    }
}
