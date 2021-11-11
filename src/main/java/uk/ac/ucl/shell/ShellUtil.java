package uk.ac.ucl.shell;

import uk.ac.ucl.shell.Applications.Tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// This class contains all the functions in middle layer.
// For Shell

public class ShellUtil {

    public static ArrayList<String> checkRedirection(ArrayList<String> appArgs) throws RuntimeException {
        ArrayList<String> inputAndOutputFile = new ArrayList<>();
        int inputIndex = appArgs.indexOf("<");
        if(inputIndex == -1){
            inputAndOutputFile.add(null);
        }else {
            appArgs.remove(inputIndex);
            if(appArgs.contains("<")){
                throw new RuntimeException("Error: several files are specified for input");
            }else if(inputIndex >= appArgs.size()){
                throw new RuntimeException("Error: no file is specified for input");
            }
            inputAndOutputFile.add(appArgs.remove(inputIndex));
        }

        int outputIndex = appArgs.indexOf(">");
        if(outputIndex == -1){
            inputAndOutputFile.add(null);
        }else {
            appArgs.remove(outputIndex);
            if(appArgs.contains(">")){
                throw new RuntimeException("Error: several files are specified for output");
            }else if(outputIndex >= appArgs.size()){
                throw new RuntimeException("Error: no file is specified for output");
            }

            inputAndOutputFile.add(appArgs.remove(outputIndex));
        }
        return inputAndOutputFile;
    }
    
    public static ArrayList<String> checkSubCmd(ArrayList<String> appArgs) throws IOException {

        ArrayList<String> resultArgs = new ArrayList<>();

        for (String curArg:appArgs) {

            List<String> matches = Pattern.compile("`([^`]*)`")
            .matcher(curArg)
            .results()
            .filter(mr -> mr.group(1) != null) 
            .map(mr -> mr.group(1)) 
            .collect(Collectors.toList());

            if (matches.size() == 0) {
                resultArgs.add(curArg);
                continue;
            }

            //need refactory
            for (String curSubCmd:matches) {
                //System.out.println("SUBCMD found -> " + curSubCmd);
                ByteArrayOutputStream subStream = new ByteArrayOutputStream();
                Shell_newParser.eval(curSubCmd, subStream);
                //check exception
                String resultStr = subStream.toString();
                //tidy string since subShell has newLine at the end
                resultStr = resultStr.replace(System.getProperty("line.separator"), "");
                //System.out.println("Result from sub shell -> " + resultStr);
                curArg = curArg.replace("`" + curSubCmd+ "`", resultStr);
                //System.out.println("Replaced arg -> "+curArg);
            }
            resultArgs.add(curArg);
        }
        return resultArgs;
    }

    // dealing with glob, which does command substitution  --> find all mathced filenameas
    // & add then into tokens.
    // eg. `*.txt` & there are "1.txt, 2.txt, 3.txt in the working directory"
    // Then globbingResult will be a list of strings {"1.txt","2.txt","3.txt"}
    // And the final tokens will be <command> <arguments> where <arguments> = globbingResult
    private static ArrayList<String> globbingHelper(String glob, String currentDirectory) throws IOException {

        ArrayList<String> globbingResult = new ArrayList<>();
        Path dir;
        if(glob.contains(System.getProperty("file.separator")) || !glob.startsWith("*")){
            dir = Paths.get(glob.substring(0, glob.indexOf("*") - 1));
        }else {
            dir = Paths.get(currentDirectory);
        }

        DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob);
        for (Path entry : stream) {
            globbingResult.add(entry.getFileName().toString());
        }

        return globbingResult;
    }

    public static ArrayList<String> globbingChecker(ArrayList<String> appArgs, String curDirectory) throws IOException {
        ArrayList<String> result = new ArrayList<>();

        for (String curString : appArgs) {
            if (curString.contains("*")) {
                ArrayList<String> globbingResult = globbingHelper(curString, curDirectory);
                result.addAll(globbingResult);
            } else {
                result.add(curString);
            }
        }
        return result;
    }

}
