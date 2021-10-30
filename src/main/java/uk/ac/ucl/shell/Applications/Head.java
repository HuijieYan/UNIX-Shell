package uk.ac.ucl.shell.Applications;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;

public class Head implements ShellApplication {

    private OutputStreamWriter writer;
    private String currentDirectory;

    public Head(OutputStreamWriter writer, String currentDirectory) {
        this.writer = writer;
        this.currentDirectory = currentDirectory;
    } 

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if (appArgs.isEmpty()) {
            throw new RuntimeException("head: missing arguments");
        }else if (appArgs.size() != 1 && appArgs.size() != 3) {
            throw new RuntimeException("head: wrong argument number");
        }else if (appArgs.size() == 3 && !appArgs.get(0).equals("-n")) {
            throw new RuntimeException("head: wrong argument " + appArgs.get(0));
        }


        int headLines = 10;
        String fileName;
        if (appArgs.size() == 3) {
            try {
                headLines = Integer.parseInt(appArgs.get(1));
            } catch (NumberFormatException e) {
                throw new RuntimeException("head: wrong argument " + appArgs.get(1));
            }
            fileName = appArgs.get(2);
        } else {
            fileName = appArgs.get(0);
        }

        File file = Tools.getFile(currentDirectory, fileName);
        if(file == null){
            throw new RuntimeException("head: " + fileName + " does not exist");
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            for (int i = 0; i < headLines; i++) {
                String line = null;
                if ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.write(System.getProperty("line.separator"));
                    writer.flush();
                }else {
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("head: cannot open " + fileName);
        }
        
        return currentDirectory;        
    }

}
