package uk.ac.ucl.shell.Applications;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

public class Head implements ShellApplication {
    private String currentDirectory;
    private BufferedReader reader;
    private OutputStreamWriter writer;
    private int headLines;

    public Head(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        headLines = 10;
        String fileName = null;
        int argSize = appArgs.size();
        if (argSize > 3) {
            throw new RuntimeException("head: wrong argument number");
        }else if (argSize == 3 || argSize == 2) {
            if(!appArgs.get(0).equals("-n")){
                throw new RuntimeException("head: wrong argument " + appArgs.get(0) + " should be -n");
            }

            try {
                headLines = Integer.parseInt(appArgs.get(1));
            } catch (NumberFormatException e) {
                throw new RuntimeException("head: " + appArgs.get(1) + " is not a integer");
            }

            if(argSize == 3){
                fileName = appArgs.get(2);
            }
        }else if(argSize == 1){
            fileName = appArgs.get(0);
        }

        if(fileName == null){
            if(this.reader == null){
                throw new RuntimeException("head: no data from pipe or redirection and can not find file to read");
            }
            try {
                writeToBuffer(this.reader);
            }catch (IOException e){
                throw new RuntimeException("head: fail to read from pipe or redirection");
            }
        } else {
            try {
                writeToBuffer(Files.newBufferedReader(ShellUtil.getPath(currentDirectory, fileName), StandardCharsets.UTF_8));
            }catch (IOException e){
                throw new RuntimeException("head: cannot open: " + fileName);
            }
        }
        
        return currentDirectory;
    }

    private void writeToBuffer(BufferedReader reader) throws IOException {
        for (int i = 0; i < headLines; i++) {
            String line;
            if ((line = reader.readLine()) != null) {
                writer.write(line + System.getProperty("line.separator"));
            }else {
                break;
            }
        }
        writer.flush();
    }

}
