package uk.ac.ucl.shell.Applications;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

public class Ls implements ShellApplication {
    private String currentDirectory;
    private OutputStreamWriter writer;

    public Ls(String currentDirectory, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.writer = writer;
    }
    
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if(appArgs.size() > 1){
            throw new RuntimeException("ls: too many arguments");
        }

        try {
            File currDir;
            int rootDirLength;
            if (appArgs.isEmpty()) {
                currDir = new File(currentDirectory);
                rootDirLength = currentDirectory.length() + 1;
            } else {
                currDir = ShellUtil.getDir(currentDirectory, appArgs.get(0));
                rootDirLength = currDir.getCanonicalPath().length() + 1;
            }

            File[] listOfFiles = currDir.listFiles();
            if (listOfFiles.length > 0) {
                for(int index = 0; index < listOfFiles.length; index++){
                    if (!listOfFiles[index].getName().startsWith(".")) {
                        writer.write(listOfFiles[index].getCanonicalPath().substring(rootDirLength));
                        if(index != listOfFiles.length - 1){
                            writer.write("\t");
                        }
                    }
                }
                writer.write(System.getProperty("line.separator"));
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("ls: no such directory: " + appArgs.get(0));
        }

        return currentDirectory;
    }

}