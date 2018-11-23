package uwallet.exceptions;

public class NoSuchObjectInDatabaseException extends Exception{

    public NoSuchObjectInDatabaseException(String message){
        super(message);
    }
}
