package uwallet;

import java.math.BigDecimal;

public class WithdrawalTransaction extends Transaction {

    WithdrawalTransaction(double amount,  Account account, String... description){super(amount, account, description);}

    @Override
    BigDecimal applyTransaction() {
        BigDecimal endingBalance = this.involvedAccount.getCurrentBalance().subtract(new BigDecimal(this.amount));
        return endingBalance;
    }

    @Override
    String getTXSymbol(){return "CR";}
}