package uwallet;

import java.math.BigDecimal;

public class WithdrawalTransaction extends Transaction {

    WithdrawalTransaction(double amount,  Account account, String... description){super(amount, account, description);}

    @Override
    BigDecimal applyTransaction() {
        //Here we use Double.ToString() because BigDecimal constructor using double inherits the accuracy errors of doubles
        BigDecimal endingBalance = this.involvedAccount.getCurrentBalance().subtract(new BigDecimal(Double.toString(this.amount)));
        return endingBalance;
    }

    @Override
    String getTXSymbol(){return "CR";}
}