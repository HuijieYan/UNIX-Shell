package uk.ac.ucl.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import uk.ac.ucl.shell.Parser.pack.command.Call;
import uk.ac.ucl.shell.Parser.pack.command.Pipe;

public class ActualCmdVisitor implements CommandVisitor {

    public String visit(Call myCall, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws IOException {
        
        OutputStreamWriter writer = new OutputStreamWriter(output);

        currentDirectory = myCall.eval(currentDirectory, bufferedReader, writer, output);
        return currentDirectory;
    }

    public String visit(Pipe myPipe, String currentDirectory, BufferedReader bufferedReader, OutputStream output)
            throws IOException {

        OutputStreamWriter writer = new OutputStreamWriter(output);

        currentDirectory = myPipe.eval(currentDirectory, bufferedReader, writer, output);
        return currentDirectory;
    }
    
}
