package uk.ac.ucl.shell.Applications;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ucl.shell.ShellApplication;

public class Grep implements ShellApplication {

    private OutputStreamWriter writer;
    private String currentDirectory;

    public Grep(OutputStreamWriter writer, String currentDirectory) {
        this.writer = writer;
        this.currentDirectory = currentDirectory;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if (appArgs.size() < 2) {
            throw new RuntimeException("grep: wrong number of arguments");
        }
        Pattern grepPattern = Pattern.compile(appArgs.get(0));
        int numOfFiles = appArgs.size() - 1;
        Path filePath;
        Path[] filePathArray = new Path[numOfFiles];
        Path currentDir = Paths.get(currentDirectory);
        for (int i = 0; i < numOfFiles; i++) {
            filePath = currentDir.resolve(appArgs.get(i + 1));
            if (Files.notExists(filePath) || Files.isDirectory(filePath) || 
                !Files.exists(filePath) || !Files.isReadable(filePath)) {
                throw new RuntimeException("grep: wrong file argument");
            }
            filePathArray[i] = filePath;
        }
        for (int j = 0; j < filePathArray.length; j++) {
            Charset encoding = StandardCharsets.UTF_8;
            try (BufferedReader reader = Files.newBufferedReader(filePathArray[j], encoding)) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = grepPattern.matcher(line);
                    if (matcher.find()) {
                        if (numOfFiles > 1) {
                            writer.write(appArgs.get(j+1));
                            writer.write(":");
                        }
                        writer.write(line);
                        writer.write(System.getProperty("line.separator"));
                        writer.flush();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("grep: cannot open " + appArgs.get(j + 1));
            }
        }

        return currentDirectory;

    }
}
