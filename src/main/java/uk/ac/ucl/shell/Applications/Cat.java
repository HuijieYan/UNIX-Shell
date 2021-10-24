package uk.ac.ucl.shell.Applications;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        } else {
            for (String arg : appArgs) {
                Charset encoding = StandardCharsets.UTF_8;
                File currFile = new File(currentDirectory + File.separator + arg);
                if (currFile.exists()) {
                    Path filePath = Paths.get(currentDirectory + File.separator + arg);
                    try (BufferedReader reader = Files.newBufferedReader(filePath, encoding)) {
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            writer.write(String.valueOf(line));
                            writer.write(System.getProperty("line.separator"));
                            writer.flush();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("cat: cannot open " + arg);
                    }
                } else {
                    throw new RuntimeException("cat: file does not exist");
                }
            }
        }
        
        return currentDirectory;
    }
    

}
