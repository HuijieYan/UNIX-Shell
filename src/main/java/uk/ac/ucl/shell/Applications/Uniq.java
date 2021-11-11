package uk.ac.ucl.shell.Applications;
import uk.ac.ucl.shell.ShellApplication;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Uniq implements ShellApplication{
    private String currentDirectory;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    public Uniq(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if(appArgs.isEmpty()){
            throw new RuntimeException("Uniq: missing arguments");
        }else if (appArgs.size() > 2){
            throw new RuntimeException("Uniq: too many arguments");
        }

        String fileName;
        String option = "";
        if (appArgs.size() == 2){
            option = appArgs.get(0);
            if (!option.equals("-i")){
                throw new RuntimeException("Uniq: invalid option "+option);
            }
            fileName = appArgs.get(1);
        }else{
            fileName = appArgs.get(0);
        }

        try (BufferedReader reader = Files.newBufferedReader(Tools.getPath(currentDirectory, fileName), StandardCharsets.UTF_8)) {
            ArrayList<String> readLines = new ArrayList<>();
            //lines that already been read
            String line = reader.readLine();
            while(line != null){
                if (!compare(option,line,readLines)){
                    writer.write(line);
                    writer.write(System.getProperty("line.separator"));
                    writer.flush();
                    readLines.add(line);
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Uniq: cannot open " + fileName);
        }

        return currentDirectory;
    }

    private boolean compare(String option, String line,ArrayList<String> readLine){
        if (option.equals("")){
            return readLine.contains(line);
        }else{
            for (String str:readLine){
                if (line.compareToIgnoreCase(str)==0){
                    return true;
                }
            }
            return false;
        }
    }
}