package uk.ac.ucl.parser;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Assert.*;
import uk.ac.ucl.shell.Parser.pack.command.Call;
import uk.ac.ucl.shell.Parser.pack.command.Command;
import uk.ac.ucl.shell.Parser.pack.command.Pipe;
import uk.ac.ucl.shell.Parser.pack.type.atom.Atom;
import uk.ac.ucl.shell.Parser.pack.type.atom.NonRedirectionString;
import uk.ac.ucl.shell.Parser.pack.type.atom.RedirectionSymbol;
import uk.ac.ucl.shell.ShellParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


/**
 * In this test, we will only test parsers in ShellParser.java file.
 * To be more specific, we will only test parsers that parses following
 * things(specified in Language.md):
 * command, call, seq, pipe, quoted, single-quoted, backquoted, double-quoted,
 * unquoted, atom, argument and redirection.
 */
public class ParserTest {
    private ShellParser shellParser = new ShellParser();
<<<<<<< HEAD
    private ArrayList<Character> tab;
    public ParserTest(){
        tab = new ArrayList<>();
        tab.add('\n');
    }

    @Test
    public void testQuotedContentParserForSingleQuoted(){
        ArrayList<Character> singleQuotedExceptions = new ArrayList<>(tab);
        singleQuotedExceptions.add('\'');
        String[] testFailCases = {"","'","\n"};
        String[] testSuccessfulCases = {"abcd","a`b`c","abc abc\tabc","a\"b\"c","a`b`\"c\"d1234534!@#%","\"abc","`abc","`abc\""};
        for (String str:testSuccessfulCases){
            Assert.assertEquals(str,shellParser.quotedContent(singleQuotedExceptions).parse(str).getValue());
        }
        for (String str:testFailCases){
            Assert.assertNull(shellParser.quotedContent(singleQuotedExceptions).parse(str).getValue());
        }
    }
=======
>>>>>>> 562045746b19a584bc39574937f885ae98460e21

    @Test
    public void testSingleQuotedParser(){
        String[] testSuccessfulCases = {"'abcd'","'a`b`c'","'abc abc \t abc'","'a\"b\"c'","'a`b`\"c\"d1234534!@#%'","'\"abc'","'`abc'","'`abc\"'"};
        //testSuccessfulCases can be viewed as testing quoted content parser
        String[] testFailCases = {"abcsa","'avad","'avxs\"","asvs'","'asa`afx`\"avc\"abc","''","'''","'adac\nabc'","\"abc\"","`asv`",""};
        String[] testSpecialFailCases = {"'a'b'","'asv'acs'abc'"};
        //for input such as 'a'b', the parser would only successfully parse 'a' and leaves b'
        //this will also be considered as parse failed in our shell

        for (String str:testSuccessfulCases){
            Assert.assertEquals(str,shellParser.singleQuoted().parse(str).getValue());
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.singleQuoted().parse(str).getValue());
        }
        for(String str:testSpecialFailCases){
            Assert.assertTrue(shellParser.singleQuoted().parse(str).getInputStream().length()>0);
        }
    }

    @Test
    public void testBackQuotedParser(){
        String[] testSuccessfulCases = {"`abcd`","`a'b'c`","`abc abc \t abc`","`a\"b\"c`","`a'b'\"c\"d1234534!@#%`","`\"abc`","`'abc`","`'abc\"`"};
        //testSuccessfulCases can be viewed as testing quoted content parser
        String[] testFailCases = {"abcsa","`avad","`avxs\"","\"abc`","asvs`","`asa'afx'\"avc\"abc","``","```","`adac\nabc`","","\"abc\"","'asv'"};
        String[] testSpecialFailCases = {"`a`b`"};
        //for string `a`b`, reference to the explanation in testSingleQuotedParser()

        for (String str:testSuccessfulCases){
            Assert.assertEquals(str,shellParser.backQuoted().parse(str).getValue());
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.backQuoted().parse(str).getValue());
        }
        for(String str:testSpecialFailCases){
            Assert.assertTrue(shellParser.backQuoted().parse(str).getInputStream().length()>0);
        }
    }

    @Test
    public void testDoubleQuotedParser(){
        String[] testSuccessfulCases = {"\"\"","\"abc\"","\"123`abc`def'abc'`'def'\"g\"`\"","\"abc \t def\""};
        //because back quoted strings will be tested separately , we are not including tests related to parsing back quoted string
        String[] testFailCases = {"abcsa","\"avad","\"avxs'","`abc\"","asvs\"","\"asa'afx'`avc`abc`\"","\"adac\nabc\"","'abc'","`asv`"};
        String[] testSpecialFailCases = {"\"a\"b\"","\"\"\""};
        //for string "a"b", reference to the explanation in testSingleQuotedParser()
        //for string """, the first two " will be viewed as a double-quoted string leaving one " symbol left

        for (String str:testSuccessfulCases){
            Assert.assertEquals(str,shellParser.doubleQuoted().parse(str).getValue());
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.doubleQuoted().parse(str).getValue());
        }
        for(String str:testSpecialFailCases){
            Assert.assertTrue(shellParser.doubleQuoted().parse(str).getInputStream().length()>0);
        }
    }

    @Test
    public void testQuotedParser(){
        String[] testSuccessfulCases = {"`backquoted`","'singlequoted'","\"doublequoted\"","\"doublequoted`backquoted`doublequoted\""};
        //Since double-quoted, single-quoted and back-quoted parsers are tested beforehand, we assume these strings are already valid
        String[] testFailCases = {"unquoted","unquoted\"","'unquoted","unquoted\"doublequoted\"","","<tag>","@abc@","\\slashquoted\\"};
        //strings such as "abc' are already tested in parsing different quoted string
        String[] testSpecialFailCases = {"\"doublequoted\"'singlequoted'`backquoted`"};
        //for "doublequoted"'singlequoted'`backquoted`, only "doublequoted" will be parsed since a quoted parser only parse one quoted string
        for (String str:testSuccessfulCases){
            Assert.assertEquals(str,shellParser.quoted().parse(str).getValue());
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.quoted().parse(str).getValue());
        }
        for(String str:testSpecialFailCases){
            Assert.assertTrue(shellParser.quoted().parse(str).getInputStream().length()>0);
        }
    }

    @Test
    public void testUnquotedParser(){
        String[] testSuccessfulCases = {"unquoted","@abc@"};
        //Since double-quoted, single-quoted and back-quoted parsers are tested beforehand, we assume these strings are already valid
        String[] testFailCases = {"'singlequoted'","`backquoted`","\"doublequoted\"","|abc",";abc",">abc","<abc"," unquoted","\tunquoted"};
        String[] testSpecialFailCases = {"unquoted\tunquoted","unquoted>unquoted","unquoted<unquoted","unquoted;unquoted",
                "unquoted\nunquoted","unquoted|unquoted","unquoted unquoted","unquoted'unquoted", "unquoted`unquoted",
                "unquoted\"unquoted"};
        //the parser will stop after parsing the first "unquoted"

        for (String str:testSuccessfulCases){
            Assert.assertEquals(str,shellParser.unQuoted().parse(str).getValue());
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.unQuoted().parse(str).getValue());
        }
        for(String str:testSpecialFailCases){
            Assert.assertTrue(shellParser.unQuoted().parse(str).getInputStream().length()>0);
        }
    }

    @Test
    public void testArgumentParser(){
        String[] testSuccessfulCases = {"unquoted","\"doublequoted\"","'singlequoted'","`backquoted`","unquoted\"doublequoted\"'singlequoted'`backquoted`unquoted"};
        //Since unquoted, double-quoted, single-quoted and back-quoted parsers are tested beforehand, we assume these strings are already valid
        String[][] successfulCasesResult = {{"unquoted"},{"\"doublequoted\""},{"'singlequoted'"},{"`backquoted`"},{"unquoted","\"doublequoted\"","'singlequoted'",
                "`backquoted`","unquoted"}};
        String[] testFailCases = {"","\n"};
        //most failing tests are covered in unquoted and quoted parser tests
        String[] testSpecialFailCases = {"unquoted\tunquoted","unquoted>unquoted","unquoted<unquoted","unquoted;unquoted",
                "unquoted\nunquoted","unquoted|unquoted","unquoted unquoted"};
        for (int i =0;i<testSuccessfulCases.length;i++){
            String str = testSuccessfulCases[i];
            ArrayList<String> result = arrayToList(successfulCasesResult[i]);
            Assert.assertEquals(result,getResultList(shellParser.parseArgument().parse(str).getValue()));
            //we cannot assert whether two atoms with same strings inside are the same, therefore we assert
            //whether the strings stored inside the atom are the same or not
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.parseArgument().parse(str).getValue());
        }
        for(String str:testSpecialFailCases){
            Assert.assertTrue(shellParser.parseArgument().parse(str).getInputStream().length()>0);
        }
    }

    @Test
    public void testRedirectionParser(){
        int index = 0;
        String[] testSuccessfulCases = {">argument","<argument","> argument","< argument",">\targument","<\targument","> \t argument","< \t argument"};
        //Since argument parsers has been tested beforehand, we assume all argument strings are already valid
        String[][] successfulCasesResult = {{">","argument"},{"<","argument"}};
        //all cases above must have one of the following result
        String[] testFailCases = {"",">\nargument","<\nargument","\n",">","<","> \t ","< \t "};
        //most failing tests are covered in argument parser tests
        //special cases are also covered in argument since you can only stop parsing
        //once the argument stops parsing, otherwise the parsing will fail

        for (String str:testSuccessfulCases){
            ArrayList<String> expectResult = arrayToList(successfulCasesResult[index]);
            ArrayList<String> result = getResultList(shellParser.redirection().parse(str).getValue());

            Assert.assertEquals(expectResult,result);

            index = getIndex(index);
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.redirection().parse(str).getValue());
        }
    }

    @Test
    public void testAtomParser(){
        String[] testSuccessfulCases = {"> argument","< argument","unquotedArgument","\"doublequoted\"","'singlequoted'"};
        //Since redirection and argument parsers have been tested beforehand, we assume all redirection and argument strings are already valid
        String[][] successfulCasesResult = {{">","argument"},{"<","argument"},{"unquotedArgument"},{"\"doublequoted\""},{"'singlequoted'"}};
        String[] testFailCases = {"","\n"};
        //most failing tests are covered in argument and redirection parser tests
        //special cases are also covered in argument or redirection parser tests since you can only stop parsing
        //once one of these parsers stops parsing, otherwise the parsing will fail

        for (int i =0;i<testSuccessfulCases.length;i++){
            String str = testSuccessfulCases[i];
            ArrayList<String> expectResult = arrayToList(successfulCasesResult[i]);
            ArrayList<String> result = getResultList(shellParser.atom().parse(str).getValue());

            Assert.assertEquals(expectResult,result);
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.atom().parse(str).getValue());
        }
    }

    @Test
    public void testCallParser(){
        testCallParserSuccessfulCases();
        testCallParserFailedCases();
    }

    private void testCallParserSuccessfulCases(){
        String[] testSuccessfulCasesNoRedirection = {"oneArgument","multiple arguments \"a1\" `a2` 'a3' \"a4`a5`a6\"",
                "argument1\targument2"," \t argument1 \t argument2 \t \n"};
        //a call with newline at the very end of the string would success because there is nothing left in the rest of input stream
        String[][] successfulCasesNoRedirectionResult = {{"oneArgument"},{"multiple","arguments","\"a1\"","`a2`","'a3'","\"a4`a5`a6\""},
                {"argument1","argument2"},{"argument1","argument2"}};
        String[] testSuccessfulCasesWithRedirection = {"> a1 argument","< a2 multiple arguments \"a1\" `a2` 'a3' \"a4`a5`a6\"",
                "< a1 argument1\targument2 > a2 <a3",">\ta1 \t argument1 \t argument2 \t >\ta2\t<a3","< a1 argument1 argument2 > a2 argument3 < a3",};
        String[][] successfulCasesWithRedirectionResult = {{">","a1","argument"},{"<","a2","multiple","arguments","\"a1\"",
        "`a2`","'a3'","\"a4`a5`a6\""},{ "<","a1","argument1","argument2",">","a2","<","a3"},{ ">","a1","argument1","argument2",">","a2","<","a3"},
                { "<","a1","argument1","argument2",">","a2","argument3","<","a3"}};

        for (int i =0;i<testSuccessfulCasesNoRedirection.length;i++){
            String str = testSuccessfulCasesNoRedirection[i];
            ArrayList<String> expectResult = arrayToList(successfulCasesNoRedirectionResult[i]);
            ArrayList<String> result = getResultListFromCommand(shellParser.call().parse(str).getValue());
            //we cannot assert whether two lists of command objects with same strings inside are the same, therefore we assert
            //whether the strings stored inside the command are the same or not

            Assert.assertEquals(expectResult,result);
        }
        for (int i =0;i<testSuccessfulCasesWithRedirection.length;i++){
            String str = testSuccessfulCasesWithRedirection[i];
            ArrayList<String> expectResult = arrayToList(successfulCasesWithRedirectionResult[i]);
            ArrayList<String> result = getResultListFromCommand(shellParser.call().parse(str).getValue());

            Assert.assertEquals(expectResult,result);
        }
    }

    private void testCallParserFailedCases(){
        String[] testFailCases = {"","\n","\nabc",">redirection1>redirection2 argument","> redirection","<",">",
                "argument >redirection1>redirection2","> redirection1 \n > redirection2 argument1\targument2 > redirection3"};
        //failing cases covered by the argument, atom and redirection parser testing will not be covered
        String[] testSpecialFailCases = {"argument1\nargument2","argument1\targument2\n> redirection",
                "argument1\targument2 > redirection1 \n > redirection2"};
        //these are special case because the parser would only take string before newline and parse them
        for(String str:testFailCases){
            Assert.assertNull(shellParser.call().parse(str).getValue());
        }
        for(String str:testSpecialFailCases){
            Assert.assertTrue(shellParser.call().parse(str).getInputStream().length()>0);
        }
    }

    @Test
    public void testPipeParser(){
        String[] testSuccessfulCases = {"command1|command2|command3"," \t > a1 command1 > a2 \t | \t > a3 command2 > a4 arg1 > a5 \t | \t > a6 command3 arg2 \t"};
        //Since call parser has been tested beforehand, we assume all call commands are already valid
        String[][] successfulCasesResult = {{"command1","command2","command3"},{">","a1","command1",">","a2",">","a3","command2",">","a4","arg1",">","a5",
                ">","a6","command3","arg2"}};
        String[] testFailCases = {"","\n","command1|","|command1","command","command1\n|command2"};
        //most failing tests are covered in call parser tests
        String[] testSpecialFailCases = {"command1|command2\n|command3"};
        //Because the parser would only parse string before the newline symbol


        for (int i =0;i<testSuccessfulCases.length;i++){
            String str = testSuccessfulCases[i];
            ArrayList<String> expectResult = arrayToList(successfulCasesResult[i]);
            ArrayList<String> result = getResultListFromCommand(shellParser.pipe().parse(str).getValue());
            //we cannot assert whether two lists of command objects with same strings inside are the same, therefore we assert
            //whether the strings stored inside the command are the same or not

            Assert.assertEquals(expectResult,result);
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.pipe().parse(str).getValue());
        }
        for(String str:testSpecialFailCases){
            Assert.assertTrue(shellParser.pipe().parse(str).getInputStream().length()>0);
        }
    }

    @Test
    public void testSeqParser(){
        String[] testSuccessfulCases = {"command1;command2;command3"," \t > a1 command1 > a2 \t | \t > a3 command2 > a4 arg1 > a5 \t ; \t > a6 command3 arg2 \t ; command4|command5|command6 \t\t "};
        //Since call and pipe parsers have been tested beforehand, we assume all pipe and call commands are already valid
        String[][] successfulCasesResult = {{"command1","command2","command3"},{">","a1","command1",">","a2",">","a3","command2",">","a4","arg1",">","a5",
                ">","a6","command3","arg2","command4","command5","command6"}};
        String[] testFailCases = {"","\n","command1;",";command1","command1|command2","command1\n;command2"};
        //most failing tests are covered in call parser tests
        String[] testSpecialFailCases = {"command1;command2\n;command3"};
        //Because the parser would only parse string before the newline symbol


        for (int i =0;i<testSuccessfulCases.length;i++){
            String str = testSuccessfulCases[i];
            ArrayList<String> expectResult = arrayToList(successfulCasesResult[i]);
            ArrayList<String> result = getResultListFromCommand(shellParser.seq().parse(str).getValue());
            //we cannot assert whether two lists of command objects with same strings inside are the same, therefore we assert
            //whether the strings stored inside the command are the same or not

            Assert.assertEquals(expectResult,result);
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.seq().parse(str).getValue());
        }
        for(String str:testSpecialFailCases){
            Assert.assertTrue(shellParser.seq().parse(str).getInputStream().length()>0);
        }
    }

    @Test
    public void testCommandParser(){
        String[] testSuccessfulCases = {"command","command|command|command","command;command;command",
                "\t > a1 < a2 arg1 > a3 arg2 < a4 \t"," \t command \t "};
        //Since seq,pipe and call parsers have been tested beforehand, we assume these strings are already valid
        String[][] successfulCasesResult = {{"command"},{"command","command","command"},{"command","command","command"},
                {">","a1","<","a2","arg1",">","a3","arg2","<","a4"},{"command"}};
        String[] testFailCases = {"","\n","\ncommand"};
        //most failing tests are covered in seq,pipe and call parser tests
        //special cases are also covered in seq,pipe and call parser tests since you can only stop parsing
        //once one of these parsers stops parsing, otherwise the parsing will fail

        for (int i =0;i<testSuccessfulCases.length;i++){
            String str = testSuccessfulCases[i];
            ArrayList<String> expectResult = arrayToList(successfulCasesResult[i]);
            ArrayList<String> result = getResultListFromCommand(shellParser.parseCommand().parse(str).getValue());

            Assert.assertEquals(expectResult,result);
        }
        for(String str:testFailCases){
            Assert.assertNull(shellParser.parseCommand().parse(str).getValue());
        }
    }

    private int getIndex(int index){
        if (index>0){
            index = 0;
        }else{
            index = 1;
        }
        return index;
    }

    private ArrayList<String> arrayToList(String[] list){
        return new ArrayList(Arrays.asList(list));
    }

    private ArrayList<String> getResultListFromCommand(ArrayList<Command> commands){
        ArrayList<String> result = new ArrayList<>();
        for (Command command:commands){
            if (command instanceof Call){
                result.addAll(getResultList(((Call) command).getArgs()));
            }else if (command instanceof Pipe){
                result.addAll(getResultListFromCommand(command.getCommands()));
            }
        }

        return result;
    }

    private ArrayList<String> getResultList(ArrayList<Atom> atoms){
        ArrayList<String> result = new ArrayList<>();
        for (Atom atom:atoms){
            result.addAll(atom.get());
        }

        return result;
    }

    private ArrayList<String> getResultList(Atom atom){
        ArrayList<String> result = new ArrayList<>();
        result.addAll(atom.get());

        return result;
    }
}
