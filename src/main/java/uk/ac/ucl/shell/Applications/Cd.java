package uk.ac.ucl.shell.Applications;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;

public class Cd implements ShellApplication {
    private String currentDirectory;

    public Cd(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if (appArgs.isEmpty()) {
            throw new RuntimeException("cd: missing argument");
        } else if (appArgs.size() > 1) {
            throw new RuntimeException("cd: too many arguments");
        }

        String dirString = appArgs.get(0);
        File dir;
        if (!(dir = new File(currentDirectory, dirString)).isDirectory() && !(dir = new File(dirString)).isDirectory()) {
            throw new RuntimeException("cd: " + dirString + " is not an existing directory");
        }

        try {
            currentDirectory = dir.getCanonicalPath();
        }catch (IOException e){
            throw new RuntimeException("cd: fail to change the directory");
        }
        return currentDirectory;
    }
}
