package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Tail implements ShellApplication {

    private OutputStreamWriter writer;
    private String currentDirectory;

    public Tail(OutputStreamWriter writer, String currentDirectory) {
        this.writer = writer;
        this.currentDirectory = currentDirectory;
    } 

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if (appArgs.isEmpty()) {
            throw new RuntimeException("tail: missing arguments");
        }else if (appArgs.size() != 1 && appArgs.size() != 3) {
            throw new RuntimeException("tail: wrong argument number");
        }else if (appArgs.size() == 3 && !appArgs.get(0).equals("-n")) {
            throw new RuntimeException("tail: wrong argument " + appArgs.get(0));
        }


        int tailLines = 10;
        String fileName;
        if (appArgs.size() == 3) {
            try {
                tailLines = Integer.parseInt(appArgs.get(1));
            } catch (Exception e) {
                throw new RuntimeException("tail: wrong argument " + appArgs.get(1));
            }
            fileName = appArgs.get(2);
        } else {
            fileName = appArgs.get(0);
        }

        File file = Tools.getFile(currentDirectory, fileName);
        if(file == null){
            throw new RuntimeException("tail: " + fileName + " does not exist");
        }

        ArrayList<String> storage = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                storage.add(line);
            }

            int index = 0;
            if (tailLines < storage.size()) {
                index = storage.size() - tailLines;
            }
            for (int i = index; i < storage.size(); i++) {
                writer.write(storage.get(i) + System.getProperty("line.separator"));
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("tail: cannot open " + fileName);
        }

        return currentDirectory;
    }
}
