package uk.ac.ucl.shell.Applications;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;

public class Echo implements ShellApplication {

    private OutputStreamWriter writer;
    private String currentDirectory;

    public Echo(OutputStreamWriter writer, String currentDirectory) {
        this.writer = writer;
        this.currentDirectory = currentDirectory;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        boolean atLeastOnePrinted = false;
        for (String arg : appArgs) {
            writer.write(arg);
            writer.write(" ");
            writer.flush();
            atLeastOnePrinted = true;
        }
        if (atLeastOnePrinted) {
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        }        

        return currentDirectory;
    }
    
}
