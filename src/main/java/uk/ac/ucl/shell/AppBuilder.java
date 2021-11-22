package uk.ac.ucl.shell;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;

public class AppBuilder {
    
    private String appName;
    private String currentDirectory;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    public AppBuilder(String appName, String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.appName = appName;
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    public ShellApplication createApp() {
        return new AppFactory(this.appName, this.currentDirectory,this.reader, this.writer).makeApp();
    }

}
