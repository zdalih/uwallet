package uwallet.exceptions;

public class NoSuchAccountInDatabaseException extends Exception{

    public NoSuchAccountInDatabaseException(String message){
        super(message);
    }
}
