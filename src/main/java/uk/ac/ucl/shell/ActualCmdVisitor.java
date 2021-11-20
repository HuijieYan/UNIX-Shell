package uk.ac.ucl.shell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import uk.ac.ucl.shell.Parser.ParserBuilder;
import uk.ac.ucl.shell.Parser.pack.command.Call;
import uk.ac.ucl.shell.Parser.pack.command.Pipe;
import uk.ac.ucl.shell.Parser.pack.type.atom.*;

public class ActualCmdVisitor implements CommandVisitor {
    private ParserBuilder parserBuilder = new ParserBuilder();

    public String visit(Call myCall, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws IOException {
        
        OutputStreamWriter writer = new OutputStreamWriter(output);

        currentDirectory = myCall.eval(currentDirectory, bufferedReader, writer, output);
        return currentDirectory;
    }

    private String eval(Call myCall, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws IOException {
        ArrayList<Atom> cmdArgs = myCall.getArgs();
        ArrayList<Atom> evaluatedArgs = new ArrayList<>();
        for (Atom atom:cmdArgs){
            evaluatedArgs.addAll(evalArg(atom));
        }
        myCall = new Call(evaluatedArgs);
    }

    private ArrayList<Atom> evalArg(Atom arg){
        ArrayList<Atom> result = new ArrayList<>();
        if (arg.isRedirectionSymbol()){
            result.add(arg);
            return result;
        }

        ArrayList<String> argumentStrings = arg.get();
        ArrayList<String> sortedArgStrings = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        for (String str:argumentStrings){
            if (str.charAt(0) == '\"'||str.charAt(0) == '`'){
                commandSubstitution(str,builder,sortedArgStrings);
            }else{
                if (str.charAt(0) == '\''){
                    str = removeQuote(str);
                }
                builder.append(str);
            }
        }

        for (String argumentCotent:sortedArgStrings){
            ArrayList<String> ls = new ArrayList<>();
            ls.add(argumentCotent);
            Atom atom = new NonRedirectionString(ls);
            result.add(atom);
        }
        return result;
    }//> echo echo 123

    private String removeQuote(String str){
        return str.substring(1,str.length()-1);
    }

    private void commandSubstitution(String str,StringBuilder builder,ArrayList<String> result){
        ArrayList<String> contentList = new ArrayList<>();
        if (str.charAt(0) == '`'){
            contentList.add(str);
        }else{
            str = removeQuote(str);
            contentList = parserBuilder.decodeDoubleQuoted().parse(str).getValue();
            //"abc`echo 123`def" becomes [abc,`echo 123 234`,def]
        }
        
        for (String content:contentList){
            if (content.charAt(0) != '`'){
                builder.append(str);
            }else{
                content = removeQuote(content);
                //content is now shell command inside the backquote
                //execute(content)
            }
        }
        //executes instruction
    }

    public String visit(Pipe myPipe, String currentDirectory, BufferedReader bufferedReader, OutputStream output)
            throws IOException {

        OutputStreamWriter writer = new OutputStreamWriter(output);

        currentDirectory = myPipe.eval(currentDirectory, bufferedReader, writer, output);
        return currentDirectory;
    }
    
}
