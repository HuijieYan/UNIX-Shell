package uk.ac.ucl.shell.Applications;

import java.io.BufferedReader;
import java.io.OutputStreamWriter;

public class AppFactory {

    private final String appName;
    private final String currentDirectory;
    private final BufferedReader reader;
    private final OutputStreamWriter writer;

    /**
     * Constructor function of AppFactory class
     * @param appName name of the application
     * @param currentDirectory currentDirectory of the Shell
     * @param reader Source of reading content
     * @param writer Destination of writing content
     */
    public AppFactory(String appName, String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.appName = appName;
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * This application make the actual app object and return it.
     * if appName start with "_" then make the unsafeApplication
     * @return myApp //Application object 
     * @throws RuntimeException The exception is thrown due to following reasons:
     * appName + ": unknown application" // When appName is not valid (app not exist)
     */
    public ShellApplication makeApp() throws RuntimeException {
        ShellApplication myApp;
        if(appName.startsWith("_")){
            myApp = new UnsafeDecorator(chooseApp(appName.substring(1)));
        }else {
            myApp = chooseApp(appName);
        }
        return myApp;
    }

    /*
     * helper function which make the application object depending on appName
     * @param appName application name. 
     */
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