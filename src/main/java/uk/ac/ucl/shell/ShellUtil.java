package uk.ac.ucl.shell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ShellUtil {
    public static Path getPath(String currentDirectory, String fileName) throws IOException {
        File file = new File(currentDirectory,fileName);
        if(file.isFile()){
            return file.toPath();
        }
        file = new File(fileName);
        if(file.isAbsolute() && file.isFile()){
            return file.toPath();
        }
        throw new IOException();
    }

    public static File getDir(String currentDirectory, String dir) throws RuntimeException {
        File file = new File(currentDirectory,dir);
        if(file.isDirectory()){
            return file;
        }
        file = new File(dir);
        if(file.isAbsolute() && file.isDirectory()){
            return file;
        }
        throw new RuntimeException("no such directory: " + dir);
    }
}
