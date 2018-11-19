package uwallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.math.BigDecimal;
import java.text.NumberFormat;

import uwallet.exceptions.InsufficientFundsException;

public class Account{
    //RI:
    //
    //AF:

    //constants
    //TODO: make language an option for the user to choose
    private static final String LANGUAGE  = "en";
    private static int NUM_ACCOUNTS = 0;

    private final String accountName;
    private final int accountNumber;
    private BigDecimal balance;
    public final NumberFormat currencyFormat;

    private List<Transaction> uncomitedTransactions = new ArrayList<Transaction>();
//    private final TransactionHistory transactionHistory; //TODO: create transaction history file


    /**
     * Creates an Account object with a name defined by accountName. Balance is initialized to 0.
     *
     * @param accountName
     *              the name of the account. a String that can not be empty.
     * @param currencyCountry
     *              ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country whose
     *              currency is desired
     *
     */
    public Account(String accountName, String currencyCountry){
        NUM_ACCOUNTS += 1;

        this.balance = new BigDecimal("0");
        this.accountName = accountName;
        this.accountNumber = NUM_ACCOUNTS;
        this.currencyFormat = NumberFormat.getCurrencyInstance( new Locale(LANGUAGE, currencyCountry) );
//        this.transactionHistory = new TransactionHistory(String.valueOf(this.accountNumber));
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
        DepositTransaction depositTX = new DepositTransaction(amount, this, description);
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
        WithdrawalTransaction withdrawalTX = new WithdrawalTransaction(amount, this, description);
        BigDecimal afterWithdrawalBalance = withdrawalTX.endingBalance;
        if (afterWithdrawalBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new InsufficientFundsException(String.format("%s only has %s", this.accountName, this.getFormattedBalance()));
        }

        this.balance = afterWithdrawalBalance;
        uncomitedTransactions.add(withdrawalTX);
    }

    /**
     * updates the persistent data to contain transactions that have not been saved yet. Must be run after complete
     * operations on the account to ensure that they will be persistent.
     */
    public void commit(){
        for(Transaction tx : this.uncomitedTransactions){
//            this.transactionHistory.audit(tx);
            System.out.println(tx);
        }
    }

    /**
     * @return String - the name of the account
     */
    public String getAccountName(){
        return this.accountName;
    }

    /**
     * @return int - the account number
     */
    public int getAccountNumber(){
        return this.accountNumber;
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
     * @param double -  a double representing the amount to be formatted.
     *
     * @return String - a formatted version of the double passed in using this account's currency format.
     */
    public String applyAccountFormat(double amount){
        return this.currencyFormat.format(amount);
    }

    /**
     * @param BigDecimal -  a BigDecimal representing the amount to be formatted.
     *
     * @return String - a formatted version of the double passed in using this account's currency format.
     */
    public String applyAccountFormat(BigDecimal amount){
        return this.currencyFormat.format(amount);
    }

    @Override
    public String toString(){
        return this.accountName + " : " + this.getFormattedBalance();
    }
}