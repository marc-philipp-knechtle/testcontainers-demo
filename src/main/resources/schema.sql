DROP TABLE IF EXISTS customers;
CREATE TABLE customers(id integer not null primary key, first_name VARCHAR(255), last_name VARCHAR(255));
insert into customers values (1, 'trisha', 'gee');
insert into customers values (2, 'marco', 'behler');