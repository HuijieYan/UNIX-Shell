package uk.ac.ucl.shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import uk.ac.ucl.shell.Parser.Monad;
import uk.ac.ucl.shell.Parser.ParserBuilder;
import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;

public class Shell_Monad {

    private static String currentDirectory = System.getProperty("user.dir");

    private static ArrayList<String> checkSubCmd(ArrayList<String> appArgs) throws IOException {

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
    private static ArrayList<String> globbingHelper(String glob) throws IOException {

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

    private static ArrayList<String> globbingChecker(ArrayList<String> appArgs) throws IOException {

        ArrayList<String> result = new ArrayList<>();

        //need refact using Stream
        for (int i=0; i< appArgs.size(); i++) {
            String curString = appArgs.get(i);

            if (curString.contains("*")) {
                ArrayList<String> globbingResult = globbingHelper(curString);
                result.addAll(globbingResult);
            } else {
                result.add(curString);
            }
        }
        return result;
    }

    //change to non-static at the moment
    public static void eval(String cmdline, OutputStream output) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(output);

        // Using monad Parser
        ParserBuilder myParser = new ParserBuilder();
        Monad<ArrayList<ArrayList<ArrayList<String>>>> sat = myParser.lexCommand();
        MonadicValue<ArrayList<ArrayList<ArrayList<String>>>, String> result = sat.parse(cmdline);

        // System.out.println("result "+result.getValue());
        // System.out.println("input left "+result.getInputStream());

        //In seq
        for (ArrayList<ArrayList<String>> command:result.getValue()) {

            //System.out.println("Current Command -> "+command);

            //may meet null

            //dealing with pipe
            for (ArrayList<String> call: command) {
                //System.out.println("Current call -> "+call);
                String appName = call.get(0);
                // tokens contain <app name> <arguments> where <arguments> is a list of argument
                ArrayList<String> appArgs = new ArrayList<String>(call.subList(1, call.size()));
            
                //check subcommand
                appArgs = checkSubCmd(appArgs);
    
                //check globbing
                appArgs = globbingChecker(appArgs);

                //change stream
                ShellApplication myApp = new AppBuilder(appName, currentDirectory, writer, output).createApp();
                // keep track of directory
                currentDirectory = myApp.exec(appArgs);
            }
        }
        
    }


    public static void main(String[] args) {
        if (args.length > 0) {
            if (args.length != 2) {
                System.out.println("COMP0010 shell: wrong number of arguments");
                return;
            }
            if (!args[0].equals("-c")) {
                System.out.println("COMP0010 shell: " + args[0] + ": unexpected argument");
            }
            try {
                Shell_Monad.eval(args[1], System.out);
            } catch (Exception e) {
                System.out.println("COMP0010 shell: " + e.getMessage());
            }
        } else {
            Scanner input = new Scanner(System.in);
            try {
                while (true) {
                    String prompt = currentDirectory + "> ";
                    System.out.print(prompt);
                    try {
                        String cmdline = input.nextLine();
                        Shell_Monad.eval(cmdline, System.out);
                    } catch (Exception e) {
                        System.out.println("COMP0010 shell: " + e.getMessage());
                    }
                }
            } finally {
                input.close();
            }
        }
    }

}
