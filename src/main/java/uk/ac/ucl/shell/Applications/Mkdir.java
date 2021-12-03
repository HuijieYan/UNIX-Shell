package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

import java.io.File;
import java.util.List;

public class Mkdir implements ShellApplication {
    private String currentDirectory;

    public Mkdir(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public String exec(List<String> appArgs) throws RuntimeException {
        for (String dir : appArgs){
            try {
                ShellUtil.getDir(currentDirectory, dir);
            }catch (RuntimeException e){
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
