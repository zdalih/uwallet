import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import uwallet.Transaction;
import uwallet.Wallet;
import uwallet.exceptions.InsufficientFundsException;
import uwallet.exceptions.NoSuchObjectInDatabaseException;
import uwallet.exceptions.UniqueIDConstraintException;

import java.util.List;


public class WalletTest {

    @BeforeAll
    static public void cleandata(){
        Wallet.deleteAllRecord("delete");
    }

    @Test
    public void testCreateWalletAndAddTwoAccount() throws NoSuchObjectInDatabaseException, UniqueIDConstraintException {
        Wallet wallet = new Wallet("WAL001", "US");
        wallet.createNewAccount("savings");
        wallet.createNewAccount("chequing");

        assert( wallet.getAccountBalanceFormatted("savings").equals("$0.00") );
        assert( wallet.getAccountBalanceFormatted("chequing").equals("$0.00") );
    }

    @Test
    public void depositMoney() throws UniqueIDConstraintException, NoSuchObjectInDatabaseException {
        Wallet wallet = new Wallet("WAL002", "US");
        wallet.createNewAccount("savings");
        wallet.depositToAccount(100.50, "savings");

        assert( wallet.getAccountBalanceFormatted("savings").equals("$100.50") );
    }

    @Test void transferMoney() throws NoSuchObjectInDatabaseException, UniqueIDConstraintException, InsufficientFundsException{
        Wallet wallet = new Wallet("WAL003", "US");
        wallet.createNewAccount("chequing");
        wallet.createNewAccount("savings");
        wallet.depositToAccount(100.50, "chequing");
        wallet.transfer(100.50, "chequing", "savings");

        assert( wallet.getAccountBalanceFormatted("chequing").equals("$0.00") );
        assert( wallet.getAccountBalanceFormatted("savings").equals("$100.50") );
    }

    @Test void gettingLastTransactions() throws UniqueIDConstraintException, NoSuchObjectInDatabaseException {
        Wallet wallet = new Wallet("WAL004", "US");
        wallet.createNewAccount("chequing");
        wallet.depositToAccount(100.50, "chequing");
        wallet.depositToAccount(101.50, "chequing");
        wallet.depositToAccount(102.50, "chequing");
        wallet.depositToAccount(103.50, "chequing");
        wallet.depositToAccount(104.50, "chequing");
        wallet.depositToAccount(105.50, "chequing");
        wallet.depositToAccount(106.50, "chequing");
        wallet.depositToAccount(107.50, "chequing");
        wallet.depositToAccount(108.50, "chequing");
        wallet.depositToAccount(109.50, "chequing");
        wallet.depositToAccount(110.50, "chequing");

        List<Transaction> past5tx = wallet.getLastNTransactions("chequing", 5);
        assert( past5tx.size() == 5 );
        assert( past5tx.get(0).getAmount() == 110.5 );
        assert( past5tx.get(1).getAmount() == 109.5 );
        assert( past5tx.get(2).getAmount() == 108.5 );
        assert( past5tx.get(3).getAmount() == 107.5 );
        assert( past5tx.get(4).getAmount() == 106.5 );


        List<Transaction> past5tx2 = Wallet.loadWallet("WAL004").getLastNTransactions("chequing", 5);
    }

    @Test void reloadWalletAndGetLastTransaction() throws NoSuchObjectInDatabaseException, UniqueIDConstraintException {
        Wallet wallet = new Wallet("WAL005", "US");
        wallet.createNewAccount("chequing");
        wallet.depositToAccount(100.50, "chequing");
        wallet.depositToAccount(101.50, "chequing");
        wallet.depositToAccount(102.50, "chequing");
        wallet.depositToAccount(103.50, "chequing");
        wallet.depositToAccount(104.50, "chequing");
        wallet.depositToAccount(105.50, "chequing");
        wallet.depositToAccount(106.50, "chequing");
        wallet.depositToAccount(107.50, "chequing");
        wallet.depositToAccount(108.50, "chequing");
        wallet.depositToAccount(109.50, "chequing");
        wallet.depositToAccount(110.50, "chequing");
        wallet = null;
        System.gc();

        Wallet loadedWallet = Wallet.loadWallet("WAL005");

        List<Transaction> past5tx = loadedWallet.getLastNTransactions("chequing", 5);
        assert( past5tx.size() == 5 );
        assert( past5tx.get(0).getAmount() == 110.5 );
        assert( past5tx.get(1).getAmount() == 109.5 );
        assert( past5tx.get(2).getAmount() == 108.5 );
        assert( past5tx.get(3).getAmount() == 107.5 );
        assert( past5tx.get(4).getAmount() == 106.5 );

    }

    @Test
    public void testDepositToNonExistentAccount() throws NoSuchObjectInDatabaseException, UniqueIDConstraintException {
        Wallet wallet = new Wallet("WALL10", "US");
        try{
            wallet.depositToAccount(100.00, "nosuchaccount");
        }catch (NoSuchObjectInDatabaseException e){
            assert(true);
        }
    }


    @Test
    public void testGetFormattedBalanceToNonExistentAccount() throws NoSuchObjectInDatabaseException, UniqueIDConstraintException {
        Wallet wallet = new Wallet("WALL10", "US");
        try{
            wallet.getAccountBalanceFormatted("nosuchaccount");
            assert(false);
        }catch (NoSuchObjectInDatabaseException e){
            assert(true);
        }
    }

    @Test
    public void testGetBigDecBalanceToNonExistentAccount() throws UniqueIDConstraintException {
        Wallet wallet = new Wallet("WALL10", "US");
        try{
            wallet.getAccountBalanceBigDecimal("nosuchaccount");
            assert(false);
        }catch (NoSuchObjectInDatabaseException e){
            assert(true);
        }
    }

    @Test
    public void testCreatingAWalletThatAlreadyExists() throws UniqueIDConstraintException {
        Wallet wallet = new Wallet("MINE", "CA");
        wallet.createNewAccount("savings");

        try{
            Wallet oopsWallet = new Wallet("MINE", "CA");
        }catch (UniqueIDConstraintException e) {
            assert(true);
            return;
        }

        assert(false);
    }

    @Test
    public void loadWalletThatDoesNotExist() {
        try{
            Wallet wallet = Wallet.loadWallet("none");
        }catch (NoSuchObjectInDatabaseException e){
         assert (true);
         return;
        }

        assert(false);

    }

}