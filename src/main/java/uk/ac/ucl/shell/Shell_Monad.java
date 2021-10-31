package uk.ac.ucl.shell;

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
import java.util.Scanner;

import uk.ac.ucl.shell.Parser.Monad;
import uk.ac.ucl.shell.Parser.ParserBuilder;
import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;

public class Shell_Monad {

    private static String currentDirectory = System.getProperty("user.dir");



    private static ArrayList<String> globbingHelper(String glob) throws IOException {

        ArrayList<String> globbingResult = new ArrayList<String>();

        // dealing with glob, which does command substitution  --> find all mathced filenameas
        // & add then into tokens.
        // eg. `*.txt` & there are "1.txt, 2.txt, 3.txt in the working directory"
        // Then globbingResult will be a list of strings {"1.txt","2.txt","3.txt"}
        // And the final tokens will be <command> <arguments> where <arguments> = globbingResult
        
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

            //For debugging
            System.out.println("Current Command -> "+command);
            //may meet null


            PipedInputStream pipInput = new PipedInputStream();
            PipedOutputStream pipOutput = new PipedOutputStream(pipInput);

            OutputStreamWriter pipeWriter = new OutputStreamWriter(pipOutput);


            //dealing with pipe
            for (ArrayList<String> call: command) {


                //debug
                System.out.println("Current call -> "+call);

                String appName = call.get(0);
                //tokens contain <app name> <arguments> where <arguments> is a list of argument
                ArrayList<String> appArgs = new ArrayList<String>(call.subList(1, call.size()));
                
                //check globbing
                appArgs = globbingChecker(appArgs);
    
                //change stream
                //ShellApplication myApp = new AppBuilder(appName, currentDirectory, pipeWriter, pipOutput).createApp();
                ShellApplication myApp = new AppBuilder(appName, currentDirectory, writer, output).createApp();
                // keep track of directory
                currentDirectory = myApp.exec(appArgs);
            }

        }
        
/*
        for (String rawCommand : rawCommands) {
            String spaceRegex = "[^\\s\"']+|\"([^\"]*)\"|'([^']*)'";
            ArrayList<String> tokens = new ArrayList<String>();
            Pattern regex = Pattern.compile(spaceRegex);
            Matcher regexMatcher = regex.matcher(rawCommand);
            String nonQuote;
            while (regexMatcher.find()) {
                if (regexMatcher.group(1) != null || regexMatcher.group(2) != null) {
                    String quoted = regexMatcher.group(0).trim();
                    tokens.add(quoted.substring(1,quoted.length()-1));
                
                // Dealing with globbling (backquotes)
                } else {
                    // this command deals with the multiple spaces
                    nonQuote = regexMatcher.group().trim();
                    
                    

                }
            }
*/

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
                eval(args[1], System.out);
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
                        eval(cmdline, System.out);
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
