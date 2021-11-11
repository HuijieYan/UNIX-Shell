package uk.ac.ucl.shell.Applications;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ucl.shell.ShellApplication;

public class Grep implements ShellApplication {
    private String currentDirectory;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    public Grep(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if (appArgs.size() < 2) {
            throw new RuntimeException("grep: wrong number of arguments");
        }

        Pattern grepPattern = Pattern.compile(appArgs.get(0));
        Charset encoding = StandardCharsets.UTF_8;
        int numOfFiles = appArgs.size() - 1;
        for (int index = 1; index < appArgs.size(); index++){
            try (BufferedReader reader = Files.newBufferedReader(Tools.getPath(currentDirectory, appArgs.get(index)), encoding)) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = grepPattern.matcher(line);
                    if (matcher.find()) {
                        if (numOfFiles > 1) {
                            writer.write(appArgs.get(index+1));
                            writer.write(":");
                        }
                        writer.write(line);
                        writer.write(System.getProperty("line.separator"));
                        writer.flush();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("grep: cannot open " + appArgs.get(index));
            }
        }

        return currentDirectory;
    }
}
