package uk.ac.ucl.shell.Applications;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

public class Grep implements ShellApplication {
    private String currentDirectory;
    private BufferedReader reader;
    private OutputStreamWriter writer;
    private boolean prefixed;

    public Grep(String currentDirectory, BufferedReader reader, OutputStreamWriter writer){
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws RuntimeException{
        if (appArgs.size() < 1) {
            throw new RuntimeException("grep: wrong number of arguments");
        }

        Pattern grepPattern = Pattern.compile(appArgs.get(0).replaceAll("\\*", ".*"));
        if(appArgs.size() == 1){
            if(this.reader == null){
                throw new RuntimeException("Grep: no data from pipe or redirection and can not find file to read");
            }
            this.prefixed = false;
            try {
                writeToBuffer(grepPattern, this.reader, "");
            }catch (IOException e){
                throw new RuntimeException("Grep: fail to read from pipe or redirection");
            }
        }else {
            this.prefixed = (appArgs.size() - 1) > 1;
            for(int index = 1; index < appArgs.size(); index++){
                try {
                    writeToBuffer(grepPattern, Files.newBufferedReader(ShellUtil.getPath(currentDirectory, appArgs.get(index)), StandardCharsets.UTF_8), appArgs.get(index));
                }catch (IOException e){
                    throw new RuntimeException("grep: cannot open " + appArgs.get(index));
                }
            }
        }

        return currentDirectory;
    }

    private void writeToBuffer(Pattern grepPattern, BufferedReader reader, String fileName) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher matcher = grepPattern.matcher(line);
            if (matcher.find()) {
                if (prefixed) {
                    writer.write(fileName);
                    writer.write(":");
                }
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
            }
        }
        writer.flush();
    }

}
