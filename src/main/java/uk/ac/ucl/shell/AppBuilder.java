package uk.ac.ucl.shell;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;

public class AppBuilder {
    
    private final String appName;
    private final String currentDirectory;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;

    /**
     * Constructor function of AppBuilder class
     * @param appName name of the application
     * @param currentDirectory currentDirectory of the Shell
     * @param reader Source of reading content
     * @param writer Destination of writing content
     */
    public AppBuilder(String appName, String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.appName = appName;
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * createApp function creates the actual application object required.
     * @return application object required
     * @throws RuntimeException
     */
    public ShellApplication createApp() throws RuntimeException {
        return new AppFactory(this.appName, this.currentDirectory,this.reader, this.writer).makeApp();
    }

}
