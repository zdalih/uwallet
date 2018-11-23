package uwallet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import uwallet.exceptions.InsufficientFundsException;
import uwallet.exceptions.UniqueAccountIDConstraintException;

public class ConcurrencyTest {

    @BeforeAll
    static public void flushDb() {
        uWalletDatabase.flush();
    }

    /**
     * Brute force testing - to find the most obvious of flaws.
     */
    @RepeatedTest(1000)
    public void testConcurrentWrites() throws InterruptedException {

        uWalletDatabase.flush();

        Thread t1 = new Thread() {
            public void run() {
                try {
                    Account acc1 = new Account("mymoney", "CONC1", "wallet", "US");
                    Account acc2 = new Account("mymoney", "CONC2", "wallet", "US");
                    Account acc3 = new Account("mymoney", "CONC3", "wallet", "US");
                    uWalletDatabase.insertAccount(acc1);
                    uWalletDatabase.insertAccount(acc2);
                    uWalletDatabase.insertAccount(acc3);
                    acc1.deposit(100);
                    acc2.deposit(200);
                    acc3.deposit(300);

                    acc1 = null;
                    acc2 = null;
                    acc3 = null;

                    System.gc();

                    Account acc1_new = Account.loadAccount("CONC1");
                    Account acc2_new = Account.loadAccount("CONC2");
                    Account acc3_new = Account.loadAccount("CONC3");

                    assert (acc1_new.getFormattedBalance().equals("$100.00"));
                    assert (acc2_new.getFormattedBalance().equals("$200.00"));
                    assert (acc3_new.getFormattedBalance().equals("$300.00"));


                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        };

        Thread t2 = new Thread() {
            public void run() {
                try {
                    Account acc1 = new Account("mymoney", "CONC4", "wallet", "US");
                    Account acc2 = new Account("mymoney", "CONC5", "wallet", "US");
                    Account acc3 = new Account("mymoney", "CONC6", "wallet", "US");
                    uWalletDatabase.insertAccount(acc1);
                    uWalletDatabase.insertAccount(acc2);
                    uWalletDatabase.insertAccount(acc3);
                    acc1.deposit(100);
                    acc2.deposit(200);
                    acc3.deposit(300);


                    acc3.withdraw(50);

                    acc1 = null;
                    acc2 = null;
                    acc3 = null;

                    System.gc();

                    Account acc1_new = Account.loadAccount("CONC4");
                    Account acc2_new = Account.loadAccount("CONC5");
                    Account acc3_new = Account.loadAccount("CONC6");

                    assert (acc1_new.getFormattedBalance().equals("$100.00"));
                    assert (acc2_new.getFormattedBalance().equals("$200.00"));
                    assert (acc3_new.getFormattedBalance().equals("$250.00"));
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        };

        Thread t3 = new Thread() {
            public void run() {
                try {
                    Account acc1 = new Account("mymoney", "CONC7", "wallet", "US");
                    Account acc2 = new Account("mymoney", "CONC8", "wallet", "US");
                    Account acc3 = new Account("mymoney", "CONC9", "wallet", "US");
                    uWalletDatabase.insertAccount(acc1);
                    uWalletDatabase.insertAccount(acc2);
                    uWalletDatabase.insertAccount(acc3);
                    acc1.deposit(700);
                    acc2.deposit(800);
                    acc3.deposit(900);


                    acc1 = null;
                    acc2 = null;
                    acc3 = null;

                    System.gc(); //throw in the GC to throw off the account's class even more

                    Account acc1_new = Account.loadAccount("CONC7");
                    Account acc2_new = Account.loadAccount("CONC8");
                    Account acc3_new = Account.loadAccount("CONC9");

                    acc3_new.withdraw(1);

                    assert (acc1_new.getFormattedBalance().equals("$700.00"));
                    assert (acc2_new.getFormattedBalance().equals("$800.00"));
                    assert (acc3_new.getFormattedBalance().equals("$899.00"));
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        };

        Thread t4 = new Thread() {
            public void run() {
                try {
                    Account acc1 = new Account("mymoney", "CONC10", "wallet", "US");
                    Account acc2 = new Account("mymoney", "CONC11", "wallet", "US");
                    Account acc3 = new Account("mymoney", "CONC12", "wallet", "US");
                    uWalletDatabase.insertAccount(acc1);
                    uWalletDatabase.insertAccount(acc2);
                    uWalletDatabase.insertAccount(acc3);
                    acc1.deposit(100);
                    acc2.deposit(200);
                    acc3.deposit(300);

                    acc1 = null;
                    acc2 = null;
                    acc3 = null;

                    System.gc();

                    Account acc1_new = Account.loadAccount("CONC10");
                    Account acc2_new = Account.loadAccount("CONC11");
                    Account acc3_new = Account.loadAccount("CONC12");

                    assert (acc1_new.getFormattedBalance().equals("$100.00"));
                    assert (acc2_new.getFormattedBalance().equals("$200.00"));
                    assert (acc3_new.getFormattedBalance().equals("$300.00"));
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        };

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        //wait for threads to end and clean up variables to repeat test gracefully
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        System.gc();

    }

    /**
     * Testing doing concurrent actions on the SAME account
     */

    @RepeatedTest(1000)
    public void testConcurrentActionsOnSameAccount() throws UniqueAccountIDConstraintException, InterruptedException {
        Account acc1 = new Account("mymoney", "CONC20", "wallet", "US");
        acc1 = null;
        System.gc();

        Thread t1 = new Thread() {
            public void run() {
                try {

                    Account loadedAccount20 = Account.loadAccount("CONC20");

                    loadedAccount20.deposit(1000.25);
                    loadedAccount20.deposit(300.25);
                    loadedAccount20.withdraw(500.00);

                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        };

        Thread t2 = new Thread() {
            public void run() {
                try {

                    Account loadedAccount20 = Account.loadAccount("CONC20");

                    loadedAccount20.withdraw(300);

                } catch (InsufficientFundsException e) {
                    //in this case, t2, which was started after
                    //t1 has attempted a withdrawal before t1 has finished.
                    //is that ok? mhh
                    System.out.println("no money");
                    return;
                } catch (Exception e) {
                    System.err.println(e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        };

        t1.start();
        t2.start();


        //wait for threads to end and clean up variables to repeat test gracefully
        t1.join();
        t2.join();
        uWalletDatabase.flush();
        System.gc();

    }
}



