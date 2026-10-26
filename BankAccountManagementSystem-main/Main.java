import java.sql.SQLException;
import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

// FRONT END USER INTERFACE: Menu-driven console application
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final BankDAO bankDAO = new BankDAO();

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   WELCOME TO BANK ACCOUNT MANAGEMENT SYSTEM    ");
        System.out.println("=================================================");

        while (true) {
            printMenu();
            System.out.print("Enter your choice (0-12): ");
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> addCustomer();
                    case 2 -> addBranch();
                    case 3 -> openAccount();
                    case 4 -> performDeposit();
                    case 5 -> performWithdrawal();
                    case 6 -> performTransfer();
                    case 7 -> viewMiniStatement();
                    case 8 -> viewAccountDetails();
                    case 9 -> applyForLoan();
                    case 10 -> viewCustomerLoans();
                    case 11 -> addYearlyInterest();
                    case 12 -> viewBranchReport();
                    case 0 -> {
                        System.out.println("\nThank you for using Bank Account Management System. Goodbye!");
                        return;
                    }
                    default -> System.out.println("\n[ERROR] Invalid option. Please select a number between 0 and 12.");
                }
            } catch (InputMismatchException e) {
                System.out.println("\n[ERROR] Invalid input type. Please enter a valid numeric choice.");
                scanner.nextLine(); // Clear invalid token
            } catch (Exception e) {
                System.out.println("\n[ERROR] An unexpected error occurred: " + e.getMessage());
            }

            System.out.println("\nPress Enter to return to main menu...");
            scanner.nextLine();
        }
    }

    private static void printMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1.  Add Customer");
        System.out.println("2.  Add Branch");
        System.out.println("3.  Open Account (Savings / Current)");
        System.out.println("4.  Deposit");
        System.out.println("5.  Withdraw");
        System.out.println("6.  Transfer Funds (Stored Procedure)");
        System.out.println("7.  Mini Statement (Stored Procedure)");
        System.out.println("8.  View Account Details (View / Join Query)");
        System.out.println("9.  Apply for Loan (Trigger Validation)");
        System.out.println("10. View Customer Loans");
        System.out.println("11. Add Yearly Interest (Polymorphism)");
        System.out.println("12. Branch Report (Group By Aggregate Query)");
        System.out.println("0.  Exit");
        System.out.println("-------------------------------------------");
    }

    // Feature 1: Add Customer
    private static void addCustomer() {
        System.out.println("\n--- ADD NEW CUSTOMER ---");
        System.out.print("Enter Customer Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter Phone Number: ");
        String phone = scanner.nextLine().trim();
        System.out.print("Enter Email Address: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter Address: ");
        String address = scanner.nextLine().trim();

        if (name.isEmpty() || phone.isEmpty()) {
            System.out.println("[ERROR] Name and Phone Number are required fields.");
            return;
        }

        try {
            Customer cust = new Customer(name, phone, email, address);
            int id = bankDAO.addCustomer(cust);
            if (id > 0) {
                System.out.println("[SUCCESS] Customer registered successfully! Generated Customer ID: " + id);
            } else {
                System.out.println("[ERROR] Failed to register customer.");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("[ERROR] Phone number '" + phone + "' is already registered with another customer.");
            } else {
                System.out.println("[DATABASE ERROR] " + e.getMessage());
            }
        }
    }

    // Feature 2: Add Branch
    private static void addBranch() {
        System.out.println("\n--- ADD NEW BRANCH ---");
        System.out.print("Enter Branch Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter IFSC Code: ");
        String ifsc = scanner.nextLine().trim();
        System.out.print("Enter City: ");
        String city = scanner.nextLine().trim();

        if (name.isEmpty() || ifsc.isEmpty()) {
            System.out.println("[ERROR] Branch Name and IFSC Code are required.");
            return;
        }

        try {
            Branch branch = new Branch(name, ifsc, city);
            int id = bankDAO.addBranch(branch);
            if (id > 0) {
                System.out.println("[SUCCESS] Branch registered successfully! Generated Branch ID: " + id);
            } else {
                System.out.println("[ERROR] Failed to register branch.");
            }
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.out.println("[ERROR] IFSC Code '" + ifsc + "' is already assigned to another branch.");
            } else {
                System.out.println("[DATABASE ERROR] " + e.getMessage());
            }
        }
    }

    // Feature 3: Open Account
    private static void openAccount() {
        System.out.println("\n--- OPEN NEW ACCOUNT ---");
        System.out.print("Enter Customer ID: ");
        int custId = scanner.nextInt();
        System.out.print("Enter Branch ID: ");
        int branchId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Account Type (1 for SAVINGS, 2 for CURRENT): ");
        int typeChoice = scanner.nextInt();
        System.out.print("Enter Opening Deposit Balance (Rs.): ");
        double initialBal = scanner.nextDouble();
        scanner.nextLine();

        if (initialBal < 0) {
            System.out.println("[ERROR] Opening deposit cannot be negative.");
            return;
        }

        String type = (typeChoice == 2) ? "CURRENT" : "SAVINGS";
        if ("SAVINGS".equals(type) && initialBal < 1000) {
            System.out.println("[ERROR] Savings Account opening deposit must be at least Rs. 1000.00.");
            return;
        }

        try {
            // POLYMORPHISM: Account reference instantiated with concrete subclass
            Account account;
            if ("SAVINGS".equals(type)) {
                account = new SavingsAccount(0, custId, branchId, initialBal, LocalDate.now());
            } else {
                account = new CurrentAccount(0, custId, branchId, initialBal, LocalDate.now());
            }

            int accNo = bankDAO.openAccount(account);
            if (accNo > 0) {
                System.out.println("[SUCCESS] " + type + " Account opened successfully! Generated Account No: " + accNo);
            } else {
                System.out.println("[ERROR] Account creation failed. Verify Customer ID and Branch ID.");
            }
        } catch (SQLException e) {
            System.out.println("[DATABASE ERROR] " + e.getMessage());
        }
    }

    // Feature 4: Deposit
    private static void performDeposit() {
        System.out.println("\n--- DEPOSIT FUNDS ---");
        System.out.print("Enter Account No: ");
        int accNo = scanner.nextInt();
        System.out.print("Enter Deposit Amount (Rs.): ");
        double amt = scanner.nextDouble();
        scanner.nextLine();

        if (amt <= 0) {
            System.out.println("[ERROR] Deposit amount must be positive.");
            return;
        }

        try {
            // BACKEND TRIGGER: trg_log_transaction automatically records transaction ledger row
            boolean ok = bankDAO.deposit(accNo, amt);
            if (ok) {
                System.out.println("[SUCCESS] Rs. " + amt + " deposited into Account #" + accNo + ". (Transaction logged via DB Trigger)");
            } else {
                System.out.println("[ERROR] Account #" + accNo + " not found.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("[INPUT ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("[DATABASE ERROR] " + e.getMessage());
        }
    }

    // Feature 5: Withdraw
    private static void performWithdrawal() {
        System.out.println("\n--- WITHDRAW FUNDS ---");
        System.out.print("Enter Account No: ");
        int accNo = scanner.nextInt();
        System.out.print("Enter Withdrawal Amount (Rs.): ");
        double amt = scanner.nextDouble();
        scanner.nextLine();

        if (amt <= 0) {
            System.out.println("[ERROR] Withdrawal amount must be positive.");
            return;
        }

        try {
            // POLYMORPHISM & CUSTOM EXCEPTION: Business logic verified on subclass, DB Trigger handles transaction row
            boolean ok = bankDAO.withdraw(accNo, amt);
            if (ok) {
                System.out.println("[SUCCESS] Rs. " + amt + " withdrawn from Account #" + accNo + ". (Transaction logged via DB Trigger)");
            }
        } catch (InsufficientFundsException e) {
            System.out.println("[INSUFFICIENT FUNDS ERROR] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("[INPUT ERROR] " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("[DATABASE ERROR] " + e.getMessage());
        }
    }

    // Feature 6: Transfer Funds (STORED PROCEDURE CALL)
    private static void performTransfer() {
        System.out.println("\n--- TRANSFER FUNDS ---");
        System.out.print("Enter Source Account No: ");
        int fromAcc = scanner.nextInt();
        System.out.print("Enter Target Account No: ");
        int toAcc = scanner.nextInt();
        System.out.print("Enter Transfer Amount (Rs.): ");
        double amt = scanner.nextDouble();
        scanner.nextLine();

        if (amt <= 0) {
            System.out.println("[ERROR] Transfer amount must be positive.");
            return;
        }

        try {
            // STORED PROCEDURE CALL: sp_transfer_funds handles transaction block
            String result = bankDAO.transferFunds(fromAcc, toAcc, amt);
            System.out.println("\n[RESULT] " + result);
        } catch (SQLException e) {
            System.out.println("[DATABASE ERROR] " + e.getMessage());
        }
    }

    // Feature 7: Mini Statement (STORED PROCEDURE CALL)
    private static void viewMiniStatement() {
        System.out.println("\n--- MINI STATEMENT ---");
        System.out.print("Enter Account No: ");
        int accNo = scanner.nextInt();
        scanner.nextLine();

        try {
            // STORED PROCEDURE CALL: sp_mini_statement fetches last 10 transactions
            List<Transaction> txns = bankDAO.getMiniStatement(accNo);
            if (txns.isEmpty()) {
                System.out.println("No transaction records found for Account #" + accNo);
            } else {
                System.out.println("\nLAST " + txns.size() + " TRANSACTIONS FOR ACCOUNT #" + accNo + ":");
                System.out.println("-----------------------------------------------------------------------------------------");
                for (Transaction t : txns) {
                    System.out.println(t);
                }
                System.out.println("-----------------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("[DATABASE ERROR] " + e.getMessage());
        }
    }

    // Feature 8: View Account Details (VIEW / JOIN QUERY)
    private static void viewAccountDetails() {
        System.out.println("\n--- VIEW ACCOUNT DETAILS ---");
        System.out.print("Enter Account No: ");
        int accNo = scanner.nextInt();
        scanner.nextLine();

        try {
            // VIEW QUERY: Uses customer_account_view
            String details = bankDAO.getAccountDetailsView(accNo);
            if (details != null) {
                System.out.println(details);
            } else {
                System.out.println("[ERROR] Account #" + accNo + " not found.");
            }
        } catch (SQLException e) {
            System.out.println("[DATABASE ERROR] " + e.getMessage());
        }
    }

    // Feature 9: Apply for Loan (DATABASE TRIGGER VALIDATION)
    private static void applyForLoan() {
        System.out.println("\n--- APPLY FOR LOAN ---");
        System.out.print("Enter Customer ID: ");
        int custId = scanner.nextInt();
        System.out.print("Enter Branch ID: ");
        int branchId = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Loan Type (e.g., PERSONAL, HOME, CAR): ");
        String type = scanner.nextLine().trim();
        System.out.print("Enter Loan Amount (Rs.): ");
        double amt = scanner.nextDouble();
        System.out.print("Enter Annual Interest Rate (%): ");
        double rate = scanner.nextDouble();
        scanner.nextLine();

        try {
            Loan loan = new Loan(custId, branchId, type, amt, rate);
            // BACKEND TRIGGER: trg_check_loan validates if customer has an account and amount > 0
            int loanId = bankDAO.applyLoan(loan);
            if (loanId > 0) {
                System.out.println("[SUCCESS] Loan application submitted successfully! Generated Loan ID: " + loanId);
            }
        } catch (SQLException e) {
            // Captures trigger SIGNAL SQLSTATE '45000' exception
            System.out.println("\n[LOAN APPLICATION REJECTED BY TRIGGER]");
            System.out.println("Reason: " + e.getMessage());
        }
    }

    // Feature 10: View Customer Loans
    private static void viewCustomerLoans() {
        System.out.println("\n--- VIEW CUSTOMER LOANS ---");
        System.out.print("Enter Customer ID: ");
        int custId = scanner.nextInt();
        scanner.nextLine();

        try {
            List<Loan> loans = bankDAO.getCustomerLoans(custId);
            if (loans.isEmpty()) {
                System.out.println("No loan applications found for Customer #" + custId);
            } else {
                System.out.println("\nLOAN RECORDS FOR CUSTOMER #" + custId + ":");
                System.out.println("-----------------------------------------------------------------------------------------");
                for (Loan l : loans) {
                    System.out.println(l);
                }
                System.out.println("-----------------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.out.println("[DATABASE ERROR] " + e.getMessage());
        }
    }

    // Feature 11: Add Yearly Interest (POLYMORPHISM)
    private static void addYearlyInterest() {
        System.out.println("\n--- APPLY YEARLY INTEREST ---");
        System.out.print("Enter Account No: ");
        int accNo = scanner.nextInt();
        scanner.nextLine();

        try {
            // POLYMORPHISM: Calls account.calculateInterest() (4% for Savings, 0% for Current)
            double interest = bankDAO.applyYearlyInterest(accNo);
            if (interest < 0) {
                System.out.println("[ERROR] Account #" + accNo + " not found.");
            } else if (interest == 0) {
                System.out.println("[INFO] Interest calculated: Rs. 0.00 (Current Accounts earn 0% interest).");
            } else {
                System.out.println("[SUCCESS] Rs. " + String.format("%.2f", interest) + " interest credited to Account #" + accNo + ". (Trigger logged INTEREST transaction)");
            }
        } catch (SQLException e) {
            System.out.println("[DATABASE ERROR] " + e.getMessage());
        }
    }

    // Feature 12: Branch Report (GROUP BY & AGGREGATE QUERY)
    private static void viewBranchReport() {
        System.out.println("\n--- BRANCH ANALYTICAL REPORT ---");
        try {
            // AGGREGATE QUERY: GROUP BY branch_id with COUNT() and SUM()
            List<String> report = bankDAO.getBranchReport();
            System.out.println("-----------------------------------------------------------------------------------------");
            for (String line : report) {
                System.out.println(line);
            }
            System.out.println("-----------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("[DATABASE ERROR] " + e.getMessage());
        }
    }
}
