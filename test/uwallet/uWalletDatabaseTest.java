package uwallet;

import org.junit.jupiter.api.*;
import uwallet.exceptions.NoSuchObjectInDatabaseException;
import uwallet.exceptions.UniqueIDConstraintException;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class uWalletDatabaseTest {

    @BeforeAll
     static void flushDb(){
        uWalletDatabase.flush();
    }

    @Test
    public void testInsertingAndLoadingTransaction() throws UniqueIDConstraintException, NoSuchObjectInDatabaseException {
        Account acc = new Account("chequing", "210","wallet", "US");
        DepositTransaction tx = new DepositTransaction(100, acc, "TX1" );
        Timestamp expectedTimestamp = tx.getTimestamp();

        uWalletDatabase.insertTransaction(tx);
        tx = null;

        System.gc();

        Transaction txLoaded = uWalletDatabase.getNLastTransactions("210", 1).get(0);
        assert( txLoaded.getDescription().equals("N/A") );
        assert( txLoaded.getUUID().equals("210TX1") );
        assert( txLoaded.getAmount() == 100 );
        assert( txLoaded.getTXSymbol().equals("DR") );
        assert( txLoaded.getTimestamp().toString().equals(expectedTimestamp.toString()) );
    }

    @Test
    public void testInsertingAndLoadingAccountAllFields() throws UniqueIDConstraintException, NoSuchObjectInDatabaseException {
        Account accountUSD = new Account("chequing", "110","wallet", "US");
        accountUSD.deposit(300.0, "deposit");

        uWalletDatabase.insertAccount(accountUSD);
        accountUSD = null;
        System.gc();

        Account loadedAccountUsd = uWalletDatabase.getAccount("110");

        assert(loadedAccountUsd.getFormattedBalance().equals("$300.00"));
        assert(loadedAccountUsd.getLastTxId() == 1);
        assert(loadedAccountUsd.getAccountID().equals("110"));
        assert(loadedAccountUsd.getRegionCode().equals("US"));
        assert(loadedAccountUsd.getParentWalletUID().equals("wallet"));
        //check transaction
        assert(loadedAccountUsd.getPastTransactions(1).get(0).getAmount() == 300.0);
        assert(loadedAccountUsd.getPastTransactions(1).get(0).getTXSymbol().equals("DR"));
        assert(loadedAccountUsd.getPastTransactions(1).get(0).getDescription().equals("deposit"));
        assert(loadedAccountUsd.getPastTransactions(1).get(0).getUUID().equals("110TX1"));
    }

    @Test
    public void testInsertingAndLoadingWallet() throws UniqueIDConstraintException, NoSuchObjectInDatabaseException {
        Wallet wallet = new Wallet("TEST" , "US");
        wallet.createNewAccount("savings");
        wallet.depositToAccount(100,"savings", "test");
        wallet.createNewAccount("chequing");
        uWalletDatabase.insertWallet(wallet);

        wallet = null;
        System.gc();

        Wallet walletLoaded = uWalletDatabase.getWallet("TEST");

        assert( walletLoaded.getUID().equals("TEST") );
        assert( walletLoaded.getRegionCode().equals("US") );
        assert( walletLoaded.getAccountBalanceFormatted("savings").equals("$100.00") );
        assert( walletLoaded.getAccountBalanceFormatted("chequing").equals("$0.00") );
        assert( walletLoaded.getLastNTransactions("savings", 1).get(0).getTXSymbol().equals("DR") );
        assert( walletLoaded.getLastNTransactions("savings", 1).get(0).getAmount() == 100 );
        assert( walletLoaded.getLastNTransactions("savings", 1).get(0).getDescription().equals("test"));

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