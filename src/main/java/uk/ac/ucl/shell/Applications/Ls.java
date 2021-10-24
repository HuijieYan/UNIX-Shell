package uk.ac.ucl.shell.Applications;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;

public class Ls implements ShellApplication {

    private OutputStreamWriter writer;
    private String currentDirectory;

    public Ls(OutputStreamWriter writer, String currentDirectory) {
        this.writer = writer;
        this.currentDirectory = currentDirectory;
    }
    
    @Override
    public String exec(List<String> appArgs) throws IOException {
        File currDir;
        if (appArgs.isEmpty()) {
            currDir = new File(currentDirectory);
        } else if (appArgs.size() == 1) {
            currDir = new File(appArgs.get(0));
        } else {
            throw new RuntimeException("ls: too many arguments");
        }
        try {
            File[] listOfFiles = currDir.listFiles();
            boolean atLeastOnePrinted = false;
            for (File file : listOfFiles) {
                if (!file.getName().startsWith(".")) {
                    writer.write(file.getName());
                    writer.write("\t");
                    writer.flush();
                    atLeastOnePrinted = true;
                }
            }
            if (atLeastOnePrinted) {
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            }
        } catch (NullPointerException e) {
            throw new RuntimeException("ls: no such directory");
        }

        return currentDirectory;
    }

}
