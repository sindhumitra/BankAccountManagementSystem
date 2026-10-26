# Bank Account Management System (DBMS & OOP Mini Project)

A complete, production-ready DBMS and OOP mini-project built with **Java (JDK 21)** and **MySQL 8**. The project demonstrates fundamental Relational Database Management System (RDBMS) concepts alongside Object-Oriented Programming (OOP) principles.

---

## 🛠 Tech Stack & Setup Guide

### 1. Prerequisites
- **JDK 17 or later** (Tested on OpenJDK 21)
- **MySQL Server 8.0 or later**
- **MySQL Connector/J JAR** (`mysql-connector-j-8.3.0.jar` included in `lib/` directory)

### 2. Database Setup
1. Start your local MySQL Server.
2. Open MySQL CLI or MySQL Workbench and execute the `schema.sql` script to create the database, tables, views, triggers, stored procedures, and sample data:
   ```bash
   mysql -u root -p < schema.sql
   ```
3. Open `DBConnection.java` and update the database credentials if necessary:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/bankdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
   private static final String USER = "root";
   private static final String PASSWORD = "password"; // Set your MySQL password here
   ```

### 3. Compilation & Execution (Windows Command Line / PowerShell)
Navigate to the project root directory (`BankAccountManagementSystem/`):

#### Compile All Java Source Files:
```powershell
javac -cp "lib/mysql-connector-j-8.3.0.jar" *.java
```

#### Run the Application:
```powershell
java -cp ".;lib/mysql-connector-j-8.3.0.jar" Main
```

*(Note: If using standard CMD, use `java -cp .;lib/mysql-connector-j-8.3.0.jar Main`)*

---

## 🖥 Sample Console Run

```text
=================================================
   WELCOME TO BANK ACCOUNT MANAGEMENT SYSTEM    
=================================================

---------------- MAIN MENU ----------------
1.  Add Customer
2.  Add Branch
3.  Open Account (Savings / Current)
4.  Deposit
5.  Withdraw
6.  Transfer Funds (Stored Procedure)
7.  Mini Statement (Stored Procedure)
8.  View Account Details (View / Join Query)
9.  Apply for Loan (Trigger Validation)
10. View Customer Loans
11. Add Yearly Interest (Polymorphism)
12. Branch Report (Group By Aggregate Query)
0.  Exit
-------------------------------------------
Enter your choice (0-12): 8
Enter Account No: 1001

================ ACCOUNT DETAILS ================
Account No     : 1001
Account Type   : SAVINGS
Current Balance: Rs. 5000.00
Date Opened    : 2026-10-07
Customer ID    : 1
Customer Name  : Alice Smith
Phone          : 9876543210
Email          : alice@example.com
Branch ID      : 1
Branch Name    : Main Branch
IFSC Code      : BANK0001001
==================================================

Enter your choice (0-12): 6
Enter Source Account No: 1001
Enter Target Account No: 1002
Enter Transfer Amount (Rs.): 1000.00

[RESULT] SUCCESS: Fund transfer completed successfully.

Enter your choice (0-12): 9
Enter Customer ID: 999
Enter Branch ID: 1
Enter Loan Type (e.g., PERSONAL, HOME, CAR): HOME
Enter Loan Amount (Rs.): 50000.00
Enter Annual Interest Rate (%): 8.5

[LOAN APPLICATION REJECTED BY TRIGGER]
Reason: Loan rejection: Customer has no active account in the bank.
```

---

## 📐 Textual UML Class Diagram

```text
                       +-----------------------------------+
                       |         <<abstract>>              |
                       |             Account               |
                       +-----------------------------------+
                       | - accountNo : int                 |
                       | - customerId : int                |
                       | - branchId : int                  |
                       | - balance : double                |
                       | - openedOn : LocalDate            |
                       +-----------------------------------+
                       | + deposit(amount: double) : void  |
                       | + withdraw(amount: double)* : void|
                       | + calculateInterest()* : double   |
                       | + getType()* : String             |
                       +-----------------------------------+
                                         ^
                                         |
               +-------------------------+-------------------------+
               |                                                   |
+------------------------------+                   +------------------------------+
|        SavingsAccount        |                   |        CurrentAccount        |
+------------------------------+                   +------------------------------+
| - MINIMUM_BALANCE : double   |                   | - OVERDRAFT_LIMIT : double   |
| - INTEREST_RATE : double     |                   +------------------------------+
+------------------------------+                   | + withdraw(amount)           |
| + withdraw(amount)           |                   | + calculateInterest()        |
| + calculateInterest()        |                   | + getType()                  |
| + getType()                  |                   +------------------------------+
+------------------------------+

+--------------------+     +--------------------+     +--------------------+     +--------------------+
|      Customer      |     |       Branch       |     |    Transaction     |     |        Loan        |
+--------------------+     +--------------------+     +--------------------+     +--------------------+
| - customerId : int |     | - branchId : int   |     | - txnId : int      |     | - loanId : int     |
| - name : String    |     | - branchName: String|     | - accountNo : int  |     | - customerId : int |
| - phone : String   |     | - ifscCode : String|     | - txnType : String |     | - branchId : int   |
| - email : String   |     | - city : String    |     | - amount : double  |     | - loanType : String|
| - address : String |     +--------------------+     | - txnDate:Timestamp|     | - amount : double  |
+--------------------+                                | - balanceAfter:dbl |     | - interestRate:dbl |
                                                      +--------------------+     | - status : String  |
                                                                                 +--------------------+

+-------------------------------------+
|        DBConnection                 |
+-------------------------------------+
| + getConnection() : Connection      |
+-------------------------------------+
                  ^
                  | uses
+-------------------------------------+
|          BankDAO                    |
+-------------------------------------+
| + addCustomer(...)                  |
| + addBranch(...)                    |
| + openAccount(...)                  |
| + deposit(...)                      |
| + withdraw(...)                     |
| + transferFunds(...)                |
| + getMiniStatement(...)             |
| + getAccountDetailsView(...)        |
| + applyLoan(...)                    |
| + getCustomerLoans(...)             |
| + applyYearlyInterest(...)          |
| + getBranchReport(...)              |
+-------------------------------------+
                  ^
                  | uses
+-------------------------------------+
|           Main                      |
+-------------------------------------+
| + main(args: String[])              |
| - printMenu()                       |
+-------------------------------------+
```

---

## 🔗 Concept Mapping Table

| Concept Category | Specific Feature / Requirement | File Implementation Location | Technical Explanation |
|---|---|---|---|
| **OOP - Abstraction** | Abstract class & methods | `Account.java` | Defines abstract base class `Account` with abstract methods `withdraw()`, `calculateInterest()`, and `getType()`. |
| **OOP - Inheritance** | Concrete subclasses | `SavingsAccount.java`, `CurrentAccount.java` | Both subclasses inherit common fields and methods from `Account`. |
| **OOP - Polymorphism** | Dynamic Method Dispatch | `BankDAO.java`, `Main.java` | Calls `account.withdraw()` and `account.calculateInterest()` through base `Account` references without checking types with `if/else`. |
| **OOP - Encapsulation** | Private fields & getters/setters | All Model Classes (`Account.java`, `Customer.java`, etc.) | All instance fields are marked `private` and accessed strictly via getters and deposit/withdraw methods. |
| **OOP - Custom Exception** | Domain exception handling | `InsufficientFundsException.java` | Thrown when minimum balance or overdraft limits are breached. |
| **DBMS - Trigger 1** | Automatic transaction logging | `schema.sql` (`trg_log_transaction`) | `AFTER UPDATE ON account` trigger automatically records transaction rows into `transactions` table when balance changes. |
| **DBMS - Trigger 2** | Pre-insertion business validation | `schema.sql` (`trg_check_loan`) | `BEFORE INSERT ON loan` trigger raises `SIGNAL SQLSTATE '45000'` if loan amount is non-positive or customer lacks an account. |
| **DBMS - Stored Procedure 1** | Atomic fund transfer | `schema.sql` (`sp_transfer_funds`) | Uses `START TRANSACTION`, `COMMIT`, `ROLLBACK`, and row locking (`FOR UPDATE`). Called via `CallableStatement` in `BankDAO.java`. |
| **DBMS - Stored Procedure 2** | Mini statement generation | `schema.sql` (`sp_mini_statement`) | Returns top 10 transactions. Called via `CallableStatement` in `BankDAO.java`. |
| **DBMS - View** | Consolidated account view | `schema.sql` (`customer_account_view`) | Joins `customer`, `account`, and `branch`. Queried in `BankDAO.java` (`getAccountDetailsView`). |
| **DBMS - Prepared Statements** | SQL injection prevention | `BankDAO.java` | All DML/DQL queries use parameter binding (`?`) with `PreparedStatement`. |
| **DBMS - Aggregation & Join** | Analytical branch report | `BankDAO.java` (`getBranchReport`) | Uses `LEFT JOIN`, `COUNT()`, `SUM()`, and `GROUP BY b.branch_id, b.branch_name`. |

---

## ❓ 15 Likely Viva Questions & Answers

#### Q1: What is JDBC and what are its major steps?
**Answer:** JDBC (Java Database Connectivity) is a Java API that enables Java applications to interact with relational databases. The main steps are: (1) Load JDBC Driver, (2) Establish Connection via `DriverManager.getConnection()`, (3) Create `PreparedStatement` or `CallableStatement`, (4) Execute SQL query/update, (5) Process `ResultSet`, and (6) Close resources using try-with-resources.

#### Q2: What is the difference between `Statement` and `PreparedStatement`?
**Answer:** `Statement` compiles SQL queries at runtime and is vulnerable to SQL Injection. `PreparedStatement` pre-compiles SQL queries on the database server, uses parameterized placeholders (`?`), offers significantly faster execution for repetitive queries, and prevents SQL injection attacks.

#### Q3: How is atomic fund transfer guaranteed in `sp_transfer_funds`?
**Answer:** The stored procedure uses explicit transaction management (`START TRANSACTION`, `COMMIT`, and `ROLLBACK`). If any step fails (such as insufficient balance or invalid target account), `ROLLBACK` undoes all updates, preventing partial transfers.

#### Q4: Why did we use a database trigger for transaction logging instead of Java code?
**Answer:** Moving transaction logging into an `AFTER UPDATE` trigger guarantees data integrity at the database level. Even if external applications or direct SQL queries modify account balances, the trigger ensures that every balance change is logged without relying on application-level discipline.

#### Q5: How does the trigger `trg_check_loan` reject invalid loan applications?
**Answer:** The trigger runs `BEFORE INSERT ON loan`. It inspects `NEW.amount` and checks for customer existence in the `account` table. If validation fails, it issues `SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '...'`, which aborts the insert and throws a `SQLException` back to Java.

#### Q6: Explain how polymorphism is demonstrated in account withdrawal.
**Answer:** `BankDAO` holds an `Account` reference. When `account.withdraw(amount)` is called, Java dynamically binds the execution to `SavingsAccount.withdraw()` (which checks the ₹1,000 minimum balance) or `CurrentAccount.withdraw()` (which allows overdraft up to ₹10,000) at runtime.

#### Q7: What is the purpose of `CallableStatement` in JDBC?
**Answer:** `CallableStatement` is used to execute SQL stored procedures. It supports `IN` parameters (passed from Java to database) and `OUT`/`INOUT` parameters (returned from database to Java).

#### Q8: What is 1NF, 2NF, and 3NF?
**Answer:**
- **1NF**: Ensures atomic attribute values and removes repeating groups.
- **2NF**: Satisfies 1NF and eliminates partial functional dependencies on composite primary keys.
- **3NF**: Satisfies 2NF and eliminates transitive functional dependencies (non-key attributes depending on other non-key attributes).

#### Q9: What is a Database View and why is `customer_account_view` useful?
**Answer:** A view is a virtual table based on the result-set of an SQL query. `customer_account_view` simplifies complex multi-table JOINs across `customer`, `account`, and `branch`, allowing application code to fetch complete details with a simple `SELECT` statement.

#### Q10: Why did we make `Account` an abstract class?
**Answer:** A generic "Account" cannot exist on its own in our business domain; an account must specifically be a Savings Account or a Current Account. Making `Account` abstract prevents direct instantiation and forces subclasses to implement rules for withdrawal and interest calculation.

#### Q11: What is the role of `try-with-resources` in Java JDBC?
**Answer:** `try-with-resources` automatically closes any resource (such as `Connection`, `PreparedStatement`, or `ResultSet`) implementing `AutoCloseable` at the end of the block, preventing database connection leaks even when exceptions occur.

#### Q12: What is the difference between primary keys and unique keys in our schema?
**Answer:** A primary key (`customer_id`, `account_no`) uniquely identifies each record in a table and cannot contain `NULL` values. A unique key (`phone` in `customer`, `ifsc_code` in `branch`) prevents duplicate non-null entries for candidate keys.

#### Q13: How does `GROUP BY` work in `getBranchReport()`?
**Answer:** `GROUP BY b.branch_id, b.branch_name` groups account rows by their respective branch. Aggregate functions `COUNT(a.account_no)` and `SUM(a.balance)` compute the total number of accounts and aggregate balance per branch group.

#### Q14: What is the purpose of `InsufficientFundsException`?
**Answer:** It is a custom domain exception that decouples business rule violations (like attempting to withdraw past minimum balance or overdraft limit) from technical database exceptions, providing clear error feedback to the user interface.

#### Q15: What is the difference between `DELETE CASCADE` and standard foreign key constraints?
**Answer:** `ON DELETE CASCADE` automatically deletes dependent child records (such as accounts or loans) when the associated parent record (such as a customer or branch) is deleted, preserving referential integrity without orphan records.
