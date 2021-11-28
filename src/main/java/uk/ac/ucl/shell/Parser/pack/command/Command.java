package uk.ac.ucl.shell.Parser.pack.command;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import uk.ac.ucl.shell.CommandVisitor;

public interface Command {
    //visitor
    String accept(CommandVisitor visitor, String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer) throws RuntimeException;
}
