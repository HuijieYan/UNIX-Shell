package uk.ac.ucl.shell.Parser.pack.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import uk.ac.ucl.shell.AppBuilder;
import uk.ac.ucl.shell.CommandVisitor;
import uk.ac.ucl.shell.ShellApplication;

public class Call implements Command {
    private ArrayList<String> rawCommand;
    private ArrayList<Command> selfList = new ArrayList<>();;
    public Call(ArrayList<String> arguments){
        rawCommand = arguments;
        selfList.add(this);
    }

    public ArrayList<Command> getCommands(){
        return selfList;
    }

    public ArrayList<String> getArgs(){
        return rawCommand;
    }

    public String eval(String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer, OutputStream output) throws IOException {
 
        String appName = this.getArgs().get(0);
        // tokens contain <app name> <arguments> where <arguments> is a list of argument
        ArrayList<String> appArgs = new ArrayList<String>(this.getArgs().subList(1, this.getArgs().size()));

        System.out.print("Current arg -> ");
        for (String curArg : appArgs) {
            System.out.print(curArg + " ");
        }
        System.out.println("");

        //check subcommand
        //appArgs = ShellUtil.checkSubCmd(appArgs);
        //check globbing
        //appArgs = ShellUtil.globbingChecker(appArgs, currentDirectory);

        //change stream
        //ShellApplication myApp = new AppBuilder(appName, currentDirectory, writer, output).createApp();
        // keep track of directory
        //currentDirectory = myApp.exec(appArgs);
        
        ShellApplication myApp = new AppBuilder(appName, currentDirectory, bufferedReader, writer).createApp();

        // keep track of directory
        currentDirectory = myApp.exec(appArgs);

        return currentDirectory;
    }

    //visitor 
    public String accept(CommandVisitor visitor, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws IOException {
        return visitor.visit(this, currentDirectory, bufferedReader, output);
    }

}
