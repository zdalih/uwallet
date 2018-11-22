package uwallet;

import uwallet.exceptions.InsufficientFundsException;
import uwallet.exceptions.NoSuchAccountInDatabaseException;
import uwallet.exceptions.UniqueAccountIDConstraintException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

public class Wallet{
    //RI:
    //
    //AF:
    //

    private HashMap<String, String> acountNameToAccountIdMap =  new HashMap<String, String>();
    private String regionCode;
    private String walletUID;

    /**
     * @param walletUID
     *      - globally unique string to identify this wallet. Can not be null or empty.
     * @param regionCode
     *         ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country whose
     *         currency is desired
     */
    public Wallet(String walletUID, String regionCode){
        this.regionCode = regionCode;
        this.walletUID = walletUID;

        uWalletDatabase db = new uWalletDatabase();
        db.insertWallet(this);
    }

    static public Wallet loadWallet(String walletUID){
        uWalletDatabase db = new uWalletDatabase();
        Wallet wallet = db.getWallet(walletUID);
        return wallet;
    }

    /**
     * @param walletUID
     *      - globally unique string to identify this wallet. Can not be null or empty.
     * @param regionCode
     *         ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country whose
     *         currency is desired
     */
    protected Wallet(String walletUID, String regionCode, HashMap acountNameToAccountIdMap){
        this.regionCode = regionCode;
        this.walletUID = walletUID;
        this.acountNameToAccountIdMap = acountNameToAccountIdMap;

        uWalletDatabase.insertWallet(this);
    }

    /**
     * Adds a new account to this wallet.
     *
     * @param accountName -  String
     *                    representing the name to be given to this account. Empty or Null strings are not allowed
     *
     * @throws UniqueAccountIDConstraintException
     *                    if this wallet already has an account with the desired name.
     */
    public void createNewAccount(String accountName) throws UniqueAccountIDConstraintException{
        String accountUID = this.walletUID + "ACC" + String.valueOf(this.acountNameToAccountIdMap.size() + 1);
        try {
            Account acc = new Account(accountName, accountUID, this.walletUID, this.regionCode);
            acc.commit();
            this.acountNameToAccountIdMap.put(accountName, accountUID);
        } catch ( UniqueAccountIDConstraintException e ){
            throw new UniqueAccountIDConstraintException("Wallet already has account with name '" + accountName + "'");
        }
    }

    /**
     * returns the formatted account balance for account with the given name.
     *
     * @param accountName - String
     *               the name of the account whose balance we want
     *
     * @throws NoSuchAccountInDatabaseException
     *               if no such account in this wallet have the given name
     */
    public String getAccountBalanceFormatted(String accountName) throws NoSuchAccountInDatabaseException{
        Account acc = Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));
        return acc.getFormattedBalance();
    }

    /**
     * returns the formatted account balance for account with the given name.
     *
     * @param accountName - String
     *               the name of the account whose balance we want
     *
     * @throws NoSuchAccountInDatabaseException
     *               if no such account in this wallet have the given name
     */
    public BigDecimal getAccountBalanceBigDecimal(String accountName) throws NoSuchAccountInDatabaseException{
        Account acc = Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));
        return acc.getCurrentBalance();
    }

    /**
     * deposits an amount of money to the account with the given name.
     *
     * @param amount - double
     *               the amount to be deposited to the account
     *
     * @param accountName - String
     *               the name of the account to deposit money to
     *
     * @throws NoSuchAccountInDatabaseException
     *               if the accountName does not represent an account that is held by this wallet.
     */
    public void depositToAccount(double amount, String accountName) throws NoSuchAccountInDatabaseException {
        Account acc = Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));
        acc.deposit(amount);
        acc.commit();
    }

    /**
     * withdraws an amount of money from the account with the given name.
     *
     * @param amount - double
     *               the amount to be withdrawn from the account
     *
     * @param accountName - String
     *               the name of the account to deposit money to
     *
     * @throws NoSuchAccountInDatabaseException
     *               if the accountName does not represent an account that is held by this wallet.
     * @throws InsufficientFundsException
     *               if the account does not have sufficient funds for the withdrawal.
     */
    public void withdrawFromAccount(double amount, String accountName)
            throws NoSuchAccountInDatabaseException, InsufficientFundsException{
        Account acc = Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));
        acc.withdraw(amount);
        acc.commit();
    }

    /**
     * transfers the given amount from one account to another
     *
     * @param amount - double
     *               the amount to be transferred between the two accounts
     *
     * @param fromAccountName - String
     *               the name of the account to withdraw money from
     *
     * @param toAccountName - String
     *                the name of the account to deposit the money to
     *
     * @throws NoSuchAccountInDatabaseException
     *               if one of the two account name do not refer to a valid account for this wallet
     * @throws InsufficientFundsException
     *               if the fromAccount does not have sufficient funds for the withdrawal.
     */
    public void transfer(double amount, String fromAccountName, String toAccountName)
            throws NoSuchAccountInDatabaseException, InsufficientFundsException{
        Account fromAcc = Account.loadAccount(this.acountNameToAccountIdMap.get(fromAccountName));
        Account toAcc = Account.loadAccount(this.acountNameToAccountIdMap.get(toAccountName));

        fromAcc.withdraw(amount);
        toAcc.deposit(amount);

        //if we got this far the transfer is possible and we can commit it
        fromAcc.commit();
        toAcc.commit();

    }

    /**
     * @param accountName - String
     *                    the account whose transaction history we want to access
     *
     * @return List<Transaction> - which is a list of length 0-N (limited by the total number of transactions for
     *   the account) of the last 0-N transactions that are on file for this account.
     *
     * @throws NoSuchAccountInDatabaseException
     *               if one of the two account name do not refer to a valid account for this wallet
     */
    public List<Transaction> getLastNTransactions(String accountName, int N) throws  NoSuchAccountInDatabaseException{
        Account acc = Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));
        return acc.getPastTransactions(N);
    }


    /**
     * erases all data, for all wallets. WARNING THIS IS IRREVERSIBLE
     *
     * @param password - String
     *      must be 'delete' for method to work, just to make it a bit
     *      harder for this method to be called accidentally.
     */
    static public void deleteAllRecord(String password){
        if(password.equals("delete")){
            uWalletDatabase.flush();
        }
    }

    /**
     *
     * @return the UID for this wallet
     */
    public String getUID() {
        return this.walletUID;
    }

    /**
     *
     * @return ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country whose
     *      currency is used in this wallet.
     */
    public String getRegionCode(){
        return  this.regionCode;
    }
}