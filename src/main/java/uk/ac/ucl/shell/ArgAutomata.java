package uk.ac.ucl.shell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ArgAutomata {

    private StringBuilder myBuilder;
    private String arg;
    private String curDir;
    private int curIndex;
    private char[] argCharArray;

    //flag
    private boolean argGlobbing = true;

    public ArgAutomata(String arg, String currentDirectory) {
        this.myBuilder = new StringBuilder();
        this.arg = arg;
        this.argCharArray = arg.toCharArray();
        this.curDir = currentDirectory;
        this.curIndex = 0;
    }

    /*
    <quoted> ::= <single-quoted> | <double-quoted> | <backquoted>
    <single-quoted> ::= "'" <non-newline and non-single-quote> "'"
    <backquoted> ::= "`" <non-newline and non-backquote> "`"
    <double-quoted> ::= """ ( <backquoted> | <double-quote-content> ) * """
    */
    public String go() {

        for (; curIndex < argCharArray.length; curIndex++) {

            char curChar = argCharArray[curIndex];
            if (curChar == '\'') {
                inSingleQuote();
            } else if (curChar == '"') {
                inDoubleQuote();
            } else {
                //inUnQuote();
                if (curChar == '`') {
                    inBackQuoted();
                    continue;
                } else {
                    myBuilder.append(curChar);
                }
            }
        }

        //do globbing
        // String result = myBuilder.toString();
        // if (argGlobbing == true && arg.contains("*")) {
        //     result = ShellUtil.globbingFunc(result, curDir);
        // }

        String result = myBuilder.toString();

        return result;
    }

    //disabled subCmd (intepritation)
    private void inSingleQuote() {
        //since paseed parser, there must be at least a pair of quotes
        curIndex += 1;
        char curChar = argCharArray[curIndex];
        while (curChar != '\'') {

            //check globbing
            if (curChar == '*') {
                argGlobbing = false;
            }

            if (curChar != '\n') {
                myBuilder.append(curChar);
                curIndex++;
            }
            curChar = argCharArray[curIndex];
        }  
    }

    private void inDoubleQuote() {
        //since paseed parser, there must be at least a pair of quotes
        curIndex += 1;
        char curChar = argCharArray[curIndex];
        while (curChar != '"') {

            //check globbing
            if (curChar == '*') {
                argGlobbing = false;
            }

            if (curChar == '`') {
                inBackQuoted();
                curIndex++;
            } else {
                if (curChar != '\n') {
                    myBuilder.append(curChar);
                    curIndex++;                    
                }                
            }
            curChar = argCharArray[curIndex];
        }    
    }

    // do subCmd
    private void inBackQuoted() {
        curIndex += 1;
        char curChar = argCharArray[curIndex];
        StringBuilder subCmdBuilder = new StringBuilder();

        while (curChar != '`') { 
            if (curChar != '\n') {
                subCmdBuilder.append(curChar);
                curIndex++;                    
            }                
            curChar = argCharArray[curIndex];
        }     
        String subCmd = subCmdBuilder.toString();

        //start subCMd
        ByteArrayOutputStream subStream = new ByteArrayOutputStream();
        Shell.eval(subCmd, subStream);
        //check exception
        String resultStr = subStream.toString();
        //tidy string since subShell has newLine at the end
        //System.out.println("resultStr = "+resultStr + " last index of result str-> " + matches.lastIndexOf(resultStr) + " matches size -> "+ (matches.size()-1));
        resultStr = resultStr.replace(System.getProperty("line.separator"), " ");
        //delete last space
        resultStr = resultStr.substring(0, resultStr.length()-1);

        myBuilder.append(resultStr);
    }



    // private void inUnQuote() {
    //     //since paseed parser, there must be at least a pair of quotes
    //     curIndex += 1;
    //     char curChar = argCharArray[curIndex];
    //     while (curIndex < argCharArray.length) {
    //         if (curChar != '\n') {
    //             if (curChar == '`') {
    //                 inBackQuoted();
    //                 curIndex++;
    //             if (curChar != '*') {
    //                 myBuilder.append(curChar);
    //                 curIndex++;
    //             } else {
    //                 argGlobbing = true;
    //                 curIndex++;
    //             }
    //         }
    //         curChar = argCharArray[curIndex];
    //     }  
    // }


}
