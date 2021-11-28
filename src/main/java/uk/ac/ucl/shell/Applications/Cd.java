package uk.ac.ucl.shell.Applications;

import java.io.File;
import java.io.IOException;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

public class Cd implements ShellApplication {
    private String currentDirectory;

    public Cd(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

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
