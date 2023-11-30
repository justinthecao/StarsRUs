INSERT INTO Administrator(username, aname, apassword, astate, phone_number, email_address, tax_id)
VALUES ('admin', 'John Admin', 'secret', 'CA', '(805)6374632', 'admin@stock.com', 1000);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('alfred', 'Alfred Hitchcock', 'hi','CA','(805)2574499','alfred@hotmail.com',1022, 001, 10000);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('billy','Billy Clinton' ,'cl','CA','(805)5629999','billy@yahoo.com',3045, 002, 100000);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('cindy','Cindy Laugher','la','CA','(805)6930011','cindy@hotmail.com',2034, 003, 50000);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('david','David Copperfill','co','CA','(805)8240011','david@yahoo.com',4093, 004, 45000);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('sailor','Elizabeth Sailor','sa','CA','(805)1234567','sailor@hotmail.com',1234, 005, 200000);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('brush','George Brush','br','CA','(805)1357999','george@hotmail.com',8956, 006, 5000);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('ivan','Ivan Stock','st','NJ','(805)3223243','ivan@yahoo.com',2341, 007, 2000);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('joe', 'Joe Pepsi','pe','CA','(805)5668123','pepsi@pepsi.com',0456, 008, 10000);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('magic','Magic Jordon','jo','NJ','(805)4535539','jordon@jordon.org',3455, 009, 130200);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('olive','Olive Stoner','st','CA','(805)2574499','olive@yahoo.com',1123, 010, 35000);

INSERT INTO Customer(username, cname, cpassword, cstate, phone_number, email_address, tax_id, markAccId, balance)
VALUES ('frank','Frank Olson','ol','CA','(805)3456789','frank@gmail.com',3306, 011, 30500);

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
('STC', 'Jerry Maguire Actor', 1996, 5000000, 'Actor', 1996);


INSERT INTO StockAccount VALUES
(1, 100, 'SKB');

INSERT INTO StockAccount VALUES
(2, 500, 'SMD');

INSERT INTO StockAccount VALUES
(2, 100, 'STC');

INSERT INTO StockAccount VALUES
(3, 250, 'STC');

INSERT INTO StockAccount VALUES
(4, 100, 'SKB');

INSERT INTO StockAccount VALUES
(4, 500, 'SMD');

INSERT INTO StockAccount VALUES
(4, 50, 'STC');

INSERT INTO StockAccount VALUES
(5, 1000, 'SMD');

INSERT INTO StockAccount VALUES
(6, 100, 'SKB');

INSERT INTO StockAccount VALUES
(7, 300, 'SMD');

INSERT INTO StockAccount VALUES
(8, 500, 'SKB');

INSERT INTO StockAccount VALUES
(8, 100, 'STC');

INSERT INTO StockAccount VALUES
(8, 200, 'SMD');

INSERT INTO StockAccount VALUES
(9, 1000, 'SKB');

INSERT INTO StockAccount VALUES
(10, 100, 'SKB');

INSERT INTO StockAccount VALUES
(10, 100, 'SMD');

INSERT INTO StockAccount VALUES
(10, 100, 'STC');

INSERT INTO StockAccount VALUES
(11, 100, 'SKB');

INSERT INTO StockAccount VALUES
(11, 200, 'STC');

INSERT INTO StockAccount VALUES
(11, 100, 'SMD');



