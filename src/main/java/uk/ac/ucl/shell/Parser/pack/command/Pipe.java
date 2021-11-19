package uk.ac.ucl.shell.Parser.pack.command;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;

import uk.ac.ucl.shell.CommandVisitor;

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

    //visitorCall 
    public String accept(CommandVisitor visitor, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws IOException {
        this.myVisitor = visitor;
        return visitor.visit(this, currentDirectory, bufferedReader, output);
    }

    //tbd
    public String eval(String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer, OutputStream output) throws IOException {
        
        ByteArrayOutputStream subStream;
        subStream = new ByteArrayOutputStream();

        //iterate size - 2 times
        for (Command curCmd:parsedArgs) {
            if (bufferedReader == null) {
                currentDirectory = curCmd.accept(this.myVisitor, currentDirectory, bufferedReader, subStream);
                bufferedReader = new BufferedReader(new StringReader(subStream.toString()));
            } else {
                bufferedReader = new BufferedReader(new StringReader(subStream.toString()));
                subStream.reset();
                if (parsedArgs.lastIndexOf(curCmd) == parsedArgs.size()-1) {
                    currentDirectory = curCmd.accept(this.myVisitor, currentDirectory, bufferedReader, output);
                } else {
                    currentDirectory = curCmd.accept(this.myVisitor, currentDirectory, bufferedReader, subStream);
                }
            }
        }

        return currentDirectory;
 
    }


}
