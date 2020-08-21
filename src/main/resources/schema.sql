DROP TABLE IF EXISTS customer;

CREATE TABLE customer
(
    id SERIAL NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(50) NOT NULL,
    created_date DATE NOT NULL,
    PRIMARY KEY (id)
);
