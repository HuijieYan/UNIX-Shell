package uk.ac.ucl.parser;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Assert.*;
import uk.ac.ucl.shell.ShellParser;

import java.util.ArrayList;
import java.util.Random;


/**
 * In this test, we will only test parsers in ShellParser.java file.
 * To be more specific, we will only test parsers that parses following
 * things(specified in Language.md):
 * command, call, seq, pipe, quoted, single-quoted, backquoted, double-quoted,
 * atom, argument and redirection.
 */
public class ParserTest {
    private ShellParser shellParser = new ShellParser();
    private ArrayList<Character> tab;
    public ParserTest(){
        tab = new ArrayList<>();
        tab.add('\t');
    }

    @Test
    public void testQuotedContentParserForSingleQuoted(){
        ArrayList<Character> singleQuotedExceptions = new ArrayList<>(tab);
        singleQuotedExceptions.add('\'');
        String[] testFailCases = {"","'","a'b","adac\n"};
        String[] testSuccessfulCases = {"abcd","a`b`c","abc abc\tabc","a\"b\"c","a`b`\"c\"d1234534!@#%","\"abc","`abc","`abc\""};
        for (String str:testSuccessfulCases){
            Assert.assertEquals(str,shellParser.quotedContent(singleQuotedExceptions).parse(str).getValue());
        }
        for (String str:testFailCases){
            Assert.assertNull(shellParser.quotedContent(singleQuotedExceptions).parse(str).getValue());
        }
    }

    @Test
    public void testSingleQuotedParser(){
        String[] testSuccessfulCases = {"'abcd'","'a`b`c'","'abc abc\tabc'","'a\"b\"c'","'a`b`\"c\"d1234534!@#%'","'\"abc'","'`abc'","'`abc\"'"};
        //testSuccessfulCases can be viewed as testing quoted content parser
        String[] testFailCases = {"abcsa","'avad","'avxs\"","asvs'","'asa`afx`\"avc\"abc","''","'''","'adac\n'"};
        String[] testSpecialFailCases = {"'a'b'"};
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
}
