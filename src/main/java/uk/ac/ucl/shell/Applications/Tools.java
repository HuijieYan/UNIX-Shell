package uk.ac.ucl.shell.Applications;

import java.io.File;


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

    /*
    public static ArrayList<String> stdinNextLine(int number){
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        ArrayList<String> lines = new ArrayList<>();
        if(number > 0){
            while(number > 0){
                try {
                    String content = input.readLine();
                    if(content == null){
                        lines.add(null);
                        input.close();
                        break;
                    }
                    lines.add(content);
                }catch (Exception ignored){}
                number--;
            }
        }else {
            while (true){
                try {
                    String content = input.readLine();
                    if(content == null){
                        lines.add(null);
                        input.close();
                        break;
                    }
                    lines.add(content);
                }catch (Exception ignored){}
            }
        }

        return lines;
    }
     */
}
