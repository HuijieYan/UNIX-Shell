package uk.ac.ucl.shell.Applications;

import java.util.List;

public class UnsafeDecorator implements ShellApplication {
    private final ShellApplication app;

    /**
     * Constructor of UnsafeDecorator
     * Exception of inner App with this decorator is caught in this class
     * An UnsafeException is thrown
     * Outer Shell will ignore error from app inside an UnsafeDecorator (continue execution).
     * @param app application object to be decorated
     */
    public UnsafeDecorator(ShellApplication app){
        this.app = app;
    }

    /**
     * exec function of UnsafeDecorator
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - unsafe exception with error message // Exception caught from inner application
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        try {
            return this.app.exec(appArgs);
        }catch (Exception e){
            throw new UnsafeException(e.getMessage());
        }
    }
}
