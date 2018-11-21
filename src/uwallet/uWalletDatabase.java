package uwallet;

import uwallet.exceptions.NoSuchAccountInDatabaseException;

import java.util.List;
import java.util.ArrayList;
import java.sql.*;
import java.io.File;


class uWalletDatabase {

    //class not meant to be public
    //TODO document this class
    //TODO test this class

    private Connection conn = null;
    private final static File dbDir = new File("sqlite");
    private final static String dbFilename = "uwallet.db";
    private final static String dbFile = "jdbc:sqlite:"+ dbDir + "/" + dbFilename;


    /**
     * uWalletDatabase class to generate the DB and write/read it. It generate a uwallet.db file in ./sqlite folder
     * if the folder does not exist, it generates it. Requries the JDBC Sqlite Driver dependency.
     */
    uWalletDatabase() {

        //check if the SQLITE_DIR exists, else create it
        if (!dbDir.exists()) {
            try {
                dbDir.mkdir();
            } catch (SecurityException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                System.exit(0);
            }
        }

        createTablesIfNotThere();

    }

    /**
     * Stores or updates the information for the given Account object in the DB.
     *
     * @param account account object whose data we wish to store in the DB
     */
    void insertAccount(Account account){
        connect();
        try{
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            String id = account.getAccountID();
            String accountName = account.getAccountName();
            String last_txID = String.valueOf(account.getLastTxId());
            String regionCode = account.getRegionCode();
            //for some reason if its purely numerical SQL gets angry and makes very big numbers infinity
            //and also reformats them.
            String numericalBalance = ">" + account.getCurrentBalance().toString();
            String formattedBalance = account.getFormattedBalance();

            stmt.executeUpdate(
                    "INSERT OR REPLACE INTO Accounts (id, accountName, last_txID, regionCode, numericalBalance, formattedBalance) values" +
                            "('" + id + "', '" + accountName + "', " + last_txID + ", '" + regionCode + "', '" + numericalBalance + "', '" + formattedBalance +"')");


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
    Account getAccount(String identifier) throws NoSuchAccountInDatabaseException{
        connect();
        try {
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                    "SELECT * FROM Accounts " +
                            "WHERE id = '" + identifier + "'");

            try{
                rs.getString("id");
            } catch (SQLException e){
                throw new NoSuchAccountInDatabaseException("No such user with identifier " + identifier + " found");
            }

            String id = rs.getString("id");
            String accountName = rs.getString("accountName");
            int last_txID = rs.getInt("last_txID");
            String regionCode = rs.getString("regionCode");
            String numericalBalance = rs.getString("numericalBalance").substring(1); //removes the '>' char
            stmt.close();
            return new Account(accountName, id, regionCode, numericalBalance, last_txID);
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
    void insertTransaction(Transaction transaction){
        connect();
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
    List<Transaction> getNLastTransactions(String accountIdentifier, int N) throws  NoSuchAccountInDatabaseException {
        connect();
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


    private void connect(){
        //check if the SQLITE_DIR exists, else create it
        if (!this.dbDir.exists()) {
            try {
                this.dbDir.mkdir();
            } catch (SecurityException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
                System.exit(0);
            }
        }

        if(conn != null){
            return;
        }
        try{
            conn = DriverManager.getConnection(this.dbFile);
        }catch(SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate the tables if the sqlite db is empty. Nothing happens if the tables already exists.
     */
    private void createTablesIfNotThere(){
        connect();
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
                            " formattedBalance          STRING                 NOT NULL)");

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
            stmt.close();
        } catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

}
