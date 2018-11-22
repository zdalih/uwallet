package uwallet;

import uwallet.exceptions.NoSuchAccountInDatabaseException;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import java.io.File;

class uWalletDatabase {

    //class not meant to be public
    //TODO document this class
    //TODO test this class

    private final static File dbDir = new File("sqlite");
    private final static String dbFilename = "uwallet.db";
    private final static String dbFile = "jdbc:sqlite:"+ dbDir + "/" + dbFilename;
    private static Connection conn = connect();




    /**
     * Stores or updates the information for the given Account object in the DB.
     *
     * @param account account object whose data we wish to store in the DB
     */
    static synchronized void insertAccount(Account account){
        try{
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            String id = account.getAccountID();
            String accountName = account.getAccountName();
            String last_txID = String.valueOf(account.getLastTxId());
            String regionCode = account.getRegionCode();
            String parentWalletId = account.getParentWalletUID();
            //for some reason if its purely numerical SQL gets angry and makes very big numbers infinity
            //and also reformats them.
            String numericalBalance = ">" + account.getCurrentBalance().toString();
            String formattedBalance = account.getFormattedBalance();

            stmt.executeUpdate(
                    "INSERT OR REPLACE INTO Accounts (id, accountName, last_txID, regionCode, numericalBalance, formattedBalance, walletId) values" +
                            "('" + id + "', '" + accountName + "', " + last_txID + ", '" + regionCode + "', '" +
                            numericalBalance + "', '" + formattedBalance + "', '" + parentWalletId + "')");


            stmt.close();
        } catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * @return Account - an account object as defined in the DB for the given identifier. The identifier must be that
     *          of an account that has been committed to the database already.
     *
     * @throws NoSuchAccountInDatabaseException
     *          if no such account with the given identifier is found in the db
     */
    static synchronized Account getAccount(String identifier) throws NoSuchAccountInDatabaseException{
        try {
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                    "SELECT * FROM Accounts " +
                            "WHERE id = '" + identifier + "'");

            try{
                rs.getString("id");
            } catch (SQLException e){
                throw new NoSuchAccountInDatabaseException("No account with identifier " + identifier + " found");
            }

            String id = rs.getString("id");
            String accountName = rs.getString("accountName");
            int last_txID = rs.getInt("last_txID");
            String regionCode = rs.getString("regionCode");
            String parentWalletUID = rs.getString("walletId");
            String numericalBalance = rs.getString("numericalBalance").substring(1); //removes the '>' char
            stmt.close();
            return new Account(accountName, id, parentWalletUID, regionCode, numericalBalance, last_txID);
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    /**
     *
     * @param transaction the Transaction object that we wish to store in the DB
     */
    static synchronized void insertTransaction(Transaction transaction){
        try{
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            Timestamp txtime = transaction.getTimestamp();
            String uuid = transaction.getUUID();
            String account = transaction.involvedAccount.getAccountID();
            String amountFormatted = transaction.involvedAccount.applyAccountFormat(transaction.amount);
            double amountDouble = transaction.amount;
            String txtype = transaction.getTXSymbol();
            String endingBalanceFormatted = transaction.involvedAccount.applyAccountFormat(transaction.getEndingBalance());
            String endingBalanceNumeric = ">" + transaction.getEndingBalance().toString();
            String description = transaction.description;

            stmt.executeUpdate(
                    "INSERT OR REPLACE INTO Transactions (txtime, uuid, account, amountFormatted, amountDouble, " +
                            "txtype, endingBalanceFormatted, endingBalanceNumeric, description) values" +
                            "('" + txtime + "', '" + uuid + "', '" + account + "', '" + amountFormatted +
                            "', " + amountDouble + ", '"  + txtype + "', '" + endingBalanceFormatted +
                            "', '" + endingBalanceNumeric + "', '" + description + "')");


            stmt.close();
        } catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * @return List<Transaction> - which is a list of length 0-N (limited by the total number of transactions for
     * the account) of the last 0-N transactions that are on file for this account.
     *
     */
    static synchronized List<Transaction> getNLastTransactions(String accountIdentifier, int N) throws  NoSuchAccountInDatabaseException {
        try {
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                    " SELECT * From Transactions " +
                            " WHERE account = '" + accountIdentifier + "' " +
                            " ORDER BY txtime DESC" +
                            " LIMIT " + String.valueOf(N) + " ;"
            );
            //iterate through the result set to form the return list
            List<Transaction> pastNTransactions = new ArrayList<Transaction>();
            while(rs.next()){
                Timestamp timestamp = rs.getTimestamp("txtime");
                String uuid = rs.getString("uuid");
                String account = rs.getString("account");
                double amountDouble = rs.getDouble("amountDouble");
                String txtype = rs.getString("txtype");
                String endingBalanceNumeric = rs.getString("endingBalanceNumeric").substring(1); // removed ">"
                String description = rs.getString("description");

                if ( txtype.equals("DR") ) {
                    pastNTransactions.add(new DepositTransaction(
                            timestamp,
                            uuid,
                            account,
                            amountDouble,
                            endingBalanceNumeric,
                            description
                    ));
                }
                if ( txtype.equals("CR") ) {
                    pastNTransactions.add(new WithdrawalTransaction(
                            timestamp,
                            uuid,
                            account,
                            amountDouble,
                            endingBalanceNumeric,
                            description
                    ));
                }

            }
            stmt.close();
            return pastNTransactions;
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    /**
     * inserts a new wallet row in the db or updates an existing one
     * @param wallet the Wallet object to be inserted into the db
     */
    static synchronized void insertWallet(Wallet wallet){
        try{
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            String walletId = wallet.getUID();
            String regionCode = wallet.getRegionCode();

            stmt.executeUpdate(
                    "INSERT OR REPLACE INTO Wallets (id, regionCode) values" +
                            "('" + walletId + "', '" + regionCode + "')");

            stmt.close();
        } catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }

    }

    static synchronized Wallet getWallet(String walletUID){
        try {
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                    "SELECT * FROM Accounts " +
                            "WHERE walletId = '" + walletUID + "'");

            HashMap<String, String> walletAccounts = new HashMap<String, String>();

            while(rs.next())
                walletAccounts.put(rs.getString("accountName"),rs.getString("id") );


            ResultSet rs2 = stmt.executeQuery(
                    "SELECT * FROM Wallets " +
                            "WHERE id = '" + walletUID + "'");

            String regionCode = rs2.getString("regionCode");

            stmt.close();
            return  new Wallet(walletUID, regionCode, walletAccounts);

        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }

        return null;
    }


    static synchronized void flush(){
        try{
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            stmt.executeUpdate(
                    "DELETE FROM Accounts");
            stmt.executeUpdate(
                    "DELETE FROM Transactions");

            stmt.close();
        } catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    static private Connection connect(){

        //check if the SQLITE_DIR exists, else create it
        if (!dbDir.exists()) {
            try {
                dbDir.mkdir();
            } catch (SecurityException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
        }

        try{
            Connection conn = DriverManager.getConnection(dbFile);
            createTablesIfNotThere(conn);
            return conn;
        }catch(SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }

        return  null;
    }

    /**
     * Generate the tables if the sqlite db is empty. Nothing happens if the tables already exists.
     */
    static private void createTablesIfNotThere(Connection conn){
        try{
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();


            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Accounts (" +
                            " id                        STRING PRIMARY KEY     NOT NULL, " +
                            " accountName               STRING                 NOT NULL, " +
                            " last_txID                 INT                    NOT NULL, " +
                            " regionCode                STRING                 NOT NULL, " +
                            " numericalBalance          STRING                 NOT NULL, " +
                            " formattedBalance          STRING                 NOT NULL, " +
                            " walletId                  STRING                 NOT NULL, " +
                            " FOREIGN KEY (walletId) REFERENCES Wallets(id))");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Transactions (" +
                            " txtime                    TIMESTAMP              NOT NULL, " +
                            " uuid                      STRING PRIMARY KEY     NOT NULL, " +
                            " account                   STRING                 NOT NULL, " +
                            " amountFormatted           STRING                 NOT NULL, " +
                            " amountDouble              DOUBLE                 NOT NULL, " +
                            " txtype                    STRING                 NOT NULL, " +
                            " description               STRING                 NOT NULL, " +
                            " endingBalanceFormatted    STRING                 NOT NULL, " +
                            " endingBalanceNumeric      STRING                 NOT NULL, " +
                            " FOREIGN KEY (account) REFERENCES Accounts(id))");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Wallets (" +
                            " id                        STRING PRIMARY KEY     NOT NULL, " +
                            " regionCode                STRING                 NOT NULL)");



            stmt.close();
        } catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

}
