package uk.ac.ucl.shell.Applications;

import java.io.File;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

public class Cd implements ShellApplication {
    private String currentDirectory;

    /**
     * Constructor of Cd application
     * @param currentDirectory currentDirectory of the Shell
     */
    public Cd(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    /**
     * exec function of "cd" application.
     * The function takes list of files from appArg & read into a bufferReader.
     * The content from bufferReader is then write into writer
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - "cd: missing argument" // When appArgs is empty
     * - "cd: too many arguments" // When appArgs has more than one arguments
     * - "cd: fail to change the directory" // When directory is not valid or can not be opened
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if (appArgs.isEmpty()) {
            throw new RuntimeException("cd: missing argument");
        } else if (appArgs.size() > 1) {
            throw new RuntimeException("cd: too many arguments");
        }

        try {
            File dir = ShellUtil.getDir(currentDirectory, appArgs.get(0));
            currentDirectory = dir.getCanonicalPath();
        }catch (Exception e){
            throw new RuntimeException("cd: can not switch to such directory " + appArgs.get(0));
        }
        return currentDirectory;
    }
}
