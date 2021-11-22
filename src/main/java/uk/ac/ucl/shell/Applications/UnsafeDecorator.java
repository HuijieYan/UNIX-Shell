package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;

import java.util.List;

public class UnsafeDecorator implements ShellApplication {
    private ShellApplication app;

    public UnsafeDecorator(ShellApplication app){
        this.app = app;
    }
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        try {
            return this.app.exec(appArgs);
        }catch (RuntimeException e){
            throw new RuntimeException("ignore" + e.getMessage());
        }
    }
}
