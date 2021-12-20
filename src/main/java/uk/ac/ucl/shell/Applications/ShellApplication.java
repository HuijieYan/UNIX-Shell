package uk.ac.ucl.shell.Applications;
import java.util.List;

public interface ShellApplication {

    /**
     * 
     * @param appArgs arguments to be passed into the application
     * @return currentDirecory of shell. (Exist here because Shell need to keep track of path)
     * eg. Cd changed the shell directory and the new path should be returned to the main Shell Program & main shell could update the path globally.
     * @throws RuntimeException
     */
    String exec(List<String> appArgs) throws RuntimeException;
}
