//Justin Cao and Collin Qian UCSB CS174A Database project

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

import javax.naming.spi.DirStateFactory.Result;

import oracle.jdbc.pool.OracleDataSource;
import oracle.jdbc.OracleConnection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;

public class TraderInterface {
    final static String DB_URL = "jdbc:oracle:thin:@jcoracledb1_tp?TNS_ADMIN=./Wallet_JCORACLEDB1";
    final static String DB_USER = "ADMIN";
    final static String DB_PASSWORD = "Password123!@#";
    final static Scanner scanner = new Scanner(System.in);
    static String currentUser;
    static int markAccId;

    public static void main(String args[]) throws SQLException {
        Properties info = new Properties();

        info.put(OracleConnection.CONNECTION_PROPERTY_USER_NAME, DB_USER);
        info.put(OracleConnection.CONNECTION_PROPERTY_PASSWORD, DB_PASSWORD);
        info.put(OracleConnection.CONNECTION_PROPERTY_DEFAULT_ROW_PREFETCH, "20");

        OracleDataSource ods = new OracleDataSource();

        ods.setURL(DB_URL);
        ods.setConnectionProperties(info);

        try (OracleConnection connection = (OracleConnection) ods.getConnection()) {
            System.out.println("");
            
            System.out.println("Do you want to Login or Register?");
            if(scanner.nextLine().toLowerCase().equals("login")){
                System.out.println("\nLogin\n======== \nUsername: ");
                String username = scanner.nextLine();
                System.out.println("Password: ");
                String password = scanner.nextLine();
                try (Statement query = connection.createStatement()) {
                    ResultSet resultSet = query.executeQuery(
                            "SELECT * FROM Customer WHERE username = '" + username + "'");
                    resultSet.next();
                    if (!resultSet.getString("cpassword").trim().equals(password.trim())) {
                        System.out.println("Incorrect Username/Password :(");
                        return;
                    }
                    markAccId = resultSet.getInt("markAccId");
                } catch (Exception e) {
                    System.out.println("Exception: Incorrect Username/Password :(");
                    return;
                }
                currentUser = username;
                System.out.println("\nYou're in " + markAccId + "! What do you wanna do.");
            }
            else{
                try{
                    System.out.println("\nWhat is your name?");
                    String firstname = scanner.nextLine();
                    System.out.println("What state are you in? (Two-Digit)");
                    String state = scanner.nextLine();
                    System.out.println("What is your phone number [(xxx)xxxxxxx]?");
                    String phone = scanner.nextLine();
                    System.out.println("What is your email address?");
                    String email = scanner.nextLine();
                    System.out.println("What is your tax id");
                    String taxid = scanner.nextLine();
                    System.out.print("New Username: ");
                    String username = scanner.nextLine();
                    System.out.print("New Password: ");
                    String password = scanner.nextLine();
                    int newMark = getAccId(connection);
                    int result = validate(connection, username, taxid);
                    while(result != 0){
                        if(result == 1){
                            System.out.println("Try a new taxid:");
                            taxid = scanner.nextLine();
                        }
                        else if(result == 2){
                            System.out.println("Try a new username:");
                            username = scanner.nextLine();
                        }
                        else if(result == 3){
                            System.out.println("Taxid is at most 9 digits, try a new taxid:");
                            taxid = scanner.nextLine();
                        }
                        result = validate(connection, username, taxid);
                    }
                    String insertCustomer = "INSERT INTO Customer VALUES ('" + username + "', '" + firstname + "', '" + password + "', '" + state + "','"  + phone + "', '"  + email + "'," + taxid + ", " + newMark + ", 1000)";
                    Statement statement = connection.createStatement();
                    try{
                        statement.executeQuery(insertCustomer);
                    }catch(SQLException e){
                        e.printStackTrace();
                        System.out.println("Something went wrong with registering");
                    }
                    currentUser = username;
                    markAccId = newMark;
                    System.out.println("\nYou're in " + markAccId + "! What do you wanna do.");

                } catch(Exception e){
                    System.out.println("The system encountered an error, try registering again.");
                }
                
            }
            
            String input;
            while (!(input = scanner.nextLine()).equals("exit")) {

                handleOutput(connection, input);
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("CONNECTION ERROR:");
            System.out.println(e);
        }

    }

    static void handleOutput(OracleConnection connection, String query) throws SQLException{
        boolean marketOpen = checkMarket(connection);
        String date = getDate(connection).toString();
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
                System.out.println("Deposit: Something went wrong...try again.");
                return;
            }
        }

        else if(query.contains("Withdraw")){
            String amount = split[1];
            System.out.println("Withdrawing " + amount + " dollars!");
            try(Statement statement = connection.createStatement()){
                Float balance = getUserBalance(statement);
                if(balance < Float.parseFloat(amount)){
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
            if(!marketOpen){
                System.out.println("Market is closed. Check back later.");
                return;
            }
            if(split.length < 2){
                System.out.println("Need to include symbol and amount.");
                return;
            }
            String symbol = split[1];
            float amount = Float.parseFloat(split[2]);

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
                if((curPrice * amount + 20) > balance){
                    System.out.println("You don't have enough money. Cancelling...");
                    return;
                }
                //------
                float amounttosubtract =amount * curPrice + 20 ;
                //update money in markacc
                String depositQuery = "UPDATE Customer SET balance = balance - (" + amounttosubtract + ") WHERE username = '" + currentUser + "'";
                statement.executeUpdate(depositQuery);
                //------

                //insert new transaction
                int newTransId = addNewTransaction(statement, 2, date);
                //if its a new stock for customer make a new one
                String getAllStock = "SELECT * FROM StockAccount WHERE customerId = " + markAccId + " AND symbol = '" + symbol + "'";
                ResultSet stockAccSet = statement.executeQuery(getAllStock);
                int stockAccId;
                if(!stockAccSet.next()){
                    stockAccId = getAccId(connection);
                    String addStockAcc = "INSERT INTO StockAccount VALUES (" + stockAccId+ ", " + markAccId + ", 0, '" + symbol + "')";
                    statement.executeQuery(addStockAcc);
                }
                else{
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

                System.out.println("Bought " + amount + " " + symbol + "'s at $" + curPrice );
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
            if(!marketOpen){
                System.out.println("Market is closed. Check back later.");
                return;
            }
            if(split.length%2 == 0 || split.length < 3){
                System.out.println("Incorrect sell format");
                return;
            }
            String symbol = split[1];
            float amount = Float.parseFloat(split[2]);

            float add = 0;
            HashMap<Float, Float> sell = new HashMap<Float, Float>();
            for(int i = 3; i< split.length; i+=2){
                add += Float.parseFloat(split[i]);
                sell.put(Float.parseFloat(split[i+1]), Float.parseFloat(split[i]));
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
                Float curPrice = symbolSet.getFloat("curPrice");
                Float balance = getUserBalance(statement);
                if((balance + amount * curPrice - 20 ) < 0){
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
                HashMap<Float, Float> amounts = new HashMap<Float, Float>();
                while(stockAmounts.next()){
                    amounts.put(stockAmounts.getFloat("price"), stockAmounts.getFloat("amount"));
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
                float profit = 0;
                //sell is price: amount
                for(Float i: sell.keySet()){
                    String updateAmount = "UPDATE StockAmount SET amount = amount - " + sell.get(i) + " WHERE stockAccId = " + stockAccId + " AND price = " + i;
                    statement.executeUpdate(updateAmount);
                    profit += (curPrice - i) * sell.get(i);
                }

                //updating stock account balance
                String updateStock = "UPDATE StockAccount SET balance = balance - " + amount + " WHERE stockAccid = " + stockAccId;
                statement.executeUpdate(updateStock);

                //adding new trans and selltrans
                int newTransId = addNewTransaction(statement,3, date);


                String sellTransaction = "INSERT INTO SellTransaction VALUES (" + newTransId +"," + amount + "," + markAccId + ", '" + symbol + "', " + curPrice + "," + profit + ")";
                statement.executeUpdate(sellTransaction);
                //adding sell buy into sellcountsbuy
                for(Float i: sell.keySet()){
                    String addSellCountsBuy = "INSERT INTO SellCountsBuy VALUES ( "+ newTransId + ", '" + symbol + "', " + markAccId + ", " + i  + ", " + sell.get(i) + ")";
                    statement.executeUpdate(addSellCountsBuy);
                }
                
                Float toRemove = amount * curPrice - 20;
                String depositQuery = "UPDATE Customer SET balance = balance + (" + toRemove + ") WHERE username = '" + currentUser + "'";
                statement.executeUpdate(depositQuery); 
                System.out.println("Sold " + amount + " " + symbol + "'s at $" + curPrice);
                
            } catch(Exception e){
                e.printStackTrace();
                System.out.println("Something went wrong...try again.");
            }

            
        }
        else if(query.contains("Cancel")){  
            if(!marketOpen){
                System.out.println("Market is closed. Check back later.");
                return;
            }          
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
                    String getRecentType = "SELECT * FROM Transaction WHERE transid = " + mostRecent;
                    ResultSet recentType = statement.executeQuery(getRecentType);
                    recentType.next();
                    int type = recentType.getInt("ttype");
                    if(type != 2 && type != 3){
                        System.out.println("most previous transaction was not buy or sell. bad.");
                        return;
                    }
                    if(!recentType.getDate("tdate").toString().equals(getDate(connection).toString())){
                        System.out.println(recentType.getDate("tdate").toString() );
                        System.out.println(getDate(connection).toString());
                        System.out.println("Cannot cancel transaction made on another day.");
                        return;
                    }
                    if(type == 2){
                        System.out.println("Cancelling the last buy transaction.");

                        revertBuy(connection, statement, mostRecent, date);
                    }
                    else{
                        System.out.println("Cancelling the last sell transaction.");
                        revertSell(connection, statement, mostRecent, date);
                    }
                }
                else{
                    System.out.println("Don't have any transactions. try again.");
                    return;
                }
            }
        }

        else if(query.contains("Show Balance")){ 
            System.out.println("Heres your balance:");
            Statement statement = connection.createStatement();
            float balance = getUserBalance(statement);
            String a = String.valueOf(balance);
            if(a.indexOf('.') == -1){
                System.out.println("$" + balance + ".00");
                return;
            }
            if(a.indexOf('.') == a.length()-2){
                System.out.println("$" + balance + "0");
                return;
            }

            if(a.indexOf('.') < a.length()-2){
                System.out.println("$" + a.substring(0, a.indexOf('.')+ 3));
                return;
            }
        }
        else if(query.contains("Show Transaction")){
            if(split.length < 3){
                System.out.println("Need to include symbol.");
                return;
            }
            System.out.print("\n");
            try{String symbol = split[2];
                String a = "SELECT DISTINCT T.transid FROM (" + 
                    "SELECT C.transid as transid " +
                    "FROM CancelTransaction C " +
                    "WHERE C.custId = "+ markAccId +" AND C.cancelSym = '" + symbol + "'"+
                
                    " UNION SELECT B.transid as transid " +
                    "FROM BuyTransaction B " +
                    "WHERE B.customerId = "+ markAccId +" AND B.stockSym = '" + symbol + "'"+
                    
                    " UNION SELECT S.transid as transid " +
                    "FROM SellTransaction S " +
                    "WHERE S.customerId = "+ markAccId + " AND S.stockSym = '" + symbol + "'"+ ") T ORDER BY T.transid";
                
                Statement statement = connection.createStatement();
                System.out.println("Here are the transactions for " + symbol + ":");
                ResultSet transaction = statement.executeQuery(a);
                Statement subStatement = connection.createStatement();
                if(transaction.next()){
                    int transid = transaction.getInt("transid");

                    String tranDetail = "SELECT * FROM Transaction WHERE transid = "+ transid;
                    ResultSet tranDetailSet = subStatement.executeQuery(tranDetail);
                    tranDetailSet.next();

                    int type = tranDetailSet.getInt("ttype");
                    Date tdate = tranDetailSet.getDate("tdate");
                    if(type == 2){
                        String getBuy = "SELECT * FROM BuyTransaction WHERE transid = " + transid;
                        ResultSet getBuySet = subStatement.executeQuery(getBuy);
                        getBuySet.next();
                        float price = getBuySet.getFloat("price");
                        float buycount = getBuySet.getFloat("buycount");
                        System.out.println(tdate + ": Buy - " + symbol + ", " + buycount + " at $" + price);
                    }
                    else if(type == 3){
                        String getSell = "SELECT * FROM SellTransaction WHERE transid = " + transid;
                        ResultSet getSellSet = subStatement.executeQuery(getSell);
                        getSellSet.next();
                        float price = getSellSet.getFloat("price");
                        float Sellcount = getSellSet.getFloat("totalCount");
                        System.out.printf(tdate + ": Sell - " + symbol + ", " + Sellcount + " at $" + price + " - Profit : " + "$" +  "%.2f\n", getSellSet.getFloat("profit"));  
                    }
                    else{
                        System.out.println(tdate + ": Cancelled.");
                    }
                } else{
                    System.out.println("No Transactions for " + symbol);
                    return;
                }
                while(transaction.next()){
                    int transid = transaction.getInt("transid");

                    String tranDetail = "SELECT * FROM Transaction WHERE transid = "+ transid;
                    ResultSet tranDetailSet = subStatement.executeQuery(tranDetail);
                    tranDetailSet.next();

                    int type = tranDetailSet.getInt("ttype");
                    Date tdate = tranDetailSet.getDate("tdate");
                    if(type == 2){
                        String getBuy = "SELECT * FROM BuyTransaction WHERE transid = " + transid;
                        ResultSet getBuySet = subStatement.executeQuery(getBuy);
                        getBuySet.next();
                        float price = getBuySet.getFloat("price");
                        float buycount = getBuySet.getFloat("buycount");
                        System.out.println(tdate + ": Buy - " + symbol + ", " + buycount + " at $" + price);
                    }
                    else if(type == 3){
                        String getSell = "SELECT * FROM SellTransaction WHERE transid = " + transid;
                        ResultSet getSellSet = subStatement.executeQuery(getSell);
                        getSellSet.next();
                        float price = getSellSet.getFloat("price");
                        float Sellcount = getSellSet.getFloat("totalCount");
                        System.out.printf(tdate + ": Sell - " + symbol + ", " + Sellcount + " at $" + price + " - Profit : " + "$" +  "%.2f\n", getSellSet.getFloat("profit"));  
                    }
                    else{
                        System.out.println(tdate + ": Cancelled.");
                    }
                }
            } catch (SQLException e){
                e.printStackTrace();
            }
        }
        else if(query.contains("Symbol")){
            if(split.length < 2){
                System.out.println("Need to include symbol name");
                return;
            }
            System.out.print("\n");
            String symbol = split[1];
            String getStock = "SELECT * FROM Stock WHERE symbol = '" + symbol + "'";
            Statement statement = connection.createStatement();
            ResultSet stockSet = statement.executeQuery(getStock);
            if(!stockSet.next()){
                System.out.println("No symbol found!");
                return;
            }
            float curPrice = stockSet.getFloat("curPrice");
            String star = stockSet.getString("starname");
            Date dob = stockSet.getDate("dob");
            System.out.println("Stock Info: " + symbol.trim());
            System.out.println("   Current Price: $" + curPrice);
            System.out.println("   Actor: " + star.trim());
            System.out.println("   Dob: " + dob);
            System.out.println("   Movies: ");
            String contract = "SELECT * FROM Contract WHERE symbol = '" + symbol + "'";
            ResultSet contractSet = statement.executeQuery(contract);
            if(contractSet.next()){
                System.out.println("      " + contractSet.getInt("prodyear") + ", " + contractSet.getString("title").trim() + ": " + contractSet.getString("roletype").trim() + ", $" + contractSet.getString("value"));
            }else{
                System.out.println("       No movies");
            }
            while(contractSet.next()){
                System.out.println("      " + contractSet.getInt("prodyear") + ", " + contractSet.getString("title").trim() + ": " + contractSet.getString("roletype").trim() +  ", $" + contractSet.getString("value"));
            }
            
        }
        else if(query.contains("Movie")){
            if(split.length < 3){
                System.out.println("Not full movie command");
            }
            try{
                String year = split[1];
                int firstSpace = query.indexOf(' ', 0);
                int secondSpace = query.indexOf(' ', firstSpace + 1);
                String movie = query.substring(secondSpace + 1);
                String getMovie = "SELECT * FROM Movie WHERE title = '" + movie + "' AND prod_year = " + year;
                Statement statement = connection.createStatement();
                ResultSet movieSet = statement.executeQuery(getMovie);
                if(!movieSet.next()){
                    System.out.println("No movie found!");
                    return;
                }
                int prodYear = movieSet.getInt("prod_year");
                float rating = movieSet.getFloat("rating");
                System.out.println("\nMovie Info");
                System.out.println("===========");
                System.out.println(movie);
                System.out.println("  Production year: " + prodYear);
                System.out.println("  Rating: " + rating);
                System.out.println("  Director:");
                String getDirectorss = "SELECT * FROM Contract C, Stock S WHERE C.symbol = S.symbol AND C.title = '" + movie + "' AND C.prodyear = " + year + " AND (roletype = 'Both' OR roletype = 'Director')";
                ResultSet DirectorsSet = statement.executeQuery(getDirectorss);
                while(DirectorsSet.next()){
                    System.out.println("   " + DirectorsSet.getString("starname").trim() + ": " + DirectorsSet.getInt("value") );
                }
                System.out.println("  Actors:");
                String getActors = "SELECT * FROM Contract C, Stock S WHERE C.symbol = S.symbol AND C.title = '" + movie + "' AND C.prodyear = " + year + " AND (roletype = 'Both' OR roletype = 'Actor')";
                ResultSet actorSet = statement.executeQuery(getActors);
                while(actorSet.next()){
                    System.out.println("   " + actorSet.getString("starname").trim() + ": Contract Value, $" + actorSet.getInt("value"));
                }
                System.out.println("  Reviews:");
                String getReviews= "SELECT * FROM Review WHERE title = '" + movie+ "' AND prodyear = " + year;
                ResultSet reviewSet = statement.executeQuery(getReviews);
                while(reviewSet.next()){
                    System.out.println("   " + reviewSet.getString("rcomment").trim());
                }
                    
            }catch(Exception e){
                System.out.println("Try again, something went wrong.");
            }
        }   
        else if(query.contains("Top")){
            System.out.print("\n");
            String range = split[1];
            int firstyear = Integer.parseInt(range.substring(0, range.indexOf('-')));
            int secondyear = Integer.parseInt(range.substring(range.indexOf('-') + 1));
            String select = "SELECT * FROM Movie WHERE prod_year >= " + firstyear + " AND prod_year <= " + secondyear + " AND rating = 10 ORDER BY prod_year";
            Statement statement = connection.createStatement();
            ResultSet selectSet = statement.executeQuery(select);
            System.out.println("Top movies within " + range + ":");
            while(selectSet.next()){
                System.out.println(selectSet.getString("title").trim() + ", " + selectSet.getInt("prod_year"));
            }
        }
        else if(query.contains("Review")){
            String year = split[1];
            int firstSpace = query.indexOf(' ', 0);
            int secondSpace = query.indexOf(' ', firstSpace + 1);
            String movie = query.substring(secondSpace + 1);
            Statement statement = connection.createStatement();
            String getReviews= "SELECT * FROM Review WHERE title = '" + movie+ "' AND prodyear = " + year;
            ResultSet reviewSet = statement.executeQuery(getReviews);
            System.out.println("Reviews For: " + movie);
            while(reviewSet.next()){
                System.out.println(reviewSet.getString("rcomment").trim());
            }
        }
        else{
            System.out.println("Not sure what you wanna do man.");
        }
    }

    static Float getUserBalance(Statement statement) throws SQLException {
        String currentBalance = "SELECT balance FROM Customer WHERE username = '" + currentUser + "'";
        ResultSet balanceSet = statement.executeQuery(
                currentBalance);
        balanceSet.next();
        Float balance = balanceSet.getFloat("balance");
        return balance;
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
        String tranQuery = "INSERT INTO Transaction VALUES (" + newTransId + ", " + type + ", DATE '" + date + "')";
        statement.executeUpdate(tranQuery);
        return newTransId;
    }

    static int getAccId(OracleConnection connection) throws SQLException {
        String getMaxTransId = "SELECT MAX(stockAccId) as max FROM StockAccount";
        Statement statement = connection.createStatement();
        ResultSet transSet = statement.executeQuery(getMaxTransId);
        transSet.next();
        String getMaxCustId = "SELECT MAX(markAccId) as max FROM Customer";
        Statement statementC = connection.createStatement();
        ResultSet CustSet = statementC.executeQuery(getMaxCustId);
        CustSet.next();
        if (!(transSet.getString("max") == null && !(CustSet.getString("max") == null))) {
            return Math.max(transSet.getInt("max"),CustSet.getInt("max"))  + 1;
        }
        return 0;
    }

    static void revertBuy(OracleConnection connection, Statement statement, int transid, String date) throws SQLException {
        // must change back everything that happened
        // remove added stocks in stockAmount
        // remove added balance in stockAccount
        // add money back to market account

        String getTransactionInfo = "SELECT * FROM BuyTransaction WHERE transid = " + transid;
        ResultSet transactionSet = statement.executeQuery(getTransactionInfo);
        transactionSet.next();
        String symbol = transactionSet.getString("stockSym");
        float price = transactionSet.getFloat("price");
        float buycount = transactionSet.getFloat("buycount");
        float balance = getUserBalance(statement);
        if ((balance + price * buycount - 20) < 0) {
            System.out.println("Don't have enough money to cancel last buy.");
        }

        String getStockAcc = "SELECT * FROM StockAccount WHERE customerId = " + markAccId + " AND symbol = '" + symbol
                + "'";
        ResultSet stockAccSet = statement.executeQuery(getStockAcc);
        stockAccSet.next();
        int stockAcc = stockAccSet.getInt("stockAccId");
        int cancelTransid = addNewTransaction(statement, 4, date);
        String addCancelTransaction = "INSERT INTO CancelTransaction VALUES(" + cancelTransid + ",'" + symbol + "', "
                + markAccId + ")";
        statement.executeUpdate(addCancelTransaction);
        String updateStockAmount = "UPDATE StockAmount SET amount = amount - " + buycount + " WHERE stockAccId = "
                + stockAcc + " AND price = " + price;
        statement.executeUpdate(updateStockAmount);
        String updateStockAccount = "UPDATE StockAccount SET balance = balance - " + buycount + " WHERE stockAccId = "
                + stockAcc;
        statement.executeUpdate(updateStockAccount);
        float value = price * buycount - 20;
        String updateMark = "UPDATE Customer SET balance = balance + " + value + " WHERE username = '" + currentUser
                + "'";
        statement.executeUpdate(updateMark);
        String removeTransaction = "DELETE FROM Transaction WHERE transid = " + transid;
        statement.executeUpdate(removeTransaction);
    }

    static void revertSell(OracleConnection connection, Statement statement, int transid, String date) throws SQLException {
        // need to
        String getTransactionInfo = "SELECT * FROM SellTransaction WHERE transid = " + transid;
        ResultSet transactionSet = statement.executeQuery(getTransactionInfo);
        transactionSet.next();
        String symbol = transactionSet.getString("stockSym");
        float totalCount = transactionSet.getFloat("totalCount");
        float price = transactionSet.getFloat("price");
        float balance = getUserBalance(statement);
        if ((balance - (price * totalCount) - 20) < 0) {
            System.out.println("Don't have enough money to cancel last buy.");
        }

        String getStockAcc = "SELECT * FROM StockAccount WHERE customerId = " + markAccId + " AND symbol = '" + symbol
                + "'";
        ResultSet stockAccSet = statement.executeQuery(getStockAcc);
        stockAccSet.next();
        int stockAcc = stockAccSet.getInt("stockAccId");
        int cancelTransid = addNewTransaction(statement, 4, date);
        String addCancelTransaction = "INSERT INTO CancelTransaction VALUES(" + cancelTransid + ", '" + symbol + "', "
                + markAccId + ")";
        statement.executeUpdate(addCancelTransaction);
        String getSellCountsBuy = "SELECT * FROM SellCountsBuy WHERE sellid = " + transid;
        ResultSet sellCountsSet = statement.executeQuery(getSellCountsBuy);
        Statement subStatement = connection.createStatement();
        while (sellCountsSet.next()) {
            float amount = sellCountsSet.getFloat("amount");
            float sellPrice = sellCountsSet.getFloat("price");
            String updateStockAmount = "UPDATE StockAmount SET amount = amount + " + amount + " WHERE stockAccId = "
                    + stockAcc + " AND price = " + sellPrice;
            subStatement.executeUpdate(updateStockAmount);
        }

        String deleteSellCounts = "DELETE FROM SellCountsBuy WHERE sellid = " + transid;
        statement.executeUpdate(deleteSellCounts);

        String updateStockAccount = "UPDATE StockAccount SET balance = balance + " + totalCount + " WHERE stockAccId = "
                + stockAcc;
        statement.executeUpdate(updateStockAccount);

        float value = price * totalCount + 20;
        String updateMark = "UPDATE Customer SET balance = balance - " + value + " WHERE username = '" + currentUser
                + "'";
        statement.executeUpdate(updateMark);
        String removeTransaction = "DELETE FROM Transaction WHERE transid = " + transid;
        statement.executeUpdate(removeTransaction);
    }

    static int validate(OracleConnection connection, String username, String taxid) throws SQLException{
        String getUsername = "SELECT * FROM Customer WHERE username = '" + username + "'";
        Statement statement = connection.createStatement();
        ResultSet usernameSet = statement.executeQuery(getUsername);
        if(usernameSet.next()){
            return 2;
        }

        if(taxid.length() > 9){
            return 3;
        }
    
        String getTaxid = "SELECT * FROM Customer WHERE tax_id = " + taxid;

        ResultSet taxIdSet = statement.executeQuery(getTaxid);
        if(taxIdSet.next()){
            return 1;
        }

        return 0;
    }

    static Date getDate(OracleConnection connection) throws SQLException{
        String getDate = "SELECT * FROM CurrentDate";
        Statement statement = connection.createStatement();
        ResultSet dateSet = statement.executeQuery(getDate);
        dateSet.next();
        return dateSet.getDate("currDate");
    }

    static boolean checkMarket(OracleConnection connection) throws SQLException{
        String market = "SELECT * FROM Market";
        Statement statement = connection.createStatement();
        ResultSet marketSet = statement.executeQuery(market);
        marketSet.next();
        if(marketSet.getInt("isOpen") == 0){
            return false;
        }
        return true;

    }
}
