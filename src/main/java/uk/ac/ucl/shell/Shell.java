package uk.ac.ucl.shell;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import uk.ac.ucl.shell.Parser.Monad;
import uk.ac.ucl.shell.Parser.ParserBuilder;
import uk.ac.ucl.shell.Parser.pack.command.Command;
import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;

public class Shell {

    private static String currentDirectory = System.getProperty("user.dir");

    //change to non-static at the moment
    public static void eval(String cmdline, OutputStream output) throws IOException {

        // Using monad Parser
        ParserBuilder myParser = new ParserBuilder();
        Monad<ArrayList<Command>> sat = myParser.parseCommand();
        ArrayList<Command> commandList = sat.parse(cmdline).getValue();

        CommandVisitor myVisitor = new ActualCmdVisitor();

        // in seq
        for (Command curCmd: commandList) {
            //access visitor
            try {
                currentDirectory = curCmd.accept(myVisitor, currentDirectory, null, output);
            } catch (IOException e) {
                System.out.println("IO error catched");
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
                Shell.eval(args[1], System.out);
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
                        Shell.eval(cmdline, System.out);
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