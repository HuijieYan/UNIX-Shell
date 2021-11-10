package uk.ac.ucl.shell.Applications;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;

public class Echo implements ShellApplication {
    private String currentDirectory;
    private OutputStreamWriter writer;

    public Echo(String currentDirectory, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws IOException {
        if(appArgs.size() > 0){
            try {
                for(int index = 0; index < appArgs.size(); index++){
                    writer.write(appArgs.get(index));
                    if(index != appArgs.size() - 1){
                        writer.write(" ");
                    }
                }
                writer.write(System.getProperty("line.separator"));
                writer.flush();
            }catch (IOException e){
                throw new RuntimeException("Echo: fail to print the arguments");
            }
        }else {
            throw new RuntimeException("Echo: application should at least has one argument");
        }
        return currentDirectory;
    }
    
}
