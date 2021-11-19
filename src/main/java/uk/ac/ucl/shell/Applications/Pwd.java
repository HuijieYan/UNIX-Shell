package uk.ac.ucl.shell.Applications;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;

public class Pwd implements ShellApplication {
    private String currentDirectory;
    private OutputStreamWriter writer;

    public Pwd(String currentDirectory, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if(appArgs.size() > 0){
            throw new RuntimeException("pwd: there can not be argument");
        }
        try {
            writer.write(currentDirectory);
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        }catch (IOException e){
            throw new RuntimeException("pwd: fail to write to the output");
        }
        return currentDirectory;
    }
}
