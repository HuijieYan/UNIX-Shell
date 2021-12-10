package uk.ac.ucl.shell.Parser.pack.command;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import uk.ac.ucl.shell.CommandVisitor;
import uk.ac.ucl.shell.Parser.pack.type.atom.*;

public class Call implements Command {
    private final ArrayList<Atom> rawCommand;

    public Call(ArrayList<Atom> arguments){
        rawCommand = arguments;
    }

    public ArrayList<Atom> getArgs(){
        return rawCommand;
    }

    //visitor 
    public String accept(CommandVisitor visitor, String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer) throws RuntimeException {
        return visitor.visit(this, currentDirectory, bufferedReader, writer);
    }

}
