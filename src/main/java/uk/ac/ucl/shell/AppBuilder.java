package uk.ac.ucl.shell;

import java.io.BufferedReader;
import java.io.OutputStream;
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

    /*public static class AppBuilderNested {
        private String nestedAppName;
        private String nestedDirectory;
        private OutputStreamWriter nestedWriter;
        private OutputStream nestedOutput;

        public AppBuilderNested(String appName) {
            this.nestedAppName = appName;
        }

        public AppBuilderNested currentDirectory(String curDirectory) {
            this.nestedDirectory = curDirectory;
            return this;
        }

        public AppBuilderNested writer(OutputStreamWriter writer) {
            this.nestedWriter = writer;
            return this;
        }

        public AppBuilderNested output(OutputStream output) {
            this.nestedOutput = output;
            return this;
        }

        //build final appBuilder
    }*/


}
