package uk.ac.ucl.shell.Applications;

import java.io.File;
import java.io.OutputStreamWriter;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

public class Ls implements ShellApplication {
    private final String currentDirectory;
    private final OutputStreamWriter writer;

    /**
     * Constructor of Ls application
     * @param currentDirectory currentDirectory of the Shell
     * @param writer Destination of writing content
     */
    public Ls(String currentDirectory, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.writer = writer;
    }
    
    /**
     * exec function of "Ls" application.
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - "ls: too many arguments" // When app argument has size more than 1.
     * - "ls: no such directory: " + appArgs.get(0) // Failed to open directory given from argument
     */
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