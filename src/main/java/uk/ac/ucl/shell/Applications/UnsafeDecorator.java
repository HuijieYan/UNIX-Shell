package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;

import java.util.List;

public class UnsafeDecorator implements ShellApplication {
    private final ShellApplication app;

    /**
     * Constructor of UnsafeDecorator
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
     * - "ignore" + e.getMessage() // Exception caught from inner application
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        try {
            return this.app.exec(appArgs);
        }catch (Exception e){
            throw new RuntimeException("ignore" + e.getMessage());
        }
    }
}
