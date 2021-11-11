package uk.ac.ucl.shell.Applications;

import uk.ac.ucl.shell.ShellApplication;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Cut implements ShellApplication {
    private String currentDirectory;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    public Cut(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if (appArgs.size() < 3) {
            throw new RuntimeException("cut: no enough arguments.");
        }
        if (!appArgs.get(0).equals("-b")) { // cut -b 1,2,3 Dockerfile
            throw new RuntimeException("cut: incorrect option input " + appArgs.get(0));
        }
        if (appArgs.size() > 3) {
            throw new RuntimeException("cut: too many arguments.");
        }

        String[] args = appArgs.get(1).split(",");

        ArrayList<Integer> singleIndexes = new ArrayList<>();
        ArrayList<ArrayList<Integer>> ranges = new ArrayList<>();

        for (String arg : args) {
            if (!Pattern.matches("[0-9]*-*[0-9]*", arg) || arg.equals("") || arg.equals("-")) {
                throw new RuntimeException("cut: invalid argument " + arg);
            }

            if(!arg.contains("-")){
                int index = 0;
                try {
                    index = Integer.parseInt(arg);
                }catch (NumberFormatException e){
                    throw new RuntimeException("cut: invalid argument " + arg);
                }

                if(index < 1){
                    throw new RuntimeException("cut: invalid argument " + arg);
                }else {
                    singleIndexes.add(index);
                }

            } else {
                //char[] argChars = arg.toCharArray();
                if (arg.charAt(0) == '-') {
                    String stringIndex = arg.substring(1);
                    int index = 0;
                    try {
                        index = Integer.parseInt(stringIndex);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    }

                    if (index < 1) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    } else {
                        ArrayList<Integer> range = new ArrayList<>();
                        range.add(1);
                        range.add(index);
                        ranges.add(range);
                    }

                } else if (arg.charAt(arg.length() - 1) == '-') {
                    String stringIndex = arg.substring(0, args.length - 1);
                    int index = 0;
                    try {
                        index = Integer.parseInt(stringIndex);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    }

                    if (index < 1) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    } else {
                        ArrayList<Integer> range = new ArrayList<>();
                        range.add(index);
                        range.add(Integer.MAX_VALUE);
                        ranges.add(range);
                    }

                } else {
                    int rangeSymbolIndex = 0;
                    rangeSymbolIndex = arg.indexOf('-');
                    ArrayList<Integer> range = new ArrayList<>();

                    int lowerBound = 0;
                    String bound = arg.substring(0,rangeSymbolIndex);
                    try {
                        lowerBound = Integer.parseInt(bound);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    }
                    if (lowerBound < 1) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    } else {
                        range.add(lowerBound);
                    }

                    int upperBound = 0;
                    bound = arg.substring(rangeSymbolIndex+1);
                    try {
                        upperBound = Integer.parseInt(bound);
                    } catch (NumberFormatException e) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    }
                    if (upperBound < 1) {
                        throw new RuntimeException("cut: invalid argument " + arg);
                    }else if(lowerBound > upperBound){
                        throw new RuntimeException("cut: invalid decreasing range " + arg);
                    }else {
                        range.add(upperBound);
                    }
                    ranges.add(range);

                }

            }
        }


        Charset charset = StandardCharsets.UTF_8;
        try (BufferedReader reader = Files.newBufferedReader(Tools.getPath(currentDirectory, appArgs.get(2)), charset)) {
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
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("cut: cannot open " + appArgs.get(2));
        }

        return currentDirectory;
    }
}
