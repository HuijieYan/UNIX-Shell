package uk.ac.ucl.shell.Applications;

import java.io.OutputStreamWriter;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;

public class Echo implements ShellApplication {
    private final String currentDirectory;
    private final OutputStreamWriter writer;

    /**
     * Constructor of Cat application
     * @param currentDirectory currentDirectory of the Shell
     * @param writer Destination of writing content
     */
    public Echo(String currentDirectory, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.writer = writer;
    }

    /**
     * exec function of "Echo" application.
     * This Application writes the arguments from user into writer(destination).
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - "Echo: application should at least has one argument" // if argument size is less than 1
     * - "Echo: fail to print the arguments" // if an IOException is caught from writer object
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if(appArgs.size() < 1){
            throw new RuntimeException("Echo: application should at least has one argument");
        }

        try {
            for(int index = 0; index < appArgs.size(); index++){
                writer.write(appArgs.get(index));
                if(index != appArgs.size() - 1){
                    writer.write(" ");
                }
            }
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        }catch (Exception e){
            throw new RuntimeException("Echo: fail to print the arguments");
        }
        return currentDirectory;
    }
    
}
