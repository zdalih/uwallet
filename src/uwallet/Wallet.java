//
//
//public class Wallet{
//    //RI:There can not have two accounts with the same accountName.
//    //
//    //AF:The accounts list contains the accounts that belong to this wallet object. the owner is the User object that
//    //retpresent who owns this wallet. the owner cannot be changed - it is immutable.
//
//    private List<Account> accounts =  new ArrayList<Account>();
//
//    /**
//     * Adds a new account to this wallet.
//     *
//     * @param accountName -  a String representing the name to be given to this account. Empty strings are not allowed
//     *
//     * @throws IdenticalAccountExistsException
//     *          if there is an account belonging to this wallet with the same accountName. accountName is case sensitive
//     *          so you can an account named 'Chequing' and one named 'chequing'.
//     *
//     * @return the Account object that was added to this wallet.
//     * @modifies this by adding an account to the list of accounts linked to the wallet.
//     */
//    public Account createNewAccount(String accountName){
//        return;
//    }
//
//    /**
//     *
//     */
//    public List<Account> getAllAcconts(){
//        return;
//    }
//}