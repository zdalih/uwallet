package uwallet;

import org.junit.jupiter.api.Test;


import uwallet.exceptions.NoSuchAccountInDatabaseException;

import java.util.List;
import java.util.concurrent.TimeUnit;

class uWalletDatabaseTest {

    @Test
    public void testInsertionOfTwoAccountsAndRetrieval() {
        Account acc = new Account("mymoney", "ACC016", "US");
        Account acc2 = new Account("mymoney", "ACC017", "FR");


        uWalletDatabase db = new uWalletDatabase();

        db.insertAccount(acc);
        db.insertAccount(acc2);


        try{
            Account acc_ret = db.getAccount("ACC016");
            Account acc2_ret = db.getAccount("ACC017");
            assert(acc_ret.toString().equals(acc.toString()));
            assert(acc2_ret.toString().equals(acc2.toString()));
        } catch (NoSuchAccountInDatabaseException e){
            System.out.println("Accounts are not in db...");
            assert(false);
        }

    }

    @Test
    public void testUpdatingAnAccountAlreadyInDB(){
        Account acc = new Account("mymoney", "ACC018", "US");

        uWalletDatabase db = new uWalletDatabase();
        db.insertAccount(acc);

        acc.deposit(0.56);
        db.insertAccount(acc);

        try{
            Account acc_ret = db.getAccount("ACC018");
            assert(acc_ret.getFormattedBalance().equals("$0.56"));

        } catch (NoSuchAccountInDatabaseException e){
            System.out.println("Account is not in db...");
            assert(false);
        }
    }

    @Test
    public void testInsertingAccountWithHighDegreeOfAccuracyRequired(){
        Account acc = new Account("mymoney", "ACC019", "US");
        acc.deposit(Double.MAX_VALUE);
        acc.deposit(Double.MAX_VALUE);
        acc.deposit(123456.2);

        uWalletDatabase db = new uWalletDatabase();
        db.insertAccount(acc);

        try{
            Account acc_ret = db.getAccount("ACC019");
            assert(acc_ret.getFormattedBalance().equals("$359,538,626,972,463,140,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,123,456.20"));
        } catch (NoSuchAccountInDatabaseException e){
            System.out.println("Account is not in db...");
            assert(false);
        }
    }

    @Test
    public void testInsertingAndFetchingATransactionWithHighDegreeOfAccuracyRequired(){
        Account acc = new Account("mymoney", "ACC020", "US");
        acc.deposit(Double.MAX_VALUE);
        acc.deposit(Double.MAX_VALUE);
        acc.deposit(123456.2);
        Transaction tx = new WithdrawalTransaction(9323.0, acc, "90","fakeTX");

        uWalletDatabase db = new uWalletDatabase();
        db.insertAccount(acc);
        db.insertTransaction(tx);

        try{
            Transaction tx_ret = db.getNLastTransactions("ACC020", 1).get(0);
            assert(tx_ret.involvedAccount.getFormattedBalance().equals("$359,538,626,972,463,140,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,123,456.20"));
        } catch (NoSuchAccountInDatabaseException e){
            System.out.println("Account is not in db...");
            assert(false);
        }

    }

    @Test
    public void fetchAccountThatIsNotInDb(){
        uWalletDatabase db = new uWalletDatabase();

        try{
            Account acc_ret = db.getAccount("nosuchaccount");
            assert(false);
        } catch (NoSuchAccountInDatabaseException e){
            assert(true);
        }
    }

    @Test
    public void fetchAccountWithEmptyIdentifier(){
        uWalletDatabase db = new uWalletDatabase();

        try{
            Account acc_ret = db.getAccount("");
            assert(false);
        } catch (NoSuchAccountInDatabaseException e){
            assert(true);
        }
    }

    @Test
    public void testSimpleRetrievalOfOneTransaction() throws NoSuchAccountInDatabaseException , InterruptedException{
        Account acc = new Account("mymoney", "ACC015", "FR");
        acc.deposit(100.50, "tx 1");
        TimeUnit.MILLISECONDS.sleep(10);
        acc.deposit(102.50, "tx 2");
        TimeUnit.MILLISECONDS.sleep(10);
        acc.deposit(103.50, "tx 3");
        TimeUnit.MILLISECONDS.sleep(10);
        acc.deposit(104.50, "tx 4");
        acc.commit();

        uWalletDatabase db = new uWalletDatabase();

        List<Transaction> pastTransactions = db.getNLastTransactions("ACC015", 2);

        assert(pastTransactions.get(0).amount == 104.50);
        assert(pastTransactions.get(1).amount == 103.50);

    }

    @Test
    public void testRetrievalOfDepositTransaction() throws NoSuchAccountInDatabaseException{
        Account acc = new Account("mymoney", "DEP", "FR");
        acc.deposit(10.50, "tx 1");
        DepositTransaction fakeTX = new DepositTransaction(10.50, acc, "89", "fake tx");

        uWalletDatabase db = new uWalletDatabase();
        db.insertAccount(acc);
        db.insertTransaction(fakeTX);

        Transaction retrievedTX = db.getNLastTransactions("DEP", 1).get(0);

        assert( retrievedTX.getTXSymbol().equals("DR") );


    }

    @Test
    public void testRetrievalOfWithdrawalTransaction() throws NoSuchAccountInDatabaseException{
        Account acc = new Account("mymoney", "WID", "FR");
        WithdrawalTransaction fakeTX = new WithdrawalTransaction(10.50, acc, "32", "fake tx 2");

        uWalletDatabase db = new uWalletDatabase();
        db.insertAccount(acc);
        db.insertTransaction(fakeTX);

        Transaction retrievedTX = db.getNLastTransactions("WID", 1).get(0);

        assert( retrievedTX.getTXSymbol().equals("CR") );


    }


}