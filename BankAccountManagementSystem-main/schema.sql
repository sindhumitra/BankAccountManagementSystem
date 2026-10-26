-- Bank Account Management System Database Schema
-- Database: bankdb

DROP DATABASE IF EXISTS bankdb;
CREATE DATABASE bankdb;
USE bankdb;

-- 1. Table: customer
CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(50),
    address VARCHAR(100)
);

-- 2. Table: branch
CREATE TABLE branch (
    branch_id INT AUTO_INCREMENT PRIMARY KEY,
    branch_name VARCHAR(50) NOT NULL,
    ifsc_code VARCHAR(11) UNIQUE NOT NULL,
    city VARCHAR(30)
);

-- 3. Table: account (account_no starting at 1001)
CREATE TABLE account (
    account_no INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    branch_id INT NOT NULL,
    account_type ENUM('SAVINGS', 'CURRENT') NOT NULL,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    opened_on DATE NOT NULL,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id) ON DELETE CASCADE
) AUTO_INCREMENT = 1001;

-- 4. Table: transactions
CREATE TABLE transactions (
    txn_id INT AUTO_INCREMENT PRIMARY KEY,
    account_no INT NOT NULL,
    txn_type ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER_IN', 'TRANSFER_OUT', 'INTEREST') NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    txn_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    balance_after DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (account_no) REFERENCES account(account_no) ON DELETE CASCADE,
    CONSTRAINT chk_txn_amount CHECK (amount > 0)
);

-- 5. Table: loan
CREATE TABLE loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    branch_id INT NOT NULL,
    loan_type VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    interest_rate DECIMAL(4,2) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'CLOSED') DEFAULT 'PENDING',
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
    FOREIGN KEY (branch_id) REFERENCES branch(branch_id) ON DELETE CASCADE,
    CONSTRAINT chk_loan_amount CHECK (amount > 0)
);

-- VIEW: customer_account_view
CREATE VIEW customer_account_view AS
SELECT 
    c.customer_id,
    c.name AS customer_name,
    c.phone,
    c.email,
    a.account_no,
    a.account_type,
    a.balance,
    a.opened_on,
    b.branch_id,
    b.branch_name,
    b.ifsc_code
FROM customer c
JOIN account a ON c.customer_id = a.customer_id
JOIN branch b ON a.branch_id = b.branch_id;

-- TRIGGER 1: trg_log_transaction (AFTER UPDATE on account)
-- Automatically inserts a row into transactions when balance changes
DELIMITER //
CREATE TRIGGER trg_log_transaction
AFTER UPDATE ON account
FOR EACH ROW
BEGIN
    IF NEW.balance <> OLD.balance THEN
        INSERT INTO transactions (account_no, txn_type, amount, txn_date, balance_after)
        VALUES (
            NEW.account_no,
            COALESCE(@txn_type, IF(NEW.balance >= OLD.balance, 'DEPOSIT', 'WITHDRAW')),
            ABS(NEW.balance - OLD.balance),
            NOW(),
            NEW.balance
        );
        SET @txn_type = NULL;
    END IF;
END//
DELIMITER ;

-- TRIGGER 2: trg_check_loan (BEFORE INSERT on loan)
-- Rejects loan if amount is not positive or customer has no account
DELIMITER //
CREATE TRIGGER trg_check_loan
BEFORE INSERT ON loan
FOR EACH ROW
BEGIN
    IF NEW.amount <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Loan rejection: Amount must be greater than zero.';
    ELSEIF NOT EXISTS (SELECT 1 FROM account WHERE customer_id = NEW.customer_id) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Loan rejection: Customer has no active account in the bank.';
    END IF;
END//
DELIMITER ;

-- STORED PROCEDURE 1: sp_transfer_funds
-- Executes atomic fund transfers using explicit transaction handling
DELIMITER //
CREATE PROCEDURE sp_transfer_funds(
    IN p_from_acc INT,
    IN p_to_acc INT,
    IN p_amt DECIMAL(12,2),
    OUT p_msg VARCHAR(255)
)
proc_label: BEGIN
    DECLARE v_from_bal DECIMAL(12,2);
    DECLARE v_to_exists INT DEFAULT 0;

    IF p_amt <= 0 THEN
        SET p_msg = 'ERROR: Transfer amount must be positive.';
        LEAVE proc_label;
    END IF;

    IF p_from_acc = p_to_acc THEN
        SET p_msg = 'ERROR: Source and target account cannot be identical.';
        LEAVE proc_label;
    END IF;

    START TRANSACTION;

    SELECT balance INTO v_from_bal FROM account WHERE account_no = p_from_acc FOR UPDATE;
    IF v_from_bal IS NULL THEN
        ROLLBACK;
        SET p_msg = 'ERROR: Source account does not exist.';
        LEAVE proc_label;
    END IF;

    SELECT COUNT(*) INTO v_to_exists FROM account WHERE account_no = p_to_acc;
    IF v_to_exists = 0 THEN
        ROLLBACK;
        SET p_msg = 'ERROR: Target account does not exist.';
        LEAVE proc_label;
    END IF;

    IF v_from_bal < p_amt THEN
        ROLLBACK;
        SET p_msg = 'ERROR: Insufficient funds in source account.';
        LEAVE proc_label;
    END IF;

    -- Update balances; trigger will log TRANSFER_OUT and TRANSFER_IN
    SET @txn_type = 'TRANSFER_OUT';
    UPDATE account SET balance = balance - p_amt WHERE account_no = p_from_acc;

    SET @txn_type = 'TRANSFER_IN';
    UPDATE account SET balance = balance + p_amt WHERE account_no = p_to_acc;

    COMMIT;
    SET p_msg = 'SUCCESS: Fund transfer completed successfully.';
END//
DELIMITER ;

-- STORED PROCEDURE 2: sp_mini_statement
-- Returns last 10 transactions for a given account
DELIMITER //
CREATE PROCEDURE sp_mini_statement(IN p_acc_no INT)
BEGIN
    SELECT txn_id, account_no, txn_type, amount, txn_date, balance_after
    FROM transactions
    WHERE account_no = p_acc_no
    ORDER BY txn_date DESC, txn_id DESC
    LIMIT 10;
END//
DELIMITER ;

-- SAMPLE DATA INSERTS
-- Insert 2 Branches
INSERT INTO branch (branch_name, ifsc_code, city) VALUES 
('Main Branch', 'BANK0001001', 'Mumbai'),
('City Branch', 'BANK0001002', 'Delhi');

-- Insert 3 Customers
INSERT INTO customer (name, phone, email, address) VALUES 
('Alice Smith', '9876543210', 'alice@example.com', '123 Park Street, Mumbai'),
('Bob Jones', '9876543211', 'bob@example.com', '456 Hill Road, Delhi'),
('Charlie Brown', '9876543212', 'charlie@example.com', '789 Lake View, Mumbai');

-- Insert 3 Accounts (2 Savings, 1 Current)
-- Note: Manually updating initial balance will trigger transaction logging
INSERT INTO account (customer_id, branch_id, account_type, balance, opened_on) VALUES 
(1, 1, 'SAVINGS', 5000.00, CURDATE()),
(2, 2, 'CURRENT', 15000.00, CURDATE()),
(3, 1, 'SAVINGS', 3000.00, CURDATE());

-- Initial transaction log inserts for seed accounts
INSERT INTO transactions (account_no, txn_type, amount, balance_after) VALUES 
(1001, 'DEPOSIT', 5000.00, 5000.00),
(1002, 'DEPOSIT', 15000.00, 15000.00),
(1003, 'DEPOSIT', 3000.00, 3000.00);
