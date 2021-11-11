package uk.ac.ucl.shell;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import uk.ac.ucl.shell.Parser.pack.command.Call;
import uk.ac.ucl.shell.Parser.pack.command.Pipe;

public class ActualCmdVisitor implements CommandVisitor {

    public String visit(Call myCall, String currentDirectory, OutputStream output) throws IOException {
        

        currentDirectory = myCall.eval(currentDirectory, output);
        return currentDirectory;
    }

    public String visit(Pipe myPipe, String currentDirectory, OutputStream output)
            throws IOException {
        currentDirectory = myPipe.eval(currentDirectory,output);
        return currentDirectory;
    }
    
}
