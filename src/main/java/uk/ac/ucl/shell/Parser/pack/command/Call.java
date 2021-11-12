package uk.ac.ucl.shell.Parser.pack.command;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import uk.ac.ucl.shell.AppBuilder;
import uk.ac.ucl.shell.CommandVisitor;
import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;
import uk.ac.ucl.shell.Applications.Tools;

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
 
        
        ArrayList<String> cmdArgs = this.getArgs();
        String appName = cmdArgs.get(0);

        //check subcommand (layer3)
        //only do check when content not in single quote
        cmdArgs = ShellUtil.checkSubCmd(cmdArgs);
        appName = cmdArgs.get(0);

        //debug
        //System.out.println("App name -> " + appName);
        // tokens contain <app name> <arguments> where <arguments> is a list of argument
        ArrayList<String> appArgs = new ArrayList<String>(cmdArgs.subList(1, cmdArgs.size()));

        //check redirection
        ArrayList<String> inputAndOutputFile = ShellUtil.checkRedirection(cmdArgs);
        if(inputAndOutputFile.get(0) != null){
            try {
                bufferedReader = Files.newBufferedReader(Tools.getPath(currentDirectory, inputAndOutputFile.get(0)), StandardCharsets.UTF_8);
            }catch (IOException e){
                throw new RuntimeException("can not open the input redirection file: " + inputAndOutputFile.get(0));
            }
        }

        //check globbing
        cmdArgs = ShellUtil.globbingChecker(cmdArgs, currentDirectory);
        appName = cmdArgs.get(0);
        appArgs = new ArrayList<String>(cmdArgs.subList(1, cmdArgs.size()));

        //extract stuff inside quotes
        cmdArgs = ShellUtil.processSingleQuotes(cmdArgs);
        cmdArgs = ShellUtil.processDoubleQuotes(cmdArgs);
        appName = cmdArgs.get(0);
        appArgs = new ArrayList<String>(cmdArgs.subList(1, cmdArgs.size()));


        OutputStream bufferedStream = new ByteArrayOutputStream();
        OutputStreamWriter innerWriter = new OutputStreamWriter(bufferedStream);

        ShellApplication myApp = new AppBuilder(appName, currentDirectory, bufferedReader, innerWriter).createApp();

        // keep track of directory
        currentDirectory = myApp.exec(appArgs);

        //redirection part
        if(inputAndOutputFile.get(1) != null) {
            try {
                FileWriter outputFile = new FileWriter(inputAndOutputFile.get(1));
                outputFile.write(bufferedStream.toString());
                outputFile.flush();
                outputFile.close();
            }catch (IOException e){
                throw new RuntimeException("fail to write to the output redirection file: " + inputAndOutputFile.get(1));
            }
        
        } else {
            try {
                writer.write(bufferedStream.toString());
                writer.flush();
            }catch (IOException e){
                throw new RuntimeException("fail to print to the shell command line");
            }
            
        }

        return currentDirectory;
    }

    //visitor 
    public String accept(CommandVisitor visitor, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws IOException {
        return visitor.visit(this, currentDirectory, bufferedReader, output);
    }

}
