package uk.ac.ucl.shell.Applications;
import uk.ac.ucl.shell.ShellApplication;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
    public String exec(List<String> appArgs) throws RuntimeException {
        if(appArgs.size() > 2){
            throw new RuntimeException("uniq: too many argument number");
        }

        String option;
        if(appArgs.size() > 0){
            option = appArgs.get(0);
        }else {
            option = "";
        }

        if (appArgs.size() == 2){
            if (!option.equals("-i")){
                throw new RuntimeException("uniq: invalid option " + option);
            }
            execFromStream(option, appArgs.get(1));
        }else if(appArgs.size() == 1){
            if(option.equals("-i")){
                execFromStream(option, null);
            }else {
                execFromStream("", appArgs.get(0));
            }
        } else {
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
                throw new RuntimeException("uniq: cannot open " + fileName);
            }
        }

        if(reader == null){
            throw new RuntimeException("uniq: no data from pipe or redirection and can not find file to read");
        }
        try {
            this.writeToBuffer(option, reader);
        } catch (IOException e) {
            throw new RuntimeException("uniq: fail to read or write");
        }
    }

    private void writeToBuffer(String option, BufferedReader reader) throws IOException {
        String lastLine = null;
        String currentLine;
        while((currentLine = reader.readLine()) != null){
            if (notEqual(option,currentLine,lastLine)){
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            lastLine = currentLine;
        }
        writer.flush();
    }

    private boolean notEqual(String option, String currentLine, String lastLine){
        if(lastLine == null){
            return true;
        }
        if (option.equals("")){
            return !currentLine.equals(lastLine);
        }else{
            return !currentLine.equalsIgnoreCase(lastLine);
        }
    }
}