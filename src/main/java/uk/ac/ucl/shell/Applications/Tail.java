package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellUtils.Utils.ShellUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Tail implements ShellApplication{
    private final String currentDirectory;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;
    private int tailLines;

    /**
     * Constructor of Tail application
     * @param currentDirectory currentDirectory of the Shell
     * @param reader Source of reading content
     * @param writer Destination of writing content
     */
    public Tail(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }


    /**
     * exec function of "Tail" application.
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - "tail: wrong argument number" // If argument size is greater than 3
     * - "tail: wrong argument " + appArgs.get(0) + " should be -n" // if first argument is not "-n"
     * - "tail: " + appArgs.get(1) + " is not a integer" // When NumberFormatException is caught & 2nd element in app argument is not integer
     * - "tail: no data from pipe or redirection and can not find file to read" // If file name & reader object is null
     * - "tail: fail to read from pipe or redirection" // IOException is caught from writer
     * - "tail: cannot open: " + fileName // IOException is caught due to invalid file or file can't be opened
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        tailLines = 10;
        String fileName = null;
        int argSize = appArgs.size();
        if (argSize > 3) {
            throw new RuntimeException("tail: wrong argument number");
        }else if (argSize == 3 || argSize == 2) {
            fileName = getName_TwoOrThree(appArgs, fileName, argSize);
        }else if(argSize == 1){
            fileName = appArgs.get(0);
        }
        execAux(fileName);

        return currentDirectory;
    }

    //helper function which returns filename when argument size is 2 or 3
    private String getName_TwoOrThree(List<String> appArgs, String fileName, int argSize) {
        if(!appArgs.get(0).equals("-n")){
            throw new RuntimeException("tail: wrong argument " + appArgs.get(0) + " should be -n");
        }

        try {
            tailLines = Integer.parseInt(appArgs.get(1));
        } catch (NumberFormatException e) {
            throw new RuntimeException("tail: " + appArgs.get(1) + " is not an integer");
        }

        if(argSize == 3){
            fileName = appArgs.get(2);
        }
        return fileName;
    }

    // aux function of exec. Write content to buffer
    private void execAux(String fileName) {
        if(fileName == null){
            try {
                writeToBuffer(this.reader);
            }catch (Exception e){
                //catch Exception for reader is null or fail to read
                throw new RuntimeException("tail: no data from pipe or redirection and can not find file to read");
            }
        } else {
            try {
                writeToBuffer(Files.newBufferedReader(ShellUtil.getPath(currentDirectory, fileName), StandardCharsets.UTF_8));
            }catch (IOException e){
                throw new RuntimeException("tail: cannot open: " + fileName);
            }
        }
    }

    // Helper function to write content required into reader object
    private void writeToBuffer(BufferedReader reader) throws IOException {
        ArrayList<String> storage = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            storage.add(line);
        }
        int index = 0;
        if (tailLines < storage.size()) {
            index = storage.size() - tailLines;
        }
        for (int i = index; i < storage.size(); i++) {
            writer.write(storage.get(i) + System.getProperty("line.separator"));
        }
        writer.flush();
    }

}
