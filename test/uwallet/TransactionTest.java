package uwallet;

import org.junit.jupiter.api.Test;
import uwallet.exceptions.UniqueIDConstraintException;

public class TransactionTest {

    @Test
    void testDepositTransaction() throws UniqueIDConstraintException {
        Account testAccount = new Account("test", "A", "wallet", "US");
        testAccount.deposit(100000.00);
        DepositTransaction depositTX = new DepositTransaction(100, testAccount, "test");

        assert( depositTX.getEndingBalance().toString().equals("100100.0") );
    }

    @Test
    void testDepositTransactionWithZero() throws UniqueIDConstraintException {
        Account testAccount = new Account("test", "B","wallet", "US");
        testAccount.deposit(100000.00);
        DepositTransaction depositTX = new DepositTransaction(0, testAccount, "test");

        assert( depositTX.getEndingBalance().toString().equals("100000.0") );
    }

    @Test
    void testWithdrawTransactionResultZero() throws UniqueIDConstraintException {
        Account testAccount =  new Account("test", "C", "wallet","US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(100000.00, testAccount, "test");

        assert( withTX.getEndingBalance().toString().equals("0.0") );
    }


    @Test
    void testWithdrawalTransaction() throws UniqueIDConstraintException {
        Account testAccount = new  Account("test", "D", "wallet","US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(100, testAccount, "test");

        assert( withTX.getEndingBalance().toString().equals("99900.0") );
    }

    @Test
    void testWithdrawalTransactionToNegative() throws UniqueIDConstraintException {
        Account testAccount = new Account("test", "E", "wallet","US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(200000.00, testAccount, "test");

        assert( withTX.getEndingBalance().toString().equals("-100000.0") );
    }

    @Test
    void testDepositAndWithdrawalCreationDoesNotChangeAccountBalance() throws UniqueIDConstraintException {
        Account testAccount = new Account("test", "F", "wallet","US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(400.0, testAccount, "test");
        DepositTransaction depositTX = new DepositTransaction(677.0, testAccount, "test");

        assert(testAccount.getCurrentBalance().toString().equals("100000.0"));
    }

    @Test
    void testDepositSymbolISDR() throws UniqueIDConstraintException {
        Account testAccount = new Account("test", "G", "wallet","US");
        testAccount.deposit(100000.00);
        DepositTransaction depositTX = new DepositTransaction(677.0, testAccount, "test");

        assert(depositTX.getTXSymbol().equals("DR"));
    }

    @Test
    void testWithdrawalSymbolISCR() throws UniqueIDConstraintException {
        Account testAccount = new Account("test", "H","wallet", "US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(400.0, testAccount, "test");

        assert(withTX.getTXSymbol().equals("CR"));
    }

    @Test
    void testUniqueIdentifier() throws UniqueIDConstraintException {
        Account testAccount = new Account("test", "I","wallet", "US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(400.0, testAccount, "TEST");
        assert(withTX.getUUID().equals("ITEST"));
    }
}