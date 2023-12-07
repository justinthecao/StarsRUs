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
                    String correctPassword = resultSet.getString("apassword").trim();
                    if(!correctPassword.equals(password.trim())){
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
                    if (!resultDateSet.next()) {
                        System.out.println("Current Date Not Found");
                        return;
                    }
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

            Statement dateQuery = connection.createStatement();
            ResultSet resultDateSet = dateQuery.executeQuery(
                "SELECT currDate FROM CurrentDate"
            );
            if (!resultDateSet.next()) {
                System.out.println("Current Date Not Found");
                return;
            }
            currentDate = resultDateSet.getDate("currDate").toString();
            System.out.println("Current Date: " + currentDate);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            LocalDate localDate = LocalDate.parse(currentDate, formatter);

            int currMonth = localDate.getMonthValue();
            int today = localDate.getDayOfMonth();

            String[] split  = query.split(" ");
            
            if (query.contains("Add Interest")) {

                Integer monthLength = localDate.lengthOfMonth();

                if (today != monthLength) {
                    System.out.println("ERROR: Not end of month yet!");
                    return;
                }

                Statement openQuery = connection.createStatement();
                ResultSet openSet = openQuery.executeQuery(
                    "SELECT isOpen FROM Market"
                );

                if (!openSet.next()) {
                    System.out.println("ERROR: Data on Market Not Found");
                    return;
                }

                if (openSet.getInt("isOpen") == 1) {
                    System.out.println("ERROR: Market has not closed yet!");
                    return;
                }

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

                        System.out.println("\nGetting Current Balance, Market Id: " + marketId);
                        ResultSet currBalanceSet = interestQuery.executeQuery(
                            "SELECT balance " +
                            "FROM Customer " +
                            "WHERE markAccId = " + marketId
                        );

                        if (!currBalanceSet.next()) {
                            System.out.println("Current Balance Not Found");
                            return;
                        }

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
                            //System.out.println("previous day: " + prevDay);
                            //System.out.println("previous balance: " + prevBalance);
                            Integer currentDay = accountHistorySet.getInt("currDay");
                            Float currentBalance = accountHistorySet.getFloat("currBalance");
                            Integer period = currentDay - prevDay;
                            totalDays += period;
                            totalBalance += prevBalance * period;
                            prevDay = currentDay;
                            prevBalance = currentBalance;
                        }

                        //System.out.println("previous day: " + prevDay);
                        //System.out.println("previous balance: " + prevBalance);

                        Integer period = today - prevDay + 1;
                        totalDays += period;
                        totalBalance += prevBalance * period;

                        // first day 1: balance = 100
                        // second day 5: balance = 200
                        // current day is 10

                        //System.out.println("Average Daily Balance: " + totalBalance / totalDays);

                        Float interestGain = (float) ((totalBalance / totalDays) * 0.02);

                        Float newBalance = (float) (currBalance + interestGain);

                        interestQuery.executeUpdate(
                            "UPDATE Customer " +
                            "SET balance = " + newBalance + " " +
                            "WHERE markAccId = " + marketId
                        );

                        System.out.println("Updated Balance with Accrued Interest: " + newBalance);

                        interestQuery.executeUpdate(
                            "INSERT INTO InterestHistory VALUES (" + marketId + ", " + (interestGain) + ")"
                        );

                        System.out.println("Amount Gained from Interest: " + (interestGain));

                        Statement statement = connection.createStatement();

                        int newTransId = addNewTransaction(statement, 5, currentDate);
                        String moneytranQuery = "INSERT INTO MoneyTransaction VALUES (" + newTransId + ", " + interestGain + "," + marketId + ")";
                        statement.executeUpdate(moneytranQuery);

                    }
                }

            }
            else if (query.contains("Generate Monthly Statement")) {

                if (split.length < 4) {
                    System.out.println("ERROR: Not Enough Arguments");
                    return;
                }

                Integer customerId;
                try {
                    customerId = Integer.parseInt(split[3]);
                }
                catch (Exception e) {
                    System.out.println("ERROR: Wrong Input");
                    return;
                }

                Statement customerQuery = connection.createStatement();
                ResultSet customerInfo = customerQuery.executeQuery(
                    "SELECT cname, email_address " +
                    "FROM Customer " +
                    "WHERE markAccId = " + customerId
                );

                if (!customerInfo.next()) {
                    System.out.println("ERROR: User Not Found");
                    return;
                }

                System.out.println("Generating Monthly Statement for Customer: \nName: " + customerInfo.getString("cname") + "\nEmail: " + customerInfo.getString("email_address"));

                String a = "SELECT DISTINCT T.transid FROM (" + 
                    "SELECT C.transid as transid " +
                    "FROM CancelTransaction C " +
                    "WHERE C.custId = "+ customerId +
                
                    " UNION SELECT B.transid as transid " +
                    "FROM BuyTransaction B " +
                    "WHERE B.customerId = "+ customerId +

                    " UNION SELECT M.transid as transid " +
                    "FROM MoneyTransaction M " +
                    "WHERE M.customerId = " + customerId +
                    
                    " UNION SELECT S.transid as transid " +
                    "FROM SellTransaction S " +
                    "WHERE S.customerId = "+ customerId + ") T ORDER BY T.transid";

                Statement statement = connection.createStatement();
                ResultSet transaction = statement.executeQuery(a);

                Integer totalCommissions = 0;

                Statement subStatement = connection.createStatement();

                float totalProfit = 0;

                while(transaction.next()){
                    int transid = transaction.getInt("transid");

                    String tranDetail = "SELECT * FROM Transaction WHERE transid = "+ transid;
                    ResultSet tranDetailSet = subStatement.executeQuery(tranDetail);
                    if (!tranDetailSet.next()) {
                        System.out.println("ERROR: Transaction Not Found");
                        continue;
                    }

                    int type = tranDetailSet.getInt("ttype");
                    Date tdate = tranDetailSet.getDate("tdate");
                    if(type == 2){
                        totalCommissions += 20;
                        String getBuy = "SELECT * FROM BuyTransaction WHERE transid = " + transid;
                        ResultSet getBuySet = subStatement.executeQuery(getBuy);
                        if (!getBuySet.next()) {
                            System.out.println("ERROR: Buy Transaction Not Found");
                            continue;
                        }
                        float price = getBuySet.getFloat("price");
                        float buycount = getBuySet.getFloat("buycount");
                        String symbol = getBuySet.getString("stockSym");
                        System.out.println(tdate + ": Buy - " + symbol + ", " + buycount + " at $" + price);
                    }
                    else if(type == 3){
                        totalCommissions += 20;
                        String getSell = "SELECT * FROM SellTransaction WHERE transid = " + transid;
                        ResultSet getSellSet = subStatement.executeQuery(getSell);
                        if (!getSellSet.next()) {
                            System.out.println("ERROR: Sell Transaction Not Found");
                            continue;
                        }
                        float price = getSellSet.getFloat("price");
                        float Sellcount = getSellSet.getFloat("totalCount");
                        float profit = getSellSet.getFloat("profit");
                        totalProfit += profit;
                        String symbol = getSellSet.getString("stockSym");
                        System.out.printf(tdate + ": Sell - " + symbol + ", " + Sellcount + " at $" + price + " - Profit : $" + " %.2f \n",profit);  
                    }
                    else if (type == 5) {
                        String getInterest = "SELECT * FROM MoneyTransaction WHERE transid = " + transid;
                        ResultSet getInterestSet = subStatement.executeQuery(getInterest);
                        if (!getInterestSet.next()) {
                            System.out.println("ERROR: Interest Transaction Not Found");
                            continue;
                        }
                        float interest = getInterestSet.getFloat("amountMoney");
                        totalProfit += interest;
                        System.out.printf(tdate + ": Interest - $" + "%.2f \n",interest);  
                    }
                    else if (type == 4){
                        totalCommissions += 40;
                        System.out.println(tdate + ": Cancelled.");
                    }
                    else if (type == 0) {
                        String getInterest = "SELECT * FROM MoneyTransaction WHERE transid = " + transid;
                        ResultSet getInterestSet = subStatement.executeQuery(getInterest);
                        if (!getInterestSet.next()) {
                            System.out.println("ERROR: Deposit Transaction Not Found");
                            continue;
                        }
                        float interest = getInterestSet.getFloat("amountMoney");
                        System.out.printf(tdate + ": Deposit - $" + "%.2f \n",interest);  
                    }
                    else if (type == 1) {
                        String getInterest = "SELECT * FROM MoneyTransaction WHERE transid = " + transid;
                        ResultSet getInterestSet = subStatement.executeQuery(getInterest);
                        if (!getInterestSet.next()) {
                            System.out.println("ERROR: Withdraw Transaction Not Found");
                            continue;
                        }
                        float interest = getInterestSet.getFloat("amountMoney");
                        System.out.printf(tdate + ": Withdraw - $" + "%.2f \n",interest);  
                    }
                }

                // Statement monthlyQuery = connection.createStatement();
                // ResultSet stockAccountsSet = monthlyQuery.executeQuery(
                //     "SELECT symbol, stockAccId " + 
                //     "FROM StockAccount " + 
                //     "WHERE customerId = " + customerId
                // );

                // Integer totalCommissions = 0;

                // while (stockAccountsSet.next()) {
                //     String symbol = stockAccountsSet.getString("symbol");
                //     String stockAccountId = Integer.toString(stockAccountsSet.getInt("stockAccId"));

                //     System.out.println("\nPrinting Information for Stock Account: " + stockAccountId + " for stock: " + symbol);

                //     Statement transactionQuery = connection.createStatement();

                //     ResultSet buySet = transactionQuery.executeQuery(
                //         "SELECT BuyTransaction.buycount, BuyTransaction.price, Transaction.tdate " +
                //         "FROM BuyTransaction, Transaction " +
                //         "WHERE BuyTransaction.customerId = " + customerId + " AND BuyTransaction.stockSym = '" + symbol + "' AND Transaction.transid = BuyTransaction.transid AND EXTRACT(MONTH FROM Transaction.tdate) = " + currMonth +
                //         " ORDER BY Transaction.tdate"
                //     );

                //     System.out.println("\nBuy Transactions: ");
                //     while (buySet.next()) {
                //         System.out.println(buySet.getDate("tdate") + " | Bought #" + Float.toString(buySet.getFloat("buycount")) + " " + symbol + "'s at Price: " + Float.toString(buySet.getFloat("price")));
                //         totalCommissions += 20;
                //     }

                //     ResultSet sellSet = transactionQuery.executeQuery(
                //         "SELECT SellTransaction.totalCount, SellTransaction.price, SellTransaction.profit, Transaction.tdate " +
                //         "FROM SellTransaction, Transaction " +
                //         "WHERE SellTransaction.customerId = " + customerId + " AND SellTransaction.stockSym = '" + symbol + "' AND Transaction.transid = SellTransaction.transid AND EXTRACT(MONTH FROM Transaction.tdate) = " + currMonth +
                //         " ORDER BY Transaction.tdate"
                //     );

                //     System.out.println("\nSell Transactions: ");
                //     while (sellSet.next()) {
                //         System.out.println(sellSet.getDate("tdate") + " | Sold #" + Float.toString(sellSet.getFloat("totalCount")) + " " + symbol + "'s at Price: " + Float.toString(sellSet.getFloat("price")) + " w/ Profit: " + Float.toString(sellSet.getFloat("profit")));
                //         totalCommissions += 20;
                //     }

                //     ResultSet cancelSet = transactionQuery.executeQuery(
                //         "SELECT CancelTransaction.transid, Transaction.tdate " +
                //         "FROM CancelTransaction, Transaction " +
                //         "WHERE CancelTransaction.custId = " + customerId + " AND CancelTransaction.cancelSym = '" + symbol + "' AND Transaction.transid = CancelTransaction.transid AND EXTRACT(MONTH FROM Transaction.tdate) = " + currMonth +
                //         " ORDER BY Transaction.tdate"
                //     );

                //     System.out.println("\nCancel Transactions: ");
                //     while (cancelSet.next()) {
                //         System.out.println( cancelSet.getDate("tdate") + " | Cancel Transaction ID: " + Integer.toString(cancelSet.getInt("transid")));
                //         totalCommissions += 40;
                //     }
                // }

                Statement balanceQuery = connection.createStatement();
                ResultSet balanceSet = balanceQuery.executeQuery(
                    "SELECT currBalance FROM MarketAccountHistory WHERE markAccId = " + customerId + " AND EXTRACT(MONTH FROM currDate) = " + currMonth +
                    " ORDER BY currDate ASC"
                );

                Float initialBalance = (float) 0;

                if (balanceSet.next()) {
                    initialBalance = balanceSet.getFloat("currBalance");
                    System.out.printf("\nInitial Balance: $" + "%.2f \n", initialBalance);
                }
                else {
                    System.out.println("\nInitial Balance not found...");
                }

                //check if balance exists -- 

                balanceSet = balanceQuery.executeQuery(
                    "SELECT balance FROM Customer WHERE markAccId = " + customerId
                );

                Float finalBalance = (float) 0;

                if (balanceSet.next()) {
                    finalBalance = balanceSet.getFloat("balance");
                    System.out.printf("Final Balance: $" + "%.2f \n", finalBalance);
                }
                else{
                    System.out.println("Final Balance not found...");
                }

                System.out.println("Total Commissions: $" + totalCommissions);

                System.out.printf("Total Earnings/Loss: $%.2f \n", totalProfit);

                
            }
            else if (query.contains("List Active Customers")) {
                System.out.println("Generating Active Customers");

                Statement accountsQuery = connection.createStatement();
                ResultSet AccountsSet = accountsQuery.executeQuery(
                    "SELECT markAccId, username FROM Customer"
                );

                List<String> activeUserNames= new ArrayList<String>();

                while (AccountsSet.next()) {
                    String marketId = Integer.toString(AccountsSet.getInt("markAccId"));
                    String username = (AccountsSet.getString("username"));

                    //System.out.println("Checking Activity of : " + marketId);

                    Float totalShares = (float) 0;

                    Statement shareCount = connection.createStatement();
                    ResultSet buySet = shareCount.executeQuery(
                        "SELECT BuyTransaction.buycount " +
                        "FROM BuyTransaction, Transaction " +
                        "WHERE BuyTransaction.customerId = " + marketId + " AND BuyTransaction.transid = Transaction.transid AND EXTRACT(MONTH FROM Transaction.tdate) = " + currMonth
                    );

                    while (buySet.next()) {
                        totalShares += buySet.getFloat("buycount");
                    }

                    //System.out.println("Retrieved Buy Count: " + totalShares);

                    ResultSet sellSet = shareCount.executeQuery(
                        "SELECT SellTransaction.totalCount " +
                        "FROM SellTransaction, Transaction " +
                        "WHERE SellTransaction.customerId = " + marketId + " AND SellTransaction.transid = Transaction.transid AND EXTRACT(MONTH FROM Transaction.tdate) = " + currMonth
                    );

                    while (sellSet.next()) {
                        totalShares += sellSet.getFloat("totalCount");
                    }

                    if (totalShares >= 1000) activeUserNames.add(username.trim());

                }

                System.out.println("List of Active Accounts: " + activeUserNames);

            }
            else if (query.contains("Generate DTER")) {

                Statement accountsQuery = connection.createStatement();
                ResultSet AccountsSet = accountsQuery.executeQuery(
                        "SELECT markAccId, username, cstate FROM Customer"
                    );

                List<String> dterUserNames= new ArrayList<String>();

                while (AccountsSet.next()) {
                    String marketId = Integer.toString(AccountsSet.getInt("markAccId"));
                    String username = (AccountsSet.getString("username"));
                    String state = AccountsSet.getString("cstate");
                    Float totalProfit = (float) 0;

                    Statement profitQuery = connection.createStatement();
                    ResultSet profitSet = profitQuery.executeQuery(
                        "SELECT SellTransaction.profit " +
                        "FROM SellTransaction, Transaction " +
                        "WHERE SellTransaction.customerId = " + marketId + " AND SellTransaction.transid = Transaction.transid AND EXTRACT(MONTH FROM Transaction.tdate) = " + currMonth
                    );
                    
                    while(profitSet.next()){
                        totalProfit += profitSet.getFloat("profit");
                    }

                    Statement interestQuery = connection.createStatement();
                    ResultSet interestSet = interestQuery.executeQuery(
                        "SELECT interestEarning " + 
                        "FROM InterestHistory " + 
                        "WHERE customerId = " + marketId
                    );

                    if(interestSet.next()) {
                        totalProfit += interestSet.getFloat("interestEarning");
                    }
                    else {
                        System.out.println("WARNING: No Interest History found... not added to DTER calculation for User: " + username);
                    }

                    if (totalProfit >= 10000) dterUserNames.add(username.trim() + "-" + state.trim());

                }

                System.out.println("List of DTER Accounts: " + dterUserNames);

            }
            else if (query.contains("Customer Report")) {
                if (split.length < 3) {
                    System.out.println("ERROR: Not Enough Arguments");
                    return;
                }
                Integer customerId;
                try {
                    customerId = Integer.parseInt(split[2]);
                }
                catch (Exception e) {
                    System.out.println("ERROR: Wrong Input");
                    return;
                }
                System.out.println("Generating Customer Report for ID: " + customerId);

                Statement reportQuery = connection.createStatement();
                ResultSet balanceSet = reportQuery.executeQuery(
                    "SELECT balance FROM Customer WHERE markAccId = " + customerId
                );

                if (balanceSet.next()) {
                    System.out.println("Market Account Balance: " + Float.toString(balanceSet.getFloat("balance")));
                }
                else {
                    System.out.println("ERROR: User does not have a market account");
                    return;
                }

                System.out.println("List of Stock Accounts: ");

                Statement stockAccountQuery = connection.createStatement();
                ResultSet stockAccountSet = stockAccountQuery.executeQuery(
                    "SELECT stockAccId, symbol, balance FROM StockAccount WHERE customerId = " + customerId
                );

                while (stockAccountSet.next()) {
                    String sym = stockAccountSet.getString("symbol");
                    String stockId = Integer.toString(stockAccountSet.getInt("stockAccId"));
                    String balance = Float.toString(stockAccountSet.getFloat("balance"));
                    System.out.println("Stock Account ID: " + stockId + " | Stock Symbol: " + sym + " | Balance: " + balance);
                }

            }
            else if (query.contains("Delete Transactions")) {
                System.out.println("Deleting All Transactions");

                Statement deleteQuery = connection.createStatement();
                deleteQuery.executeUpdate(
                    "DELETE FROM SellTransaction"
                );

                deleteQuery.executeUpdate(
                    "DELETE FROM BuyTransaction"
                );
                
                deleteQuery.executeUpdate(
                    "DELETE FROM CancelTransaction"
                );

                deleteQuery.executeUpdate(
                    "DELETE FROM MoneyTransaction"
                );

                deleteQuery.executeUpdate(
                    "DELETE FROM Transaction"
                );

                deleteQuery.executeUpdate(
                    "DELETE FROM InterestHistory"
                );

                System.out.println("All Transactions have been deleted");

            }
            else if (query.contains("Open Market")) {
                System.out.println("Opening Marketing");

                Statement openQuery = connection.createStatement();
                ResultSet openSet = openQuery.executeQuery(
                    "SELECT isOpen FROM Market"
                );

                if (!openSet.next()) {
                    System.out.println("ERROR: Data on Market Not Found");
                    return;
                }

                if(openSet.getInt("isOpen") == 1) {
                    System.out.println("ERROR: Market is Already Open");
                    return;
                }

                Statement checkPastQuery = connection.createStatement();
                ResultSet checkPastSet = checkPastQuery.executeQuery(
                    "SELECT * FROM MarketAccountHistory WHERE currDate = DATE '" + currentDate + "'"
                );

                if (checkPastSet.next()) {
                    System.out.println("ERROR: Market has been opened before on this date");
                    return;
                }

                Statement openMarketQuery = connection.createStatement();
                openMarketQuery.executeUpdate(
                    "UPDATE Market " +
                    "SET isOpen = 1"
                );

                System.out.println("Market is Open");

            }
            else if (query.contains("Close Market")) {
                System.out.println("Closing Market");

                Statement openQuery = connection.createStatement();
                ResultSet openSet = openQuery.executeQuery(
                    "SELECT isOpen FROM Market"
                );

                if (!openSet.next()) {
                    System.out.println("ERROR: Data on Market Not Found");
                    return;
                }

                if(openSet.getInt("isOpen") == 0) {
                    System.out.println("ERROR: Market is Already Closed");
                    return;
                }

                Statement openMarketQuery = connection.createStatement();
                openMarketQuery.executeUpdate(
                    "UPDATE Market " +
                    "SET isOpen = 0"
                );

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

                    //System.out.println("Obtained All Account ID's");

                    if (!marketAccountInfo.next()) {
                        System.out.println("ERROR: No balance found for " + marketId);
                    }

                    String currBalance = Float.toString(marketAccountInfo.getFloat("balance"));

                    updateHistoryQuery.executeUpdate(
                        "INSERT INTO MarketAccountHistory " +
                        "VALUES ( DATE '" + currentDate + "' , " + currBalance + " , " + marketId + " )"
                    );

                    //System.out.println("Updated Account #" + marketId);

                }

                Statement currPriceQuery = connection.createStatement();
                ResultSet currPriceSet = currPriceQuery.executeQuery(
                    "SELECT curPrice, symbol FROM Stock"
                );

                while (currPriceSet.next()) {
                    Float price = currPriceSet.getFloat("curPrice");
                    String symbol = currPriceSet.getString("symbol");
                    Statement closingPriceQuery = connection.createStatement();
                    closingPriceQuery.executeUpdate(
                        "INSERT INTO Price VALUES ('" + symbol + "', DATE '" + currentDate + "', " + price + ")" 
                    );
                    //System.out.println("Updated Closing Prices For " + symbol);
                }

                System.out.println("Market is Closed");


            }
            else if (query.contains("Set Stock Price")) {
                if (split.length < 5) {
                    System.out.println("ERROR: Not Enough Arguments");
                    return;
                }
                String symbol;
                String newPrice;

                try {
                    symbol = split[3];
                }
                catch (Exception e) {
                    System.out.println("ERROR: Invalid Symbol");
                    return;
                }

                try {
                    newPrice = Float.toString(Float.parseFloat(split[4]));
                }
                catch (Exception ec) {
                    System.out.println("ERROR: Invalid Price");
                    return;
                }

                String symbolquery = "SELECT * FROM Stock WHERE symbol = '" + symbol + "'";
                Statement statement = connection.createStatement();
                ResultSet symbolSet = statement.executeQuery(
                    symbolquery
                );
                if(!symbolSet.next()){
                    System.out.println("ERROR: No symbol found!");
                    return;
                }

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
                if (split.length < 3) {
                    System.out.println("ERROR: Not Enough Arguments");
                    return;
                }
                String date;
                try {
                    date = split[2];
                }
                catch (Exception e) {
                    System.out.println("ERROR: Invalid Date");
                    return;
                }

                try {
                    LocalDate newDate = LocalDate.parse(date);
                }
                catch (Exception e) {
                    System.out.println("ERROR: Date is not valid");
                    return;
                }

                System.out.println("Changing date to " + date);

                Statement setDateQuery = connection.createStatement();
                setDateQuery.executeUpdate(
                    "UPDATE CurrentDate " +
                    "SET currDate = DATE '" + date + "' "
                );

                System.out.println("Date Changed");

            }
        }









        static int getNewTransId(Statement statement) throws SQLException {
            String getMaxTransId = "SELECT MAX(transid) as max FROM Transaction";
            ResultSet transSet = statement.executeQuery(getMaxTransId);
            transSet.next();
            if (!(transSet.getString("max") == null)) {
                return transSet.getInt("max") + 1;
            }
            return 0;
        }
    
        static int addNewTransaction(Statement statement, int type, String date) throws SQLException {
            int newTransId = getNewTransId(statement);
            //System.out.println("new transid: " + newTransId);
            String tranQuery = "INSERT INTO Transaction VALUES (" + newTransId + ", " + type + ", DATE '" + date + "')";
            statement.executeUpdate(tranQuery);
            return newTransId;
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
