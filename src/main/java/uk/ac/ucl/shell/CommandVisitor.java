package uk.ac.ucl.shell;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import uk.ac.ucl.shell.Parser.pack.command.Call;
import uk.ac.ucl.shell.Parser.pack.command.Pipe;

public interface CommandVisitor {
    public String visit(Call myCall, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws IOException;
    public String visit(Pipe myPipe, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws IOException;
}