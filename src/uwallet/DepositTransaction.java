package uwallet;

import java.math.BigDecimal;

public class DepositTransaction extends Transaction {

    public DepositTransaction(double amount,  Account account, String... description){super(amount, account, description);}

    @Override
    BigDecimal applyTransaction() {
        //Here we use Double.ToString() because BigDecimal constructor using double inherits the accuracy errors of doubles
        BigDecimal endingBalance = this.involvedAccount.getCurrentBalance().add(new BigDecimal(Double.toString(this.amount)));
        return endingBalance;
    }

    @Override
    public String getTXSymbol(){return "DR";}
}