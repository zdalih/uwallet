package uwallet;

import uwallet.exceptions.NoSuchAccountInDatabaseException;

import java.sql.*;
import java.io.File;


class SQL {

    //class not meant to be public
    //TODO document this class
    //TODO test this class

    private Connection conn = null;
    private final static File dbDir = new File("sqlite");
    private final static String dbFilename = "uwallet.db";
    private final static String dbFile = "jdbc:sqlite:"+ dbDir + "/" + dbFilename;


    /**
     * SQL class to generate the DB and write/read it. It generate a uwallet.db file in ./sqlite folder
     * if the folder does not exist, it generates it. Requries the JDBC Sqlite Driver dependency.
     */
    SQL() {

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
     * Generate the tables if the sqlite db is empty. Nothing happens if the tables already exists.
     */
    void createTablesIfNotThere(){
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
                            " balance                   STRING)");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS Transactions (" +
                            " txtime                    TIMESTAMP              NOT NULL, " +
                            " uuid                      STRING PRIMARY KEY     NOT NULL, " +
                            " account                   STRING                 NOT NULL, " +
                            " amount                    STRING                 NOT NULL, " +
                            " txtype                    STRING                 NOT NULL, " +
                            " endingBalance             STRING                 NOT NULL, " +
                            " FOREIGN KEY (account) REFERENCES Accounts(id))");
            stmt.close();
        } catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
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
            String balance = account.getCurrentBalance().toString();

            stmt.executeUpdate(
                    "INSERT OR REPLACE INTO Accounts (id, accountName, last_txID, regionCode, balance) values" +
                            "('" + id + "', '" + accountName + "', " + last_txID + ", '" + regionCode + "', '" + balance + "')");


            stmt.close();
        } catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * @return Account - an account object as defined in the DB for the given identifier. The identifier must be that
     * of an account that has been committed to the database already.
     */
    Account getAccount(String identifier) {
        connect();
        try {
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                    "SELECT * FROM Accounts " +
                            "WHERE id = '" + identifier + "'");

            String id = rs.getString("id");
            String accountName = rs.getString("accountName");
            int last_txID = rs.getInt("last_txID");
            String regionCode = rs.getString("regionCode");
            String balance = rs.getString("balance");
            stmt.close();
            return new Account(accountName, id, regionCode, balance, last_txID);
        } catch (SQLException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    /**
     *
     * @param transaction the transaction object that we wish to store in the DB
     */
    void insertTransaction(Transaction transaction){
        connect();
        try{
            //we want to create a table for the given transactionGroupId.
            Statement stmt = conn.createStatement();

            Timestamp txtime = transaction.getTimestamp();
            String uuid = transaction.getUUID();
            String account = transaction.involvedAccount.getAccountID();
            String amount = transaction.involvedAccount.applyAccountFormat(transaction.amount);
            String txtype = transaction.getTXSymbol();
            String endingBalance = transaction.involvedAccount.applyAccountFormat(transaction.getEndingBalance());

            stmt.executeUpdate(
                    "INSERT OR REPLACE INTO Transactions (txtime, uuid, account, amount, txtype, endingBalance) values" +
                            "('" + txtime + "', '" + uuid + "', '" + account + "', '" + amount + "', '" + txtype + "', '" + endingBalance + "')");


            stmt.close();
        } catch (SQLException e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            System.exit(0);
        }
    }

    void connect(){
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





}
