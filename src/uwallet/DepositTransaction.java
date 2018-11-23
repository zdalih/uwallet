package uwallet;

import uwallet.exceptions.NoSuchObjectInDatabaseException;

import java.math.BigDecimal;
import java.sql.Timestamp;

  class DepositTransaction extends Transaction {

     DepositTransaction(double amount,  Account account, String txID, String... description){super(amount, account, txID, description);}
     DepositTransaction(Timestamp timestamp, String uuid, String account,
                              double amount, String endingBalance, String description) throws NoSuchObjectInDatabaseException {
        super(timestamp, uuid, account, amount, endingBalance, description);
    }
    @Override
     BigDecimal applyTransaction() {
        //Here we use Double.ToString() because BigDecimal constructor using double inherits the accuracy errors of doubles
        BigDecimal endingBalance = this.involvedAccount.getCurrentBalance().add(new BigDecimal(Double.toString(this.amount)));
        return endingBalance;
    }

    @Override
     String getTXSymbol(){return "DR";}
}