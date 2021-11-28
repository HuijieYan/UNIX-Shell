package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;

import java.util.List;

public class UnsafeDecorator implements ShellApplication {
    private ShellApplication app;

    /**
     * Constructor of UnsafeDecorator
     * @param app application object to be decoratored
     */
    public UnsafeDecorator(ShellApplication app){
        this.app = app;
    }

    /**
     * exec function of UnsafeDecorator
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirecory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is throwed due to following reasons:
     * - "ignore" + e.getMessage() // Exception catched from inner applicatioin
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
