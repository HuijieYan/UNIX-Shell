package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Rmdir implements ShellApplication {
    private String currentDirectory;

    /**
     * Constructor of Rmdir app
     * The app receives list of names of empty directories & remove them under current shell path
     * @param currentDirectory currentDirectory of the Shell
     */
    public Rmdir(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }


    /**
     * Execution function of Rmdir app
     * @param appArgs // list of names of empty directories to be deleted
     * @return currentDirectory // This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException When directory to be delted is not empty
     */
    public String exec(List<String> appArgs) throws RuntimeException {
        for (String dir : appArgs){
            File targetDir;
            try {
                targetDir = ShellUtil.getDir(currentDirectory, dir);
            }catch (IOException e){
                throw new RuntimeException("rmdir: no such directory " + dir);
            }

            File[] listFiles = targetDir.listFiles();
            if(listFiles.length > 0){
                throw new RuntimeException("rmdir: can not delete nonempty directory " + dir);
            }
            targetDir.delete();
        }
        return currentDirectory;
    }
}
