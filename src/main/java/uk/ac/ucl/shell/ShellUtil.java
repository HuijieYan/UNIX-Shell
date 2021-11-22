package uk.ac.ucl.shell;

import uk.ac.ucl.shell.Parser.pack.type.atom.Atom;
import uk.ac.ucl.shell.Parser.pack.type.atom.RedirectionSymbol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// This class contains all the functions in middle layer.
// For Shell

public class ShellUtil {

    public static ArrayList<String> checkRedirection(ArrayList<Atom> cmdArgs) throws RuntimeException {
        ArrayList<String> inputAndOutputFile = new ArrayList<>();
        inputAndOutputFile.add(null);
        inputAndOutputFile.add(null);

        boolean hasSeveralInput = false;
        boolean hasSeveralOutput = false;
        for(int argIndex = 0; argIndex < cmdArgs.size();){
            Atom arg = cmdArgs.get(argIndex);
            if(arg instanceof RedirectionSymbol){
                if(((RedirectionSymbol)arg).isTowardsNext()){
                    if(!hasSeveralInput){
                        if(argIndex + 1 < cmdArgs.size()){
                            inputAndOutputFile.set(1, cmdArgs.get(argIndex + 1).get().get(0));
                            cmdArgs.remove(argIndex);
                            cmdArgs.remove(argIndex);
                        }else {
                            throw new RuntimeException("Error: no file is specified for input");
                        }
                        hasSeveralInput = true;
                    }else {
                        throw new RuntimeException("Error: several files are specified for input");
                    }

                } else {
                    if(!hasSeveralOutput){
                        if(argIndex + 1 < cmdArgs.size()){
                            inputAndOutputFile.set(0, cmdArgs.get(argIndex + 1).get().get(0));
                            cmdArgs.remove(argIndex);
                            cmdArgs.remove(argIndex);
                        }else {
                            throw new RuntimeException("Error: no file is specified for output");
                        }
                        hasSeveralOutput = true;
                    }else {
                        throw new RuntimeException("Error: several files are specified for output");
                    }

                }
            }else {
                argIndex++;
            }
        }

        return inputAndOutputFile;
    }

    public static String processDoubleQuotes(String arg) {
        
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(arg);

        //if not found
        if (!matcher.find()) {
            //System.out.println("match not found");
            return arg;
        }
        //extract content
        String matchedContent = matcher.group(1);
        //System.out.println("matchedContent -> "+matchedContent);
        arg = arg.replace("\"" + matchedContent + "\"", matchedContent);

        return arg;
    }
    
    public static ArrayList<String> checkSubCmd(ArrayList<String> callCmd) throws IOException {

        ArrayList<String> resultCmd = new ArrayList<>();

        for (String curArg:callCmd) {

            List<String> matches = Pattern.compile("`([^`]*)`")
            .matcher(curArg)
            .results()
            .filter(mr -> mr.group(1) != null) 
            .map(mr -> mr.group(1)) 
            .collect(Collectors.toList());
            
            //debug
            // for (String curMatch:matches) {
            //     System.out.println("mathed -> "+curMatch);
            // }

            if (matches.size() == 0) {
                resultCmd.add(curArg);
                continue;
            }
            
            //need refactory
            for (String curSubCmd: matches) {

                //System.out.println("SUBCMD found -> " + curSubCmd);
                ByteArrayOutputStream subStream = new ByteArrayOutputStream();
                Shell.eval(curSubCmd, subStream);
                //check exception
                String resultStr = subStream.toString();
                //tidy string since subShell has newLine at the end
                //System.out.println("resultStr = "+resultStr + " last index of result str-> " + matches.lastIndexOf(resultStr) + " matches size -> "+ (matches.size()-1));

                resultStr = resultStr.replace(System.getProperty("line.separator"), " ");
                //delete last space
                resultStr = resultStr.substring(0, resultStr.length()-1);
                //System.out.println("Result from sub shell -> " + resultStr);
                curArg = curArg.replace("`" + curSubCmd + "`", resultStr);
                //System.out.println("Replaced arg -> "+curArg);

                resultCmd.add(curArg);
            }
        }
        //debug
        // System.out.print("Curcmd -> ");
        // for (String curCmd: resultCmd) {
        //     System.out.print(curCmd+" ");
        // }
        // System.out.println("");

        return resultCmd;
    }

    // dealing with glob, which does command substitution  --> find all mathced filenameas
    // & add then into tokens.
    // eg. `*.txt` & there are "1.txt, 2.txt, 3.txt in the working directory"
    // Then globbingResult will be a list of strings {"1.txt","2.txt","3.txt"}
    // And the final tokens will be <command> <arguments> where <arguments> = globbingResult

    /* has bugggggggggggg */
    private static String globbingHelper(String glob, String currentDirectory) throws IOException {
        ArrayList<String> globbingResult = new ArrayList<>();

        Path dir;
        DirectoryStream<Path> stream;
        
        if(glob.contains(System.getProperty("file.separator")) || !glob.startsWith("*")){
            dir = Paths.get(glob.substring(0, glob.indexOf("*") - 1));
            stream = Files.newDirectoryStream(dir, glob.substring(glob.indexOf("*")));
        } else {
            dir = Paths.get(currentDirectory);
            stream = Files.newDirectoryStream(dir, glob);
        }

        return Globbing.exec(dir, glob);
    }

    public static String globbingFunc(String arg, String curDirectory) {
        try {
            return globbingHelper(arg, curDirectory);
        } catch (IOException e) {
            return "";
        }
    }

}
