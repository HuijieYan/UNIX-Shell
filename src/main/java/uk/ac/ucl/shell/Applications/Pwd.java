package uk.ac.ucl.shell.Applications;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;

public class Pwd implements ShellApplication {

    private OutputStreamWriter writer;
    String currentDirectory;

    public Pwd(OutputStreamWriter writer, String currentDirectory) {
        this.writer = writer;
        this.currentDirectory = currentDirectory;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        writer.write(currentDirectory);
        writer.write(System.getProperty("line.separator"));
        writer.flush();
        return currentDirectory;
    }
}
