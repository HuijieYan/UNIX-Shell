package uk.ac.ucl.shell.Applications;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class Tools {
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

    public static ArrayList<String> globbingHelper(String glob, String currentDirectory) throws IOException {
        ArrayList<String> globbingResult = new ArrayList<>();

        Path dir;
        DirectoryStream<Path> stream;
        if(glob.contains(System.getProperty("file.separator")) || !glob.startsWith("*")){
            dir = Paths.get(glob.substring(0, glob.indexOf("*") - 1));
            stream = Files.newDirectoryStream(dir, glob.substring(glob.indexOf("*")));
        }else {
            dir = Paths.get(currentDirectory);
            stream = Files.newDirectoryStream(dir, glob);
        }

        for (Path entry : stream) {
            globbingResult.add(entry.getFileName().toString());
        }

        if(globbingResult.size() == 0){
            throw new IOException();
        }
        return globbingResult;
    }
}
