package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.OutputStreamWriter;

public class Sort implements ShellApplication {
    private String currentDirectory;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    public Sort(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if(appArgs.size() > 2){
            throw new RuntimeException("sort: wrong argument number");
        }

        String option;
        if(appArgs.size() > 0){
            option = appArgs.get(0);
        }else {
            option = "";
        }

        if (appArgs.size() == 2){
            if (!option.equals("-r")){
                throw new RuntimeException("sort: invalid option "+option);
            }
            execFromStream(option, appArgs.get(1));
        }else if(appArgs.size() == 1){
            if(option.equals("-r")){
                execFromStream(option, null);
            }else {
                execFromStream("", appArgs.get(0));
            }
        }else {
            execFromStream(option, null);
        }

        return currentDirectory;
    }

    private void execFromStream(String option, String fileName) {
        BufferedReader reader;
        if(fileName == null){
            reader = this.reader;
        }else {
            try {
                reader = Files.newBufferedReader(Tools.getPath(currentDirectory, fileName), StandardCharsets.UTF_8);
            }catch (IOException e){
                throw new RuntimeException("sort: cannot open " + fileName);
            }
        }

        if(reader == null){
            throw new RuntimeException("sort: no data from pipe or redirection and can not find file to read");
        }
        try {
            ArrayList<String> lines = readFromReader(reader);
            sort(option, lines);
            writeToBuffer(lines);
        }catch (IOException e){
            throw new RuntimeException("sort: fail to read or write");
        }
    }

    private void sort(String option, ArrayList<String> readLines){
        Collections.sort(readLines);
        if (option.equals("-r")){
            Collections.reverse(readLines);
        }
    }

    private ArrayList<String> readFromReader(BufferedReader reader) throws IOException{
        ArrayList<String> lines = new ArrayList<>();
        String line;
        while((line = reader.readLine()) != null){
            lines.add(line);
        }
        return lines;
    }

    private void writeToBuffer(ArrayList<String> lines) throws IOException{
        for(String str : lines){
            writer.write(str);
            writer.write(System.getProperty("line.separator"));
        }
        writer.flush();
    }
}