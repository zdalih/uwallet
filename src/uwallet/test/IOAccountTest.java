import uwallet.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import uwallet.Account;
import uwallet.exceptions.NoSuchAccountInDatabaseException;

import java.util.concurrent.TimeUnit;

public class IOAccountTest {

    @Test
    public void testLoadingSameAccountMultipleTimesYieldSameReference()  throws NoSuchAccountInDatabaseException{
        Account accountUSD = new Account("chequing", "AC002", "US");
        accountUSD.deposit(100, "yay");
        accountUSD.deposit(1000, "love it");
        accountUSD.commit();

        Account loadedAccount1 = Account.loadAccount("AC002");
        Account loadedAccount2 = Account.loadAccount("AC002");

        //these should all refer to the same object
        assert (accountUSD == loadedAccount1 && loadedAccount1 == loadedAccount2);
    }

    @Test
    public void testLoadingSameAccountMultipleTimesYieldSameReferenceWithoutComit() throws NoSuchAccountInDatabaseException{
        Account accountUSD = new Account("chequing", "AC006", "US");
        accountUSD.deposit(100, "yay");
        accountUSD.deposit(1000, "love it");

        Account loadedAccount1 = Account.loadAccount("AC006");
        Account loadedAccount2 = Account.loadAccount("AC006");

        //these should all refer to the same object even though it was not committed.
        assert (accountUSD == loadedAccount1 && loadedAccount1 == loadedAccount2);
    }

    @Test
    public void testLoadingAcountWhoIsNotReferencedAnymore()  throws NoSuchAccountInDatabaseException{
        Account accountUSD = new Account("chequing", "AC003", "US");
        accountUSD.deposit(100, "yay");
        accountUSD.deposit(1000, "love it");
        accountUSD.commit();

        accountUSD = null;

        Account loadedAccount = Account.loadAccount("AC003");
        //these should all refer to the same object
        assert (loadedAccount != accountUSD);
        assert (loadedAccount.getCurrentBalance().toString().equals("1100.0"));

        Account loadedAccount2 = Account.loadAccount("AC003");

        assert( loadedAccount == loadedAccount2 );

        Account richGuy = new Account("savings", "AC004", "FR");
        richGuy.deposit(100000);
        richGuy.commit();
        richGuy = null;

        Account loadedRichGuy = Account.loadAccount("AC004");
        assert (loadedRichGuy.getCurrentBalance().toString().equals("100000.0"));
    }

    @Test
    public void testLoadingAcountWhoIsNotReferencedAnymoreWithNoComits()  throws NoSuchAccountInDatabaseException{
        Account accountUSD = new Account("chequing", "AC007", "US");
        accountUSD.deposit(100, "yay");
        accountUSD.deposit(1000, "love it");

        accountUSD = null;
        System.gc();

        //without the commit, and having the gc remove the reference, there should be no way to get the object back
        try{
            Account loadedAccount = Account.loadAccount("AC007");
        } catch (NoSuchAccountInDatabaseException e){
            assert(true);
            return;
        }

        assert(false);



    }
}
