package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Tail implements ShellApplication{
    private String currentDirectory;
    private BufferedReader reader;
    private OutputStreamWriter writer;
    private int tailLines;

    public Tail(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        tailLines = 10;
        String fileName = null;
        int argSize = appArgs.size();
        if (argSize > 3) {
            throw new RuntimeException("tail: wrong argument number");
        }else if (argSize == 3 || argSize == 2) {
            if(!appArgs.get(0).equals("-n")){
                throw new RuntimeException("tail: wrong argument " + appArgs.get(0) + " should be -n");
            }

            try {
                tailLines = Integer.parseInt(appArgs.get(1));
            } catch (NumberFormatException e) {
                throw new RuntimeException("tail: wrong argument " + appArgs.get(1));
            }

            if(argSize == 3){
                fileName = appArgs.get(2);
            }
        }else if(argSize == 1){
            fileName = appArgs.get(0);
        }

        if(fileName == null){
            if(this.reader == null){
                throw new RuntimeException("tail: no data from pipe or redirection and can not find file to read");
            }
            try {
                writeToBuffer(this.reader);
            }catch (IOException e){
                throw new RuntimeException("tail: fail to read from pipe or redirection");
            }
        } else {
            try {
                writeToBuffer(Files.newBufferedReader(ShellUtil.getPath(currentDirectory, fileName), StandardCharsets.UTF_8));
            }catch (IOException e){
                throw new RuntimeException("tail: cannot open " + fileName);
            }
        }

        return currentDirectory;
    }

    private void writeToBuffer(BufferedReader reader) throws IOException {
        ArrayList<String> storage = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            storage.add(line);
        }

        int index = 0;
        if (tailLines < storage.size()) {
            index = storage.size() - tailLines;
        }
        for (int i = index; i < storage.size(); i++) {
            writer.write(storage.get(i) + System.getProperty("line.separator"));
        }
        writer.flush();
    }

}
