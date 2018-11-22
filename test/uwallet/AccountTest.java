package uwallet;

import org.junit.jupiter.api.BeforeAll;
import uwallet.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import uwallet.Account;
import uwallet.exceptions.UniqueAccountIDConstraintException;

import java.lang.reflect.Constructor;

class AccountTest {

    @BeforeAll
    public static void flushDb(){
        uWalletDatabase db = new uWalletDatabase();
        db.flush();
    }

    @Test
    public void testAccountGeneratorDifferentCountries() throws UniqueAccountIDConstraintException {
        //list of currency formats
        //https://www.thefinancials.com/Default.aspx?SubSectionID=curformat

        //list of region codes
        //https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2

        Account accountUSD = new Account("chequing", "1", "wallet","US");
        assert("$0.00".equals(accountUSD.getFormattedBalance()));

        Account accountEUR = new Account("chequing", "2", "wallet","FR");
        assert("€0.00".equals(accountEUR.getFormattedBalance()));

        // expecting 0 decimals
        Account accountPESO = new Account("chequing", "3", "wallet","CL");
        assert("CLP0".equals(accountPESO.getFormattedBalance()));

        // here we expect 3 decimal points
        Account accountDINAR = new Account("chequing", "4","wallet", "JO");
        assert("JOD0.000".equals(accountDINAR.getFormattedBalance()));

    }

    @Test
    public void recurrentBinaryDecimalsDepositTest() throws UniqueAccountIDConstraintException {
        Account accountUSD = new Account("chequing", "5", "wallet","US");
        accountUSD.deposit(0.1);
        accountUSD.deposit(0.2);

        assert("$0.30".equals(accountUSD.getFormattedBalance()));
    }

    @Test
    public void depositSmallValueToBigBalance() throws UniqueAccountIDConstraintException {
        //this is meant to test the accuracy not being limited by that of a double
        Account accountUSD = new Account("chequing" , "6","wallet", "US");
        accountUSD.deposit(1000000000000000000000.00);
        accountUSD.deposit(10.21);

        assert("$1,000,000,000,000,000,000,010.21".equals(accountUSD.getFormattedBalance()));

    }


    @Test
    public void addMaxDoubleValueTwiceAndAddOne() throws UniqueAccountIDConstraintException {
        // tests that the precision is truly arbitrary and only limited by memory on the system
        Account accountUSD = new Account("chequing", "7", "wallet","US");
        accountUSD.deposit(Double.MAX_VALUE);
        accountUSD.deposit(Double.MAX_VALUE);
        accountUSD.deposit(1.25);

        assert("$359,538,626,972,463,140,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,001.25".equals(accountUSD.getFormattedBalance()));
    }


    @Test
    public void recurrentBase10DecimalsDepositTest() throws UniqueAccountIDConstraintException {
        Account accountUSD = new Account("chequing", "8", "wallet","US");
        accountUSD.deposit((double)1/3);
        accountUSD.deposit((double)1/3);
        accountUSD.deposit((double)1/3);

        assert("$1.00".equals(accountUSD.getFormattedBalance()));
    }


    @Test
    public void testAgainstDoubleRoundingError() throws UniqueAccountIDConstraintException {
        Account accountUSD = new Account("chequing", "9", "wallet", "US");
        accountUSD.deposit(0.1);
        accountUSD.deposit(0.2);

        assert(accountUSD.getCurrentBalance().toString().equals("0.3"));
    }

    @Test
    public void simpleWithdrawalTest() throws UniqueAccountIDConstraintException {
        Account accountUSD = new Account("chequing", "10","wallet", "US");
        accountUSD.deposit(1);

        try{
            accountUSD.withdraw(0.3);
        } catch (InsufficientFundsException err){
            //this should not be thrown
            assert(false);
        }

        assert("$0.70".equals(accountUSD.getFormattedBalance()));
    }


    @Test
    public void insufficientFundsWithdrawalTest() throws UniqueAccountIDConstraintException {
        Account accountUSD = new Account("chequing", "11","wallet", "US");

        try{
            accountUSD.withdraw(0.3);
        } catch (InsufficientFundsException err){
            assert(true);
            return;
        }

        assert(false);
    }

}

