package uwallet;

import java.util.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;


import uwallet.exceptions.InsufficientFundsException;
import uwallet.exceptions.NoSuchAccountInDatabaseException;
import uwallet.exceptions.UniqueAccountIDConstraintException;

class Account {
    //RI: There can never be more then two objects in existence with the same id. Once a regionCode
    //is chosen to initialize an account it can not be changed. The last_txID needs to be incremented
    //after ALL new deposit or withdrawal calls.
    //
    //AF: Represents a financial account.  When calls to deposit and withdrawal
    //are made the whole account gets locked until it is done pushing changes to the uWalletDatabase.
    //it has a parentUID which can be any String and will not hamper the functioning of the class.
    //the balance is of arbitrary accuracy and size. When the constructor is called - a new Account is
    //that must have a non-existent id is created. To get the object referring to a previously created account
    //one must use the static loadAccount(id) method. To assure unique creation of Transaction ID the account
    //keeps track of last_txId which get incremented after each new Transaction.

    //TODO: make language an option for the user to choose
    private static final String LANGUAGE = "en";

    //the following vars are  used to refer to active objects to ensure that we never have two
    //Account objects referring to the same account active at the same time.
    private static List<WeakReference<Account>> loadedAccountObjects = new ArrayList<WeakReference<Account>>();
    private static ReferenceQueue<Object> rq = new ReferenceQueue<Object>();

    private BigDecimal balance;
    private final String accountName;
    private final String id;
    private final String parentWalletUID;
    private final String regionCode;
    private int last_txID = 0; //the last txID that was created. 0 referring to nothing was last.
    private final NumberFormat currencyFormat;

    /**
     * Creates an Account object with a name defined by accountName. Balance is initialized to 0.
     *
     * @param accountName      the name of the account. a String that can not be empty.
     * @param uniqueIdentifier a string that uniquely identifies this account - needs to be globally unique. No
     *                         two accounts should have the same uniqueIdentifier - can not be Null or empty
     * @param currencyCountry  ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country whose
     *                         currency is desired
     * @throws UniqueAccountIDConstraintException if another Account object with the same uniqueIdentifier already exists either in DB or in memory.
     */
    Account(String accountName, String uniqueIdentifier, String parentWalletUID, String currencyCountry) throws UniqueAccountIDConstraintException {
        try {
            this.loadAccount(uniqueIdentifier);
            throw new UniqueAccountIDConstraintException("Unique Identifier: " + uniqueIdentifier + " is already allocated to an account!");
        } catch (NoSuchAccountInDatabaseException e) {
            this.id = uniqueIdentifier;
            this.parentWalletUID = parentWalletUID;
            this.accountName = accountName;
            this.balance = new BigDecimal("0");
            this.regionCode = currencyCountry;
            this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale(LANGUAGE, currencyCountry));
            this.commit(new ArrayList<Transaction>());

            //this must always be locked when being changed as it is static
            //and if another object iterates through the list while we change it
            //it will raise a ConcurrentModificationException
            synchronized (loadedAccountObjects) {
                WeakReference<Account> weakr = new WeakReference<Account>(this, rq);
                this.removeNullPointersInActiveObjectList();
                loadedAccountObjects.add(weakr);
            }
        }
    }

    Account(String accountName, String uniqueIdentifier, String parentWalletUID,
            String currencyCountry, String balance, int last_txID) {

        this.id = uniqueIdentifier;
        this.parentWalletUID = parentWalletUID;
        this.last_txID = last_txID;
        this.accountName = accountName;
        this.balance = new BigDecimal(balance);
        this.regionCode = currencyCountry;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale(LANGUAGE, currencyCountry));

        synchronized (loadedAccountObjects) {
            WeakReference<Account> weakr = new WeakReference<Account>(this, rq);
            //clean up null pointers and add a pointer to this object
            this.removeNullPointersInActiveObjectList();
            loadedAccountObjects.add(weakr);
        }
    }

    /**
     * Loads an Account object from persistent database given the uniqueIdentifier. requires the SQLite JDBC driver library
     * dependency. If a reference to the account object with the identifier already exists, the object will be returned
     * instead of creating a new one to ensure that no 2 Account object exists to represent a single account.
     *
     * @param uniqueIdentifier the uniqueIdentifer of the account that wants to be accessed. can not be empty or null
     * @return Account object as defined within the DB
     * @throws NoSuchAccountInDatabaseException if the account with the given uniqueIdentifier does not match any account that has been committed to
     *                                          the database as well as accounts in memory.
     */
    static Account loadAccount(String uniqueIdentifier) throws NoSuchAccountInDatabaseException {

        synchronized (loadedAccountObjects) {
            for (Iterator<WeakReference<Account>> itr = loadedAccountObjects.iterator(); itr.hasNext(); ) {

                Account acc = itr.next().get();

                if (acc == null)
                    continue;
                else if (acc.getAccountID().equals(uniqueIdentifier))
                    return acc;

            }
        }

        //a Account object for this account is not already loaded, so load one from the DB and return it.
        return uWalletDatabase.getAccount(uniqueIdentifier);


    }

    /**
     * Increases the balance by the given amount, and adds a DepositTransaction representing this transaction
     * to the list of transactions that have not been commited to transaction history. Please use Account.commit()
     * to ensure that the changes will persist past system shutdown.
     *
     * @param amount - double
     *               the amount to be deposited. must be positive. the balance is an arbitrary precision class and will
     *               get as big as the systems memory allows it
     *
     * @param description String (optional).
     *            description[0] is a String of at most 50char that is not null. All other items in description
     *            are ignored. The default description is N/A. Should not be an empty string.
     *
     */
    synchronized void deposit(double amount, String... description){
        this.last_txID += 1;
        DepositTransaction depositTX = new DepositTransaction(amount, this, "TX"+String.valueOf(this.last_txID),description);
        this.balance = depositTX.endingBalance;
        List<Transaction> uncomitedTransactions = new ArrayList<Transaction>();
        uncomitedTransactions.add(depositTX);
        this.commit(uncomitedTransactions);
    }


    /**
     * Decreases the balance by the given amount, and adds a WithdrawalTransaction representing this transaction
     * to the list of transactions that have not been commited to transaction history. Please use Account.commit()
     * to ensure that the changes will persist past system shutdown.
     *
     * @param amount - double
     *               the amount to be withdrawn. must be positive. the balance is an arbitrary precision class and will
     *               get as big as the systems memory allows it
     * @param description String (optional).
     *            description[0] is a String of at most 50char that is not null. All other items in description
     *            are ignored. The default description is N/A. Should not be an empty string.
     *
     * @throws InsufficientFundsException
     *               if the the withdrawal would cause the balance in the account to be negative
     */
    synchronized void withdraw(double amount, String... description) throws InsufficientFundsException{
        this.last_txID += 1;
        WithdrawalTransaction withdrawalTX = new WithdrawalTransaction(amount, this, "TX"+String.valueOf(this.last_txID), description);
        BigDecimal afterWithdrawalBalance = withdrawalTX.endingBalance;
        if (afterWithdrawalBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new InsufficientFundsException(String.format("%s only has %s", this.accountName, this.getFormattedBalance()));
        }

        this.balance = afterWithdrawalBalance;
        List<Transaction> uncomitedTransactions = new ArrayList<Transaction>();
        uncomitedTransactions.add(withdrawalTX);
        this.commit(uncomitedTransactions);
    }

    /**
     * updates the persistent data to contain transactions that have not been saved yet. Must be run after complete
     * operations on the account to ensure that they will be persistent. Uploads the state of the account and
     * transactions to SQLite databases. Requires the JDBC SQLite driver dependency.
     *
     * It will create a directory ./sqlite to store database files if it does not exist.
     */
    private void commit(List<Transaction> uncomitedTransactions ){

        uWalletDatabase.insertAccount(this);

        for(Transaction tx : uncomitedTransactions) {
            uWalletDatabase.insertTransaction(tx);
        }

        //clear the list as the transactions have now been committed to the DB
        uncomitedTransactions.clear();
    }

    /**
     * @return List<Transaction> - which is a list of length 0-N (limited by the total number of transactions for
     * the account) of the last 0-N transactions that are on file for this account. If multiple transactions have
     * timestamp within 1ms of each other - which transaction is prioritized is not defined. Only returns
     * transactions made BEFORE method is called.
     *
     * This will only return transactions that have been made before calls to account.commit(), as those
     * are the only transactions that have actually been recorded.
     */
    synchronized List<Transaction> getPastTransactions(int N) {
        try {
            return uWalletDatabase.getNLastTransactions(this.id, N);
        } catch (NoSuchAccountInDatabaseException e) {
            //this account has never been committed to the DB yet
            //so return an empty list
            return new ArrayList<Transaction>();
        }
    }

    /**
     * @return String - the name of the account
     */
    String getAccountName(){
        return this.accountName;
    }

    /**
     * @return int - the account id
     */
    String getAccountID(){
        return this.id;
    }

    /**
     *
     * @return String - ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country that was used.
     */
    String getRegionCode(){
        return this.regionCode;
    }

    /**
     *
     * @return int - a variable that keeps track of the number of transaction object was created, this variable is
     * used to ensure uniqueness in the identifier for the transactions and does not represent the actual number of
     * transactions stored for this account.
     */
    int getLastTxId(){
        return this.last_txID;
    }

    /**
     *
     * @return a BigDecimal object representing the CURRENT balance -  it is a new object that will not update
     * with changes to the account.
     */
    BigDecimal getCurrentBalance(){
        return new BigDecimal(this.balance.toString());
    }

    /**
     * @return String - the balance in english, as formatted as per standards for the country that was used to initialize
     * this account.
     */
    String getFormattedBalance(){

        return this.currencyFormat.format(this.balance);
    }

    /**
     *
     * @return the UID of the parent wallet.
     */
    String getParentWalletUID(){
        return this.parentWalletUID;
    }

    /**
     * @param amount double -  a double representing the amount to be formatted.
     *
     * @return String - a formatted version of the double passed in using this account's currency format.
     */
    String applyAccountFormat(double amount){
        return this.currencyFormat.format(amount);
    }

    /**
     * @param amount BigDecimal -  a BigDecimal representing the amount to be formatted.
     *
     * @return String - a formatted version of the double passed in using this account's currency format.
     */
    String applyAccountFormat(BigDecimal amount){
        return this.currencyFormat.format(amount);
    }

    @Override
    public String toString(){
        return this.id + "|" + this.accountName + " : " + this.getFormattedBalance();
    }

    private void removeNullPointersInActiveObjectList(){
        //clean up the loadedAccountObject list to remove null pointers
        synchronized (loadedAccountObjects){
            Iterator<WeakReference<Account>> itr = loadedAccountObjects.iterator();
            while(itr.hasNext()) {
                Account acc = itr.next().get();
                if (acc == null)
                    itr.remove();
            }
        }

    }

}