package uk.ac.ucl.shell;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;

import uk.ac.ucl.shell.Parser.pack.command.Call;
import uk.ac.ucl.shell.Parser.pack.command.Pipe;

public interface CommandVisitor {
    String visit(Call myCall, String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer) throws RuntimeException;
    String visit(Pipe myPipe, String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer) throws RuntimeException;
}