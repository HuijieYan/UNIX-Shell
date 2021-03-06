package uk.ac.ucl.shell.Applications;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import uk.ac.ucl.shell.ShellUtils.Utils.ShellUtil;

public class Head implements ShellApplication {
    private final String currentDirectory;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;
    private int headLines;

    /**
     * Constructor of Head application
     * @param currentDirectory currentDirectory of the Shell
     * @param reader Source of reading content
     * @param writer Destination of writing content
     */
    public Head(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * exec function of "Head" application.
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - "head: wrong argument number" // When appArgs is more than 3
     * - "head: wrong argument " + appArgs.get(0) + " should be -n" // When first element in appArg is not equal to "-n"
     * - "head: " + appArgs.get(1) + " is not a integer" // When second element in appArg is not an Integer. (NumberFormatException is caught)
     * - "head: no data from pipe or redirection and can not find file to read" // When fileName is null & reader object is null
     * - "head: fail to read from pipe or redirection" // When IOException is caught from reader
     * - "head: cannot open: " + fileName // When fileName is not null & IOException caught from reader
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        headLines = 10;
        String fileName = null;
        int argSize = appArgs.size();
        if (argSize > 3) {
            throw new RuntimeException("head: wrong argument number");
        }else if (argSize == 3 || argSize == 2) {
            fileName = getName_TwoOrThree(appArgs, fileName, argSize);
        }else if(argSize == 1){
            fileName = appArgs.get(0);
        }

        execAux(fileName);

        return currentDirectory;
    }

    // Helper function of exec that deals with writing content into buffer
    private void execAux(String fileName) {
        if(fileName == null){
            try {
                writeToBuffer(this.reader);
            }catch (Exception e){
                //catch Exception for reader is null or fail to read
                throw new RuntimeException("head: no data from pipe or redirection and can not find file to read");
            }
        } else {
            try {
                writeToBuffer(Files.newBufferedReader(ShellUtil.getPath(currentDirectory, fileName), StandardCharsets.UTF_8));
            }catch (IOException e){
                throw new RuntimeException("head: cannot open: " + fileName);
            }
        }
    }

    //helper function which returns filename when argument size is 2 or 3
    private String getName_TwoOrThree(List<String> appArgs, String fileName, int argSize) {
        if(!appArgs.get(0).equals("-n")){
            throw new RuntimeException("head: wrong argument " + appArgs.get(0) + " should be -n");
        }

        try {
            headLines = Integer.parseInt(appArgs.get(1));
        } catch (NumberFormatException e) {
            throw new RuntimeException("head: " + appArgs.get(1) + " is not an integer");
        }

        if(argSize == 3){
            fileName = appArgs.get(2);
        }
        return fileName;
    }

    /*
     helper function of exec()
     The function writes content from reader into writer
    */
    private void writeToBuffer(BufferedReader reader) throws IOException {
        for (int i = 0; i < headLines; i++) {
            String line;
            if ((line = reader.readLine()) != null) {
                writer.write(line + System.getProperty("line.separator"));
            }else {
                break;
            }
        }
        writer.flush();
    }

}
