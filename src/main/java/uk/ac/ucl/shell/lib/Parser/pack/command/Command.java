package uk.ac.ucl.shell.lib.Parser.pack.command;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;

import uk.ac.ucl.shell.ShellUtils.CommandVisitor.CommandVisitor;

public interface Command {
    //visitor
    String accept(CommandVisitor visitor, String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer) throws RuntimeException;
}
