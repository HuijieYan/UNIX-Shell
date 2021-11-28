package uk.ac.ucl.shell;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;

import uk.ac.ucl.shell.Applications.*;

public class AppFactory {

    private String appName;
    private String currentDirectory;
    private  BufferedReader reader;
    private OutputStreamWriter writer;

    public AppFactory(String appName, String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.appName = appName;
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    public ShellApplication makeApp() throws RuntimeException {
        ShellApplication myApp;
        if(appName.startsWith("_")){
            myApp = new UnsafeDecorator(chooseApp(appName.substring(1)));
        }else {
            myApp = chooseApp(appName);
        }
        return myApp;
    }

    private ShellApplication chooseApp(String appName){
        ShellApplication myApp;

        switch (appName) {
            case "pwd":
                myApp = new Pwd(currentDirectory, writer);
                break;
            case "cd":
                myApp = new Cd(currentDirectory);
                break;
            case "ls":
                myApp = new Ls(currentDirectory, writer);
                break;
            case "cat":
                myApp = new Cat(currentDirectory, reader, writer);
                break;
            case "echo":
                myApp = new Echo(currentDirectory, writer);
                break;
            case "head":
                myApp = new Head(currentDirectory, reader, writer);
                break;
            case "tail":
                myApp = new Tail(currentDirectory, reader, writer);
                break;
            case "grep":
                myApp = new Grep(currentDirectory, reader, writer);
                break;
            case "cut":
                myApp = new Cut(currentDirectory, reader, writer);
                break;
            case "find":
                myApp = new Find(currentDirectory, writer);
                break;
            case "uniq":
                myApp = new Uniq(currentDirectory, reader, writer);
                break;
            case "sort":
                myApp = new Sort(currentDirectory, reader, writer);
                break;
            default:
                throw new RuntimeException("unknown application");
        }

        return myApp;
    }

}