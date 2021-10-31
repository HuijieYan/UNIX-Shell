package uk.ac.ucl.shell.Applications;

import java.io.File;
import java.util.List;

public class Tools {
    public static File getFile(String currentDirectory, String fileName){
        File file = new File(currentDirectory + File.separator + fileName);
        if(file.isFile()){
            return file;
        }
        file = new File(currentDirectory);
        if(file.isFile()){
            return file;
        }
        return null;
    }

    public static List<String> getFileByStdin(){
        return null;
    }
}
