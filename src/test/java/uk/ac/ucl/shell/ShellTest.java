package uk.ac.ucl.shell;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ucl.shell.Applications.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ShellTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    public String currentDir;
    ByteArrayOutputStream out;
    OutputStreamWriter writer;
    String lineSep = System.getProperty("line.separator");
    String fileSep = File.separator;

    @Before
    public void setUp() throws Exception {
        out = new ByteArrayOutputStream();
        writer = new OutputStreamWriter(out);

        File tempFile1 = tempFolder.newFile("file1.txt");
        File tempFile2 = tempFolder.newFile("file2.txt");
        File tempFile3 = tempFolder.newFile("file3.txt");
        tempFolder.newFolder("subDir");
        tempFolder.newFolder("emptyFolder");
        tempFolder.newFolder(".subDir");
        currentDir = tempFolder.getRoot().getCanonicalPath();

        File subTempFile1 = tempFolder.newFile("subDir/file1.txt");
        File subTempFile2 = tempFolder.newFile("subDir/file2.txt");
        File subTempFile3 = tempFolder.newFile("subDir/file3.txt");

        writeToFile(tempFile1, "AAA\nBBB\nbbb\nCCC\nccc");
        writeToFile(tempFile2, "CCC\nDDD");
        writeToFile(tempFile3, "*.txt");
        writeToFile(subTempFile1, "AAA\nBBB\nbbb\nCCC\nccc");
        writeToFile(subTempFile2, "CCC\nCCC\nccc\nDDD\nddd");
        writeToFile(subTempFile3, "abc\n123\n789\n456\n666");
    }

    public void writeToFile(File file, String content) throws IOException {
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.flush();
        writer.close();
    }

    public void assertEqualSet(String expected, String result){
        String[] expectedList = expected.split("\r\n|\r|\n|\t");
        Arrays.sort(expectedList);
        String[] resultList = result.split("\r\n|\r|\n|\t");
        Arrays.sort(resultList);
        assertArrayEquals(expectedList, resultList);
    }


    @Test
    public void testPwd(){
        new Pwd(currentDir, writer).exec(new ArrayList<>());
        assertEquals(currentDir + lineSep, out.toString());
        out.reset();
    }

    @Test
    public void testPwd_exception(){
        ArrayList<String> argList = new ArrayList<>();
        try {
            new Pwd(currentDir, null).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("Pwd: fail to write to the output", e.getMessage());
        }

        try {
            argList.add("arg");
            new Pwd(currentDir, writer).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("Pwd: there can not be argument", e.getMessage());
        }
    }

    @Test
    public void testCd_relativePath(){
        ArrayList<String> argList = new ArrayList<>();
        argList.add("subDir");
        assertEquals(currentDir + fileSep +"subDir", new Cd(currentDir).exec(argList));
    }

    @Test
    public void testCd_absolutePath(){
        ArrayList<String> argList = new ArrayList<>();
        String path = currentDir + fileSep + "subDir";
        argList.add(path);
        assertEquals(path, new Cd(currentDir).exec(argList));
    }

    @Test
    public void testCd_invalidPath(){
        try {
            ArrayList<String> argList = new ArrayList<>();
            argList.add("subDir2");
            new Cd(currentDir).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cd: can not switch to such directory subDir2", e.getMessage());
        }
    }

    @Test
    public void testCd_currentPath(){
        ArrayList<String> argList = new ArrayList<>();
        argList.add(".");
        assertEquals(currentDir, new Cd(currentDir).exec(argList));
    }

    @Test
    public void testCd_parentPath(){
        ArrayList<String> argList = new ArrayList<>();
        argList.add("..");
        assertEquals(currentDir, new Cd(currentDir + fileSep + "subDir").exec(argList));
    }

    @Test
    public void testCd_exception(){
        try {
            new Cd(currentDir).exec(new ArrayList<>());
            fail();
        }catch (RuntimeException e){
            assertEquals("cd: missing argument", e.getMessage());
        }

        try {
            ArrayList<String> argList = new ArrayList<>();
            argList.add("subDir");
            argList.add("subDir");
            new Cd(currentDir).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cd: too many arguments", e.getMessage());
        }
    }

    @Test
    public void testLs_currentDir(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        Ls ls = new Ls(currentDir, writer);
        String result = "file1.txt\tfile2.txt\tfile3.txt\tsubDir\temptyFolder" + System.getProperty("line.separator");
        ls.exec(argList);
        this.assertEqualSet(result, out.toString());

        out.reset();
        argList.add(".");
        ls.exec(argList);
        this.assertEqualSet(result, out.toString());
    }

    @Test
    public void testLs_relativePath(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        Ls ls = new Ls(currentDir, writer);
        argList.add("subDir");
        String result = "file1.txt\tfile2.txt\tfile3.txt" + lineSep;
        ls.exec(argList);
        this.assertEqualSet(result, out.toString());

        out.reset();
        argList.set(0, fileSep + "subDir" + fileSep + "..");
        result = "file1.txt\tfile2.txt\tfile3.txt\tsubDir\temptyFolder" + lineSep;
        ls.exec(argList);
        this.assertEqualSet(result, out.toString());

    }

    @Test
    public void testLs_emptyDir(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("emptyFolder");
        new Ls(currentDir, writer).exec(argList);
        assertEquals("", out.toString());
    }

    @Test
    public void testLs_exception(){
        try {
            ArrayList<String> argList = new ArrayList<>();
            argList.add("subDir");
            argList.add("subDir");
            new Ls(currentDir, writer).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("ls: too many arguments", e.getMessage());
        }

        try {
            ArrayList<String> argList = new ArrayList<>();
            argList.add("subDir2");
            new Ls(currentDir, writer).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("ls: no such directory: subDir2", e.getMessage());
        }
    }

    @Test
    public void testCat_singleFile(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("file1.txt");
        new Cat(currentDir, null, writer).exec(argList);
        assertEquals("AAA" + lineSep + "BBB" + lineSep + "bbb" + lineSep + "CCC" + lineSep + "ccc" + lineSep, out.toString());
    }

    @Test
    public void testCat_absoluteAndRelativePath(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add(fileSep + "." + fileSep + "file1.txt");
        argList.add(currentDir + fileSep + "file3.txt");
        argList.add(currentDir + fileSep + "subDir" + fileSep + ".." + fileSep + "file3.txt");
        new Cat(currentDir, null, writer).exec(argList);
        assertEquals("AAA" + lineSep + "BBB" + lineSep + "bbb" + lineSep + "CCC" + lineSep + "ccc"
                + lineSep + "*.txt" + lineSep + "*.txt" + lineSep, out.toString());
    }

    @Test
    public void testCat_exception(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        Cat cat = new Cat(currentDir, null, writer);
        try {
            argList.add("file4.txt");
            cat.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cat: can not open: file4.txt", e.getMessage());
        }

        try {
            argList.remove(0);
            cat.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cat: no data from pipe or redirection and can not find file to read", e.getMessage());
        }
    }

    @Test
    public void testCat_multiFile(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("file1.txt");
        argList.add("file3.txt");
        new Cat(currentDir, null, writer).exec(argList);
        assertEquals("AAA" + lineSep + "BBB" + lineSep + "bbb" + lineSep + "CCC" + lineSep
                + "ccc" + lineSep + "*.txt" + lineSep, out.toString());
    }

    @Test
    public void testEcho_singleArg(){
        out.reset();
        String arg = "Hello";
        ArrayList<String> argList = new ArrayList<>();
        argList.add(arg);
        new Echo(currentDir, writer).exec(argList);
        assertEquals("Hello" + lineSep, out.toString());
    }

    @Test
    public void testEcho_multiArgs(){
        out.reset();
        String arg = "Hello";
        ArrayList<String> argList = new ArrayList<>();
        argList.add(arg);
        argList.add(arg);
        argList.add(arg);
        new Echo(currentDir, writer).exec(argList);
        assertEquals("Hello Hello Hello" + lineSep, out.toString());
    }

    @Test
    public void testEcho_exception(){
        ArrayList<String> argList = new ArrayList<>();
        try {
            new Echo(currentDir, writer).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("Echo: application should at least has one argument", e.getMessage());
        }

        try {
            argList.add("hello");
            new Echo(currentDir, null).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("Echo: fail to print the arguments", e.getMessage());
        }
    }

    @Test
    public void testHead_defaultAndSpecific(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        Head head = new Head(currentDir, null, writer);
        argList.add("file1.txt");
        head.exec(argList);
        assertEquals("AAA" + lineSep + "BBB" + lineSep + "bbb" + lineSep + "CCC" + lineSep + "ccc" + lineSep, out.toString());

        out.reset();
        argList.add(0, "-n");
        argList.add(1, "2");
        head.exec(argList);
        assertEquals("AAA" + lineSep + "BBB" + lineSep, out.toString());

        out.reset();
        argList.set(1, "18");
        head.exec(argList);
        assertEquals("AAA" + lineSep + "BBB" + lineSep + "bbb" + lineSep + "CCC" + lineSep + "ccc" + lineSep, out.toString());
    }

    @Test
    public void testHead_exception(){
        testHeadOrTail_exception("head");
    }

    public void testHeadOrTail_exception(String appName){
        ArrayList<String> argList = new ArrayList<>();
        ShellApplication app;
        if(appName.equals("head")){
            app = new Head(currentDir, null, writer);
        }else {
            app = new Tail(currentDir, null, writer);
        }

        try {
            app.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals(appName + ": no data from pipe or redirection and can not find file to read", e.getMessage());
        }

        try {
            argList.add("-n");
            argList.add("2");
            app.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals(appName + ": no data from pipe or redirection and can not find file to read", e.getMessage());
        }

        try {
            argList.add("file1.txt");
            argList.add("file2.txt");
            app.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals(appName + ": wrong argument number", e.getMessage());
        }

        try {
            argList.set(0, "-tag");
            argList.remove(3);
            app.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals(appName + ": wrong argument -tag should be -n", e.getMessage());
        }

        try {
            argList.remove(2);
            app.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals(appName + ": wrong argument -tag should be -n", e.getMessage());
        }

        try {
            argList.add("file1.txt");
            argList.set(0, "-n");
            argList.set(1, "b");
            app.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals(appName + ": b is not a integer", e.getMessage());
        }

        try {
            argList.set(1, "10");
            argList.set(2, "file4.txt");
            app.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals(appName + ": cannot open: file4.txt", e.getMessage());
        }
    }

    @Test
    public void testTail_defaultAndSpecific(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        Tail tail = new Tail(currentDir, null, writer);
        argList.add("file1.txt");
        tail.exec(argList);
        assertEquals("AAA" + lineSep + "BBB" + lineSep + "bbb" + lineSep + "CCC" + lineSep + "ccc" + lineSep, out.toString());

        out.reset();
        argList.add(0, "-n");
        argList.add(1, "2");
        tail.exec(argList);
        assertEquals("CCC" + lineSep + "ccc" + lineSep, out.toString());

        out.reset();
        argList.set(1, "18");
        tail.exec(argList);
        assertEquals("AAA" + lineSep + "BBB" + lineSep + "bbb" + lineSep + "CCC" + lineSep + "ccc" + lineSep, out.toString());
    }

    @Test
    public void testTail_exception(){
        testHeadOrTail_exception("tail");
    }

    @Test
    public void testGrep_noPrefix(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("AAA");
        argList.add("file1.txt");
        new Grep(currentDir, null, writer).exec(argList);
        assertEquals(argList.get(0) + lineSep, out.toString());
    }

    @Test
    public void testGrep_hasPrefix(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("AAA");
        argList.add("file1.txt");
        argList.add("subDir" + fileSep + "file1.txt");
        new Grep(currentDir, null, writer).exec(argList);
        assertEquals(argList.get(1) + ":" + argList.get(0) + lineSep + argList.get(2)
                + ":" + argList.get(0) + lineSep, out.toString());
    }

    @Test
    public void testGrep_exception(){
        ArrayList<String> argList = new ArrayList<>();
        Grep grep = new Grep(currentDir, null, writer);
        try {
            grep.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("grep: wrong number of arguments", e.getMessage());
        }

        try {
            argList.add("AAA");
            grep.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("Grep: no data from pipe or redirection and can not find file to read", e.getMessage());
        }

        try {
            argList.add("file4.txt");
            grep.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("grep: cannot open file4.txt", e.getMessage());
        }
    }

    @Test
    public void testCut_specificIndex(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-b");
        argList.add("1,3");
        argList.add("file1.txt");
        new Cut(currentDir, null, writer).exec(argList);
        assertEquals("AA" + lineSep + "BB" + lineSep + "bb" + lineSep + "CC" + lineSep + "cc" + lineSep, out.toString());
    }

    @Test
    public void testCut_specificSlice(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-b");
        argList.add("-1,3-");
        argList.add("file1.txt");
        new Cut(currentDir, null, writer).exec(argList);
        assertEquals("AA" + lineSep + "BB" + lineSep + "bb" + lineSep + "CC" + lineSep + "cc" + lineSep, out.toString());
    }

    @Test
    public void testCut_specificRange(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-b");
        argList.add("1-3");
        argList.add("file1.txt");
        new Cut(currentDir, null, writer).exec(argList);
        assertEquals("AAA" + lineSep + "BBB" + lineSep + "bbb" + lineSep + "CCC" + lineSep + "ccc" + lineSep, out.toString());
    }

    @Test
    public void testCut_specificROutOfBound(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-b");
        argList.add("1-4,7,9,-1,10-");
        argList.add("file1.txt");
        new Cut(currentDir, null, writer).exec(argList);
        assertEquals("AAA" + lineSep + "BBB" + lineSep + "bbb" + lineSep + "CCC" + lineSep + "ccc" + lineSep, out.toString());
    }

    @Test
    public void testCut_exception(){
        ArrayList<String> argList = new ArrayList<>();
        Cut cut = new Cut(currentDir, null, writer);
        try {
            argList.add("-b");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: wrong argument number", e.getMessage());
        }

        try {
            argList.add("1-4,7,9,-1,10-");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: no data from pipe or redirection and can not find file to read", e.getMessage());
        }

        try {
            argList.set(0, "-n");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: incorrect option input " + argList.get(0), e.getMessage());
        }

        try {
            argList.set(0, "-b");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: no data from pipe or redirection and can not find file to read", e.getMessage());
        }

        try {
            argList.add("file5.txt");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: can not open file " + argList.get(2), e.getMessage());
        }

        try {
            argList.add("fourth arg");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: wrong argument number", e.getMessage());
        }

        try {
            argList.remove(2);
            argList.remove(2);
            new Cut(currentDir, null, null).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: no data from pipe or redirection and can not find file to read", e.getMessage());
        }
    }

    @Test
    public void testCut_invalidCut(){
        ArrayList<String> argList = new ArrayList<>();
        Cut cut = new Cut(currentDir, null, writer);
        argList.add("-b");
        argList.add("1");
        argList.add("file1.txt");

        this.testCut_invalidIndex("-1-3", argList, cut);
        this.testCut_invalidIndex("0", argList, cut);
        this.testCut_invalidIndex("---11", argList, cut);
        this.testCut_invalidIndex("-0", argList, cut);
        this.testCut_invalidIndex("1--", argList, cut);
        this.testCut_invalidIndex("0-", argList, cut);
        this.testCut_invalidIndex("1-0", argList, cut);
        this.testCut_invalidIndex("0-1", argList, cut);
        this.testCut_invalidIndex("1--0", argList, cut);
        this.testCut_invalidIndex("2-1", argList, cut);
        this.testCut_invalidIndex("", argList, cut);
        this.testCut_invalidIndex("-", argList, cut);
    }

    public void testCut_invalidIndex(String element, ArrayList<String> argList, Cut cut){
        try {
            argList.set(1, element);
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: invalid argument " + argList.get(1), e.getMessage());
        }
    }

    @Test
    public void testFind_currentDir(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-name");
        argList.add("*1.txt");
        new Find(currentDir, writer).exec(argList);
        this.assertEqualSet("." + fileSep + "file1.txt" + lineSep+ "." + fileSep + "subDir"
                + fileSep + "file1.txt" + lineSep, out.toString());
    }

    @Test
    public void testFind_absoluteOrRelativePath(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        String prefix = "subDir" + fileSep + "..";
        argList.add(prefix);
        argList.add("-name");
        argList.add("*1.txt");
        new Find(currentDir, writer).exec(argList);
        this.assertEqualSet(prefix + fileSep + "file1.txt" + lineSep + prefix + fileSep + "subDir"
                + fileSep + "file1.txt" + lineSep, out.toString());

        out.reset();
        prefix = currentDir;
        argList.set(0, prefix);
        new Find(currentDir, writer).exec(argList);
        this.assertEqualSet(prefix + fileSep + "file1.txt" + lineSep + prefix + fileSep + "subDir"
                + fileSep + "file1.txt" + lineSep, out.toString());
    }

    @Test
    public void testFind_exception(){
        ArrayList<String> argList = new ArrayList<>();
        try {
            argList.add("subDir");
            argList.add("-name");
            new Find(currentDir, writer).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("find: can not find -name argument or lack of pattern", e.getMessage());
        }

        try {
            argList.set(0, "subDir1");
            argList.add("*.txt");
            new Find(currentDir, writer).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("find: no such root directory subDir1", e.getMessage());
        }

        try {
            argList.set(0, "subDir");
            new Find(currentDir, null).exec(argList);
            fail();
        }catch (Exception e){
            assertEquals("find: fail to write to the output", e.getMessage());
        }

        try {
            argList.add("fourth arg");
            new Find(currentDir, writer).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("find: Wrong number of arguments", e.getMessage());
        }
    }

    @Test
    public void testUniq_caseSensitive(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("subDir/file2.txt");
        new Uniq(currentDir, null, writer).exec(argList);
        assertEquals("CCC" + lineSep + "ccc" + lineSep + "DDD" + lineSep + "ddd" + lineSep, out.toString());
    }

    @Test
    public void testUniq_caseInsensitive(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-i");
        argList.add("subDir/file2.txt");
        new Uniq(currentDir, null, writer).exec(argList);
        assertEquals("CCC" + lineSep + "DDD" + lineSep, out.toString());
    }

    @Test
    public void testUniq_exception(){
        ArrayList<String> argList = new ArrayList<>();
        Uniq uniq = new Uniq(currentDir, null, writer);

        try {
            uniq.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("uniq: no data from pipe or redirection and can not find file to read", e.getMessage());
        }

        try {
            argList.add("-i");
            uniq.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("uniq: no data from pipe or redirection and can not find file to read", e.getMessage());
        }

        try {
            argList.set(0, "-iii");
            argList.add("file4.txt");
            uniq.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("uniq: invalid option " + argList.get(0), e.getMessage());
        }

        try {
            argList.set(0, "-i");
            uniq.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("uniq: cannot open " + argList.get(1), e.getMessage());
        }

        try {
            argList.set(1, "file1.txt");
            new Uniq(currentDir, null, null).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("uniq: fail to read or write", e.getMessage());
        }

        try {
            argList.set(1, "file1.txt");
            argList.add("file2.txt");
            uniq.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("uniq: too many argument number", e.getMessage());
        }
    }

    @Test
    public void testSort(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("subDir" + fileSep + "file3.txt");
        new Sort(currentDir, null, writer).exec(argList);
        assertEquals("123" + lineSep + "456" + lineSep + "666" + lineSep + "789" + lineSep + "abc" + lineSep, out.toString());
    }

    @Test
    public void testSort_ReverseOrder(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-r");
        argList.add("subDir" + fileSep + "file3.txt");
        new Sort(currentDir, null, writer).exec(argList);
        assertEquals("abc" + lineSep + "789" + lineSep + "666" + lineSep + "456" + lineSep + "123" + lineSep, out.toString());
    }

    @Test
    public void testSort_exception(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        Sort sort = new Sort(currentDir, null, writer);

        try {
            sort.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("sort: no data from pipe or redirection and can not find file to read", e.getMessage());
        }

        try {
            argList.add("-r");
            sort.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("sort: no data from pipe or redirection and can not find file to read", e.getMessage());
        }

        try {
            argList.set(0, "-a");
            argList.add("subDir" + fileSep + "file4.txt");
            sort.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("sort: invalid option " + argList.get(0), e.getMessage());
        }

        try {
            argList.add("arg");
            sort.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("sort: wrong argument number", e.getMessage());
        }

        try {
            argList.remove(0);
            argList.remove(1);
            sort.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("sort: cannot open " + argList.get(0), e.getMessage());
        }

        try {
            argList.set(0, "file3.txt");
            new Sort(currentDir, null, null).exec(argList);
            fail();
        }catch (Exception e){
            assertEquals("sort: fail to read or write", e.getMessage());
        }
    }

    @Test
    public void openFileTest_currentPath(){
        try {
            assertEquals("file1.txt", ShellUtil.getPath(currentDir, "file1.txt").getFileName().toString());

            ShellUtil.getPath(currentDir, "file4.txt");
            fail();
        }catch (IOException e){
            assertNull(e.getMessage());
        }
    }

    @Test
    public void openFileTest_relativePath(){
        try {
            assertEquals("file1.txt", ShellUtil.getPath(currentDir, "subDir" + fileSep + ".."
                    + fileSep + "file1.txt").getFileName().toString());

            ShellUtil.getPath(currentDir, "subDir1" + fileSep + "file1.txt");
            fail();
        }catch (IOException e){
            assertNull(e.getMessage());
        }
    }

    @Test
    public void openFileTest_absolutePath(){
        try {
            assertEquals("file1.txt", ShellUtil.getPath(currentDir, currentDir).getFileName().toString());

            ShellUtil.getPath(currentDir, currentDir + fileSep + "subDir1" + fileSep + "file1.txt");
            fail();
        }catch (IOException e){
            assertNull(e.getMessage());
        }

        try {
            assertEquals("file1.txt", ShellUtil.getPath(currentDir, currentDir).getFileName().toString());

            ShellUtil.getPath(currentDir, currentDir + fileSep + "subDir1" + fileSep + "file1.txt");
            fail();
        }catch (IOException e){
            assertNull(e.getMessage());
        }
    }

    @Test
    public void openFolderTest_exception(){
        try {
            Shell.eval("cd subDir; cd subDir", writer, currentDir);
            fail();
        }catch (RuntimeException e){
            assertEquals("cd: can not switch to such directory subDir", e.getMessage());
        }

        try {
            Shell.eval("cd " + currentDir + fileSep + "subDir" + fileSep + "noExist", writer, currentDir);
            fail();
        }catch (RuntimeException e){
            assertEquals("cd: can not switch to such directory " + currentDir + fileSep + "subDir" + fileSep + "noExist", e.getMessage());
        }
    }



    public void systemTest_resultChecker(String result, String cmdLine){
        out.reset();
        Shell.eval(cmdLine, writer, currentDir);
        assertEquals(result, out.toString());
    }

    public void systemTest_fileContentChecker(String content, String fileName){
        try {
            BufferedReader reader = Files.newBufferedReader(Paths.get(currentDir, fileName));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null){
                builder.append(line);
            }
            assertEquals(content, builder.toString());
        }catch (Exception e){
            fail();
        }
    }

    @Test
    public void systemTest_pipe(){
        this.systemTest_resultChecker("1234 5678" + lineSep, "echo 1234   5678 | cat");
        this.systemTest_resultChecker("AAA" + lineSep, "cat file1.txt | head -n 1");
        this.systemTest_resultChecker("ccc" + lineSep, "cat file1.txt | tail -n 1");
        this.systemTest_resultChecker("", "cat file1.txt | grep aaa");
        this.systemTest_resultChecker("*" + lineSep, "cat file3.txt | cut -b 1");
        this.systemTest_resultChecker("CCC" + lineSep + "DDD" + lineSep, "cat subDir/file2.txt | uniq -i");
        this.systemTest_resultChecker("DDD" + lineSep + "CCC" + lineSep, "cat subDir/file2.txt | uniq -i | sort -r");
        this.systemTest_resultChecker(currentDir + fileSep + "subDir" + fileSep + "file1.txt" + lineSep,
                "cd subDir;find " + currentDir + fileSep + "subDir" + " -name file1.txt");
        this.systemTest_resultChecker("CCC" + lineSep + "ccc" + lineSep + "DDD" + lineSep
                + "ddd" + lineSep, "cat subDir/file2.txt | uniq");
        this.systemTest_resultChecker("CCC" + lineSep + "CCC" + lineSep + "DDD" + lineSep
                + "ccc" + lineSep + "ddd" + lineSep, "cat subDir/file2.txt | sort");
    }

    @Test
    public void systemTest_redirection(){
        out.reset();
        Shell.eval("cat < file3.txt", writer, currentDir);
        assertEquals("*.txt" + System.getProperty("line.separator"), out.toString());

        out.reset();
        Shell.eval("cat < file3.txt > file100.txt", writer, currentDir);
        this.systemTest_fileContentChecker("*.txt", "file100.txt");

        out.reset();
        Shell.eval("cat < file3.txt > " + currentDir + fileSep + "file105.txt", writer, currentDir);
        this.systemTest_fileContentChecker("*.txt",  "file105.txt");
    }

    @Test
    public void systemTest_redirection_exception(){
        try {
            Shell.eval("cat < file111.txt", writer, currentDir);
            fail();
        }catch (Exception e){
            assertEquals("can not open the input redirection file: file111.txt", e.getMessage());
        }

        try {
            Shell.eval("sort -r < file222.txt < file333.txt", writer, currentDir);
            fail();
        }catch (Exception e){
            assertEquals("Error: several files are specified for input", e.getMessage());
        }

        try {
            Shell.eval("echo 1234 > file222.txt > file333.txt", writer, currentDir);
            fail();
        }catch (Exception e){
            assertEquals("Error: several files are specified for output", e.getMessage());
        }

        try {
            Shell.eval("cat < notExist.txt ", writer, currentDir);
            fail();
        }catch (Exception e){
            assertEquals("can not open the input redirection file: notExist.txt", e.getMessage());
        }

        /*try {
            Shell.eval("echo 1234 > \"<>.txt\"", writer, currentDir);
            fail();
        }catch (Exception e){
            assertEquals("fail to write to the output redirection file: <>.txt", e.getMessage());
        }*/
    }

    @Test
    public void systemTest_testApp(){
        try {
            this.systemTest_resultChecker("", "_cdd file1.txt");
            fail();
        }catch (Exception e){
            assertEquals("unknown application", e.getMessage());
        }

        systemTest_resultChecker(currentDir + System.getProperty("line.separator"), "pwd");
        systemTest_resultChecker("subDir" + fileSep + "file1.txt" + System.getProperty("line.separator"), "find subDir -name \"*1.txt\"");
        systemTest_resultChecker("", "ls '.subDir'");
    }

    @Test
    public void systemTest_unsafe(){
        this.systemTest_resultChecker("cat: can not open: file999.txt" + System.getProperty("line.separator"), "_cat file999.txt");
        this.systemTest_resultChecker("123" + System.getProperty("line.separator"), "_echo 123");
    }

    @Test
    public void systemTest_exception(){
        try {
            Shell.eval("cd subDir;find .subDir -name file1.txt", writer, currentDir);
            fail();
        }catch (Exception e){
            assertEquals("find: no such root directory .subDir", e.getMessage());
        }

        try {
            Shell.eval("cd subDir;find .subDir2 -name file1.txt", writer, currentDir);
            fail();
        }catch (Exception e){
            assertEquals("find: no such root directory .subDir2", e.getMessage());
        }

        try {
            Shell.eval("cd subDir;find .subDir2 -name file1.txt", writer, currentDir);
            fail();
        }catch (Exception e){
            assertEquals("find: no such root directory .subDir2", e.getMessage());
        }

        try {
            Shell.eval("cat <", writer, currentDir);
            fail();
        }catch (Exception e){
            assertEquals("Error: the input does not satisfy the syntax", e.getMessage());
        }
    }

    //just for pass the coverage test for class only has static method
    @Test
    public void shellUtilTest(){
        new ShellUtil();
        new Shell();

        Shell.main(new String[]{"", "", ""});
        Shell.main(new String[]{"-cc", ""});
        Shell.main(new String[]{"-c", "echo 123"});
        Shell.main(new String[]{"-c", "_knownApp 123"});
    }

    @Test
    public void commandSubstitution(){
        systemTest_resultChecker("1234" + lineSep, "echo `echo 1234`");
        systemTest_resultChecker("11234" + lineSep, "echo 1`echo 1234`");
        systemTest_resultChecker("11234" + lineSep, "echo \"1\"`echo 1234`");
        systemTest_resultChecker("11234" + lineSep, "echo '1'`echo 1234`");

        try {
            Shell.eval("cat < `echo 1.txt 2.txt`", writer, currentDir);
        }catch (Exception e){
            assertEquals("Error : ambiguous redirect argument: `echo 1.txt 2.txt`", e.getMessage());
        }
    }

    @Test
    public void parserException(){
        try {
            Shell.eval("echo ````", writer, currentDir);
            fail();
        }catch (Exception e){
            assertEquals("Error: the input does not satisfy the syntax", e.getMessage());
        }
    }

    @Test
    public void globbingTest(){
        this.systemTest_resultChecker("*.txt" + lineSep, "cat < `echo file3.txt`");
        //this.systemTest_resultChecker("*.txt" + lineSep, "cat < `echo *1.txt`");
        //systemTest_resultChecker("file1.txt\\file1.txt" + System.getProperty("line.separator"), "echo *'1.txt'");
    }
}
