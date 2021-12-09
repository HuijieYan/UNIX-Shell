package uk.ac.ucl.shell;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import uk.ac.ucl.shell.Parser.pack.command.Call;
import uk.ac.ucl.shell.Parser.pack.command.Command;
import uk.ac.ucl.shell.Parser.pack.command.Pipe;
import uk.ac.ucl.shell.Parser.pack.type.atom.Atom;
import uk.ac.ucl.shell.Parser.pack.type.atom.NonRedirectionString;
import uk.ac.ucl.shell.Parser.pack.type.atom.RedirectionSymbol;

public class ActualCmdVisitor implements CommandVisitor {
    private final ShellParser shellParser = new ShellParser();

    /**
     * Main entry for Call
     * @param myCall Call object
     * @param currentDirectory CurrentDirectory of the shell
     * @param bufferedReader source of reading content
     * @param writer destination StreamWriter
     * @return currentDirectory of the shell (for tracking).
     * @throws RuntimeException
     */
    public String visit(Call myCall, String currentDirectory, BufferedReader bufferedReader, OutputStreamWriter writer) throws RuntimeException {
        ArrayList<Atom> cmdArgs = myCall.getArgs();
        this.eval(cmdArgs,currentDirectory);

        ArrayList<String> inputAndOutputFile = this.checkRedirection(cmdArgs);

        ArrayList<String> callArgs = new ArrayList<>();
        for(Atom arg : cmdArgs){
            callArgs.addAll(arg.get());
        }

        return this.execCall(callArgs, currentDirectory, bufferedReader, inputAndOutputFile, writer);
    }

    private String execCall(ArrayList<String> callArgs, String currentDirectory, BufferedReader bufferedReader,
                          ArrayList<String> inputAndOutputFile, OutputStreamWriter writer){
        if(inputAndOutputFile.get(0) != null){
            bufferedReader = doInputRedirection(currentDirectory, inputAndOutputFile.get(0));
        }

        ByteArrayOutputStream bufferedStream = new ByteArrayOutputStream();
        OutputStreamWriter innerWriter = new OutputStreamWriter(bufferedStream);
        ShellApplication myApp = new AppBuilder(callArgs.get(0), currentDirectory, bufferedReader, innerWriter).createApp();
        currentDirectory = myApp.exec(new ArrayList<>(callArgs.subList(1, callArgs.size())));

        if(inputAndOutputFile.get(1) != null) {
            doOutputRedirection(currentDirectory, inputAndOutputFile.get(1), bufferedStream);
        } else {
            try {
                writer.write(bufferedStream.toString());
                writer.flush();
            }catch (Exception ignored){}
        }
        return currentDirectory;
    }

    /**
     * Main entry for Pipe
     * @param myPipe pipe object
     * @param currentDirectory CurrentDirectory of the shell
     * @param bufferedReader source of reading content
     * @param writer destination StreamWriter
     * @return currentDirectory of the shell (for tracking).
     * @throws RuntimeException
     */
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

    /*
     * Utility function for writing content from bufferedStream into a file.
     * @param currentDirectory target directory of file
     * @fileName target filename to be written
     * @bufferedStream source stream that contains the content to be written into the file
     */
    private void doOutputRedirection(String currentDirectory, String fileName, ByteArrayOutputStream bufferedStream) {
        try {
            File file = new File(fileName);
            if(!file.isAbsolute()){
                file = new File(currentDirectory, fileName);
            }
            FileWriter outputFile = new FileWriter(file);
            outputFile.write(bufferedStream.toString());
            outputFile.flush();
            outputFile.close();
        }catch (IOException e){
            throw new RuntimeException("fail to write to the output redirection file: " + fileName);
        }
    }

    /*
     * Utility function for opening file into a bufferReader.
     * @param currentDirectory target directory of file
     * @fileName target filename to be written
     * @return BufferedReader that contains the content from inputFile
     */
    private BufferedReader doInputRedirection(String currentDirectory, String fileName) {
        BufferedReader bufferedReader;
        try {
            bufferedReader = Files.newBufferedReader(ShellUtil.getPath(currentDirectory, fileName), StandardCharsets.UTF_8);
        }catch (IOException e){
            throw new RuntimeException("can not open the input redirection file: " + fileName);
        }
        return bufferedReader;
    }

    /*
     * Function that checks redirection
     * @param cmdArgs List of arguments(Atom)
     * @return a list that contains information of inputFle/outputFile extracted from arguments
     * inputAndOutputFile -> [inputFileName, outputFileName]
     * @throws RuntimeException // "Error: several files are specified for output"
     */
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
                    if(!hasSeveralOutput){
                        hasSeveralOutput = saveTargetFile(cmdArgs, inputAndOutputFile, argIndex, 1);
                    }else {
                        throw new RuntimeException("Error: several files are specified for output");
                    }
                } else {
                    if(!hasSeveralInput){
                        hasSeveralInput = saveTargetFile(cmdArgs, inputAndOutputFile, argIndex, 0);
                    }else {
                        throw new RuntimeException("Error: several files are specified for input");
                    }
                }
            }else {argIndex++;}
        }
        return inputAndOutputFile;
    }

    private boolean saveTargetFile(ArrayList<Atom> cmdArgs, ArrayList<String> inputAndOutputFile, int argIndex, int position){
        inputAndOutputFile.set(position, cmdArgs.get(argIndex + 1).get().get(0));
        cmdArgs.remove(argIndex);
        cmdArgs.remove(argIndex);
        return true;
    }

    /*
     * evaluation (entry) function for evaluating arguments
     * @param cmdArgs List of arguments(Atom)
     * @param currentDirectory of the shell
     * @throws RuntimeException // Error : ambiguous redirect argument
     */
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


    /*
     * helper function to deal with globbing
     * @param arg Single argument(Atom) to do further globbing
     * @param currentDirectory current directory of the shell
     * @return List of results after globbing
     */
    private ArrayList<String> globbingHelper(Atom arg, String currentDirectory) {
        ArrayList<String> result = new ArrayList<>();

        for (String curString : arg.get()) {
            if (curString.contains("*")) {
                ArrayList<String> globbingResult = (ArrayList<String>)Globbing.exec(currentDirectory, curString);
                result.addAll(globbingResult);
            } else {
                result.add(curString);
            }
        }
        return result;
    }

    /*
     * Evaluation function for single argument(Atom)
     * @param arg Single argument(Atom) to do further globbing
     * @param currentDirectory current directory of the shell
     * @throws RuntimeException
     */
    private Atom evalArg(Atom arg, String currentDirectory) throws RuntimeException {
        if (arg.isRedirectionSymbol()){
            return arg;
        }

        boolean canBeGlob = false;
        ArrayList<String> argumentStrings = arg.get();
        ArrayList<StringBuilder> builders = new ArrayList<>(List.of(new StringBuilder()));
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
        return getFinalAtom(builders, canBeGlob);
    }

    //Get atom which has the list of fully executed and tidied string
    private Atom getFinalAtom(ArrayList<StringBuilder> builders, boolean canBeGlob){
        ArrayList<String> subArgs = new ArrayList<>();
        for(StringBuilder builder : builders){
            subArgs.add(builder.toString());
        }
        NonRedirectionString args = new NonRedirectionString(subArgs);
        args.setCanBeGlob(canBeGlob);
        return args;
    }

    /*
     * function removes quotes ("1234" -> 1234)
     */
    private String removeQuote(String str){
        return str.substring(1,str.length()-1);
    }

    /*
     * Function dealing with command substitution
     * @param str current argument
     * @param builders list of stringBuilders that contains arguments
     * @param currentDirectory current directory of the shell
     * @returns boolean // true if command can be globed, vise versa
     */
    private boolean commandSubstitution(String str, ArrayList<StringBuilder> builders, String currentDirectory) throws RuntimeException {
        ArrayList<String> contentList = new ArrayList<>();
        boolean canBeGlob = false;
        if (str.charAt(0) == '`'){
            canBeGlob = true;
            contentList.add(str);
        }else{
            contentList = shellParser.decodeDoubleQuoted(removeQuote(str)).getValue();
            //"abc`echo 123`def" becomes [abc,`echo 123 234`,def]
        }

        for (String content : contentList){
            if (content.charAt(0) != '`'){
                builders.get(builders.size() - 1).append(content);
            }else{
                this.doSubstitution(content, builders, currentDirectory);
            }
        }
        return canBeGlob;
    }

    private void doSubstitution(String content, ArrayList<StringBuilder> builders, String currentDirectory){
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
