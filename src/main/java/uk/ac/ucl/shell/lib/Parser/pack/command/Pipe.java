package uk.ac.ucl.shell.lib.Parser.pack.command;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import uk.ac.ucl.shell.ShellUtils.CommandVisitor.CommandVisitor;

public class Pipe implements Command {
    private final ArrayList<Command> parsedArgs;

    public Pipe(ArrayList<Command> call, ArrayList<Command> listOfCalls){
        parsedArgs = call;
        parsedArgs.addAll(listOfCalls);
    }

    public ArrayList<Command> getCommands(){
        return parsedArgs;
    }

    //visitorCall 
    public String accept(CommandVisitor visitor, String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer) throws RuntimeException {
        return visitor.visit(this, currentDirectory, bufferedReader, writer);
    }

}
