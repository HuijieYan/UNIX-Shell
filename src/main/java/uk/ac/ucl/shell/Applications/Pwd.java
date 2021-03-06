package uk.ac.ucl.shell.Applications;

import java.io.OutputStreamWriter;
import java.util.List;

public class Pwd implements ShellApplication {
    private final String currentDirectory;
    private final OutputStreamWriter writer;

    /**
     * Constructor of Pwd application
     * @param currentDirectory currentDirectory of the Shell
     * @param writer Destination of writing content
     */
    public Pwd(String currentDirectory, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.writer = writer;
    }

    /**
     * exec function of "Pwd" application.
     * The function writes the current shell directory into writer.
     * @param appArgs list of application arguments stored in List<String>
     * @return currentDirectory This is not used in this function (variable exists here because of the requirement from interface)
     * @throws RuntimeException The exception is thrown due to following reasons:
     * - "Pwd: there can not be argument" // When appArgs is greater than 0
     * - "Pwd: fail to write to the output" // When IOException is caught from writer
     */
    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if(appArgs.size() > 0){
            throw new RuntimeException("Pwd: there can not be argument");
        }
        try {
            writer.write(currentDirectory);
            writer.write(System.getProperty("line.separator"));
            writer.flush();
        }catch (Exception e){
            //catch Exception for writer is null or fail to write
            throw new RuntimeException("Pwd: fail to write to the output");
        }
        return currentDirectory;
    }
}
