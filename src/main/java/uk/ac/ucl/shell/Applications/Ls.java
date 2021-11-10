package uk.ac.ucl.shell.Applications;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;

public class Ls implements ShellApplication {
    private String currentDirectory;
    private OutputStreamWriter writer;

    public Ls(String currentDirectory, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.writer = writer;
    }
    
    @Override
    public String exec(List<String> appArgs) throws IOException {
        File currDir;
        int rootDirLength;
        if (appArgs.isEmpty()) {
            currDir = new File(currentDirectory);
            rootDirLength = currentDirectory.length();
        } else if (appArgs.size() == 1) {
            currDir = new File(appArgs.get(0));
            rootDirLength = appArgs.get(0).length();
        } else {
            throw new RuntimeException("ls: too many arguments");
        }

        try {
            File[] listOfFiles = currDir.listFiles();
            for(int index = 0; index < listOfFiles.length; index++){
                if (!listOfFiles[index].getName().startsWith(".")) {
                    writer.write(listOfFiles[index].getAbsolutePath().substring(rootDirLength + 1));
                    if(index != listOfFiles.length - 1){
                        writer.write("\t");
                    }
                }
            }
            if (listOfFiles.length > 0) {
                writer.write(System.getProperty("line.separator"));
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("ls: no such directory");
        }

        return currentDirectory;
    }

}
