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
    private final String currentDirectory;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;
    private boolean prefixed;

    /**
     * Constructor of Grep application
     * @param currentDirectory currentDirectory of the Shell
     * @param reader Source of reading content
     * @param writer Destination of writing content
     */
    public Grep(String currentDirectory, BufferedReader reader, OutputStreamWriter writer){
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }


    /**
     * exec function of "Grep" application.
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - "grep: wrong number of arguments" // if argument size is less than 1
     * - "grep: no data from pipe or redirection and can not find file to read" // if appArg has size 1 and reader object is null.
     * - "grep: fail to read from pipe or redirection" // if appArg size is 1 and IOException is caught from reader.
     * - "grep: cannot open " + appArgs.get(index) // Failed to write content into buffer
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException{
        if (appArgs.size() < 1) {
            throw new RuntimeException("grep: wrong number of arguments");
        }

        Pattern grepPattern = Pattern.compile(appArgs.get(0).replaceAll("\\*", ".*"));
        if(appArgs.size() == 1){
            this.prefixed = false;
            try {
                writeToBuffer(grepPattern, this.reader, "");
            }catch (Exception e){
                throw new RuntimeException("Grep: no data from pipe or redirection and can not find file to read");
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

    /*
     * help function of exec()
     * The function writes matched content from reader into writer
     * @param grepPattern //Grep pattern
     * @param reader //Source of reading content
     * @param fileName //name of the file
     * @throws IOException The exception is thrown from writer object.
     */
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
