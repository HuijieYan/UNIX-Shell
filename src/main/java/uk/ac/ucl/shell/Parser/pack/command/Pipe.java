package uk.ac.ucl.shell.Parser.pack.command;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import uk.ac.ucl.shell.CommandVisitor;
import uk.ac.ucl.shell.Shell_newParser;

public class Pipe implements Command {
    private ArrayList<Command> parsedArgs;
    
    // for eval function
    private CommandVisitor myVisitor;

    public Pipe(ArrayList<Command> call, ArrayList<Command> listOfCalls){
        parsedArgs = call;
        parsedArgs.addAll(listOfCalls);
    }

    public Pipe(ArrayList<Command> parsedArgs){    
        this.parsedArgs = parsedArgs;
    }

    public ArrayList<Command> getCommands(){
        return parsedArgs;
    }

    //visitor
    public String accept(CommandVisitor visitor, String currentDirectory, OutputStream output) throws IOException {
        this.myVisitor = visitor;
        return visitor.visit(this, currentDirectory, output);
    }

    //tbd
    public String eval(String currentDirectory, OutputStream output) throws IOException {
        
        ByteArrayOutputStream subStream;
        subStream = new ByteArrayOutputStream();

        for (Command curCmd : parsedArgs) {

                if (subStream.toString().isEmpty()) {
                    currentDirectory = curCmd.accept(this.myVisitor, currentDirectory, subStream);

                } else {
                    currentDirectory = curCmd.accept(this.myVisitor, currentDirectory, subStream);
                    //clear stream
                    subStream.reset();
                }
                
        }

        //last command
        Command lastCmd = parsedArgs.get(parsedArgs.size()-1);
        currentDirectory = lastCmd.accept(this.myVisitor, currentDirectory, output);

        
        return currentDirectory;
 
       //return currentDirectory;

    }


}
