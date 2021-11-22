package uk.ac.ucl.shell.Parser.pack.command;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.util.ArrayList;

import uk.ac.ucl.shell.CommandVisitor;

public interface Command {
    public ArrayList<Command> getCommands();

    //visitor
    public String accept(CommandVisitor visitor, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws RuntimeException;

}
