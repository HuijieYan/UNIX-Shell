package uk.ac.ucl.shell.Applications;


public class UnsafeException extends RuntimeException{

    /**
     * This is a custom exception extended from RuntimeException
     * The exception is used for identifying exceptions from an unsafe version of app
     * @param message error message
     */
    public UnsafeException(String message){
        super(message);
    }
}
