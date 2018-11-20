package uwallet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;


import uwallet.exceptions.InsufficientFundsException;
import uwallet.exceptions.NoSuchAccountInDatabaseException;

public class Account{
    //RI:
    //
    //AF:

    //constants
    //TODO: make language an option for the user to choose
    private static final String LANGUAGE  = "en";

    private static List<WeakReference<Account>> loadedAccountObjects = new ArrayList<WeakReference<Account>>();
    private static ReferenceQueue<Object> rq = new ReferenceQueue<Object>();

    private BigDecimal balance;
    private final String accountName;
    private final String id;
    private final String regionCode;
    private int last_txID = 0; //the last txID that was created. 0 referring to nothing was last.
    private final NumberFormat currencyFormat;

    private List<Transaction> uncomitedTransactions = new ArrayList<Transaction>();
//    private final TransactionHistory transactionHistory; //TODO: create transaction history file


    /**
     * Creates an Account object with a name defined by accountName. Balance is initialized to 0.
     *
     * @param accountName
     *              the name of the account. a String that can not be empty.
     * @param uniqueIdentifier
     *              a string that uniquely identifies this account - needs to be globally unique. No
     *              two accounts should have the same uniqueIdentifier - can not be Null or empty
     * @param currencyCountry
     *              ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country whose
     *              currency is desired
     *
     */
    public Account(String accountName, String uniqueIdentifier, String currencyCountry){

        this.id = uniqueIdentifier;
        this.accountName = accountName;
        this.balance = new BigDecimal("0");
        this.regionCode = currencyCountry;
        this.currencyFormat = NumberFormat.getCurrencyInstance( new Locale(LANGUAGE, currencyCountry) );


        WeakReference<Account> weakr = new WeakReference<Account>(this, rq);
        loadedAccountObjects.add(weakr);
//        this.transactionHistory = new TransactionHistory(String.valueOf(this.accountNumber));
    }

    protected Account(String accountName, String uniqueIdentifier,
                      String currencyCountry, String balance, int last_txID){

        this.id = uniqueIdentifier;
        this.last_txID = last_txID;
        this.accountName = accountName;
        this.balance = new BigDecimal(balance);
        this.regionCode = currencyCountry;
        this.currencyFormat = NumberFormat.getCurrencyInstance( new Locale(LANGUAGE, currencyCountry) );

        //remove null pointers
        WeakReference<Account> weakr = new WeakReference<Account>(this, rq);
        loadedAccountObjects.add(weakr);
//        this.transactionHistory = new TransactionHistory(String.valueOf(this.accountNumber));
    }

    /**
     * Loads an Account object from persistent database given the uniqueIdentifier. requires the SQLite JDBC driver library
     * dependency. If a reference to the account object with the identifier already exists, the object will be returned
     * instead of creating a new one to ensure that no 2 Account object exists to represent a single account.
     *
     * @param uniqueIdentifier the uniqueIdentifer of the account that wants to be accessed. can not be empty or null
     *
     * @return Account object as defined within the DB
     *
     * @throws NoSuchAccountInDatabaseException
     *          if the account with the given uniqueIdentifier does not match any account that has been committed to
     *          the database as well as accounts in memory.
     */
    static public Account loadAccount(String uniqueIdentifier) throws NoSuchAccountInDatabaseException {
        Iterator<WeakReference<Account>> itr = loadedAccountObjects.iterator();
        while(itr.hasNext()) {
            Account acc = (Account) itr.next().get();
            if (acc == null)
                itr.remove();
            else if(acc.getAccountID() == uniqueIdentifier)
                return acc;
        }
        //a Account object for this account is not already loaded, so load one from the DB and return it.

        SQL sql = new SQL();

        return sql.getAccount(uniqueIdentifier);

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
    public synchronized void deposit(double amount, String... description){
        this.last_txID += 1;
        DepositTransaction depositTX = new DepositTransaction(amount, this, "TX"+String.valueOf(this.last_txID),description);
        this.balance = depositTX.endingBalance;
        uncomitedTransactions.add(depositTX);
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
    public synchronized void withdraw(double amount, String... description) throws InsufficientFundsException{
        this.last_txID += 1;
        WithdrawalTransaction withdrawalTX = new WithdrawalTransaction(amount, this, "TX"+String.valueOf(this.last_txID), description);
        BigDecimal afterWithdrawalBalance = withdrawalTX.endingBalance;
        if (afterWithdrawalBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new InsufficientFundsException(String.format("%s only has %s", this.accountName, this.getFormattedBalance()));
        }

        this.balance = afterWithdrawalBalance;
        uncomitedTransactions.add(withdrawalTX);
    }

    /**
     * updates the persistent data to contain transactions that have not been saved yet. Must be run after complete
     * operations on the account to ensure that they will be persistent. Uploads the state of the account and
     * transactions to SQLite databases. Requires the JDBC SQLite driver dependency.
     *
     * It will create a directory ./sqlite to store database files if it does not exist.
     */
    public void commit(){

        SQL sql = new SQL();
        sql.insertAccount(this);

        for(Transaction tx : this.uncomitedTransactions) {
            sql.insertTransaction(tx);
        }

        //clear the list as the transactions have now been committed to the DB
        this.uncomitedTransactions.clear();

    }

    /**
     * @return String - the name of the account
     */
    public String getAccountName(){
        return this.accountName;
    }

    /**
     * @return int - the account id
     */
    public String getAccountID(){
        return this.id;
    }

    /**
     *
     * @return String - ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country that was used.
     */
    public String getRegionCode(){
        return this.regionCode;
    }

    /**
     *
     * @return int - a variable that keeps track of the number of transaction object was created, this variable is
     * used to ensure uniqueness in the identifier for the transactions and does not represent the actual number of
     * transactions stored for this account.
     */
    public int getLastTxId(){
        return this.last_txID;
    }

    /**
     *
     * @return a BigDecimal object representing the CURRENT balance -  it is a new object that will not update
     * with changes to the account.
     */
    public BigDecimal getCurrentBalance(){
        return new BigDecimal(this.balance.toString());
    }

    /**
     * @return String - the balance in english, as formatted as per standards for the country that was used to initialize
     * this account.
     */
    public String getFormattedBalance(){

        return this.currencyFormat.format(this.balance);
    }

    /**
     * @param amount double -  a double representing the amount to be formatted.
     *
     * @return String - a formatted version of the double passed in using this account's currency format.
     */
    public String applyAccountFormat(double amount){
        return this.currencyFormat.format(amount);
    }

    /**
     * @param amount BigDecimal -  a BigDecimal representing the amount to be formatted.
     *
     * @return String - a formatted version of the double passed in using this account's currency format.
     */
    public String applyAccountFormat(BigDecimal amount){
        return this.currencyFormat.format(amount);
    }

    @Override
    public String toString(){
        return this.id + "|" + this.accountName + " : " + this.getFormattedBalance();
    }
}