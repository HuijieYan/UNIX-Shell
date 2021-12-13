package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Cut implements ShellApplication {
    private final String currentDirectory;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;

    /**
     * Constructor of Cut application
     * @param currentDirectory currentDirectory of the Shell
     * @param reader Source of reading content
     * @param writer Destination of writing content
     */
    public Cut(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * exec function of "cut" application.
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - "cut: wrong argument number" // if number of arguments are less than 2 or greater than 3
     * - "cut: incorrect option input " + appArgs.get(0) // When first element of appArg is not equal to "-b"
     * - "cut: no data from pipe or redirection and can not find file to read" // When argument size is 2 and reader object is null
     * - "cut: can not open file: " + appArgs.get(2) // When argument size is 3 and filePath is invalid or IOException is caught from OS
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if (appArgs.size() < 2 || appArgs.size() > 3) {
            throw new RuntimeException("cut: wrong argument number");
        }
        if (!appArgs.get(0).equals("-b")) {
            throw new RuntimeException("cut: incorrect option input " + appArgs.get(0));
        }

        String[] args = appArgs.get(1).split(",");
        ArrayList<Integer> singleIndexes = new ArrayList<>();
        ArrayList<ArrayList<Integer>> ranges = new ArrayList<>();

        findIndexRange(args, singleIndexes, ranges);

        if (appArgs.size() == 2) {
            try {
                this.writeToBuffer(this.reader, singleIndexes, ranges);
            }catch (Exception e){
                throw new RuntimeException("cut: no data from pipe or redirection and can not find file to read");
            }
        } else {
            try {
            this.writeToBuffer(Files.newBufferedReader(ShellUtil.getPath(currentDirectory, appArgs.get(2)), StandardCharsets.UTF_8), singleIndexes, ranges);
            }catch (Exception e){
                throw new RuntimeException("cut: can not open file " + appArgs.get(2));
            }
        }

        return currentDirectory;
    }

    /*
      helper function of exec() to find the indexRange from arguments
      @param args Application arguments
      @param singleIndexes stores cut(single index) information for cutting content (eg. 7)
      @param ranges stores range information for cutting content (eg. 7-10)
      @throws RuntimeException // When argument is invalid
     */
    private void findIndexRange(String[] args, ArrayList<Integer> singleIndexes, ArrayList<ArrayList<Integer>> ranges) {
        for (String arg : args) {
            if (!Pattern.matches("[0-9]*-*[0-9]*", arg) || arg.equals("") || arg.equals("-")) {
                throw new RuntimeException("cut: invalid argument " + arg);
            }

            if(!arg.contains("-")){
                int index = Integer.parseInt(arg);

                if(index < 1){
                    throw new RuntimeException("cut: invalid argument " + arg);
                }else {
                    singleIndexes.add(index);
                }
            } else {
                if(arg.indexOf('-') != arg.lastIndexOf('-')){
                    throw new RuntimeException("cut: invalid argument " + arg);
                }else if (arg.charAt(0) == '-') {
                    String stringIndex = arg.substring(1);
                    int index = Integer.parseInt(stringIndex);
                    if (index < 1) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    } else {
                        ArrayList<Integer> range = new ArrayList<>();
                        range.add(1);
                        range.add(index);
                        ranges.add(range);
                    }

                } else if (arg.charAt(arg.length() - 1) == '-') {
                    String stringIndex = arg.substring(0, arg.length() - 1);
                    int index = Integer.parseInt(stringIndex);
                    if (index < 1) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    } else {
                        ArrayList<Integer> range = new ArrayList<>();
                        range.add(index);
                        range.add(Integer.MAX_VALUE);
                        ranges.add(range);
                    }

                } else {
                    int rangeSymbolIndex;
                    rangeSymbolIndex = arg.indexOf('-');
                    ArrayList<Integer> range = new ArrayList<>();

                    String bound = arg.substring(0,rangeSymbolIndex);
                    int lowerBound = Integer.parseInt(bound);
                    if (lowerBound < 1) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    } else {
                        range.add(lowerBound);
                    }

                    bound = arg.substring(rangeSymbolIndex+1);
                    int upperBound = Integer.parseInt(bound);
                    if (upperBound < 1) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    }else if(lowerBound > upperBound){
                        throw new RuntimeException("cut: invalid argument " + arg);
                    }else {
                        range.add(upperBound);
                    }
                    ranges.add(range);
                }
            }
        }
    }

    // helper function to write required content into destination stream
    private void writeToBuffer(BufferedReader reader, ArrayList<Integer> singleIndexes, ArrayList<ArrayList<Integer>> ranges) throws IOException{
        Charset charset = StandardCharsets.UTF_8;
        String line;
        while ((line = reader.readLine()) != null) {
            byte[] bytes = line.getBytes(charset);
            ArrayList<Integer> bytesToPrintIndexes = new ArrayList<>();
            for(int index = 0; index < bytes.length; index++){
                int bytesToPrintIndex = index + 1;
                if(singleIndexes.contains(bytesToPrintIndex)){
                    bytesToPrintIndexes.add(index);
                    continue;
                }

                for(ArrayList<Integer> range : ranges){
                    if(range.get(0) <= bytesToPrintIndex && bytesToPrintIndex <= range.get(1)){
                        bytesToPrintIndexes.add(index);
                        break;
                    }
                }
            }

            byte[] bytesToPrint = new byte[bytesToPrintIndexes.size()];
            for(int index = 0; index < bytesToPrintIndexes.size(); index++){
                bytesToPrint[index] = bytes[bytesToPrintIndexes.get(index)];
            }
            writer.write(new String(bytesToPrint, charset));
            writer.write(System.getProperty("line.separator"));
        }
        writer.flush();
    }
}
