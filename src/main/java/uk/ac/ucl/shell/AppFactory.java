package uk.ac.ucl.shell;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

import uk.ac.ucl.shell.Applications.*;

public class AppFactory {

    private String appName;
    private String currentDirectory;
    private OutputStreamWriter writer;

    public AppFactory(String appName, String currentDirectory, OutputStreamWriter writer) {
        this.appName = appName;
        this.currentDirectory = currentDirectory;
        this.writer = writer;
    }

    public ShellApplication makeApp() {

        ShellApplication myApp;

        switch (appName) {
        case "cd":
            myApp = new Cd(currentDirectory);
            break;
        case "pwd":
            myApp = new Pwd(writer, currentDirectory);
            break;
        case "ls":
            myApp = new Ls(writer, currentDirectory);
            break;
        case "cat":
            myApp = new Cat(writer, currentDirectory);
            break;
        case "echo":
            myApp = new Echo(writer, currentDirectory);
            break;
        case "head":
            myApp = new Head(writer, currentDirectory);
            break;
        case "tail":
            myApp = new Tail(writer, currentDirectory);
            break;
        case "grep":
            myApp = new Grep(writer, currentDirectory);
            break;
        default:
            throw new RuntimeException(appName + ": unknown application");
        }

        return myApp;
    }

}