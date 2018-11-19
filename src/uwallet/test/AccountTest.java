package uwallet.test;

import uwallet.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.Test;

import uwallet.Account;

class AccountTest {

    @Test
    public void testAccountGeneratorDifferentCountries(){
        //list of currency formats
        //https://www.thefinancials.com/Default.aspx?SubSectionID=curformat

        //list of region codes
        //https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2

        Account accountUSD = new Account("chequing", "US");
        assert("$0.00".equals(accountUSD.getFormattedBalance()));

        Account accountEUR = new Account("chequing", "FR");
        assert("€0.00".equals(accountEUR.getFormattedBalance()));

        // expecting 0 decimals
        Account accountPESO = new Account("chequing", "CL");
        assert("CLP0".equals(accountPESO.getFormattedBalance()));

        // here we expect 3 decimal points
        Account accountDINAR = new Account("chequing", "JO");
        assert("JOD0.000".equals(accountDINAR.getFormattedBalance()));

    }

    @Test
    public void recurrentBinaryDecimalsDepositTest(){
        Account accountUSD = new Account("chequing", "US");
        accountUSD.deposit(0.1);
        accountUSD.deposit(0.2);

        assert("$0.30".equals(accountUSD.getFormattedBalance()));
    }

    @Test
    public void depositSmallValueToBigBalance(){
        //this is meant to test the accuracy not being limited by that of a double
        Account accountUSD = new Account("chequing", "US");
        accountUSD.deposit(1000000000000000000000.00);
        accountUSD.deposit(10.21);

        assert("$1,000,000,000,000,000,000,010.21".equals(accountUSD.getFormattedBalance()));

    }


    @Test
    public void addMaxDoubleValueTwiceAndAddOne(){
        // tests that the precision is truly arbitrary and only limited by memory on the system
        Account accountUSD = new Account("chequing", "US");
        accountUSD.deposit(Double.MAX_VALUE);
        accountUSD.deposit(Double.MAX_VALUE);
        accountUSD.deposit(1.25);

        assert("$359,538,626,972,463,140,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,000,001.25".equals(accountUSD.getFormattedBalance()));
    }


    @Test
    public void recurrentBase10DecimalsDepositTest(){
        Account accountUSD = new Account("chequing", "US");
        accountUSD.deposit((double)1/3);
        accountUSD.deposit((double)1/3);
        accountUSD.deposit((double)1/3);

        assert("$1.00".equals(accountUSD.getFormattedBalance()));
    }


    @Test
    public void testAgainstDoubleRoundingError(){
        Account accountUSD = new Account("chequing", "US");
        accountUSD.deposit(0.1);
        accountUSD.deposit(0.2);

        assert(accountUSD.getCurrentBalance().toString().equals("0.3"));
    }

    @Test
    public void simpleWithdrawalTest(){
        Account accountUSD = new Account("chequing", "US");
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
    public void insufficientFundsWithdrawalTest(){
        Account accountUSD = new Account("chequing", "US");

        try{
            accountUSD.withdraw(0.3);
        } catch (InsufficientFundsException err){
            assert(true);
            return;
        }

        assert(false);
    }

    @Test
    public void accountCommitTest(){
        Account accountUSD = new Account("chequing", "US");
        accountUSD.deposit(1, "some cash");
        accountUSD.deposit(2, "nahmsaying");
        try{
            accountUSD.withdraw(1, "lost it");
        } catch (InsufficientFundsException err){}
        accountUSD.deposit(3, "but made it back");
        accountUSD.commit();
        System.out.println(accountUSD);
        assert(false);
    }

}

