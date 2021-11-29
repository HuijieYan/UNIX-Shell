package uk.ac.ucl.shell;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ShellUtil {
    
    /**
     * getPath
     * @param currentDirectory directory of the target Pah
     * @param fileName target fileName
     * @return Target path object
     * @throws IOException IO error from OS
     */
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

    /**
     * getDir
     * @param currentDirectory directory of Shell
     * @param dir target directory
     * @return File object of target directory
     * @throws RuntimeException "no such directory: " + dir // Invalid directory or IO error
     */
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
