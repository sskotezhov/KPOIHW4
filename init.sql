CREATE TABLE accounts (
    user_id VARCHAR(255) PRIMARY KEY,
    balance DECIMAL(19,4) NOT NULL
);

CREATE TABLE orders (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    amount DECIMAL(19,4) NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE processed_payments (
    order_id VARCHAR(255) PRIMARY KEY,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE order_log (
    order_id VARCHAR(255) PRIMARY KEY,
    published BOOLEAN NOT NULL DEFAULT FALSE
);