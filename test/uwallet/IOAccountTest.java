package uwallet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uwallet.exceptions.NoSuchAccountInDatabaseException;
import uwallet.exceptions.UniqueAccountIDConstraintException;

public class IOAccountTest {

    @BeforeAll
     public static void flushDb(){
        uWalletDatabase.flush();
    }

    @Test
    public void testLoadingSameAccountMultipleTimesYieldSameReference() throws NoSuchAccountInDatabaseException, UniqueAccountIDConstraintException {
        Account accountUSD = new Account("chequing", "AC002","wallet", "US");
        accountUSD.deposit(100.0, "yay");
        accountUSD.deposit(1000.0, "love it");

        Account loadedAccount1 = Account.loadAccount("AC002");
        Account loadedAccount2 = Account.loadAccount("AC002");

        //these should all refer to the same object
        assert (accountUSD == loadedAccount1 && loadedAccount1 == loadedAccount2);
    }

    @Test
    public void testLoadingSameAccountMultipleTimesYieldSameReferenceWithoutComit() throws NoSuchAccountInDatabaseException, UniqueAccountIDConstraintException {
        Account accountUSD = new Account("chequing", "AC006", "wallet","US");
        accountUSD.deposit(100.0, "yay");
        accountUSD.deposit(1000.0, "love it");

        Account loadedAccount1 = Account.loadAccount("AC006");
        Account loadedAccount2 = Account.loadAccount("AC006");

        //these should all refer to the same object even though it was not committed.
        assert (accountUSD == loadedAccount1 && loadedAccount1 == loadedAccount2);
    }

    @Test
    public void testLoadingAcountWhoIsNotReferencedAnymore() throws NoSuchAccountInDatabaseException, UniqueAccountIDConstraintException {
        Account accountUSD = new Account("chequing", "AC003", "wallet","US");
        accountUSD.deposit(100.23, "yay");
        accountUSD.deposit(1000.0, "love it");

        accountUSD = null;

        Account loadedAccount = Account.loadAccount("AC003");
        //these should all refer to the same object
        assert (loadedAccount != accountUSD);
        assert (loadedAccount.getCurrentBalance().toString().equals("1100.23"));

        Account loadedAccount2 = Account.loadAccount("AC003");

        assert( loadedAccount == loadedAccount2 );

        Account richGuy = new Account("savings", "AC004","wallet", "FR");
        richGuy.deposit(100000.98);
        richGuy = null;

        Account loadedRichGuy = Account.loadAccount("AC004");
        assert (loadedRichGuy.getCurrentBalance().toString().equals("100000.98"));
    }

    @Test
    public void testLoadingAcountWhoIsNotReferencedAnymoreWithNoComits() throws NoSuchAccountInDatabaseException, UniqueAccountIDConstraintException {
        Account accountUSD = new Account("chequing", "AC007", "wallet","US");
        accountUSD.deposit(100.0, "yay");
        accountUSD.deposit(1000.0, "love it");

        accountUSD = null;
        System.gc();

        try{
            Account loadedAccount = Account.loadAccount("AC007");
        } catch (NoSuchAccountInDatabaseException e){
            assert(false);
            return;
        }

    }
}
