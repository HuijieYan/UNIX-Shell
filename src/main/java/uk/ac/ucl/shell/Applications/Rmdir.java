package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

import java.io.File;
import java.util.List;

public class Rmdir implements ShellApplication {
    private String currentDirectory;

    public Rmdir(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public String exec(List<String> appArgs) throws RuntimeException {
        for (String dir : appArgs){
            File targetDir = ShellUtil.getDir(currentDirectory, dir);
            File[] listFiles = targetDir.listFiles();
            if(listFiles.length > 0){
                throw new RuntimeException("rmdir: can not delete nonempty directory " + dir);
            }
            targetDir.delete();
        }
        return currentDirectory;
    }
}
