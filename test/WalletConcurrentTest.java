import org.junit.jupiter.api.RepeatedTest;
import uwallet.Wallet;
import uwallet.exceptions.InsufficientFundsException;
import uwallet.exceptions.NoSuchAccountInDatabaseException;
import uwallet.exceptions.UniqueAccountIDConstraintException;

public class WalletConcurrentTest {

    @RepeatedTest(1000)
    public void testTwoDifferentConcurrentWallet() throws InterruptedException {

        Wallet.deleteAllRecord("delete");

        Thread t1 = new Thread() {
            public void run() {
                boolean passed = false;
                try {
                    Wallet wallet = new Wallet("WALL001", "FR");
                    wallet.createNewAccount("savings");
                    wallet.depositToAccount(50.00, "savings");
                    wallet.depositToAccount(70.00, "savings");

                    if (wallet.getAccountBalanceFormatted("savings").equals("€120.00"))
                        passed = true;

                    wallet.withdrawFromAccount(130, "savings");
                    passed = false;

                } catch (InsufficientFundsException e){
                    passed = true;
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
                }
                assert(passed);
            }
        };
        Thread t2 = new Thread() {
            public void run() {
                boolean passed = false;
                try {
                    Wallet wallet = new Wallet("WALL002", "US");
                    wallet.createNewAccount("savings");
                    wallet.depositToAccount(40.00, "savings");
                    wallet.depositToAccount(40.00, "savings");

                    if (wallet.getAccountBalanceFormatted("savings").equals("$80.00"))
                        passed = true;

                    wallet.withdrawFromAccount(80.000001, "savings");
                    passed = false;

                } catch (InsufficientFundsException e){
                    passed = true;
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
                }
                assert(passed);
            }
        };

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.gc();
    }

    @RepeatedTest(1000)
    public void testOneWalletConcurrentAccess() throws UniqueAccountIDConstraintException, NoSuchAccountInDatabaseException, InterruptedException {
        Wallet.deleteAllRecord("delete");
        Wallet wallet = new Wallet("WALL002", "US");
        wallet.createNewAccount("chequing");
        wallet.depositToAccount(150.00, "chequing");
        wallet = null;
        System.gc();


        Thread t1 = new Thread() {
            public void run() {
                try {
                    Wallet wallet = Wallet.loadWallet("WALL002");

                    if (!wallet.getAccountBalanceFormatted("chequing").equals("$150.00")){
                        System.out.println("Test Fail, $150.00 is no balance of account.");
                        System.exit(0);
                    }

                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();

                }
            }
        };

        Thread t2 = new Thread() {
            public void run() {
                try {
                    Wallet wallet = Wallet.loadWallet("WALL002");

                    if (!wallet.getAccountBalanceFormatted("chequing").equals("$150.00")){
                        System.out.println("Test Fail, $150.00 is no balance of account.");
                        System.exit(0);
                    }

                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        };

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.gc();

    }
}
