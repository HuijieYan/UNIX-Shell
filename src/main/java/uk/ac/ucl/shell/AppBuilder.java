package uk.ac.ucl.shell;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class AppBuilder {
    
    private String appName;
    private String currentDirectory;
    private OutputStreamWriter writer;
    private OutputStream output;

    public AppBuilder(String appName, String currentDirectory, OutputStreamWriter writer, OutputStream output) {
        this.appName = appName;
        this.currentDirectory = currentDirectory;
        this.writer = writer;
        this.output = output;
    }

    public ShellApplication createApp() {
        AppFactory myFactory = new AppFactory(this.appName, this.currentDirectory, this.writer);
        ShellApplication myApp = myFactory.makeApp();
        return myApp;
    }

    public static class AppBuilderNested {
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
        public AppBuilder createBuilder() {
            return new AppBuilder(
                nestedAppName,
                nestedDirectory,
                nestedWriter,
                nestedOutput
            );
        }
    }


}
