public class IdenticalAccountExistsException extends Exception{

    /**
     * Create a new exception with a custom message - for the case when someone
     * tries to have two accounts with the same name in a single wallet.
     *
     * @param accountName -  the name of the account that already exits
     *
     */
    public IdenticalAccountExistsException(String accountName){

    }
}