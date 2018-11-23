import org.junit.jupiter.api.RepeatedTest;
import uwallet.Wallet;
import uwallet.exceptions.InsufficientFundsException;
import uwallet.exceptions.NoSuchObjectInDatabaseException;
import uwallet.exceptions.UniqueIDConstraintException;

import java.math.BigDecimal;

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
    public void testOneWalletConcurrentAccess() throws UniqueIDConstraintException, NoSuchObjectInDatabaseException, InterruptedException {
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

                    synchronized (wallet.getAccount("chequing")){
                        BigDecimal currentBalance = wallet.getAccountBalanceBigDecimal("chequing");
                        wallet.depositToAccount(200.0, "chequing", "a test");
                        BigDecimal finalBalance = currentBalance.add(new BigDecimal(200.0));

                        if (!wallet.getAccountBalanceBigDecimal("chequing").toString().equals(finalBalance.toString())){
                            System.out.println("Test Fail. Expected Balance = " + finalBalance + " but balance is " + wallet.getAccountBalanceBigDecimal("chequing").toString());
                            System.exit(0);
                        }

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

                    synchronized (wallet.getAccount("chequing")) {
                        BigDecimal currentBalance = wallet.getAccountBalanceBigDecimal("chequing");
                        wallet.depositToAccount(500.0, "chequing", "a second test");
                        BigDecimal finalBalance = currentBalance.add(new BigDecimal(500.0));

                        if (!wallet.getAccountBalanceBigDecimal("chequing").toString().equals(finalBalance.toString())) {
                            System.out.println("Test Fail. Expected Balance = " + finalBalance + " but balance is " + wallet.getAccountBalanceBigDecimal("chequing").toString());
                            System.exit(0);
                        }
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
