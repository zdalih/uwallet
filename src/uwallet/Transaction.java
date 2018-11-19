package uwallet;

import java.sql.Timestamp;
import java.util.UUID;
import java.math.BigDecimal;

public abstract class Transaction {

    //RI: Each transaction must have a globally unique UUID, a timestamp created during call of it's creation, and
    //    the endingBalance must only be defined after the transaction has been completed to the involved Account.
    //    So after call to applyTransaction() which returns the endingBalance, it must set the endingBalance
    //
    //AF: This class represents an abstraction for a transaction. A transaction only refers to the substraction or
    //    addition of value to an account - a transfer is made up of two transactions. It has the logic to calculate
    //    the impact of a transaction on an account but does not modify the account, it simply returns the BigDecimal
    //    result.
    //

    private final String uuid;
    private final Timestamp timestamp;

    public final double amount;
    public final Account involvedAccount;
    public final String description;
    public  BigDecimal endingBalance;

    /**
     *
     * @param amount double
     *
     * @param account Account
     *
     * @param description String (optional).
     *            description[0] is a String of at most 50char that is not null. All other items in description
     *            are ignored. The default description is N/A.
     *
     */
    public Transaction(double amount, Account account, String... description){
        this.uuid = UUID.randomUUID().toString();
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.amount = amount;
        this.involvedAccount = account;
        this.endingBalance = this.applyTransaction();

        if (description.length > 0){
            this.description = description[0];
            return;
        }
        this.description = "N/A";
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

    @Override
    public String toString(){
        return this.timestamp.toString() + " | " + this.uuid + " | account:" +
                this.involvedAccount.getAccountName() + " | " + this.getTXSymbol() + " | " +
                this.involvedAccount.applyAccountFormat(this.amount) +
                " | Ending Balance: " + this.involvedAccount.applyAccountFormat(this.endingBalance) +
                " | " + this.description;
    }


}

