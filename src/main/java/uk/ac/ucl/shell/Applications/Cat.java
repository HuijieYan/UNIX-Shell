package uk.ac.ucl.shell.Applications;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import uk.ac.ucl.shell.ShellUtils.Utils.ShellUtil;

public class Cat implements ShellApplication {
    private final String currentDirectory;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;

    /**
     * Constructor of Cat application
     * @param currentDirectory currentDirectory of the Shell
     * @param reader Source of reading content
     * @param writer Destination of writing content
     */
    public Cat(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * exec function of "cat" application. (Use UTF_8 CharSet)
     * The function takes list of files from appArg & read into a bufferReader.
     * The content from bufferReader is then write into writer
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - "cat: no data from pipe or redirection and can not find file to read" // When appArgs is empty & reader object is null
     * - "cat: fail to read from pipe or redirection" // When appArgs is empty & failed to read from pipe or redirection
     * - "cat: can not open " + file // When file can not be opened
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if (appArgs.isEmpty()) {
            try {
                writeToBuffer(this.reader);
            }catch (Exception e){
                //catch Exception for reader is null or fail to read
                throw new RuntimeException("cat: no data from pipe or redirection and can not find file to read");
            }

        } else {
            for(String file : appArgs){
                try {
                    writeToBuffer(Files.newBufferedReader(ShellUtil.getPath(currentDirectory, file), StandardCharsets.UTF_8));
                }catch (IOException e){
                    throw new RuntimeException("cat: can not open: " + file);
                }
            }

        }
        return currentDirectory;
    }


    // The function takes a BufferedReader then write content from reader into a writer 
    private void writeToBuffer(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line + System.getProperty("line.separator"));
        }
        writer.flush();
    }

}