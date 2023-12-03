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

    import java.nio.file.attribute.FileStoreAttributeView;
    import java.sql.Connection;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.sql.Statement;
    import java.time.LocalDate;
    import java.time.format.DateTimeFormatter;
    import java.util.Date;
    import java.util.Properties;
    import java.util.Scanner;
    import java.util.Locale.IsoCountryCode;
    import java.util.ArrayList;
    import java.util.List;

    import oracle.jdbc.pool.OracleDataSource;
    import oracle.security.pki.internal.cert.ext.SubjectAltNameExtension;
    import oracle.jdbc.OracleConnection;
    import java.sql.DatabaseMetaData;
    import java.sql.PreparedStatement;






    public class ManagerInterface {
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
        static String currentDate;

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
                        "SELECT apassword FROM Administrator WHERE username = '" + username + "'"
                    );
                    resultSet.next();
                    if(resultSet.getString("apassword").equals(password)){
                        System.out.println("Incorrect Username/Password :(");
                        return;
                    }
                } catch (Exception e){
                    System.out.println("ERROR: selection failed.");
                    System.out.println(e);
                    System.out.println("Exception: Incorrect Username/Password :(");
                    return;
                }
                currentUser = username;

                System.out.println("You're in! What do you wanna do.");
                String input;

                try (Statement dateQuery = connection.createStatement()) {
                    ResultSet resultDateSet = dateQuery.executeQuery(
                        "SELECT currDate FROM CurrentDate"
                    );
                    resultDateSet.next();
                    currentDate = resultDateSet.getDate("currDate").toString();
                } catch (Exception ee) {
                    System.out.println("ERROR: Current Date not found.");
                    System.out.println(ee);
                    return;
                }

                System.out.println("Current Date: " + currentDate);

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

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate localDate = LocalDate.parse(currentDate, formatter);

            int currMonth = localDate.getMonthValue();
            int today = localDate.getDayOfMonth();

            String[] split  = query.split(" ");
            
            if (query.contains("Add Interest")) {

                try (Statement accountsQuery = connection.createStatement()) {
                    Statement interestQuery = connection.createStatement();
                    ResultSet AccountsSet = accountsQuery.executeQuery(
                        "SELECT markAccId FROM Customer"
                    );

                    while (AccountsSet.next()) {
                        String marketId = Integer.toString(AccountsSet.getInt("markAccId"));
                        Integer totalDays = 0;
                        Float totalBalance = (float) 0;

                        Float currBalance;

                        System.out.println("Getting Current Balance, Market Id: " + marketId);
                        ResultSet currBalanceSet = interestQuery.executeQuery(
                            "SELECT balance " +
                            "FROM Customer " +
                            "WHERE markAccId = " + marketId
                        );
                        currBalanceSet.next();
                        currBalance = currBalanceSet.getFloat("balance");
                        
                        System.out.println("Received Balance: " + currBalance);

                        Integer prevDay = 1;
                        Float prevBalance = (float) 0;

                        ResultSet accountHistorySet = interestQuery.executeQuery(
                            "SELECT EXTRACT (DAY FROM currDate) as currDay, currBalance " +
                            "FROM MarketAccountHistory " +
                            "WHERE markAccId = " + marketId + " AND EXTRACT(MONTH FROM currdate) = " + currMonth + " " +
                            "ORDER BY EXTRACT(DAY FROM currDate)"
                        );
                        
                        while (accountHistorySet.next()) {
                            System.out.println("previous day: " + prevDay);
                            System.out.println("previous balance: " + prevBalance);
                            Integer currentDay = accountHistorySet.getInt("currDay");
                            Float currentBalance = accountHistorySet.getFloat("currBalance");
                            Integer period = currentDay - prevDay;
                            totalDays += period;
                            totalBalance += prevBalance * period;
                            prevDay = currentDay;
                            prevBalance = currentBalance;
                        }

                        System.out.println("previous day: " + prevDay);
                        System.out.println("previous balance: " + prevBalance);

                        Integer period = today - prevDay + 1;
                        totalDays += period;
                        totalBalance += prevBalance * period;

                        // first day 1: balance = 100
                        // second day 5: balance = 200
                        // current day is 10

                        System.out.println("Average Daily Balance: " + totalBalance / totalDays);

                        Float newBalance = (float) (currBalance + ((totalBalance / totalDays) * 0.02));

                        interestQuery.executeUpdate(
                            "UPDATE Customer " +
                            "SET balance = " + newBalance + " " +
                            "WHERE markAccId = " + marketId
                        );

                        System.out.println("Updated Balance with Accrued Interest: " + newBalance);

                    }
                }

            }
            else if (query.contains("Generate Monthly Statement")) {
                Integer customerId = Integer.parseInt(split[3]);

                Statement customerQuery = connection.createStatement();
                ResultSet customerInfo = customerQuery.executeQuery(
                    "SELECT cname, email_address " +
                    "FROM Customer " +
                    "WHERE markAccId = " + customerId
                );

                customerInfo.next();

                System.out.println("Generating Monthly Statement for Customer: \nName: " + customerInfo.getString("cname") + "\nEmail: " + customerInfo.getString("email_address"));

                Statement monthlyQuery = connection.createStatement();
                ResultSet stockAccountsSet = monthlyQuery.executeQuery(
                    "SELECT symbol, stockAccId " + 
                    "FROM StockAccount " + 
                    "WHERE customerId = " + customerId
                );

                while (stockAccountsSet.next()) {
                    String symbol = stockAccountsSet.getString("symbol");
                    String stockAccountId = stockAccountsSet.getString("stockAccId");

                    System.out.println("Printing Information for Stock Account: " + stockAccountId + " for stock: " + symbol);

                    Statement transactionQuery = connection.createStatement();

                    ResultSet buySet = transactionQuery.executeQuery(
                        "SELECT BuyTransaction.buycount, BuyTransaction.price " +
                        "FROM BuyTransaction, Transaction " +
                        "WHERE BuyTransaction.customerId = " + customerId + " AND BuyTransaction.stockSym = '" + symbol + "' AND Transaction.transid = BuyTransaction.transid AND EXTRACT(MONTH FROM Transaction.tdate) = " + currMonth
                    );

                    System.out.println("Buy Transactions: ");
                    while (buySet.next()) {
                        System.out.println("Bought #" + Integer.toString(buySet.getInt("buycount")) + " " + symbol + "'s at Price: " + Integer.toString(buySet.getInt("price")));
                    }

                    ResultSet sellSet = transactionQuery.executeQuery(
                        "SELECT SellTransaction.totalCount, SellTransaction.price " +
                        "FROM SellTransaction, Transaction " +
                        "WHERE SellTransaction.customerId = " + customerId + " AND SellTransaction.stockSym = '" + symbol + "' AND Transaction.transid = SellTransaction.transid AND EXTRACT(MONTH FROM Transaction.tdate) = " + currMonth
                    );

                    System.out.println("Sell Transactions: ");
                    while (sellSet.next()) {
                        System.out.println("Bought #" + Integer.toString(sellSet.getInt("totalCount")) + " " + symbol + "'s at Price: " + Integer.toString(sellSet.getInt("price")));
                    }
                }

                Statement balanceQuery = connection.createStatement();
                ResultSet balanceSet = balanceQuery.executeQuery(
                    "SELECT currBalance FROM MarketAccountHistory WHERE markAccId = " + customerId + " AND EXTRACT(MONTH FROM currDate) = " + currMonth +
                    " ORDER BY currDate ASC"
                );

                balanceSet.next();

                System.out.println("Initial Balance: " + Integer.toString(balanceSet.getInt("currBalance")));

                balanceSet = balanceQuery.executeQuery(
                    "SELECT currBalance FROM MarketAccountHistory WHERE markAccId = " + customerId + " AND EXTRACT(MONTH FROM currDate) = " + currMonth +
                    " ORDER BY currDate DESC"
                );

                balanceSet.next();

                System.out.println("Final Balance: " + Integer.toString(balanceSet.getInt("currBalance")));




                
            }
            else if (query.contains("List Active Customers")) {
                System.out.println("Generating Active Customers");

                Statement accountsQuery = connection.createStatement();
                ResultSet AccountsSet = accountsQuery.executeQuery(
                    "SELECT markAccId FROM Customer"
                );

                List<String> activeIds= new ArrayList<String>();

                while (AccountsSet.next()) {
                    String marketId = Integer.toString(AccountsSet.getInt("markAccId"));
                    System.out.println("Checking Activity of : " + marketId);

                    Integer totalShares = 0;

                    Statement shareCount = connection.createStatement();
                    ResultSet buySet = shareCount.executeQuery(
                        "SELECT buycount " +
                        "FROM BuyTransaction " +
                        "WHERE customerId = " + marketId
                    );

                    while (buySet.next()) {
                        totalShares += buySet.getInt("buycount");
                    }

                    System.out.println("Retrieved Buy Count: " + totalShares);

                    ResultSet sellSet = shareCount.executeQuery(
                        "SELECT totalCount " +
                        "FROM SellTransaction " +
                        "WHERE customerId = " + marketId
                    );

                    while (sellSet.next()) {
                        totalShares += sellSet.getInt("totalCount");
                    }

                    if (totalShares >= 1000) activeIds.add(marketId);

                }

                System.out.println("List of Active Accounts: " + activeIds);

            }
            else if (query.contains("Generate DTER")) {

            }
            else if (query.contains("Customer Report")) {
                Integer customerId = Integer.parseInt(split[2]);
                System.out.println("Generating Customer Report for ID: " + customerId);

                Statement reportQuery = connection.createStatement();
                ResultSet balanceSet = reportQuery.executeQuery(
                    "SELECT balance FROM Customer WHERE markAccId = " + customerId
                );

                balanceSet.next();

                System.out.println("Current Balance: " + Integer.toString(balanceSet.getInt("balance")));

                System.out.println("List of Stock Accounts: ");

                Statement stockAccountQuery = connection.createStatement();
                ResultSet stockAccountSet = stockAccountQuery.executeQuery(
                    "SELECT stockAccId, symbol, balance FROM StockAccount WHERE customerId = " + customerId
                );

                while (stockAccountSet.next()) {
                    String sym = stockAccountSet.getString("symbol");
                    String stockId = Integer.toString(stockAccountSet.getInt("stockAccId"));
                    String balance = Integer.toString(stockAccountSet.getInt("balance"));
                    System.out.println("Stock Account ID: " + stockId + " | Stock Symbol: " + sym + " | Balance: " + balance);
                }

            }
            else if (query.contains("Delete Transactions")) {

            }
            else if (query.contains("Open Market")) {
                System.out.println("Current Date: " + currentDate);
                System.out.println("Opening Marketing");

                Statement openMarketQuery = connection.createStatement();
                openMarketQuery.executeUpdate(
                    "UPDATE Market " +
                    "SET isOpen = 1"
                );

                System.out.println("Market is Open");

            }
            else if (query.contains("Close Market")) {
                System.out.println("Current Date: " + currentDate);
                System.out.println("Closing Marketing");

                Statement openMarketQuery = connection.createStatement();
                openMarketQuery.executeUpdate(
                    "UPDATE Market " +
                    "SET isOpen = 0"
                );

                System.out.println("Market is Closed");

                Statement accountsQuery = connection.createStatement();
                ResultSet AccountsSet = accountsQuery.executeQuery(
                        "SELECT markAccId FROM Customer"
                    );

                while (AccountsSet.next()) {
                    String marketId = Integer.toString(AccountsSet.getInt("markAccId"));
                    Statement updateHistoryQuery = connection.createStatement();
                    Statement getBalanceQuery = connection.createStatement();

                    ResultSet marketAccountInfo = getBalanceQuery.executeQuery(
                        "SELECT balance " + 
                        "FROM Customer " +
                        "WHERE markAccId = " + marketId
                    );

                    System.out.println("Obtained All Account ID's");

                    marketAccountInfo.next();

                    String currBalance = Float.toString(marketAccountInfo.getFloat("balance"));

                    updateHistoryQuery.executeUpdate(
                        "INSERT INTO MarketAccountHistory " +
                        "VALUES ( DATE '" + currentDate + "' , " + currBalance + " , " + marketId + " )"
                    );

                    System.out.println("Updated Account #" + marketId);

                }


            }
            else if (query.contains("Set Stock Price")) {
                String symbol = split[3];
                Integer newPrice = Integer.parseInt(split[4]);

                System.out.println("Setting price of '" + symbol + "' to " + newPrice);

                Statement stockPriceQuery = connection.createStatement();
                stockPriceQuery.executeUpdate(
                    "UPDATE Stock " + 
                    "SET curPrice = " + newPrice + " " +
                    "WHERE symbol = '" + symbol + "'"
                );

                System.out.println("Price has been set");

            }
            else if (query.contains("Set Date")) {
                String date = split[2];
                System.out.println("Changing date to " + date);

                Statement setDateQuery = connection.createStatement();
                setDateQuery.executeUpdate(
                    "UPDATE CurrentDate " +
                    "SET currDate = DATE( " + date + " )"
                );

                System.out.println("Date Changed");

            }
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
