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
        if(appArgs.size() > 0){
            for(int index = 0; index < appArgs.size(); index++){
                writer.write(appArgs.get(index));
                if(index != appArgs.size() - 1){
                    writer.write(" ");
                }
                writer.flush();
            }
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        }else {
            throw new RuntimeException("Echo application should at least has one argument");
        }

        return currentDirectory;
    }
    
}
