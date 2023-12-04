Drop Table Price;
Drop Table PricePair;
Drop Table Review;

Drop Table Contract;
DROP Table SellCountsBuy;
DROP Table SellTransaction;
Drop Table BuyTransaction;
DROP table CancelTransaction;
DROP Table MoneyTransaction;
DROP Table Transaction;
DROP Table Movie;
DROP Table StockAmount;
DROP Table StockAccount;
DROP Table Stock;
DROP Table MarketAccountHistory;
Drop Table Customer;
DROP Table Administrator;
DROP Table CurrentDate;
DROP Table Market;

CREATE TABLE Customer(
    username CHAR(64),
    cname CHAR(64),
    cpassword CHAR(64),
    cstate Char(2),
    phone_number CHAR(12),
    email_address CHAR(64),
    tax_id INTEGER,
    markAccId INTEGER NOT NULL,
    balance FLOAT,
    PRIMARY KEY (username),
    UNIQUE (markAccId),
    UNIQUE (tax_id)
);

CREATE TABLE MarketAccountHistory (
    currDate DATE,
    currBalance FLOAT,
    markAccId INTEGER NOT NULL,
    PRIMARY KEY (currDate, markAccId),
    FOREIGN KEY (markAccId) REFERENCES Customer(markAccId) ON DELETE CASCADE
);

CREATE TABLE Administrator(
    username CHAR(64),
    aname CHAR(64),
    apassword CHAR(64),
    astate Char(2),
    phone_number CHAR(12),
    email_address CHAR(64),
    tax_id INTEGER,
    PRIMARY KEY (username),
    UNIQUE (tax_id)
);

CREATE TABLE Stock(
    symbol CHAR(3),
    curPrice FLOAT,
    starname CHAR(64) NOT NULL,
    dob DATE,
    PRIMARY KEY(symbol),
    UNIQUE (starname)
);

CREATE TABLE StockAccount(
    stockAccId INT NOT NULL,
    customerId INT NOT NULL,
    balance FLOAT,
    symbol CHAR(3) NOT NULL,
    PRIMARY KEY(symbol, customerId),
    UNIQUE (stockAccId),
    FOREIGN KEY (customerId) references Customer(markAccId) ON DELETE CASCADE,
    FOREIGN KEY (symbol) references Stock(symbol)
);

CREATE TABLE StockAmount(
    stockAccId INT,
    amount FLOAT,
    price FLOAT,
    PRIMARY KEY(stockAccId, price),
    FOREIGN KEY (stockAccId) references StockAccount (stockAccId)
);

CREATE TABLE Movie(
    title CHAR(20),
    prod_year INT,
    rating FLOAT,
    PRIMARY KEY(title, prod_year)
);

CREATE Table Transaction(
    transid INT, 
    ttype INT, 
    tdate DATE,
    PRIMARY KEY (transid)
);

CREATE Table MoneyTransaction(
    transid INT NOT NULL,
    amountMoney FLOAT,
    customerId INT NOT NULL,
    PRIMARY KEY(transid, customerId),
    FOREIGN KEY(customerId) references Customer(markAccId) ON DELETE CASCADE,
    FOREIGN KEY(transid) references Transaction ON DELETE CASCADE
);

CREATE Table CancelTransaction(
    transid INT NOT NULL,
    cancelSym CHAR(3) NOT NULL,
    custId INT NOT NULL,
    PRIMARY KEY(transid, cancelSym, custId),
    FOREIGN KEY(cancelSym, custId) references StockAccount(symbol, customerId) ON DELETE CASCADE,
    FOREIGN KEY(transid) references Transaction ON DELETE CASCADE
);

CREATE Table BuyTransaction(
    transid INT NOT NULL,
    customerId INT NOT NULL,
    stockSym CHAR(3) NOT NULL,
    price FLOAT,
    buycount FLOAT,
    PRIMARY KEY(transid, stockSym, customerId),
    FOREIGN KEY(stockSym, customerId) references StockAccount(symbol, customerId) ON DELETE CASCADE,
    FOREIGN KEY(transid) references Transaction ON DELETE CASCADE
);

CREATE Table SellTransaction(
    transid INT NOT NULL,
    totalCount FLOAT,
    customerId INT NOT NULL,
    stockSym CHAR(3) NOT NULL,
    price FLOAT,
    profit FLOAT,
    PRIMARY KEY(transid, stockSym, customerId),
    FOREIGN KEY(stockSym, customerId) references StockAccount(symbol, customerId) ON DELETE CASCADE,
    FOREIGN KEY(transid) references Transaction ON DELETE CASCADE
);

CREATE Table SellCountsBuy(
    sellid INT,
    stockSym CHAR(3),
    custAcc INT,
    price FLOAT,
    amount FLOAT,
    PRIMARY KEY (sellid, stockSym, custAcc, price),
    FOREIGN KEY(sellid, stockSym, custAcc) references SellTransaction(transid, stockSym, customerId) ON DELETE CASCADE
);


CREATE Table Contract(
    symbol CHAR(3),
    title CHAR(20),
    prodyear INT,
    value INT,
    roletype CHAR(10),
    cyear INT,
    PRIMARY KEY(symbol, title, prodyear),
    FOREIGN KEY (symbol) references Stock ,
    FOREIGN KEY (title, prodyear) references Movie(title, prod_year)
);

CREATE Table Review(
    reviewId INT,
    rcomment CHAR(500),
    title CHAR(20) NOT NULL,
    prodyear INT NOT NULL,
    PRIMARY KEY(reviewId),
    FOREIGN KEY (title, prodyear) references Movie(title, prod_year) ON DELETE CASCADE
);

CREATE Table PricePair(
    pricedate DATE,
    closeprice FLOAT,
    PRIMARY KEY(pricedate, closeprice)
);

CREATE Table Price(
    stockSym char(3),
    pdate DATE,
    closePrice FLOAT,
    PRIMARY KEY(stockSym, pdate, closePrice),
    FOREIGN KEY(stockSym) references Stock(symbol),
    FOREIGN KEY(pdate, closePrice) references PricePair(pricedate, closeprice)
);

CREATE TABLE CurrentDate(
    currDate DATE,
    PRIMARY KEY (currDate)
);

CREATE TABLE Market(
    isOpen INT,
    PRIMARY KEY (isOpen)
);

CREATE TABLE InterestHistory(
    customerId INT,
    interestEarning FLOAT,
    PRIMARY KEY (customerId)
);

INSERT INTO Administrator(username, aname, apassword, astate, phone_number, email_address, tax_id)
VALUES ('admin', 'John Admin', 'secret', 'CA', '(805)6374632', 'admin@stock.com', 1000);


INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('alfred', 'Alfred Hitchcock', 'hi','CA','(805)2574499','alfred@hotmail.com',1022, 001, 10000);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',10000,001);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('billy','Billy Clinton' ,'cl','CA','(805)5629999','billy@yahoo.com',3045, 002, 100000);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',100000,002);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('cindy','Cindy Laugher','la','CA','(805)6930011','cindy@hotmail.com',2034, 003, 50000);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',50000,003);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('david','David Copperfill','co','CA','(805)8240011','david@yahoo.com',4093, 004, 45000);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',45000,004);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('sailor','Elizabeth Sailor','sa','CA','(805)1234567','sailor@hotmail.com',1234, 005, 200000);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',200000,005);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('brush','George Brush','br','CA','(805)1357999','george@hotmail.com',8956, 006, 5000);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',5000,006);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('ivan','Ivan Stock','st','NJ','(805)3223243','ivan@yahoo.com',2341, 007, 2000);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',2000,007);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('joe', 'Joe Pepsi','pe','CA','(805)5668123','pepsi@pepsi.com',0456, 008, 10000);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',10000,008);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('magic','Magic Jordon','jo','NJ','(805)4535539','jordon@jordon.org',3455, 009, 130200);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',130200,009);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('olive','Olive Stoner','st','CA','(805)2574499','olive@yahoo.com',1123, 010, 35000);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',35000,010);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('frank','Frank Olson','ol','CA','(805)3456789','frank@gmail.com',3306, 011, 30500);
INSERT INTO MarketAccountHistory VALUES(DATE '2023-10-1',30500,011);

INSERT INTO Stock VALUES ('SKB', 40.00, 'Kim Basinger',DATE '1958-12-08');

INSERT INTO Stock VALUES ('SMD', 71.00, 'Michael Douglas',DATE '1944-09-25');

INSERT INTO Stock VALUES ('STC', 32.5, 'Tom Cruise',DATE '1962-12-08');

INSERT INTO Movie VALUES 
('L.A. Confidential', 1997, 10);
INSERT INTO Movie VALUES 
('A Perfect Murder', 1998, 6.1);
INSERT INTO Movie VALUES 
('Jerry Maguire', 1996, 8.3);

INSERT INTO Review VALUES
(1, 'I loved it - it''s almost as good as Chinatown!', 'L.A. Confidential', 1997);
INSERT INTO Review VALUES
(2, 'Super clever story with an amazing cast as well.', 'L.A. Confidential', 1997);
INSERT INTO Review VALUES
(3, 'Truly one of the movies of all time.', 'A Perfect Murder',1998);
INSERT INTO Review VALUES
(4, 'What an emotional rollercoaster!', 'Jerry Maguire',1996);


INSERT INTO Contract VALUES 
('SKB', 'L.A. Confidential', 1997, 5000000, 'Actor', 1997);

INSERT INTO Contract VALUES 
('SMD', 'A Perfect Murder', 1998, 5000000, 'Actor', 1997);

INSERT INTO Contract VALUES
('STC', 'Jerry Maguire', 1996, 5000000, 'Actor', 1996);


INSERT INTO StockAccount VALUES
(12, 1, 100, 'SKB');

INSERT INTO StockAmount VALUES
(12, 100, 40.00);

INSERT INTO StockAccount VALUES
(13, 2, 500, 'SMD');

INSERT INTO StockAmount VALUES
(13, 500, 71.00);

INSERT INTO StockAccount VALUES
(14, 2, 100, 'STC');

INSERT INTO StockAmount VALUES
(14, 100, 32.50);

INSERT INTO StockAccount VALUES
(15, 3, 250, 'STC');

INSERT INTO StockAmount VALUES
(15, 250, 32.50);

INSERT INTO StockAccount VALUES
(16, 4, 100, 'SKB');

INSERT INTO StockAmount VALUES
(16, 100, 40.00);

INSERT INTO StockAccount VALUES
(17, 4, 500, 'SMD');

INSERT INTO StockAmount VALUES
(17, 500, 71.00);

INSERT INTO StockAccount VALUES
(18, 4, 50, 'STC');

INSERT INTO StockAmount VALUES
(18, 50, 32.50);

INSERT INTO StockAccount VALUES
(19, 5, 1000, 'SMD');

INSERT INTO StockAmount VALUES
(19, 1000, 71.00);

INSERT INTO StockAccount VALUES
(20, 6, 100, 'SKB');

INSERT INTO StockAmount VALUES
(20, 100, 40.00);

INSERT INTO StockAccount VALUES
(21, 7, 300, 'SMD');

INSERT INTO StockAmount VALUES
(21, 300, 71.00);

INSERT INTO StockAccount VALUES
(22, 8, 500, 'SKB');

INSERT INTO StockAmount VALUES
(22, 500, 40.00);

INSERT INTO StockAccount VALUES
(23, 8, 100, 'STC');

INSERT INTO StockAmount VALUES
(23, 100, 32.50);

INSERT INTO StockAccount VALUES
(24, 8, 200, 'SMD');

INSERT INTO StockAmount VALUES
(24, 200, 71.00);

INSERT INTO StockAccount VALUES
(25, 9, 1000, 'SKB');

INSERT INTO StockAmount VALUES
(25, 1000, 40.00);

INSERT INTO StockAccount VALUES
(26, 10, 100, 'SKB');

INSERT INTO StockAmount VALUES
(26, 100, 40.00);

INSERT INTO StockAccount VALUES
(27, 10, 100, 'SMD');

INSERT INTO StockAmount VALUES
(27, 100, 71.00);

INSERT INTO StockAccount VALUES
(28, 10, 100, 'STC');

INSERT INTO StockAmount VALUES
(28, 100, 32.50);

INSERT INTO StockAccount VALUES
(29, 11, 100, 'SKB');

INSERT INTO StockAmount VALUES
(29, 100, 40.00);

INSERT INTO StockAccount VALUES
(30, 11, 200, 'STC');

INSERT INTO StockAmount VALUES
(30, 200, 32.50);

INSERT INTO StockAccount VALUES
(31, 11, 100, 'SMD');

INSERT INTO StockAmount VALUES
(31, 100, 71.00);

INSERT INTO Market VALUES (1);
INSERT INTO CurrentDate VALUES (DATE '2023-10-16');



