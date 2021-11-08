package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.OutputStreamWriter;

public class Sort implements ShellApplication {
    private OutputStreamWriter writer;
    private String currentDirectory;

    public Sort(OutputStreamWriter outputStreamWriter,String currentDirectory){
        this.currentDirectory = currentDirectory;
        this.writer = outputStreamWriter;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if(appArgs.isEmpty()){
            throw new RuntimeException("sort: missing arguments");
        }else if (appArgs.size() > 2){
            throw new RuntimeException("sort: too many arguments");
        }

        String fileName;
        String option = "";
        if (appArgs.size() == 2){
            option = appArgs.get(0);
            if (!option.equals("-r")){
                throw new RuntimeException("exec: invalid option "+option);
            }
            fileName = appArgs.get(1);
        }else{
            fileName = appArgs.get(0);
        }

        File file = Tools.getFile(currentDirectory, fileName);
        if(file == null){
            throw new RuntimeException("exec: " + fileName + " does not exist");
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            ArrayList<String> readLines = new ArrayList<>();
            //lines that already been read
            String line = reader.readLine();
            while(line != null){
                readLines.add(line);
                line = reader.readLine();
            }
            readLines = sort(option,readLines);
            for(String str:readLines){
                writer.write(str);
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("exec: cannot open " + fileName);
        }

        return currentDirectory;
    }

    private ArrayList<String> sort(String option,ArrayList<String> readLines){
        Collections.sort(readLines);
        if (option.equals("-r")){
            Collections.reverse(readLines);
        }
        return readLines;
    }
}
