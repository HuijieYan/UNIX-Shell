package uk.ac.ucl.shell.Applications;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;

public class Cat implements ShellApplication {

    private OutputStreamWriter writer;
    private String currentDirectory;

    public Cat(OutputStreamWriter writer, String currentDirectory) {
        this.writer = writer;
        this.currentDirectory = currentDirectory;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if (appArgs.isEmpty()) {
            throw new RuntimeException("cat: missing arguments");
        }

        Charset encoding = StandardCharsets.UTF_8;
        for (String arg : appArgs){
            File file = Tools.getFile(currentDirectory, arg);
            if(file == null){
                throw new RuntimeException("cat: file" + arg + "does not exist");
            }

            try (BufferedReader reader = Files.newBufferedReader(file.toPath(), encoding)) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.write(System.getProperty("line.separator"));
                    writer.flush();
                }
            } catch (IOException e) {
                throw new RuntimeException("cat: cannot open " + arg);
            }
        }
        
        return currentDirectory;
    }
    

}
