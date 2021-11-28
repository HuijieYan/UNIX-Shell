package uk.ac.ucl.shell;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import uk.ac.ucl.shell.Parser.Monad;
import uk.ac.ucl.shell.Parser.pack.command.Command;
import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;

public class Shell {

    /**
     * The main eval function of Shell program
     * @param cmdline commandline from user
     * @param writer Destination of writing content
     * @param currentDirectory currentDirectory of the Shell
     * @return currentDirecotry of Shell
     * @throws RuntimeException
     */
    public static String eval(String cmdline, OutputStreamWriter writer, String currentDirectory) throws RuntimeException {

        // Using monad Parser
        ShellParser myParser = new ShellParser();
        Monad<ArrayList<Command>> sat = myParser.parseCommand();
        MonadicValue<ArrayList<Command>, String> resultPair = sat.parse(cmdline);
        ArrayList<Command> commandList = resultPair.getValue();
        if(!resultPair.getInputStream().equals("") || commandList == null){
            throw new RuntimeException("Error: the input does not satisfy the syntax");
        }

        CommandVisitor myVisitor = new ActualCmdVisitor();
        // in seq
        for (Command curCmd: commandList) {
            //access visitor
            try {
                currentDirectory = curCmd.accept(myVisitor, currentDirectory, null, writer);
            }catch (Exception e){
                if(e.getMessage().startsWith("ignore")){
                    try {
                        writer.write(e.getMessage().substring(6) + System.getProperty("line.separator"));
                        writer.flush();
                    }catch (Exception ignored){}
                }else {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
        return currentDirectory;
    }

    /**
     * Entry of the Shell Program
     * @param args
     */
    public static void main(String[] args) {
        String currentDirectory = System.getProperty("user.dir");
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        if (args.length > 0) {
            if (args.length != 2) {
                System.out.println("COMP0010 shell: wrong number of arguments");
                return;
            }
            if (!args[0].equals("-c")) {
                System.out.println("COMP0010 shell: " + args[0] + ": unexpected argument");
            }
            try {
                Shell.eval(args[1], writer, currentDirectory);
            } catch (Exception e) {
                System.out.print("");
            }
        } else {
            try (Scanner input = new Scanner(System.in)) {
                while (true) {
                    String prompt = currentDirectory + "> ";
                    System.out.print(prompt);
                    String cmdline = input.nextLine();
                    currentDirectory = Shell.eval(cmdline, writer, currentDirectory);
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

}