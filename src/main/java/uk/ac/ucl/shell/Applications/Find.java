package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.Shell;
import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

import java.io.*;
import java.util.List;
import java.util.regex.Pattern;

public class Find implements ShellApplication {
    private String currentDirectory;
    private OutputStreamWriter writer;
    private int rootDirLength = -1;
    private boolean isChildDir = false;

    public Find(String currentDirectory, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if (appArgs.size() != 2 && appArgs.size() != 3) {
            throw new RuntimeException("find: Wrong number of arguments");
        }
        if (!appArgs.get(appArgs.size() - 2).equals("-name")) {
            throw new RuntimeException("find: can not find -name argument or lack of pattern");
        }

        File rootDirectory;
        if (appArgs.size() == 2) {
            rootDirectory = new File(currentDirectory);
            this.rootDirLength = currentDirectory.length();
        } else {
            rootDirectory = new File(currentDirectory, appArgs.get(0));
            if(rootDirectory.isDirectory()){
                this.rootDirLength = currentDirectory.length() + 1;
                this.isChildDir = true;
            } else {
                try {
                    rootDirectory = ShellUtil.getDir(currentDirectory, appArgs.get(0));
                }catch (RuntimeException e){
                    throw new RuntimeException("find: no such root directory " + appArgs.get(0));
                }
            }
        }

        Pattern findPattern = Pattern.compile(appArgs.get(appArgs.size() - 1).replaceAll("\\*", ".*"));
        try {
            findFilesInDir(rootDirectory, findPattern);
        }catch (Exception e){
            throw new RuntimeException("find: fail to write to the output");
        }
        return currentDirectory;
    }

    private void findFilesInDir(File currDirectory, Pattern findPattern) throws IOException {
        File[] listFiles = currDirectory.listFiles();
        for (File file : listFiles) {
            if (file.isDirectory()) {
                findFilesInDir(file, findPattern);
            } else if (findPattern.matcher(file.getName()).matches()) {
                if(this.rootDirLength == -1){
                    writer.write(file.getAbsolutePath());
                }else if(isChildDir){
                    writer.write(file.getAbsolutePath().substring(this.rootDirLength));
                } else {
                    writer.write("." + file.getAbsolutePath().substring(this.rootDirLength));
                }
                writer.write(System.getProperty("line.separator"));
            }
        }
        writer.flush();
    }
}
