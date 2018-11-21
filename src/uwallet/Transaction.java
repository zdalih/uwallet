package uwallet;

import uwallet.exceptions.NoSuchAccountInDatabaseException;

import java.sql.Time;
import java.sql.Timestamp;
import java.math.BigDecimal;

abstract class Transaction {

    //RI: Each transaction must have a globally unique UUID, a timestamp created during call of it's creation, and
    //    the endingBalance must only be defined after the transaction has been completed to the involved Account.
    //    So after call to applyTransaction() which returns the endingBalance, it must set the endingBalance
    //
    //AF: This class represents an abstraction for a transaction. A transaction only refers to the substraction or
    //    addition of value to an account - a transfer is made up of two transactions. It has the logic to calculate
    //    the impact of a transaction on an account but does not modify the account, it simply returns the BigDecimal
    //    result.
    //

     final String uuid;
     final Timestamp timestamp;
     final double amount;
     final Account involvedAccount;
     final String description;
     final BigDecimal endingBalance;

    /**
     *
     * @param amount double - the value magnitude of the transaction. must be positive.
     *
     * @param account Account
     *
     * @param txID String  - unique identifier , for transactions linked to a single account, no two transaction
     *             can have the same txID. But different accounts can. Can not be null or empty.
     *
     * @param description String (optional).
     *            description[0] is a String of at most 50char that is not null. All other items in description
     *            are ignored. The default description is N/A.
     *
     */
     Transaction(double amount, Account account, String txID, String... description){
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.amount = amount;
        this.involvedAccount = account;
        this.endingBalance = this.applyTransaction();
        this.uuid = this.generateUniqueIdentifier(txID);

        if (description.length > 0){
            this.description = description[0];
            return;
        }
        this.description = "N/A";
    }

     Transaction (Timestamp timestamp, String uuid, String account,
                           double amount, String endingBalance, String description)
            throws NoSuchAccountInDatabaseException {
        this.timestamp = timestamp;
        this.uuid = uuid;
        this.involvedAccount = Account.loadAccount(account);
        this.amount = amount;
        this.endingBalance = new BigDecimal(endingBalance);
        this.description = description;
    }

    /**
     * Calculate the ending balance after application of the application to the account's balance. It then return
     * the balance after the transaction.
     *
     * @return endingBalance - BigDecimal
     *          the endingBalance after the transaction has been applied
     */
    abstract BigDecimal applyTransaction();

    /**
     *
     * @return a string code representing the nature of the transaction such as CR for credit, DR for debit
     */
    abstract String getTXSymbol();

    /**
     *
     * @return a string for this transaction that will serve as a purely unique identifier of this transaction object
     */
    private String generateUniqueIdentifier(String txID){
        return this.involvedAccount.getAccountID() + txID;
    }

    /**
     *
     * @return returns the endingBalance that results after the transaction
     */
     BigDecimal getEndingBalance(){
        return new BigDecimal(this.endingBalance.toString());
    }

    /**
     *
     * @return the timestamp from the time this transaction object was created
     */
     Timestamp getTimestamp(){
        return this.timestamp;
    }

    /**
     *
     * @return returns the unique identifier tied to this transaction
     */
     String getUUID(){
        return this.uuid;
    }

    @Override
     public String toString(){
        return this.timestamp.toString() + " | " + this.uuid + " | account:" +
                this.involvedAccount.getAccountName() + " | " + this.getTXSymbol() + " | " +
                this.involvedAccount.applyAccountFormat(this.amount) +
                " | Ending Balance: " + this.involvedAccount.applyAccountFormat(this.endingBalance) +
                " | " + this.description;
    }


}

