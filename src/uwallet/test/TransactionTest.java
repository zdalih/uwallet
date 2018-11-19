import uwallet.*;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    static private Account testAccount(){
        Account testAccount = new Account("test", "US");
        testAccount.deposit(100000.00);
        return testAccount;
    }


    @Test
    void testDepositTransaction() {
        Account testAccount = testAccount();
        DepositTransaction depositTX = new DepositTransaction(100, testAccount, "test");

        assert( depositTX.endingBalance.toString().equals("100100.0") );
    }

    @Test
    void testDepositTransactionWithZero() {
        Account testAccount = testAccount();
        DepositTransaction depositTX = new DepositTransaction(0, testAccount, "test");

        assert( depositTX.endingBalance.toString().equals("100000.0") );
    }

    @Test
    void testWithdrawTransactionResultZero() {
        Account testAccount = testAccount();
        WithdrawalTransaction withTX = new WithdrawalTransaction(100000.00, testAccount, "test");

        assert( withTX.endingBalance.toString().equals("0.0") );
    }


    @Test
    void testWithdrawalTransaction() {
        Account testAccount = testAccount();
        WithdrawalTransaction withTX = new WithdrawalTransaction(100, testAccount, "test");

        assert( withTX.endingBalance.toString().equals("99900.0") );
    }

    @Test
    void testWithdrawalTransactionToNegative() {
        Account testAccount = testAccount();
        WithdrawalTransaction withTX = new WithdrawalTransaction(200000.00, testAccount, "test");

        assert( withTX.endingBalance.toString().equals("-100000.0") );
    }

    @Test
    void testDepositAndWithdrawalCreationDoesNotChangeAccountBalance(){
        Account testAccount = testAccount();
        WithdrawalTransaction withTX = new WithdrawalTransaction(400.0, testAccount, "test");
        DepositTransaction depositTX = new DepositTransaction(677.0, testAccount, "test");

        assert(testAccount.getCurrentBalance().toString().equals("100000.0"));
    }

    @Test
    void testDepositSymbolISDR(){
        Account testAccount = testAccount();
        DepositTransaction depositTX = new DepositTransaction(677.0, testAccount, "test");

        assert(depositTX.getTXSymbol().equals("DR"));
    }

    @Test
    void testWithdrawalSymbolISCR(){
        Account testAccount = testAccount();
        WithdrawalTransaction withTX = new WithdrawalTransaction(400.0, testAccount, "test");

        assert(withTX.getTXSymbol().equals("CR"));
    }
}