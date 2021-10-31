package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.regex.Pattern;

public class Find implements ShellApplication {
    private OutputStreamWriter writer;
    private String currentDirectory;

    public Find(OutputStreamWriter writer, String currentDirectory) {
        this.writer = writer;
        this.currentDirectory = currentDirectory;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if (appArgs.size() != 2 && appArgs.size() != 3) {
            throw new RuntimeException("find: Wrong number of arguments");
        }else if (!((appArgs.get(appArgs.size() - 2)).equals("-name"))) {
            throw new RuntimeException("find: Wrong argument " + appArgs.get(appArgs.size() - 2));
        }

        File rootDirectory;
        int rootDirLength;
        if (appArgs.size() == 2) {
            rootDirectory = new File(currentDirectory);
            rootDirLength = currentDirectory.length();
        } else {
            rootDirectory = new File(appArgs.get(0));
            rootDirLength = appArgs.get(0).length();
        }

        if(!rootDirectory.isDirectory()){
            throw new RuntimeException("find: no such root directory " + rootDirectory.getAbsolutePath());
        }

        Pattern findPattern = Pattern.compile(appArgs.get(appArgs.size() - 1).replaceAll("\\*", "*"));
        findFilesInDir(writer, rootDirectory, findPattern, rootDirLength + 1);
        return currentDirectory;
    }

    private void findFilesInDir(OutputStreamWriter writer, File currDirectory, Pattern findPattern, int rootDirLength) throws IOException {
        try {
            File[] listFiles = currDirectory.listFiles();
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    findFilesInDir(writer, file, findPattern, rootDirLength);
                } else if (findPattern.matcher(file.getName()).matches()) {
                    writer.write(file.getAbsolutePath().substring(rootDirLength));
                    writer.write(System.getProperty("line.separator"));
                    writer.flush();
                }
            }
        }catch (NullPointerException ignored){}
    }
}
