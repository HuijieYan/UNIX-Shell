package uk.ac.ucl.shell.Applications;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

public class Cat implements ShellApplication {
    private String currentDirectory;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    /**
     * Constructor of Cat application
     * @param currentDirectory currentDirectory of the Shell
     * @param reader Source of reading content
     * @param writer Destination of writing content
     **/
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
     * @return currentDirecory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is throwed due to following reasons:
     * - "cat: no data from pipe or redirection and can not find file to read" // When appArgs is empty & reader object is null
     * - "cat: fail to read from pipe or redirection" // When appArgs is empty & failed to read from pipe or redirection
     * - "cat: can not open " + file // When file can not be opened
     **/
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if (appArgs.isEmpty()) {
            if(this.reader == null){
                throw new RuntimeException("cat: no data from pipe or redirection and can not find file to read");
            }
            try {
                writeToBuffer(this.reader);
            }catch (Exception e){
                throw new RuntimeException("cat: fail to read from pipe or redirection");
            }

        } else {
            //Use UTF8 charset
            Charset encoding = StandardCharsets.UTF_8;
            for(String file : appArgs){
                try {
                    writeToBuffer(Files.newBufferedReader(ShellUtil.getPath(currentDirectory, file), encoding));
                }catch (IOException e){
                    throw new RuntimeException("cat: can not open: " + file);
                }
            }

        }

        return currentDirectory;
    }

    /*
     * writeToBuffer fuction
     * The function takes a BufferedReader then write content from reader into a writer 
     * @param reader A BufferedReader which tells function the source of the content 
    */
    private void writeToBuffer(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line + System.getProperty("line.separator"));
        }
        writer.flush();
    }

}