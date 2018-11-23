package uwallet;

import uwallet.exceptions.InsufficientFundsException;
import uwallet.exceptions.NoSuchObjectInDatabaseException;
import uwallet.exceptions.UniqueIDConstraintException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

/**
 *    Wallet object holds multiple accounts of the same currency. It tracks these
 *    accounts in a hashMap that maps the name of an account under this wallet to it's
 *    unique identifier (which is created by the wallet) to load account objects.
 *    methods interact with the accounts in the wallet through the account name that
 *    was given to the account - of course this means no two account can have identical names.
 *
 * RI: No two wallet can EVER be created with the same UID unless first ensures both the
 *     persistent data stores are clear of references to the UID. A single wallet can
 *     only be used for a single geographic region. No two accounts belonging to this wallet
 *     can have identical names.
 */
public class Wallet{


    private HashMap<String, String> acountNameToAccountIdMap =  new HashMap<String, String>();
    private String regionCode;
    private String walletUID;

    /**
     *
     * Creates a new wallet with the given UID and adds reference to it in the persistent storage.
     *
     * @param walletUID
     *      - globally unique string to identify this wallet. Can not be null or empty.
     * @param regionCode
     *         ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country whose
     *         currency is desired
     * @throws UniqueIDConstraintException
     *          if another wallet object with the same uniqueIdentifier already exists either in DB.
     */
    public Wallet(String walletUID, String regionCode) throws UniqueIDConstraintException{
        try{
            this.loadWallet(walletUID);
            throw new UniqueIDConstraintException("Unique Identifier: " + walletUID + " is already allocated to a wallet!");
        } catch (NoSuchObjectInDatabaseException e){
            this.regionCode = regionCode;
            this.walletUID = walletUID;

            uWalletDatabase db = new uWalletDatabase();
            db.insertWallet(this);
        }
    }

    /**
     * Loads the wallet object for a wallet stored in the records.
     *
     * @param walletUID
     *          globally unique string to identify this wallet. Can not be null or empty.
     *
     * @return the wallet object for this walletUID.
     * @throws NoSuchObjectInDatabaseException
     *          if no wallet with the given walletUID exists in the records.
     */
    static public Wallet loadWallet(String walletUID) throws NoSuchObjectInDatabaseException {
        uWalletDatabase db = new uWalletDatabase();
        Wallet wallet = db.getWallet(walletUID);
        return wallet;
    }

    /**
     *
     * Constructor for the Wallet.
     *
     * @param walletUID
     *          globally unique string to identify this wallet. Can not be null or empty.
     * @param regionCode
     *          ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country whose
     *          currency is desired
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
     * @param accountName
     *          representing the name to be given to this account. Empty or Null strings are not allowed
     *
     * @throws UniqueIDConstraintException
     *          if this wallet already has an account with the desired name.
     */
    public void createNewAccount(String accountName) throws UniqueIDConstraintException {
        String accountUID = this.walletUID + "ACC" + String.valueOf(this.acountNameToAccountIdMap.size() + 1);
        try {
            Account acc = new Account(accountName, accountUID, this.walletUID, this.regionCode);
            this.acountNameToAccountIdMap.put(accountName, accountUID);
        } catch ( UniqueIDConstraintException e ){
            throw new UniqueIDConstraintException("Wallet already has account with name '" + accountName + "'");
        }
    }

    /**
     * Returns the formatted account balance for account with the given name. The format is according to the given
     * region code's customs.
     *
     * @param accountName
     *        the name of the account whose balance we want. Should not be Null or Empty.
     *
     * @throws NoSuchObjectInDatabaseException
     *        if no such account in this wallet have the given name
     *
     * @return the formatted account balance using the region code for this wallet.
     */
    public String getAccountBalanceFormatted(String accountName) throws NoSuchObjectInDatabaseException {
        try{
            Account acc = Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));
            return acc.getFormattedBalance();
        }catch (NoSuchObjectInDatabaseException e){
            throw  new NoSuchObjectInDatabaseException("No account with name '" + accountName + "' associated to this wallet.");
        }
    }

    /**
     * Returns the account balance for the account with the given name as a BigDecimal.
     *
     * @param accountName
     *        the name of the account whose balance we want. Should not be null or empty.
     *
     * @throws NoSuchObjectInDatabaseException
     *         if no such account in this wallet have the given name
     *
     * @return the account balance as a BigDecimal.
     */
    public BigDecimal getAccountBalanceBigDecimal(String accountName) throws NoSuchObjectInDatabaseException {
        try{
            Account acc = Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));
            return acc.getCurrentBalance();
        }catch (NoSuchObjectInDatabaseException e){
            throw  new NoSuchObjectInDatabaseException("No account with name '" + accountName + "' associated to this wallet.");
        }
    }

    /**
     * Deposits an amount of money to the account with the given name.
     *
     * @param amount
     *        the amount to be deposited to the account. Should not be null or empty.
     *
     * @param accountName
     *        the name of the account to deposit money to
     *
     * @param description (optional)
     *         description[0] is a String of at most 50char that is not null. All other items in description
     *         are ignored. The default description is N/A. Should not be an empty string.
     *
     * @throws NoSuchObjectInDatabaseException
     *          if the accountName does not represent an account that is held by this wallet.
     */
    public void depositToAccount(double amount, String accountName, String... description) throws NoSuchObjectInDatabaseException {
        Account acc = Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));
        acc.deposit(amount, description);
    }

    /**
     * Withdraws an amount of money from the account with the given name.
     *
     * @param amount
     *        the amount to be withdrawn from the account. Should not be null or empty.
     *
     * @param accountName
     *        the name of the account to deposit money to
     *
     * @param description (optional)
     *        description[0] is a String of at most 50char that is not null. All other items in description
     *        are ignored. The default description is N/A. Should not be an empty string.
     *
     * @throws NoSuchObjectInDatabaseException
     *         if the accountName does not represent an account that is held by this wallet.
     * @throws InsufficientFundsException
     *         if the account does not have sufficient funds for the withdrawal.
     */
    public void withdrawFromAccount(double amount, String accountName, String... description)
            throws NoSuchObjectInDatabaseException, InsufficientFundsException{
        Account acc = Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));

        acc.withdraw(amount, description);
    }

    /**
     * transfers the given amount from one account to another
     *
     * @param amount
     *        the amount to be transferred between the two accounts. Should not be null or empty.
     *
     * @param fromAccountName
     *        the name of the account to withdraw money from
     *
     * @param toAccountName
     *         the name of the account to deposit the money to
     *
     * @param description (optional).
     *        description[0] is a String of at most 50char that is not null. All other items in description
     *        are ignored. The default description is N/A. Should not be an empty string.
     *
     * @throws NoSuchObjectInDatabaseException
     *         if one of the two account name do not refer to a valid account for this wallet
     * @throws InsufficientFundsException
     *         if the fromAccount does not have sufficient funds for the withdrawal.
     */
    public void transfer(double amount, String fromAccountName, String toAccountName,  String... description)
            throws NoSuchObjectInDatabaseException, InsufficientFundsException{
        Account fromAcc = Account.loadAccount(this.acountNameToAccountIdMap.get(fromAccountName));
        Account toAcc = Account.loadAccount(this.acountNameToAccountIdMap.get(toAccountName));

        //if we can withdraw the money, depositing it is not an issue

        fromAcc.withdraw(amount, description);
        toAcc.deposit(amount, description);



    }

    /**
     *
     * Returns the past N transactions on record for the account with the given name.
     *
     * @param accountName - String
     *        the account whose transaction history we want to access. Should not be null or empty.
     *
     * @param N - the number of records to return.
     *
     * @return a list of length 0-N (limited by the total number of transactions for
     *        the account) of the last 0-N transactions that are on file for this account. The list is made
     *        of Transaction objects
     *
     * @throws NoSuchObjectInDatabaseException
     *         if one of the two account name do not refer to a valid account for this wallet
     */
    public List<Transaction> getLastNTransactions(String accountName, int N) throws NoSuchObjectInDatabaseException {
        Account acc = Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));
        return acc.getPastTransactions(N);
    }


    /**
     *  Returns a protected Account object - meant for locking it.
     *
     * @param accountName - String
     *        the account whose transaction history we want to access. Should not be null or empty.
     * @return the Account object for the given account name.
     * @throws NoSuchObjectInDatabaseException
     *         if one of the two account name do not refer to a valid account for this wallet
     */
    public Account getAccount(String accountName) throws NoSuchObjectInDatabaseException {
        return Account.loadAccount(this.acountNameToAccountIdMap.get(accountName));
    }


    /**
     * Erases all data, for all wallets. WARNING THIS IS IRREVERSIBLE
     *
     * @param password
     *      must be 'delete' for method to work, just to make it a bit
     *      harder for this method to be called accidentally.
     */
    static public void deleteAllRecord(String password){
        if(password.equals("delete")){
            uWalletDatabase.flush();
        }
    }

    /**
     * Get the wallet unique identifier.
     *
     * @return the UID for this wallet
     */
    public String getUID() {
        return this.walletUID;
    }

    /**
     *  Get the code for this wallet's currency region.
     *
     * @return ISO 3166 alpha-2 country code or UN M.49 numeric-3 area code for the country whose
     *      currency is used in this wallet.
     */
    public String getRegionCode(){
        return  this.regionCode;
    }
}