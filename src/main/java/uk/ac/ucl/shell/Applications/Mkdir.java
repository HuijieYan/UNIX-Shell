package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Mkdir implements ShellApplication {
    private String currentDirectory;

    /**
     * Constructor of Mkdir app
     * The app receives list of directory names & creates them under current shell path
     * @param currentDirectory currentDirectory of the Shell
     */
    public Mkdir(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    /**
     * Execution function of Mkdir app
     * @param appArgs // list of directory names to be created
     * @return currentDirectory // This is not used in this function (variable exists here because of the requirement from interface)
     */
    public String exec(List<String> appArgs) throws RuntimeException {
        for (String dir : appArgs){
            try {
                ShellUtil.getDir(currentDirectory, dir);
            }catch (IOException e){
                File newDir = new File(dir);
                if(!newDir.isAbsolute()){
                    newDir = new File(currentDirectory, dir);
                }
                newDir.mkdir();
                continue;
            }
            throw new RuntimeException("mkdir: " + dir + " is already exist");
        }
        return currentDirectory;
    }
}
