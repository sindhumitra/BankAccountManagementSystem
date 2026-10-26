# Bank Account Management System
## Part A: Database Design Documentation

---

### 1. Problem Statement and Functional Requirements

#### 1.1 Problem Statement
Modern banking institutions require robust, reliable, and secure information systems to manage customer accounts, daily monetary transactions, loan applications, and branch-level analytics. Traditional paper-based or un-normalized systems suffer from data redundancy, inconsistency, lack of transactional integrity, and limited multi-user tracking.

The **Bank Account Management System** is designed to address these challenges by combining Object-Oriented Programming (OOP) principles in Java with a relational database management system (MySQL). The system maintains customer profiles, branch details, account records (Savings and Current), transaction histories, and loan records while enforcing strict business rules (e.g., minimum balance constraints, overdraft limits, transaction logging via database triggers, and atomic fund transfers via stored procedures).

#### 1.2 Functional Requirements
1. **Customer Management**: Register new bank customers with unique contact details (phone, email, address).
2. **Branch Management**: Register bank branches with unique IFSC codes and location details.
3. **Account Operations**:
   - Open Savings or Current accounts with an initial deposit.
   - Maintain minimum balance requirements (₹1,000 for Savings) and overdraft limits (₹10,000 for Current).
   - Deposit funds directly into accounts.
   - Withdraw funds while verifying balance limits using OOP polymorphism and custom domain exceptions.
4. **Fund Transfers**: Perform atomic transfer of funds between two accounts using database transactions and stored procedures (`sp_transfer_funds`).
5. **Transaction Tracking**: Automatically record all balance updates (Deposits, Withdrawals, Transfers, Interest) via database triggers (`trg_log_transaction`) without explicit application-level SQL inserts into the transaction ledger.
6. **Mini Statement**: Fetch recent transaction histories for any account via stored procedures (`sp_mini_statement`).
7. **Loan Processing**:
   - Allow customers to apply for loans.
   - Enforce database-level validation using a trigger (`trg_check_loan`) that rejects non-positive loan amounts or applications from non-account holders.
   - View loan details per customer.
8. **Interest Calculation**: Calculate and credit 4.0% annual interest to Savings accounts.
9. **Branch Analytics**: Generate branch-wise summary reports showing total accounts hosted and aggregate balance maintained using SQL aggregate functions and `GROUP BY`.
10. **Account Overview**: Provide consolidated account views joining customer, account, and branch details via a database view (`customer_account_view`).

---

### 2. Entity-Relationship (ER) Diagram Description

#### 2.1 Entities and Attributes
- **CUSTOMER**
  - `customer_id`: Key attribute (Primary Key, Simple, Single-valued).
  - `name`: Simple attribute.
  - `phone`: Key attribute (Candidate Key / Unique, Simple).
  - `email`: Simple attribute.
  - `address`: Composite attribute (composed of street/city details represented as a string).
- **BRANCH**
  - `branch_id`: Key attribute (Primary Key, Simple).
  - `branch_name`: Simple attribute.
  - `ifsc_code`: Key attribute (Candidate Key / Unique, Simple).
  - `city`: Simple attribute.
- **ACCOUNT**
  - `account_no`: Key attribute (Primary Key, Simple, auto-generated starting at 1001).
  - `account_type`: Simple attribute (Domain: 'SAVINGS', 'CURRENT').
  - `balance`: Simple attribute.
  - `opened_on`: Simple attribute.
- **TRANSACTIONS**
  - `txn_id`: Key attribute (Primary Key, Simple).
  - `txn_type`: Simple attribute (Domain: 'DEPOSIT', 'WITHDRAW', 'TRANSFER_IN', 'TRANSFER_OUT', 'INTEREST').
  - `amount`: Simple attribute.
  - `txn_date`: Simple attribute (Timestamp).
  - `balance_after`: Derived attribute (computed based on previous balance + / - amount).
- **LOAN**
  - `loan_id`: Key attribute (Primary Key, Simple).
  - `loan_type`: Simple attribute (e.g., 'HOME', 'PERSONAL', 'CAR').
  - `amount`: Simple attribute.
  - `interest_rate`: Simple attribute.
  - `status`: Simple attribute (Domain: 'PENDING', 'APPROVED', 'CLOSED').

#### 2.2 Relationships, Cardinality, and Participation Constraints
1. **CUSTOMER opens ACCOUNT**
   - Relationship: `Opens`
   - Cardinality: **1 : N** (One Customer can open multiple Accounts; each Account belongs to exactly one Customer).
   - Participation: Customer has **partial** participation (a customer may exist without an active account initially), Account has **total** participation (every account MUST belong to a customer).
2. **BRANCH maintains ACCOUNT**
   - Relationship: `Maintains`
   - Cardinality: **1 : N** (One Branch maintains multiple Accounts; each Account belongs to one Branch).
   - Participation: Branch has partial participation, Account has total participation.
3. **ACCOUNT has TRANSACTIONS**
   - Relationship: `Has_Txn`
   - Cardinality: **1 : N** (One Account can have multiple Transactions; each Transaction belongs to exactly one Account).
   - Participation: Account has partial participation, Transaction has total participation.
4. **CUSTOMER applies LOAN**
   - Relationship: `Applies`
   - Cardinality: **1 : N** (One Customer can apply for multiple Loans; each Loan belongs to one Customer).
   - Participation: Customer has partial participation, Loan has total participation.
5. **BRANCH issues LOAN**
   - Relationship: `Issues`
   - Cardinality: **1 : N** (One Branch issues multiple Loans; each Loan is issued by one Branch).
   - Participation: Branch has partial participation, Loan has total participation.

---

### 3. ER-to-Relational Mapping (Step-by-Step)

#### Step 1: Mapping Strong Entity Types
- **Rule**: Create a table for each strong entity type. Choose one of the key attributes as Primary Key (PK).
  - `customer` (**customer_id** [PK], name, phone [UNIQUE], email, address)
  - `branch` (**branch_id** [PK], branch_name, ifsc_code [UNIQUE], city)

#### Step 2: Mapping Weak Entity Types
- *(Note: No weak entity types exist in this schema; all entities have independent primary key generators).*

#### Step 3: Mapping Binary 1:1 Relationship Types
- *(Note: No 1:1 relationships present).*

#### Step 4: Mapping Binary 1:N Relationship Types
- **Rule**: For a 1:N relationship from entity $A$ (side 1) to entity $B$ (side $N$), include the Primary Key of $A$ as a Foreign Key (FK) in the table corresponding to $B$.
  - **CUSTOMER 1 : N ACCOUNT**: Include `customer_id` as FK in `account`.
  - **BRANCH 1 : N ACCOUNT**: Include `branch_id` as FK in `account`.
  - Resulting Table: `account` (**account_no** [PK], *customer_id* [FK], *branch_id* [FK], account_type, balance, opened_on)

  - **ACCOUNT 1 : N TRANSACTIONS**: Include `account_no` as FK in `transactions`.
  - Resulting Table: `transactions` (**txn_id** [PK], *account_no* [FK], txn_type, amount, txn_date, balance_after)

  - **CUSTOMER 1 : N LOAN**: Include `customer_id` as FK in `loan`.
  - **BRANCH 1 : N LOAN**: Include `branch_id` as FK in `loan`.
  - Resulting Table: `loan` (**loan_id** [PK], *customer_id* [FK], *branch_id* [FK], loan_type, amount, interest_rate, status)

#### Step 5: Mapping Binary M:N Relationship Types
- *(Note: No M:N relationships exist directly; relationships are modeled via explicit entity foreign keys).*

#### Step 6: Mapping Multivalued Attributes
- *(Note: No multivalued attributes requiring separate bridge tables).*

---

### 4. Normalization Analysis

#### 4.1 Initial Unnormalized Relation (UNF)
Consider a flat file representation of bank transactions and loans:
`BankRecord(CustId, CustName, CustPhone, CustEmail, CustAddress, BranchId, BranchName, IFSC, BranchCity, AccNo, AccType, AccBalance, AccOpenedOn, TxnId, TxnType, TxnAmt, TxnDate, TxnBalanceAfter, LoanId, LoanType, LoanAmt, LoanRate, LoanStatus)`

#### 4.2 Functional Dependencies (FDs)
- $FD_1: \text{customer\_id} \rightarrow \text{name, phone, email, address}$
- $FD_2: \text{phone} \rightarrow \text{customer\_id, name, email, address}$
- $FD_3: \text{branch\_id} \rightarrow \text{branch\_name, ifsc\_code, city}$
- $FD_4: \text{ifsc\_code} \rightarrow \text{branch\_id, branch\_name, city}$
- $FD_5: \text{account\_no} \rightarrow \text{customer\_id, branch\_id, account\_type, balance, opened\_on}$
- $FD_6: \text{txn\_id} \rightarrow \text{account\_no, txn\_type, amount, txn\_date, balance\_after}$
- $FD_7: \text{loan\_id} \rightarrow \text{customer\_id, branch\_id, loan\_type, amount, interest\_rate, status}$

#### 4.3 1st Normal Form (1NF)
- **Requirement**: Eliminate repeating groups and ensure atomic values.
- **Decomposition**: Separate transactions and loans into distinct rows so each cell contains a single atomic value.

#### 4.4 2nd Normal Form (2NF)
- **Requirement**: Must be in 1NF, and no non-prime attribute should be partially dependent on a composite key.
- **Analysis**: Since all candidate keys in decomposed relations are single attributes (`customer_id`, `branch_id`, `account_no`, `txn_id`, `loan_id`), there are **no partial dependencies**. All relations are automatically in 2NF.

#### 4.5 3rd Normal Form (3NF)
- **Requirement**: Must be in 2NF, and no non-prime attribute should be transitively dependent on the primary key (i.e., for every non-trivial $X \rightarrow Y$, $X$ must be a superkey or $Y$ must be a prime attribute).
- **Decomposition into Final 5 Tables**:
  1. `customer`: ($\underline{\text{customer\_id}}$, name, phone, email, address)
  2. `branch`: ($\underline{\text{branch\_id}}$, branch_name, ifsc_code, city)
  3. `account`: ($\underline{\text{account\_no}}$, customer_id, branch_id, account_type, balance, opened_on)
  4. `transactions`: ($\underline{\text{txn\_id}}$, account_no, txn_type, amount, txn_date, balance_after)
  5. `loan`: ($\underline{\text{loan\_id}}$, customer_id, branch_id, loan_type, amount, interest_rate, status)

All 5 relations satisfy 3NF (and BCNF) because every left-hand side determinant in the functional dependencies is a superkey.

---

### 5. Application Development Process & Test Cases

#### 5.1 Development Life Cycle
1. **Requirements Gathering**: Identified key banking operations (account creation, transactions, loan evaluation, reporting).
2. **System & Database Design**: Designed text ER diagram, executed ER-to-Relational mapping, normalized database to 3NF, and defined OOP abstraction hierarchy.
3. **Backend Implementation**: Wrote DDL scripts, triggers (`trg_log_transaction`, `trg_check_loan`), stored procedures (`sp_transfer_funds`, `sp_mini_statement`), views, and sample dataset in `schema.sql`.
4. **Frontend & DAO Integration**: Implemented domain models, DAO pattern with JDBC PreparedStatement/CallableStatement, custom exceptions, and menu interface in Java.
5. **System Verification**: Tested edge cases such as overdraft limits, negative inputs, atomic transfers, and trigger aborts.

#### 5.2 Test Cases Table

| Test ID | Feature / Module | Test Scenario / Input | Expected Output | Actual Output | Status |
|---|---|---|---|---|---|
| **TC-01** | Branch Creation | Branch Name: "Main Branch", IFSC: "BANK0001001", City: "Mumbai" | Branch created successfully with generated ID 1. | Branch created successfully with generated ID 1. | **PASS** |
| **TC-02** | Customer Creation | Name: "Alice Smith", Phone: "9876543210", Email: "alice@example.com", Address: "123 Park St" | Customer registered with generated ID 1. | Customer registered with generated ID 1. | **PASS** |
| **TC-03** | Account Creation | Customer ID: 1, Branch ID: 1, Type: SAVINGS, Deposit: 2000.00 | Account #1001 opened with balance 2000.00. | Account #1001 opened with balance 2000.00. | **PASS** |
| **TC-04** | Withdrawal (Savings) | Account #1001, Withdraw Amount: 1500.00 (Remaining balance = 500.00 < 1000 minimum) | Throws `InsufficientFundsException`: Minimum balance of 1000.00 must be maintained. | Throws `InsufficientFundsException`: Minimum balance of 1000.00 must be maintained. | **PASS** |
| **TC-05** | Transaction Trigger | Account #1001, Deposit: 500.00 | Balance updated to 2500.00. Trigger inserts row into `transactions` with type DEPOSIT and balance_after 2500.00. | Balance updated to 2500.00. Row automatically logged in `transactions`. | **PASS** |
| **TC-06** | Fund Transfer (SP) | From Acc: 1001, To Acc: 1002, Amount: 500.00 | `sp_transfer_funds` debits 1001, credits 1002 within transaction block. Returns "SUCCESS". | `sp_transfer_funds` executed cleanly; both accounts updated and logged. | **PASS** |
| **TC-07** | Loan Trigger Check | Customer ID: 999 (No accounts), Amount: 50000.00 | Trigger `trg_check_loan` rejects insert with SQLSTATE '45000' (Customer has no account). | Execution rejected with SQLSTATE '45000' message displayed in console. | **PASS** |
| **TC-08** | Input Validation | Withdraw Amount: -500.00 | Console error message: "Amount must be positive." No database changes. | Console error message displayed. Operation rejected. | **PASS** |

#### 5.3 Conclusion
The **Bank Account Management System** successfully demonstrates the integration of robust relational database architecture with clean Object-Oriented design principles. Database integrity is preserved through foreign keys, CHECK constraints, stored procedures, and triggers, while the Java application provides an intuitive, robust interface with proper exception handling and code organization.
