/* Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.*/
/*
   DESCRIPTION    
   This code sample shows how to use JDBC and the OracleDataSource API to establish a
   connection to your database.
   This is adapted from an official Oracle sample project
   (https://github.com/oracle-samples/oracle-db-examples/blob/main/java/jdbc/ConnectionSamples/DataSourceSample.java)
   to suit the needs of your CS174A project.
    
    Step 1: Download the Zipped JDBC driver (ojdbc11.jar) and Companion Jars from this
            link:
            https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html
            Extract the zipped contents into the lib folder. This allows your code to
            interface properly with JDBC.
    Step 2: Enter the database details (DB_USER, DB_PASSWORD and DB_URL) in this file.
            Note that DB_URL will require you to know the path to your connection
            wallet.
    Step 3: Run the file with "java -cp lib/ojdbc11.jar ./src/TestConnection.java"
 */

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;






public class TraderInterface {
    // The recommended format of a connection URL is:
    // "jdbc:oracle:thin:@<DATABASE_NAME_LOWERCASE>_tp?TNS_ADMIN=<PATH_TO_WALLET>"
    // where
    // <DATABASE_NAME_LOWERCASE> is your database name in lowercase
    // and
    // <PATH_TO_WALLET> is the path to the connection wallet on your machine.
    final static String DB_URL = "jdbc:oracle:thin:@jcoracledb1_tp?TNS_ADMIN=./Wallet_JCORACLEDB1";
    final static String DB_USER = "ADMIN";
    final static String DB_PASSWORD = "Password123!@#";
    final static Scanner scanner = new Scanner(System.in);
    static String currentUser;
    static String date;
    static int markAccId;


    // This method creates a database connection using
    // oracle.jdbc.pool.OracleDataSource.
    public static void main(String args[]) throws SQLException {
        Properties info = new Properties();

        System.out.println("Initializing connection properties...");
        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        System.out.println("Creating OracleDataSource...");
        OracleDataSource ods = new OracleDataSource();

        System.out.println("Setting connection properties...");
        ods.setURL(DB_URL);
        ods.setConnectionProperties(info);

        // With AutoCloseable, the connection is closed automatically
        try (OracleConnection connection = (OracleConnection) ods.getConnection()) {
            System.out.println("Connection established!");
            // Get JDBC driver name and version
            DatabaseMetaData dbmd = connection.getMetaData();
            System.out.println("Driver Name: " + dbmd.getDriverName());
            System.out.println("Driver Version: " + dbmd.getDriverVersion());
            // Print some connection properties
            System.out.println(
                "Default Row Prefetch Value: " + connection.getDefaultRowPrefetch()
            );
            System.out.println("Database username: " + connection.getUserName());
            System.out.println();
            // Perform some database operations
            // insertTA(connection);
            
            System.out.println("Login\n======== \nUsername: ");
            String username = scanner.nextLine();
            System.out.println("Password: ");
            String password = scanner.nextLine();
            try(Statement query = connection.createStatement()){
                ResultSet resultSet = query.executeQuery(
                    "SELECT * FROM Customer WHERE username = '" + username + "'"
                );
                resultSet.next();
                if(resultSet.getString("cpassword").equals(password)){
                    System.out.println("Incorrect Username/Password :(");
                    return;
                }
                markAccId = resultSet.getInt("markAccId");
            } catch (Exception e){
                System.out.println("ERROR: selection failed.");
                System.out.println(e);
                System.out.println("Exception: Incorrect Username/Password :(");
                return;
            }
            currentUser = username;
            System.out.println("You're in! What do you wanna do.");
            String input;
            date = "2023-10-16";

            while(!(input = scanner.nextLine()).equals("exit")){

                handleOutput(connection, dbmd, input);
                System.out.println();
            }
            printInstructors(connection);
        } catch (Exception e) {
            System.out.println("CONNECTION ERROR:");
            System.out.println(e);
        }
        
    }


    static void handleOutput(OracleConnection connection, DatabaseMetaData dbmd, String query) throws SQLException{
        String[] split  = query.split(" ");
        if(query.contains("Deposit")){
            String amount = split[1];
            System.out.println("Depositing " + amount + " dollars!");
            try(Statement statement = connection.createStatement()){
                String depositQuery = "UPDATE Customer SET balance = balance + " +  amount + " WHERE username = '" + currentUser + "'";
                statement.executeUpdate(depositQuery);
                System.out.println("Deposited");
                //add to money transaction
                int newTransId = addNewTransaction(statement, 0, date);
                String moneytranQuery = "INSERT INTO MoneyTransaction VALUES (" + newTransId + ", " + amount + "," + markAccId + ")";
                statement.executeUpdate(moneytranQuery);
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("Deposit: Something went wrong...try again.");
                return;
            }
        }

        else if(query.contains("Withdraw")){
            String amount = split[1];
            System.out.println("Withdrawing " + amount + " dollars!");
            try(Statement statement = connection.createStatement()){
                Float balance = getUserBalance(statement);
                if(balance < Integer.parseInt(amount)){
                    System.out.println("You don't have enough money. Cancelling...");
                    return;
                }
                String withdrawQuery = "UPDATE Customer SET balance = balance - " +  amount + " WHERE username = '" + currentUser + "'";
                statement.executeUpdate(withdrawQuery);
                
                int newTransId = addNewTransaction(statement, 1, date);
                String moneytranQuery = "INSERT INTO MoneyTransaction VALUES (" + newTransId + ", " + amount + "," + markAccId + ")";
                statement.executeUpdate(moneytranQuery);
                System.out.println("Withdrawed");
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("Something went wrong...try again.");
            }
        }

        else if(query.contains("Buy")){
            String symbol = split[1];
            int amount = Integer.parseInt(split[2]);

            //check the symbol exists
            try(Statement statement = connection.createStatement()){
                Float balance = getUserBalance(statement);
                

                //check stock exists
                String symbolquery = "SELECT * FROM Stock WHERE symbol = '" + symbol + "'";
                ResultSet symbolSet = statement.executeQuery(
                    symbolquery
                );
                if(!symbolSet.next()){
                    System.out.println("No symbol found!");
                    return;
                }
                //-----

                //check enough money
                Float curPrice = symbolSet.getFloat("curPrice");
                System.out.println(curPrice);
                if((curPrice * amount + 20) > balance){
                    System.out.println("You don't have enough money. Cancelling...");
                    return;
                }
                //------

                //update money in markacc
                String depositQuery = "UPDATE Customer SET balance = balance - (" + amount * curPrice + 20 + ") WHERE username = '" + currentUser + "'";
                statement.executeUpdate(depositQuery);
                //------

                //insert new transaction
                int newTransId = addNewTransaction(statement, 2, date);
                //if its a new stock for customer make a new one
                String getAllStock = "SELECT * FROM StockAccount WHERE customerId = " + markAccId + " AND symbol = '" + symbol + "'";
                ResultSet stockAccSet = statement.executeQuery(getAllStock);
                int stockAccId;
                if(!stockAccSet.next()){
                    stockAccId = getNewStockAccId(statement);
                    System.out.println("new stockaccid: " + stockAccId);
                    String addStockAcc = "INSERT INTO StockAccount VALUES (" + stockAccId+ ", " + markAccId + ", 0, '" + symbol + "')";
                    statement.executeQuery(addStockAcc);
                }
                else{
                    System.out.println("found stockacc");
                    stockAccId = stockAccSet.getInt("stockAccId");
                }
                //---------

                //insert into buytransaction
                String buyTransaction = "INSERT INTO BuyTransaction VALUES (" + newTransId + "," + markAccId + ", '" + symbol + "', " + curPrice + ", " + amount + ")";
                statement.executeUpdate(buyTransaction);
                //update balance in stockaccount
                String updateStockBalance = "UPDATE StockAccount SET balance = balance + " + amount + " WHERE symbol = '" + symbol + "' AND customerId = " + markAccId;
                statement.executeQuery(updateStockBalance);
                //update stock amount need to check if there is already one at that price
                String isTherePrice = "SELECT * FROM StockAmount WHERE stockAccId = " + stockAccId + " AND price = " + curPrice;
                ResultSet isTherePriceSet = statement.executeQuery(isTherePrice);
                if(isTherePriceSet.next()){
                    String addToPrice = "UPDATE StockAmount SET amount = amount + "+  amount + " WHERE  stockAccId = " + stockAccId + " AND price = " + curPrice;
                    statement.executeUpdate(addToPrice);
                }
                else{
                    String insertPriceAmount = "INSERT INTO StockAmount VALUES (" + stockAccId + ", " + amount + ", " + curPrice + ")";
                    statement.executeUpdate(insertPriceAmount);
                }
            } catch(Exception e){
                e.printStackTrace();

                System.out.println("Buy: Something went wrong...try again.");
            }

            //get the current price and then check if buying will become negative

            //subtract price * count from balance

            //if its a new stock account make a new stock account
        }
        

        // Sell [symbol] [number of stock] [map of num of stock to og price (4 100 5 200)]
        else if(query.contains("Sell")){
            String symbol = split[1];
            int amount = Integer.parseInt(split[2]);

            int add = 0;
            HashMap<Float, Integer> sell = new HashMap<Float, Integer>();
            for(int i = 3; i< split.length; i+=2){
                add += Integer.parseInt(split[i]);
                sell.put(Float.parseFloat(split[i+1]), Integer.parseInt(split[i]));
            }
            if(add != amount){
                System.out.println("Number of each price to total not equal");
                return;
            }
            //check the symbol exists
            //add transaction add to sell transaction add to sellandbuy transaction
            try(Statement statement = connection.createStatement()){

                //check stock exists
                String symbolquery = "SELECT * FROM Stock WHERE symbol = '" + symbol + "'";
                ResultSet symbolSet = statement.executeQuery(
                    symbolquery
                );
                if(!symbolSet.next()){
                    System.out.println("No symbol found!");
                    return;
                }
                // -----
                Float balance = getUserBalance(statement);
                Float curPrice = symbolSet.getFloat("curPrice");
                if((amount * curPrice - 20 )> balance){
                    System.out.println("The commission will bankrupt you. Cancelling.");
                    return;
                }

                //getting stock amounts and checks
                String stockQuery= "SELECT * FROM StockAccount WHERE symbol = '" + symbol + "' AND customerID = " + markAccId;
                ResultSet stockAccountSet = statement.executeQuery(stockQuery);
                stockAccountSet.next();
                int stockAccId = stockAccountSet.getInt("stockAccId");
                String checkStockAmount = "SELECT * FROM StockAmount WHERE stockAccId = " + stockAccId;
                ResultSet stockAmounts = statement.executeQuery(checkStockAmount);
                HashMap<Float, Integer> amounts = new HashMap<Float, Integer>();
                while(stockAmounts.next()){
                    amounts.put(stockAmounts.getFloat("price"), stockAmounts.getInt("amount"));
                }
                for(Float i: sell.keySet()){
                    if(!amounts.keySet().contains(i) || amounts.get(i) < sell.get(i)){
                        System.out.println("Either you never bought at one of these prices or you're trying to sell too much at one of those prices");
                        return;
                    }
                }



                /*we need to update the stock amount 
                    the stock account balance 
                    the sell transaction 
                    the sellcountsbuy */
                //updating stock amounts
                for(Float i: sell.keySet()){
                    String updateAmount = "UPDATE StockAmount SET amount = amount - " + sell.get(i) + " WHERE stockAccId = " + stockAccId + " AND price = " + i;
                    statement.executeUpdate(updateAmount);
                }

                //updating stock account balance
                String updateStock = "UPDATE StockAccount SET balance = balance - " + amount + " WHERE stockAccid = " + stockAccId;
                statement.executeUpdate(updateStock);

                //adding new trans and selltrans
                int newTransId = addNewTransaction(statement,3, date);
                String sellTransaction = "INSERT INTO SellTransaction VALUES (" + newTransId +"," + amount + "," + markAccId + ", '" + symbol + "')";
                statement.executeUpdate(sellTransaction);
                
                //adding sell buy into sellcountsbuy
                for(Float i: sell.keySet()){
                    String addSellCountsBuy = "INSERT INTO SellCountsBuy VALUES ( "+ newTransId + ", '" + symbol + "', " + markAccId + ", " + i  + ", " + sell.get(i) + ")";
                    statement.executeUpdate(addSellCountsBuy);
                }
                
                Float toRemove = amount * curPrice - 20;
                String depositQuery = "UPDATE Customer SET balance = balance + (" + toRemove + ") WHERE username = '" + currentUser + "'";
                statement.executeUpdate(depositQuery); 
                
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("Something went wrong...try again.");
            }

            
        }
        else if(query.contains("Cancel")){            
            try(Statement statement = connection.createStatement()){
                String getRecent = "SELECT MAX(T.transid) as max FROM (SELECT M.transid " +
                "FROM MoneyTransaction M " +
                "WHERE M.customerid = " + markAccId +
                
                " UNION SELECT C.transid " +
                "FROM CancelTransaction C " +
                "WHERE C.custId = "+ markAccId +
                
                " UNION SELECT B.transid " +
                "FROM BuyTransaction B " +
                "WHERE B.customerId = "+ markAccId +
                
                " UNION SELECT S.transid " +
                "FROM SellTransaction S " +
                "WHERE S.customerId = "+ markAccId + ") T";
                ResultSet recentSet = statement.executeQuery(getRecent);
                if(recentSet.next()){
                    int mostRecent = recentSet.getInt("max");
                    String getRecentType = "SELECT ttype FROM Transaction WHERE transid = " + mostRecent;
                    ResultSet recentType = statement.executeQuery(getRecentType);
                    recentType.next();
                    int type = recentType.getInt("ttype");
                    if(type != 2 && type != 3){
                        System.out.println("most previous transaction was not buy or sell. bad.");
                        return;
                    }
                    if(type == 2){
                        revertBuy(statement, mostRecent);
                    }
                    //TODO: need to make sure to print out what transaction you are cancelling
                    else{
                        revertSell(statement, mostRecent);
                    }
                }
                else{
                    System.out.println("Don't have any transactions. try again.");
                    return;
                }
            }
        }

        else if(query.contains("Show Balance")){
            
        }
        else if(query.contains("Show Transaction")){
            
        }
        else if(query.contains("Symbol")){
            
        }
        else if(query.contains("Movie")){
            
        }
        else if(query.contains("Top")){
            
        }
        else if(query.contains("Review")){
            
        }
        else{
            System.out.println("Not sure what you wanna do man.");
        }
    }


    static Float getUserBalance(Statement statement) throws SQLException{
        String currentBalance = "SELECT balance FROM Customer WHERE username = '" + currentUser + "'";
        ResultSet balanceSet = statement.executeQuery(
            currentBalance
        );
        balanceSet.next();
        Float balance = balanceSet.getFloat("balance");
        return balance;
    }
    
    static int getNewTransId(Statement statement) throws SQLException{
        String getMaxTransId = "SELECT MAX(transid) as max FROM Transaction";
        ResultSet transSet = statement.executeQuery(getMaxTransId);
        transSet.next();
        if(!(transSet.getString("max") == null)){
            return transSet.getInt("max") + 1;
        }
        return 0;
    }

    static int addNewTransaction(Statement statement, int type, String date) throws SQLException{
        int newTransId = getNewTransId(statement);
        System.out.println("new transid: " + newTransId);
        String tranQuery = "INSERT INTO Transaction VALUES (" + newTransId + ", " + type +  ", DATE '" + date + "')";
        statement.executeUpdate(tranQuery);
        return newTransId;
    }

    static int getNewStockAccId(Statement statement) throws SQLException{
        String getMaxTransId = "SELECT MAX(stockAccId) as max FROM StockAccount";
        ResultSet transSet = statement.executeQuery(getMaxTransId);
        transSet.next();
        if(!(transSet.getString("max") == null)){
            return transSet.getInt("max") + 1;
        }
        return 0;
    }

    static void revertBuy(Statement statement, int transid){
        //must change back everything that happened
        //remove added stocks in stockAmount
        //remove added balance in stockAccount
        //add money back to market account

        int newTransId = addNewTransaction(statement, , date)


    }

    static void revertSell(Statement statement, int transid){
        //
        
    }










    // Inserts another TA into the Instructors table.
    public static void insertTA(Connection connection) throws SQLException {
        System.out.println("Preparing to insert TA into Instructors table...");
        // Statement and ResultSet are AutoCloseable and closed automatically. 
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "INSERT INTO INSTRUCTORS VALUES (3, 'Momin Haider', 'TA')"
                )
            ) {}
        } catch (Exception e) {
            System.out.println("ERROR: insertion failed.");
            System.out.println(e);
        }
    }

    // Displays data from Instructors table.
    public static void printInstructors(Connection connection) throws SQLException {
        // Statement and ResultSet are AutoCloseable and closed automatically. 
        try (Statement statement = connection.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM Customer"
                )
            ) {
                System.out.println("Customer:");
                System.out.println("I_ID\tI_NAME\t\tI_ROLE");
                while (resultSet.next()) {
                    System.out.println(
                        resultSet.getString("Username") + "\t"
                        + resultSet.getString("Cname") + "\t"
                        + resultSet.getString("Cpassword")
                    );
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: selection failed.");
            System.out.println(e);
        }
    }
}
