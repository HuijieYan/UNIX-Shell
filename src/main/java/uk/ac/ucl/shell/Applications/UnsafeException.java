package uk.ac.ucl.shell.Applications;

public class UnsafeException extends RuntimeException{
    private final Exception exception;

    public UnsafeException(Exception e){
        this.exception = e;
    }

    public String getMessage(){
        return this.exception.getMessage();
    }
}
