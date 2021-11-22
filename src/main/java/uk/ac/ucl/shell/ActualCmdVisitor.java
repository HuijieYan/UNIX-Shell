package uk.ac.ucl.shell;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import uk.ac.ucl.shell.Applications.Tools;
import uk.ac.ucl.shell.Parser.ParserBuilder;
import uk.ac.ucl.shell.Parser.pack.command.Call;
import uk.ac.ucl.shell.Parser.pack.command.Command;
import uk.ac.ucl.shell.Parser.pack.command.Pipe;
import uk.ac.ucl.shell.Parser.pack.type.atom.*;

public class ActualCmdVisitor implements CommandVisitor {
    private ParserBuilder parserBuilder = new ParserBuilder();

    public String visit(Call myCall, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws RuntimeException {
        currentDirectory = this.eval(myCall,currentDirectory, bufferedReader, output);
        return currentDirectory;
    }

    private String eval(Call myCall, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws RuntimeException {
        //writer should be given rather than OutputStream
        OutputStreamWriter writer = new OutputStreamWriter(output);

        ArrayList<Atom> cmdArgs = myCall.getArgs();
        for (int argIndex = 0; argIndex < cmdArgs.size(); argIndex++){
            Atom evaluatedArg = evalArg(cmdArgs.get(argIndex));

            if(!evaluatedArg.isRedirectionSymbol() && ((NonRedirectionString)evaluatedArg).canBeGlob()){
                ArrayList<String> globbingResult = globbingHelper(evaluatedArg);
                if(argIndex > 0 && cmdArgs.get(argIndex - 1) instanceof RedirectionSymbol && globbingResult.size() != 1){
                    StringBuilder target = new StringBuilder();
                    for(String str : cmdArgs.get(argIndex).get()){
                        target.append(str);
                    }
                    throw new RuntimeException("Error : ambiguous redirect argument: " + target.toString());
                }
                evaluatedArg = new NonRedirectionString(globbingResult);
            }
            cmdArgs.set(argIndex, evaluatedArg);
        }

        ArrayList<String> inputAndOutputFile = ShellUtil.checkRedirection(cmdArgs);
        if(inputAndOutputFile.get(0) != null){
            try {
                bufferedReader = Files.newBufferedReader(Tools.getPath(currentDirectory, inputAndOutputFile.get(0)), StandardCharsets.UTF_8);
            }catch (IOException e){
                throw new RuntimeException("can not open the input redirection file: " + inputAndOutputFile.get(0));
            }
        }

        ArrayList<String> finalArgs = new ArrayList<>();
        for(Atom arg : cmdArgs){
            finalArgs.addAll(arg.get());
        }

        String appName = finalArgs.get(0);
        OutputStream bufferedStream = new ByteArrayOutputStream();
        OutputStreamWriter innerWriter = new OutputStreamWriter(bufferedStream);
        ShellApplication myApp = new AppBuilder(appName, currentDirectory, bufferedReader, innerWriter).createApp();
        currentDirectory = myApp.exec(new ArrayList<>(finalArgs.subList(1, finalArgs.size())));


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

    private ArrayList<String> globbingHelper(Atom arg){
        ArrayList<String> result = new ArrayList<>();
        for(String str : arg.get()){
            result.add(str);
        }
        return result;
    }

    private Atom evalArg(Atom arg) throws RuntimeException {
        if (arg.isRedirectionSymbol()){
            return arg;
        }

        boolean canBeGlob = false;
        ArrayList<String> argumentStrings = arg.get();
        StringBuilder builder = new StringBuilder();
        for (String str : argumentStrings){
            if (str.charAt(0) == '\"' || str.charAt(0) == '`'){
                canBeGlob = commandSubstitution(str,builder);
            }else{
                if (str.charAt(0) == '\''){
                    str = removeQuote(str);
                }else if(str.contains("*")){
                    canBeGlob = true;
                }
                builder.append(str);
            }
        }

        String[] subArgs = builder.toString().split(" ");
        NonRedirectionString args = new NonRedirectionString(new ArrayList<>(Arrays.asList(subArgs)));
        if(canBeGlob){
            args.setCanBeGlob(true);
        }
        return args;
    }//> echo echo 123

    private String removeQuote(String str){
        return str.substring(1,str.length()-1);
    }

    private boolean commandSubstitution(String str, StringBuilder builder) throws RuntimeException {
        ArrayList<String> contentList = new ArrayList<>();
        boolean canBeGlob = false;
        if (str.charAt(0) == '`'){
            canBeGlob = true;
            contentList.add(str);
        }else{
            str = removeQuote(str);
            contentList = parserBuilder.decodeDoubleQuoted().parse(str).getValue();
            //"abc`echo 123`def" becomes [abc,`echo 123 234`,def]
        }
        
        for (String content : contentList){
            if (content.charAt(0) != '`'){
                builder.append(str);
            }else{
                content = removeQuote(content);
                ByteArrayOutputStream subStream = new ByteArrayOutputStream();
                Shell.eval(content, subStream);
                builder.append(subStream.toString().replaceAll("\r\n|\r|\n|\t", " "));
            }
        }

        return canBeGlob;
    }

    public String visit(Pipe myPipe, String currentDirectory, BufferedReader bufferedReader, OutputStream output) throws RuntimeException {
        ByteArrayOutputStream subStream;
        subStream = new ByteArrayOutputStream();
        ArrayList<Command> parsedArgs = myPipe.getCommands();

        //iterate size - 2 times
        for (Command curCmd:parsedArgs) {
            if (bufferedReader == null) {
                currentDirectory = curCmd.accept(this, currentDirectory, bufferedReader, subStream);
                bufferedReader = new BufferedReader(new StringReader(subStream.toString()));
            } else {
                bufferedReader = new BufferedReader(new StringReader(subStream.toString()));
                subStream.reset();
                if (parsedArgs.lastIndexOf(curCmd) == parsedArgs.size()-1) {
                    currentDirectory = curCmd.accept(this, currentDirectory, bufferedReader, output);
                } else {
                    currentDirectory = curCmd.accept(this, currentDirectory, bufferedReader, subStream);
                }
            }
        }

        return currentDirectory;
    }
    
}
