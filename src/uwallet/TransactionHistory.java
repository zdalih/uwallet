package uwallet;

import java.sql.*;
import java.io.File;

public class TransactionHistory {

    //RI: Each transaction has the following columns: TXUID, TIMESTAMP, TXTYPE, AMOUNT, BALANCE and the optional
    //    DESCRIPTION.
    //      -TX_UID - a globally unique transaction id.
    //      -TIMESTAMP - the timestamp of the transaction
    //      -TX_TYPE - 'W' or 'D' for deposit or withdrawal
    //      -TX_AMOUNT - the value of the transaction
    //      -ENDING_BALANCE_SIGNIFICANT - the ending balance's scale (from BigDecimal)
    //      -ENDING_BALANCE_SCALE - the ending balance's significant (from BigDecimal)
    //      -TX_DESCRIPTION - optional short descriptive text of the transaction
    //
    //
    //AF:  Each object represents a table on the sqlite db, a table defined by the transactionGroup identifier.
    //     for example one could use accountNumber from the Account object as the grouping mechanism for the table.
    //     This class is built with the idea that one day, SQLite will reach a performance bottleneck if too
    //     many concurrent threads are trying to write to the db file. if we were to change the db system
    //     we would do so by changing this class.


    // TODO: Populate the transaction history
    // TODO: Introduce a Transaction Object
    // TODO: Test the TransactionHistory Object
    // TODO: Get last N Transactions

    private final static File SQLITE_DIR = new File("sqlite");
    private final static String SQLITE_FILENAME = "transactionHistory.db";

    private final String transactionGroup;

    /**
     * Generate a TransactionHistory object connected to a DB containing TransactionHistory objects.
     *
     * If there is an issue connecting to the  DB such as not having the JDBC driver dependency the system
     * the error will be printed to System.err and the system will exit.
     */
    public TransactionHistory(String transactionGroup){

        //check if the SQLITE_DIR exists, else create it
        if (!SQLITE_DIR.exists()){
            try{
                SQLITE_DIR.mkdir();
            } catch (SecurityException e){
                System.err.println( e.getClass().getName() + ": " + e.getMessage() );
                System.exit(0);
            }
        }
        //now we can create a sqlite db for this transaction history object
        this.transactionGroup = transactionGroup;
        this.createTableForTransactionGroup();

    }

    private void createTableForTransactionGroup(){
        try{

            //we want to create a table for the given transactionGroupId.
            Connection connection = DriverManager.getConnection("jdbc:sqlite:"+ SQLITE_DIR + "/" + SQLITE_FILENAME);
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(String.format("CREATE TABLE Account_%s " +
                    "(TX_UID                     STRING KEY     NOT NULL," +
                    " TIMESTAMP                 TIMESTAMP      NOT NULL," +
                    " TX_TYPE                    CHAR(1)        NOT NULL, " +
                    " TX_AMOUNT                    DOUBLE         NOT NULL, " +
                    " TX_DESCRIPTION               CHAR(50), " +
                    " ENDING_BALANCE_SIGNIFICANT       INT     NOT NULL, " +
                    " ENDING_BALANCE_SCALE             INT     NOT NULL)",this.transactionGroup));
            stmt.close();
            connection.close();

        } catch (SQLException e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    }
}
