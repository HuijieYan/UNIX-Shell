package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellUtils.Utils.ShellUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sort implements ShellApplication {
    private final String currentDirectory;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;

    /**
     * Constructor of Sort application
     * @param currentDirectory currentDirectory of the Shell
     * @param reader Source of reading content
     * @param writer Destination of writing content
     */
    public Sort(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }


    /**
     * exec function of Sort application.
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - "sort: wrong argument number" // if number of arguments is more than 2
     * - "sort: invalid option "+option // When argument size is 2 and option not equal to "-r"
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if(appArgs.size() > 2){
            throw new RuntimeException("sort: wrong argument number");
        }

        String option;
        if(appArgs.size() == 0){
            execFromStream("", null);
        }else {
            option = appArgs.get(0);
            if (appArgs.size() == 2) {
                if (!option.equals("-r")) {
                    throw new RuntimeException("sort: invalid option " + option);
                }
                execFromStream(option, appArgs.get(1));
            }else {
                if (option.equals("-r")) {
                    execFromStream(option, null);
                } else {
                    execFromStream("", appArgs.get(0));
                }
            }
        }

        return currentDirectory;
    }

    /*
     execFromStream
     helper function of exec() which does the execution from stream.
     @param option // if "-r" then sorts lines in reverse order
     */
    private void execFromStream(String option, String fileName) {
        BufferedReader reader;
        if(fileName == null){
            if(this.reader == null){
                throw new RuntimeException("sort: no data from pipe or redirection and can not find file to read");
            }
            reader = this.reader;
        }else {
            try {
                reader = Files.newBufferedReader(ShellUtil.getPath(currentDirectory, fileName), StandardCharsets.UTF_8);
            }catch (IOException e){
                throw new RuntimeException("sort: cannot open " + fileName);
            }
        }
        try {
            ArrayList<String> lines = readFromReader(reader);
            sort(option, lines);
            writeToBuffer(lines);
        }catch (Exception e){
            //catch Exception for writer or reader is null or fail to write or read
            throw new RuntimeException("sort: fail to read or write");
        }
    }

    // helper function to sort a list of string
    private void sort(String option, ArrayList<String> readLines){
        Collections.sort(readLines);
        if (option.equals("-r")){
            Collections.reverse(readLines);
        }
    }


    // helper function which reads content from a stream into a list of string  
    private ArrayList<String> readFromReader(BufferedReader reader) throws IOException{
        ArrayList<String> lines = new ArrayList<>();
        String line;
        while((line = reader.readLine()) != null){
            lines.add(line);
        }
        return lines;
    }


    // helper function which takes list of string and writes the content into writer.
    private void writeToBuffer(ArrayList<String> lines) throws IOException{
        for(String str : lines){
            writer.write(str);
            writer.write(System.getProperty("line.separator"));
        }
        writer.flush();
    }
}
