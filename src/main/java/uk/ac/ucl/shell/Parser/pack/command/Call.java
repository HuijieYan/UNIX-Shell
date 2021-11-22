package uk.ac.ucl.shell.Parser.pack.command;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import uk.ac.ucl.shell.AppBuilder;
import uk.ac.ucl.shell.ArgAutomata;
import uk.ac.ucl.shell.CommandVisitor;
import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;
import uk.ac.ucl.shell.Applications.Tools;
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
    public String accept(CommandVisitor visitor, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws RuntimeException {
        return visitor.visit(this, currentDirectory, bufferedReader, output);
    }

}
