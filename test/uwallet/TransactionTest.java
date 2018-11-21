package uwallet;

import org.junit.jupiter.api.BeforeAll;
import uwallet.*;

import org.junit.jupiter.api.Test;
import uwallet.exceptions.UniqueAccountIDConstraintException;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {


    @Test
    void testDepositTransaction() throws UniqueAccountIDConstraintException {
        Account testAccount = new Account("test", "A", "US");
        testAccount.deposit(100000.00);
        DepositTransaction depositTX = new DepositTransaction(100, testAccount, "test");

        assert( depositTX.getEndingBalance().toString().equals("100100.0") );
    }

    @Test
    void testDepositTransactionWithZero() throws UniqueAccountIDConstraintException {
        Account testAccount = new Account("test", "B", "US");
        testAccount.deposit(100000.00);
        DepositTransaction depositTX = new DepositTransaction(0, testAccount, "test");

        assert( depositTX.getEndingBalance().toString().equals("100000.0") );
    }

    @Test
    void testWithdrawTransactionResultZero() throws UniqueAccountIDConstraintException {
        Account testAccount =  new Account("test", "C", "US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(100000.00, testAccount, "test");

        assert( withTX.getEndingBalance().toString().equals("0.0") );
    }


    @Test
    void testWithdrawalTransaction() throws UniqueAccountIDConstraintException {
        Account testAccount = new  Account("test", "D", "US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(100, testAccount, "test");

        assert( withTX.getEndingBalance().toString().equals("99900.0") );
    }

    @Test
    void testWithdrawalTransactionToNegative() throws UniqueAccountIDConstraintException {
        Account testAccount = new Account("test", "E", "US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(200000.00, testAccount, "test");

        assert( withTX.getEndingBalance().toString().equals("-100000.0") );
    }

    @Test
    void testDepositAndWithdrawalCreationDoesNotChangeAccountBalance() throws UniqueAccountIDConstraintException {
        Account testAccount = new Account("test", "F", "US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(400.0, testAccount, "test");
        DepositTransaction depositTX = new DepositTransaction(677.0, testAccount, "test");

        assert(testAccount.getCurrentBalance().toString().equals("100000.0"));
    }

    @Test
    void testDepositSymbolISDR() throws UniqueAccountIDConstraintException {
        Account testAccount = new Account("test", "G", "US");
        testAccount.deposit(100000.00);
        DepositTransaction depositTX = new DepositTransaction(677.0, testAccount, "test");

        assert(depositTX.getTXSymbol().equals("DR"));
    }

    @Test
    void testWithdrawalSymbolISCR() throws UniqueAccountIDConstraintException {
        Account testAccount = new Account("test", "H", "US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(400.0, testAccount, "test");

        assert(withTX.getTXSymbol().equals("CR"));
    }

    @Test
    void testUniqueIdentifier() throws UniqueAccountIDConstraintException {
        Account testAccount = new Account("test", "I", "US");
        testAccount.deposit(100000.00);
        WithdrawalTransaction withTX = new WithdrawalTransaction(400.0, testAccount, "TEST");
        System.out.println(withTX.getUUID());
        assert(withTX.getUUID().equals("ITEST"));
    }
}