package uwallet;

import org.junit.jupiter.api.*;
import uwallet.exceptions.NoSuchObjectInDatabaseException;
import uwallet.exceptions.UniqueIDConstraintException;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class uWalletDatabaseTest {

    @BeforeAll
     static void flushDb(){
        uWalletDatabase.flush();
    }

    @Test
    public void testInsertionOfTwoAccountsAndRetrieval() throws UniqueIDConstraintException {
        Account acc = new Account("mymoney",  "ACC016", "wallet", "US");
        Account acc2 = new Account("mymoney", "ACC017", "wallet", "FR");


        uWalletDatabase.insertAccount(acc);
        uWalletDatabase.insertAccount(acc2);


        try{
            Account acc_ret = uWalletDatabase.getAccount("ACC016");
            Account acc2_ret = uWalletDatabase.getAccount("ACC017");
            assert(acc_ret.toString().equals(acc.toString()));
            assert(acc2_ret.toString().equals(acc2.toString()));
        } catch (NoSuchObjectInDatabaseException e){
            System.out.println("Accounts are not in db...");
            assert(false);
        }

    }

    @Test
    public void testUpdatingAnAccountAlreadyInDB() throws UniqueIDConstraintException {
        Account acc = new Account("mymoney", "ACC018", "wallet", "US");

        uWalletDatabase.insertAccount(acc);

        acc.deposit(0.56);
        uWalletDatabase.insertAccount(acc);

        try{
            Account acc_ret = uWalletDatabase.getAccount("ACC018");
            assert(acc_ret.getFormattedBalance().equals("$0.56"));

        } catch (NoSuchObjectInDatabaseException e){
            System.out.println("Account is not in db...");
            assert(false);
        }
    }

    @Test
    public void testInsertingAccountWithHighDegreeOfAccuracyRequired() throws UniqueIDConstraintException {
        Account acc = new Account("mymoney", "ACC019", "wallet", "US");
        acc.deposit(Double.MAX_VALUE);
        acc.deposit(Double.MAX_VALUE);
        acc.deposit(123456.2);

        uWalletDatabase.insertAccount(acc);

        try{
            Account acc_ret = uWalletDatabase.getAccount("ACC019");
            assert(acc_ret.getFormattedBalance().equals("$359,538,626,972,463,140,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,123,456.20"));
        } catch (NoSuchObjectInDatabaseException e){
            System.out.println("Account is not in db...");
            assert(false);
        }
    }

    @Test
    public void testInsertingAndFetchingATransactionWithHighDegreeOfAccuracyRequired() throws UniqueIDConstraintException {
        Account acc = new Account("mymoney", "ACC020", "wallet","US");
        acc.deposit(Double.MAX_VALUE);
        acc.deposit(Double.MAX_VALUE);
        acc.deposit(123456.2);
        Transaction tx = new WithdrawalTransaction(9323.0, acc, "90","fakeTX");

        uWalletDatabase.insertAccount(acc);
        uWalletDatabase.insertTransaction(tx);

        try{
            Transaction tx_ret = uWalletDatabase.getNLastTransactions("ACC020", 1).get(0);
            assert(tx_ret.involvedAccount.getFormattedBalance().equals("$359,538,626,972,463,140,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,123,456.20"));
        } catch (NoSuchObjectInDatabaseException e){
            System.out.println("Account is not in db...");
            assert(false);
        }

    }

    @Test
    public void fetchAccountThatIsNotInDb(){

        try{
            Account acc_ret = uWalletDatabase.getAccount("nosuchaccount");
            assert(false);
        } catch (NoSuchObjectInDatabaseException e){
            assert(true);
        }
    }

    @Test
    public void fetchAccountWithEmptyIdentifier(){

        try{
            Account acc_ret = uWalletDatabase.getAccount("");
            assert(false);
        } catch (NoSuchObjectInDatabaseException e){
            assert(true);
        }
    }

    @Test
    public void testSimpleRetrievalOfOneTransaction() throws NoSuchObjectInDatabaseException, InterruptedException, UniqueIDConstraintException {
        Account acc = new Account("mymoney", "ACC015", "wallet", "FR");
        acc.deposit(100.50, "tx 1");
        TimeUnit.MILLISECONDS.sleep(10);
        acc.deposit(102.50, "tx 2");
        TimeUnit.MILLISECONDS.sleep(10);
        acc.deposit(103.50, "tx 3");
        TimeUnit.MILLISECONDS.sleep(10);
        acc.deposit(104.50, "tx 4");


        List<Transaction> pastTransactions = uWalletDatabase.getNLastTransactions("ACC015", 2);

        assert(pastTransactions.get(0).amount == 104.50);
        assert(pastTransactions.get(1).amount == 103.50);

    }

    @Test
    public void testRetrievalOfDepositTransaction() throws NoSuchObjectInDatabaseException, UniqueIDConstraintException {
        Account acc = new Account("mymoney", "DEP","wallet", "FR");
        acc.deposit(10.50, "tx 1");
        DepositTransaction fakeTX = new DepositTransaction(10.50, acc, "89", "fake tx");

        uWalletDatabase.insertAccount(acc);
        uWalletDatabase.insertTransaction(fakeTX);

        Transaction retrievedTX = uWalletDatabase.getNLastTransactions("DEP", 1).get(0);

        assert( retrievedTX.getTXSymbol().equals("DR") );


    }

    @Test
    public void testRetrievalOfWithdrawalTransaction() throws NoSuchObjectInDatabaseException, UniqueIDConstraintException {
        Account acc = new Account("mymoney", "WID", "wallet", "FR");
        WithdrawalTransaction fakeTX = new WithdrawalTransaction(10.50, acc, "32", "fake tx 2");

        uWalletDatabase.insertAccount(acc);
        uWalletDatabase.insertTransaction(fakeTX);

        Transaction retrievedTX = uWalletDatabase.getNLastTransactions("WID", 1).get(0);

        assert( retrievedTX.getTXSymbol().equals("CR") );

    }

}