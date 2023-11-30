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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import oracle.jdbc.pool.OracleDataSource;
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
                    "SELECT cpassword FROM Manager WHERE username = '" + username + "'"
                );
                resultSet.next();
                if(resultSet.getString("cpassword").equals(password)){
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
                    "SELECT currDate FROM Date"
                );
                resultDateSet.next();
                currentDate = resultDateSet.getDate("currDate").toString();
            } catch (Exception ee) {
                System.out.println("ERROR: Current Date not found.");
                System.out.println(ee);
                return;
            }

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
        
        if (query.contains("Add Interest")) {

            try (Statement accountQuery = connection.createStatement()) {
                ResultSet AccountsSet = accountQuery.executeQuery(
                    "SELECT markAccId FROM Customer"
                );

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                LocalDate localDate = LocalDate.parse(currentDate, formatter);

                int currMonth = localDate.getMonthValue();
                int currDay = localDate.getDayOfMonth();

                while (AccountsSet.next()) {
                    String marketId = AccountsSet.getString("markAccId");
                    ResultSet accountHistorySet = accountQuery.executeQuery(
                        "SELECT EXTRACT (DAY FROM currDate), currBalance " +
                        "FROM MarketAccountHistory " +
                        "WHERE markAccId = " + marketId + " AND EXTRACT(MONTH FROM currdate) = " + currMonth + " " +
                        "ORDER BY EXTRACT(DAY FROM currDate)"
                    );

                    Integer totalDays = 0;
                    Float totalBalance = (float) 0;

                    while (accountHistorySet.next()) {
                        totalDays++;
                        totalBalance += accountHistorySet.getInt("currBalance");
                    }

                    System.out.println("Average Daily Balance: " + totalBalance / totalDays);








                }
            }

        }
        else if (query.contains("Generate Monthly Statement")) {

        }
        else if (query.contains("List Active Customers")) {

        }
        else if (query.contains("Generate DTER")) {

        }
        else if (query.contains("Customer Report")) {

        }
        else if (query.contains("Delete Transactions")) {

        }
        else if (query.contains("OpenMarket")) {

        }
        else if (query.contains("CloseMarket")) {

        }
        else if (query.contains("SetPriceStock")) {

        }
        else if (query.contains("SetDate")) {

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