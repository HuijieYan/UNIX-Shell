package uk.ac.ucl.shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    
    public static ArrayList<String> checkSubCmd(ArrayList<String> appArgs) throws IOException {

        ArrayList<String> resultArgs = new ArrayList<>();

        for (String curArg:appArgs) {

            List<String> matches = Pattern.compile("`([^`]*)`")
            .matcher(curArg)
            .results()
            .filter(mr -> mr.group(1) != null) 
            .map(mr -> mr.group(1)) 
            .collect(Collectors.toList());

            if (matches == null || matches.size() == 0) {
                resultArgs.add(curArg);
                continue;
            }

            //need refactory
            for (String curSubCmd:matches) {
                //System.out.println("SUBCMD found -> " + curSubCmd);
                ByteArrayOutputStream subStream = new ByteArrayOutputStream();
                Shell_Monad.eval(curSubCmd, subStream);
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

        ArrayList<String> globbingResult = new ArrayList<String>();
        Path dir = Paths.get(currentDirectory);
        DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob);
        for (Path entry : stream) {
            globbingResult.add(entry.getFileName().toString());
        }
        if (globbingResult.isEmpty()) {
            globbingResult.add(glob);
        }
        return globbingResult;
    }

    public static ArrayList<String> globbingChecker(ArrayList<String> appArgs, String curDirectory) throws IOException {

        ArrayList<String> result = new ArrayList<>();

        //need refact using Stream
        for (int i=0; i< appArgs.size(); i++) {
            String curString = appArgs.get(i);

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
