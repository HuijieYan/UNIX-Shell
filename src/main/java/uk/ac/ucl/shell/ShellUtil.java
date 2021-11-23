package uk.ac.ucl.shell;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ShellUtil {
    public static Path getPath(String currentDirectory, String fileName) throws IOException {
        File file = new File(currentDirectory + File.separator + fileName);
        if(file.isFile()){
            return file.toPath();
        }
        file = new File(fileName);
        if(file.isFile() && file.isAbsolute()){
            return file.toPath();
        }
        throw new IOException();
    }

    public static File getDir(String currentDirectory, String dir) throws RuntimeException {
        File file = new File(currentDirectory + File.separator + dir);
        if(file.isDirectory()){
            return file;
        }
        file = new File(dir);
        if(file.isDirectory() && file.isAbsolute()){
            return file;
        }
        throw new RuntimeException("no such directory: " + dir);
    }
}
