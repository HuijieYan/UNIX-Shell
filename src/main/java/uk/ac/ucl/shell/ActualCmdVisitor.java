package uk.ac.ucl.shell;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import uk.ac.ucl.shell.Parser.ParserBuilder;
import uk.ac.ucl.shell.Parser.pack.command.Call;
import uk.ac.ucl.shell.Parser.pack.command.Command;
import uk.ac.ucl.shell.Parser.pack.command.Pipe;
import uk.ac.ucl.shell.Parser.pack.type.atom.*;

public class ActualCmdVisitor implements CommandVisitor {
    private ParserBuilder parserBuilder = new ParserBuilder();

    public String visit(Call myCall, String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer) throws RuntimeException {
        ArrayList<Atom> cmdArgs = myCall.getArgs();
        this.eval(cmdArgs,currentDirectory);
        ArrayList<String> inputAndOutputFile = this.checkRedirection(cmdArgs);
        if(inputAndOutputFile.get(0) != null){
            try {
                bufferedReader = Files.newBufferedReader(ShellUtil.getPath(currentDirectory, inputAndOutputFile.get(0)), StandardCharsets.UTF_8);
            }catch (IOException e){
                throw new RuntimeException("can not open the input redirection file: " + inputAndOutputFile.get(0));
            }
        }

        ArrayList<String> callArgs = new ArrayList<>();
        for(Atom arg : cmdArgs){
            callArgs.addAll(arg.get());
        }

        String appName = callArgs.get(0);
        ByteArrayOutputStream bufferedStream = new ByteArrayOutputStream();
        OutputStreamWriter innerWriter = new OutputStreamWriter(bufferedStream);
        ShellApplication myApp = new AppBuilder(appName, currentDirectory, bufferedReader, innerWriter).createApp();
        currentDirectory = myApp.exec(new ArrayList<>(callArgs.subList(1, callArgs.size())));


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

    private ArrayList<String> checkRedirection(ArrayList<Atom> cmdArgs) throws RuntimeException {
        ArrayList<String> inputAndOutputFile = new ArrayList<>();
        inputAndOutputFile.add(null);
        inputAndOutputFile.add(null);

        boolean hasSeveralInput = false;
        boolean hasSeveralOutput = false;
        for(int argIndex = 0; argIndex < cmdArgs.size();){
            Atom arg = cmdArgs.get(argIndex);
            if(arg instanceof RedirectionSymbol){
                if(((RedirectionSymbol)arg).isTowardsNext()){
                    if(!hasSeveralInput){
                        if(argIndex + 1 < cmdArgs.size()){
                            inputAndOutputFile.set(1, cmdArgs.get(argIndex + 1).get().get(0));
                            cmdArgs.remove(argIndex);
                            cmdArgs.remove(argIndex);
                        }else {
                            throw new RuntimeException("Error: no file is specified for input");
                        }
                        hasSeveralInput = true;
                    }else {
                        throw new RuntimeException("Error: several files are specified for input");
                    }

                } else {
                    if(!hasSeveralOutput){
                        if(argIndex + 1 < cmdArgs.size()){
                            inputAndOutputFile.set(0, cmdArgs.get(argIndex + 1).get().get(0));
                            cmdArgs.remove(argIndex);
                            cmdArgs.remove(argIndex);
                        }else {
                            throw new RuntimeException("Error: no file is specified for output");
                        }
                        hasSeveralOutput = true;
                    }else {
                        throw new RuntimeException("Error: several files are specified for output");
                    }

                }
            }else {
                argIndex++;
            }
        }
        return inputAndOutputFile;
    }

    private void eval(ArrayList<Atom> cmdArgs, String currentDirectory) throws RuntimeException {
        for (int argIndex = 0; argIndex < cmdArgs.size(); argIndex++){
            Atom evaluatedArg = this.evalArg(cmdArgs.get(argIndex), currentDirectory);

            if(!evaluatedArg.isRedirectionSymbol() && ((NonRedirectionString)evaluatedArg).canBeGlob()){
                ArrayList<String> globbingResult = this.globbingHelper(evaluatedArg, currentDirectory);
                if(argIndex > 0 && cmdArgs.get(argIndex - 1) instanceof RedirectionSymbol && globbingResult.size() != 1){
                    StringBuilder target = new StringBuilder();
                    cmdArgs.get(argIndex).get().forEach(target::append);
                    throw new RuntimeException("Error : ambiguous redirect argument: " + target.toString());
                }
                evaluatedArg = new NonRedirectionString(globbingResult);
            }
            cmdArgs.set(argIndex, evaluatedArg);
        }
    }

    private ArrayList<String> globbingHelper(Atom arg, String currentDirectory){
        ArrayList<String> result = new ArrayList<>();
        for(String str : arg.get()){
            if(str.contains("*")){
                //result.addAll(doGlob(str, currentDirectory));
            }else {
                result.add(str);
            }
        }
        return new ArrayList<>(arg.get());
    }

    private Atom evalArg(Atom arg, String currentDirectory) throws RuntimeException {
        if (arg.isRedirectionSymbol()){
            return arg;
        }

        boolean canBeGlob = false;
        ArrayList<String> argumentStrings = arg.get();
        ArrayList<StringBuilder> builders = new ArrayList<>();
        builders.add(new StringBuilder());
        for (String str : argumentStrings){
            if (str.charAt(0) == '\"' || str.charAt(0) == '`'){
                canBeGlob = this.commandSubstitution(str, builders, currentDirectory);
            }else{
                if (str.charAt(0) == '\''){
                    str = removeQuote(str);
                }else if(str.contains("*")){
                    canBeGlob = true;
                }
                builders.get(builders.size() - 1).append(str);
            }
        }

        ArrayList<String> subArgs = new ArrayList<>();
        for(StringBuilder builder : builders){
            subArgs.add(builder.toString());
        }
        NonRedirectionString args = new NonRedirectionString(subArgs);
        if(canBeGlob){
            args.setCanBeGlob(true);
        }
        return args;
    }//> echo echo 123

    private String removeQuote(String str){
        return str.substring(1,str.length()-1);
    }

    private boolean commandSubstitution(String str, ArrayList<StringBuilder> builders, String currentDirectory) throws RuntimeException {
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
                builders.get(builders.size() - 1).append(content);
            }else{
                ByteArrayOutputStream subStream = new ByteArrayOutputStream();
                OutputStreamWriter subStreamWriter = new OutputStreamWriter(subStream);
                Shell.eval(removeQuote(content), subStreamWriter, currentDirectory);
                String[] resultArgs = subStream.toString().replaceAll("\r\n|\r|\n|\t", " ").split(" ");
                for(int argIndex = 0; argIndex < resultArgs.length; argIndex++){
                    builders.get(builders.size() - 1).append(resultArgs[argIndex]);
                    if(argIndex != resultArgs.length - 1){
                        builders.add(new StringBuilder());
                    }
                }
            }
        }

        return canBeGlob;
    }

    public String visit(Pipe myPipe, String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer) throws RuntimeException {
        ByteArrayOutputStream subStream = new ByteArrayOutputStream();
        OutputStreamWriter subStreamWriter = new OutputStreamWriter(subStream);
        ArrayList<Command> calls = myPipe.getCommands();

        //iterate size - 2 times
        for(int callIndex = 0; callIndex < calls.size(); callIndex++){
            if(callIndex > 0){
                bufferedReader = new BufferedReader(new StringReader(subStream.toString()));
                subStream.reset();
            }
            if(callIndex == calls.size() - 1){
                currentDirectory = calls.get(callIndex).accept(this, currentDirectory, bufferedReader, writer);
            }else {
                currentDirectory = calls.get(callIndex).accept(this, currentDirectory, bufferedReader, subStreamWriter);
            }
        }
        return currentDirectory;
    }
}
