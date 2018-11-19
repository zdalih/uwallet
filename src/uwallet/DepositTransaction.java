package uwallet;

import java.math.BigDecimal;

public class DepositTransaction extends Transaction {

    DepositTransaction(double amount,  Account account, String... description){super(amount, account, description);}

    @Override
    BigDecimal applyTransaction() {
        BigDecimal endingBalance = this.involvedAccount.getCurrentBalance().add(new BigDecimal(this.amount));
        return endingBalance;
    }

    @Override
    String getTXSymbol(){return "DR";}
}