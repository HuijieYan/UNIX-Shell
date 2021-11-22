package uk.ac.ucl.shell.Parser.pack.command;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import uk.ac.ucl.shell.CommandVisitor;
import uk.ac.ucl.shell.Parser.pack.type.atom.*;

public class Call implements Command {
    private ArrayList<Atom> rawCommand;
    private ArrayList<Command> selfList = new ArrayList<>();;
    
    public Call(ArrayList<Atom> arguments){
        rawCommand = arguments;
        selfList.add(this);
    }

    public ArrayList<Command> getCommands(){
        return selfList;
    }

    public ArrayList<Atom> getArgs(){
        return rawCommand;
    }

    //visitor 
    public String accept(CommandVisitor visitor, String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer) throws RuntimeException {
        return visitor.visit(this, currentDirectory, bufferedReader, writer);
    }

}
