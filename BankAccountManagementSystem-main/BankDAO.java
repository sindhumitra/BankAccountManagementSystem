import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// DATA ACCESS OBJECT (DAO): All SQL operations, stored procedures, and triggers are executed here
public class BankDAO {

    // 1. Add Customer (Prepared Statement)
    public int addCustomer(Customer customer) throws SQLException {
        String sql = "INSERT INTO customer (name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getAddress());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // 2. Add Branch (Prepared Statement)
    public int addBranch(Branch branch) throws SQLException {
        String sql = "INSERT INTO branch (branch_name, ifsc_code, city) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, branch.getBranchName());
            pstmt.setString(2, branch.getIfscCode());
            pstmt.setString(3, branch.getCity());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // 3. Open Account (Prepared Statement)
    public int openAccount(Account account) throws SQLException {
        String sql = "INSERT INTO account (customer_id, branch_id, account_type, balance, opened_on) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, account.getCustomerId());
            pstmt.setInt(2, account.getBranchId());
            pstmt.setString(3, account.getType());
            pstmt.setDouble(4, account.getBalance());
            pstmt.setDate(5, Date.valueOf(account.getOpenedOn()));
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int accNo = rs.getInt(1);
                    // Initial transaction seed entry
                    if (account.getBalance() > 0) {
                        String logSql = "INSERT INTO transactions (account_no, txn_type, amount, balance_after) VALUES (?, 'DEPOSIT', ?, ?)";
                        try (PreparedStatement logPstmt = conn.prepareStatement(logSql)) {
                            logPstmt.setInt(1, accNo);
                            logPstmt.setDouble(2, account.getBalance());
                            logPstmt.setDouble(3, account.getBalance());
                            logPstmt.executeUpdate();
                        }
                    }
                    return accNo;
                }
            }
        }
        return -1;
    }

    // Fetch Account helper (Polymorphism instantiation)
   public Account getAccount(int accountNo) throws SQLException {
        String sql = "SELECT account_no, customer_id, branch_id, account_type, balance, opened_on FROM account WHERE account_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int accNo = rs.getInt("account_no");
                    int custId = rs.getInt("customer_id");
                    int branchId = rs.getInt("branch_id");
                    String type = rs.getString("account_type");
                    double bal = rs.getDouble("balance");
                    LocalDate date = rs.getDate("opened_on").toLocalDate();

                    // Instantiating polymorphic concrete objects
                    if ("SAVINGS".equalsIgnoreCase(type)) {
                        return new SavingsAccount(accNo, custId, branchId, bal, date);
                    } else {
                        return new CurrentAccount(accNo, custId, branchId, bal, date);
                    }
                }
            }
        }
        return null;
    }

    // 4. Deposit (Uses OOP deposit + DB Trigger trg_log_transaction)
    public boolean deposit(int accountNo, double amount) throws SQLException {
        Account acc = getAccount(accountNo);
        if (acc == null) return false;

        // Perform balance update in OOP model
        acc.deposit(amount);

        // Update database balance (Trigger trg_log_transaction automatically inserts into transactions table)
        String sql = "UPDATE account SET balance = ? WHERE account_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, acc.getBalance());
            pstmt.setInt(2, accountNo);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 5. Withdraw (POLYMORPHISM: withdraw logic enforced by subclass + DB Trigger trg_log_transaction)
    public boolean withdraw(int accountNo, double amount) throws SQLException, InsufficientFundsException {
        Account acc = getAccount(accountNo);
        if (acc == null) {
            throw new IllegalArgumentException("Account #" + accountNo + " does not exist.");
        }

        // POLYMORPHISM: Call withdraw on Account reference; Savings or Current rules execute dynamically
        acc.withdraw(amount);

        // Update DB balance (Trigger trg_log_transaction handles transaction ledger entry)
        String sql = "UPDATE account SET balance = ? WHERE account_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, acc.getBalance());
            pstmt.setInt(2, accountNo);
            return pstmt.executeUpdate() > 0;
        }
    }

    // 6. Transfer Funds (STORED PROCEDURE CALL: sp_transfer_funds)
    public String transferFunds(int fromAcc, int toAcc, double amount) throws SQLException {
        String sql = "{CALL sp_transfer_funds(?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, fromAcc);
            cstmt.setInt(2, toAcc);
            cstmt.setDouble(3, amount);
            cstmt.registerOutParameter(4, Types.VARCHAR);

            cstmt.execute();
            return cstmt.getString(4);
        }
    }

    // 7. Mini Statement (STORED PROCEDURE CALL: sp_mini_statement)
    public List<Transaction> getMiniStatement(int accountNo) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        String sql = "{CALL sp_mini_statement(?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setInt(1, accountNo);
            try (ResultSet rs = cstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                        rs.getInt("txn_id"),
                        rs.getInt("account_no"),
                        rs.getString("txn_type"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("txn_date"),
                        rs.getDouble("balance_after")
                    ));
                }
            }
        }
        return list;
    }

    // 8. View Account Details (VIEW QUERY: customer_account_view JOIN query)
    public String getAccountDetailsView(int accountNo) throws SQLException {
        String sql = "SELECT * FROM customer_account_view WHERE account_no = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return String.format("""
                        ================ ACCOUNT DETAILS ================
                        Account No     : %d
                        Account Type   : %s
                        Current Balance: Rs. %.2f
                        Date Opened    : %s
                        "Customer ID    : %d
                        "Customer Name  : %s
                        "Phone          : %s
                        "Email          : %s
                        "Branch ID      : %d
                        "Branch Name    : %s
                        "IFSC Code      : %s
                        ==================================================""",
                        rs.getInt("account_no"),
                        rs.getString("account_type"),
                        rs.getDouble("balance"),
                        rs.getDate("opened_on"),
                        rs.getInt("customer_id"),
                        rs.getString("customer_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getInt("branch_id"),
                        rs.getString("branch_name"),
                        rs.getString("ifsc_code")
                    );
                }
            }
        }
        return null;
    }

    // 9. Apply Loan (DATABASE TRIGGER: trg_check_loan validates insert)
    public int applyLoan(Loan loan) throws SQLException {
        String sql = "INSERT INTO loan (customer_id, branch_id, loan_type, amount, interest_rate) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, loan.getCustomerId());
            pstmt.setInt(2, loan.getBranchId());
            pstmt.setString(3, loan.getLoanType());
            pstmt.setDouble(4, loan.getAmount());
            pstmt.setDouble(5, loan.getInterestRate());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // 10. View Loans of Customer (SUBQUERY / PREPARED STATEMENT)
    public List<Loan> getCustomerLoans(int customerId) throws SQLException {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT loan_id, customer_id, branch_id, loan_type, amount, interest_rate, status FROM loan WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Loan(
                        rs.getInt("loan_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("branch_id"),
                        rs.getString("loan_type"),
                        rs.getDouble("amount"),
                        rs.getDouble("interest_rate"),
                        rs.getString("status")
                    ));
                }
            }
        }
        return list;
    }

    // 11. Add Yearly Interest (POLYMORPHISM: calculateInterest() called on Account reference)
    public double applyYearlyInterest(int accountNo) throws SQLException {
        Account acc = getAccount(accountNo);
        if (acc == null) return -1.0;

        // POLYMORPHISM: Dynamic interest calculation (4% for Savings, 0% for Current)
        double interest = acc.calculateInterest();
        if (interest > 0) {
            String setVarSql = "SET @txn_type = 'INTEREST'";
            String updateSql = "UPDATE account SET balance = balance + ? WHERE account_no = ?";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                stmt.execute(setVarSql);
                pstmt.setDouble(1, interest);
                pstmt.setInt(2, accountNo);
                pstmt.executeUpdate();
            }
        }
        return interest;
    }

    // 12. Branch Report (GROUP BY & AGGREGATE QUERY)
    public List<String> getBranchReport() throws SQLException {
        List<String> report = new ArrayList<>();
        String sql = "SELECT b.branch_id, b.branch_name, COUNT(a.account_no) AS total_accounts, COALESCE(SUM(a.balance), 0.00) AS total_balance " +
                     "FROM branch b LEFT JOIN account a ON b.branch_id = a.branch_id " +
                     "GROUP BY b.branch_id, b.branch_name " +
                     "ORDER BY b.branch_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                report.add(String.format("Branch ID: %d | Name: %-15s | Total Accounts: %d | Total Balance: Rs. %.2f",
                        rs.getInt("branch_id"),
                        rs.getString("branch_name"),
                        rs.getInt("total_accounts"),
                        rs.getDouble("total_balance")));
            }
        }
        return report;
    }
}
