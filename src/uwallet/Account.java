package uwallet;

import java.util.Locale;
import java.math.BigDecimal;
import java.text.NumberFormat;

import java.io.File;


import uwallet.exceptions.InsufficientFundsException;

public class Account{
    //RI:
    //
    //AF:

    //constants
    //TODO: make language an option for the user to choose
    private static final String LANGUAGE  = "en";

    private BigDecimal balance;
    private final String accountName;
    private final NumberFormat currencyFormat;
//    private final File transactionHistoryCSV; //TODO: create transaction history file




    /**
     * Creates an Account object with a name defined by accountName. Balance is initialized to 0.
     *
     * @param accountName
     *              the name of the account. a String that can not be empty.
     * @param currencyCountry
     *              ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country whose
     *              currency is desired
     */
    public Account(String accountName, String currencyCountry){
        this.balance = new BigDecimal("0");
        this.accountName = accountName;
        this.currencyFormat = NumberFormat.getCurrencyInstance( new Locale(LANGUAGE, currencyCountry) );
    }

    /**
     * Increases the balance by the given amount.
     *
     * @param amount - double
     *               the amount to be deposited. must be positive. the balance is an arbitrary precision class and will
     *               get as big as the systems memory allows it
     *
     */
    public void deposit(double amount){

        this.balance = this.balance.add(new BigDecimal(amount));
    }

    /**
     * Increases the balance by the given amount.
     *
     * @param amount - double
     *               the amount to be withdrawn. must be positive. the balance is an arbitrary precision class and will
     *               get as big as the systems memory allows it
     *
     * @throws InsufficientFundsException
     *               if the the withdrawal would cause the balance in the account to be negative
     */
    public void withdraw(double amount) throws InsufficientFundsException{
        BigDecimal afterWithdrawalBalance = this.balance.subtract(new BigDecimal(amount));
        if (afterWithdrawalBalance.compareTo(BigDecimal.ZERO) < 0){
            throw new InsufficientFundsException(String.format("%s only has %s", this.accountName, this.getFormattedBalance()));
        }

        this.balance = afterWithdrawalBalance;
    }



    /**
     * @return String - the balance in english, as formatted as per standards for the country that was used to initialize
     * this account.
     */
    public String getFormattedBalance(){

        return this.currencyFormat.format(this.balance);
    }

    @Override
    public String toString(){
        return this.accountName + " : " + this.getFormattedBalance();
    }
}