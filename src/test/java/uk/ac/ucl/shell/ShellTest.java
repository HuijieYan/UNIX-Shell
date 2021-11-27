package uk.ac.ucl.shell;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ucl.shell.Applications.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ShellTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    public String currentDir;
    ByteArrayOutputStream out;
    OutputStreamWriter writer;

    @Before
    public void setUp() throws Exception {
        out = new ByteArrayOutputStream();
        writer = new OutputStreamWriter(out);

        File tempFile1 = tempFolder.newFile("file1.txt");
        File tempFile2 = tempFolder.newFile("file2.txt");
        File tempFile3 = tempFolder.newFile("file3.txt");
        tempFolder.newFolder("subDir");
        tempFolder.newFolder(".subDir");
        currentDir = tempFolder.getRoot().getCanonicalPath();

        File subTempFile1 = tempFolder.newFile("subDir/file1.txt");
        File subTempFile2 = tempFolder.newFile("subDir/file2.txt");
        File subTempFile3 = tempFolder.newFile("subDir/file3.txt");

        writeToFile(tempFile1, "AAA\nBBB\nbbb\nCCC\nccc");
        writeToFile(tempFile2, "CCC\nDDD");
        writeToFile(tempFile3, "*.txt");
        writeToFile(subTempFile1, "AAA\nBBB\nbbb\nCCC\nccc");
        writeToFile(subTempFile2, "CCC\nccc\nDDD\nddd");
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
        assertEquals(currentDir + System.getProperty("line.separator"), out.toString());
        out.reset();
    }

    @Test
    public void testPwd_exception(){
        try {
            ArrayList<String> argList = new ArrayList<>();
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
        assertEquals(currentDir + File.separator +"subDir", new Cd(currentDir).exec(argList));
    }

    @Test
    public void testCd_absolutePath(){
        ArrayList<String> argList = new ArrayList<>();
        String path = currentDir + File.separator + "subDir";
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
            assertEquals("no such directory: subDir2", e.getMessage());
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
        assertEquals(currentDir, new Cd(currentDir + File.separator + "subDir").exec(argList));
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
        String result = "file1.txt\tfile2.txt\tfile3.txt\tsubDir" + System.getProperty("line.separator");
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
        String result = "file1.txt\tfile2.txt\tfile3.txt" + System.getProperty("line.separator");
        ls.exec(argList);
        this.assertEqualSet(result, out.toString());

        out.reset();
        argList.set(0, File.separator + "subDir" + File.separator + "..");
        result = "file1.txt\tfile2.txt\tfile3.txt\tsubDir" + System.getProperty("line.separator");
        ls.exec(argList);
        this.assertEqualSet(result, out.toString());

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
        assertEquals("AAA" + System.getProperty("line.separator") + "BBB" + System.getProperty("line.separator")
                + "bbb" + System.getProperty("line.separator") + "CCC" + System.getProperty("line.separator")
                + "ccc" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testCat_absoluteAndRelativePath(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add(File.separator + "." + File.separator + "file1.txt");
        argList.add(currentDir + File.separator + "file3.txt");
        argList.add(currentDir + File.separator + "subDir" + File.separator + ".." + File.separator + "file3.txt");
        new Cat(currentDir, null, writer).exec(argList);
        assertEquals("AAA" + System.getProperty("line.separator") + "BBB" + System.getProperty("line.separator")
                + "bbb" + System.getProperty("line.separator") + "CCC" + System.getProperty("line.separator")
                + "ccc" + System.getProperty("line.separator") + "*.txt" + System.getProperty("line.separator")
                + "*.txt" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testCat_exception(){
        out.reset();
        try {
            ArrayList<String> argList = new ArrayList<>();
            argList.add("file4.txt");
            new Cat(currentDir, null, writer).exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cat: can not open: file4.txt", e.getMessage());
        }
    }

    @Test
    public void testCat_multiFile(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("file1.txt");
        argList.add("file3.txt");
        new Cat(currentDir, null, writer).exec(argList);
        assertEquals("AAA" + System.getProperty("line.separator") + "BBB" + System.getProperty("line.separator")
                + "bbb" + System.getProperty("line.separator") + "CCC" + System.getProperty("line.separator")
                + "ccc" + System.getProperty("line.separator") + "*.txt" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testEcho_singleArg(){
        out.reset();
        String arg = "Hello";
        ArrayList<String> argList = new ArrayList<>();
        argList.add(arg);
        new Echo(currentDir, writer).exec(argList);
        assertEquals("Hello" + System.getProperty("line.separator"), out.toString());
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
        assertEquals("Hello Hello Hello" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testEcho_exception(){
        out.reset();
        try {
            new Echo(currentDir, writer).exec(new ArrayList<>());
        }catch (RuntimeException e){
            assertEquals("Echo: application should at least has one argument", e.getMessage());
        }
    }

    @Test
    public void testHead_defaultAndSpecific(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        Head head = new Head(currentDir, null, writer);
        argList.add("file1.txt");
        head.exec(argList);
        assertEquals("AAA" + System.getProperty("line.separator") + "BBB" + System.getProperty("line.separator")
                + "bbb" + System.getProperty("line.separator") + "CCC" + System.getProperty("line.separator")
                + "ccc" + System.getProperty("line.separator"), out.toString());

        out.reset();
        argList.add(0, "-n");
        argList.add(1, "2");
        head.exec(argList);
        assertEquals("AAA" + System.getProperty("line.separator") + "BBB" + System.getProperty("line.separator"), out.toString());

        out.reset();
        argList.set(1, "18");
        head.exec(argList);
        assertEquals("AAA" + System.getProperty("line.separator") + "BBB" + System.getProperty("line.separator")
                + "bbb" + System.getProperty("line.separator") + "CCC" + System.getProperty("line.separator")
                + "ccc" + System.getProperty("line.separator"), out.toString());
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
            argList.add("-n");
            argList.add("2");
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
        assertEquals("AAA" + System.getProperty("line.separator") + "BBB" + System.getProperty("line.separator")
                + "bbb" + System.getProperty("line.separator") + "CCC" + System.getProperty("line.separator")
                + "ccc" + System.getProperty("line.separator"), out.toString());

        out.reset();
        argList.add(0, "-n");
        argList.add(1, "2");
        tail.exec(argList);
        assertEquals("CCC" + System.getProperty("line.separator") + "ccc" + System.getProperty("line.separator"), out.toString());

        out.reset();
        argList.set(1, "18");
        tail.exec(argList);
        assertEquals("AAA" + System.getProperty("line.separator") + "BBB" + System.getProperty("line.separator")
                + "bbb" + System.getProperty("line.separator") + "CCC" + System.getProperty("line.separator")
                + "ccc" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testTail_exception(){
        testHeadOrTail_exception("tail");
    }

    @Test
    public void testGrep(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("AAA");
        argList.add("file1.txt");
        argList.add("subDir" + File.separator + "file1.txt");
        new Grep(currentDir, null, writer).exec(argList);
        assertEquals(argList.get(1) + ":" + argList.get(0) + System.getProperty("line.separator")
                + argList.get(2) + ":" + argList.get(0) + System.getProperty("line.separator"), out.toString());
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
        assertEquals("AA" + System.getProperty("line.separator") + "BB" + System.getProperty("line.separator")
                + "bb" + System.getProperty("line.separator") + "CC" + System.getProperty("line.separator")
                + "cc" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testCut_specificSlice(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-b");
        argList.add("-1,3-");
        argList.add("file1.txt");
        new Cut(currentDir, null, writer).exec(argList);
        assertEquals("AA" + System.getProperty("line.separator") + "BB" + System.getProperty("line.separator")
                + "bb" + System.getProperty("line.separator") + "CC" + System.getProperty("line.separator")
                + "cc" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testCut_specificRange(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-b");
        argList.add("1-3");
        argList.add("file1.txt");
        new Cut(currentDir, null, writer).exec(argList);
        assertEquals("AAA" + System.getProperty("line.separator") + "BBB" + System.getProperty("line.separator")
                + "bbb" + System.getProperty("line.separator") + "CCC" + System.getProperty("line.separator")
                + "ccc" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testCut_specificROutOfBound(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-b");
        argList.add("1-4,7,9,-1,10-");
        argList.add("file1.txt");
        new Cut(currentDir, null, writer).exec(argList);
        assertEquals("AAA" + System.getProperty("line.separator") + "BBB" + System.getProperty("line.separator")
                + "bbb" + System.getProperty("line.separator") + "CCC" + System.getProperty("line.separator")
                + "ccc" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testCut_exception(){
        ArrayList<String> argList = new ArrayList<>();
        Cut cut = new Cut(currentDir, null, writer);
        try {
            argList.add("-n");
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
            assertEquals("cut: incorrect option input " + argList.get(0), e.getMessage());
        }

        try {
            argList.set(0, "-b");
            argList.add("file4.txt");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: can not open file: " + argList.get(2), e.getMessage());
        }
    }

    @Test
    public void testCut_invalidCut(){
        ArrayList<String> argList = new ArrayList<>();
        Cut cut = new Cut(currentDir, null, writer);
        argList.add("-b");
        argList.add("file1.txt");

        try {
            argList.add(1, "-1-3");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: invalid argument " + argList.get(1), e.getMessage());
        }

        try {
            argList.set(1, "b1-");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: invalid argument " + argList.get(1), e.getMessage());
        }

        try {
            argList.set(1, "1-0");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: invalid argument " + argList.get(1), e.getMessage());
        }

        try {
            argList.set(1, "0-1");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: invalid argument " + argList.get(1), e.getMessage());
        }

        try {
            argList.set(1, "2-1");
            cut.exec(argList);
            fail();
        }catch (RuntimeException e){
            assertEquals("cut: invalid decreasing range " + argList.get(1), e.getMessage());
        }
    }

    @Test
    public void testFind_currentDir(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-name");
        argList.add("*1.txt");
        new Find(currentDir, writer).exec(argList);
        this.assertEqualSet("." + File.separator + "file1.txt" + System.getProperty("line.separator")+ "." + File.separator
                + "subDir" + File.separator + "file1.txt" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testFind_absoluteOrRelativePath(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        String prefix = "subDir" + File.separator + "..";
        argList.add(prefix);
        argList.add("-name");
        argList.add("*1.txt");
        new Find(currentDir, writer).exec(argList);
        this.assertEqualSet(prefix + File.separator + "file1.txt" + System.getProperty("line.separator") + prefix + File.separator
                + "subDir" + File.separator + "file1.txt" + System.getProperty("line.separator"), out.toString());

        out.reset();
        prefix = currentDir;
        argList.set(0, prefix);
        new Find(currentDir, writer).exec(argList);
        this.assertEqualSet(prefix + File.separator + "file1.txt" + System.getProperty("line.separator") + prefix + File.separator
                + "subDir" + File.separator + "file1.txt" + System.getProperty("line.separator"), out.toString());
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
    }

    @Test
    public void testUniq_caseSensitive(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("subDir/file2.txt");
        new Uniq(currentDir, null, writer).exec(argList);
        assertEquals("CCC" + System.getProperty("line.separator") + "ccc" + System.getProperty("line.separator")
                + "DDD" + System.getProperty("line.separator") + "ddd" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testUniq_caseInsensitive(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-i");
        argList.add("subDir/file2.txt");
        new Uniq(currentDir, null, writer).exec(argList);
        assertEquals("CCC" + System.getProperty("line.separator") + "DDD" + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testUniq_exception(){
        ArrayList<String> argList = new ArrayList<>();
        Uniq uniq = new Uniq(currentDir, null, writer);
        try {
            argList.add("-iii");
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
        argList.add("subDir" + File.separator + "file3.txt");
        new Sort(currentDir, null, writer).exec(argList);
        assertEquals("123" + System.getProperty("line.separator") + "456" + System.getProperty("line.separator")
                + "666" + System.getProperty("line.separator") + "789" + System.getProperty("line.separator") + "abc"
                + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testSort_ReverseOrder(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        argList.add("-r");
        argList.add("subDir" + File.separator + "file3.txt");
        new Sort(currentDir, null, writer).exec(argList);
        assertEquals("abc" + System.getProperty("line.separator") + "789" + System.getProperty("line.separator")
                + "666" + System.getProperty("line.separator") + "456" + System.getProperty("line.separator") + "123"
                + System.getProperty("line.separator"), out.toString());
    }

    @Test
    public void testSort_exception(){
        out.reset();
        ArrayList<String> argList = new ArrayList<>();
        Sort sort = new Sort(currentDir, null, writer);

        try {
            argList.add("-a");
            argList.add("subDir" + File.separator + "file4.txt");
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
            assertEquals("file1.txt", ShellUtil.getPath(currentDir, "subDir" + File.separator + ".."
                    + File.separator + "file1.txt").getFileName().toString());

            ShellUtil.getPath(currentDir, "subDir1" + File.separator + "file1.txt");
            fail();
        }catch (IOException e){
            assertNull(e.getMessage());
        }
    }

    @Test
    public void openFileTest_absolutePath(){
        try {
            assertEquals("file1.txt", ShellUtil.getPath(currentDir, currentDir).getFileName().toString());

            ShellUtil.getPath(currentDir, currentDir + File.separator + "subDir1" + File.separator + "file1.txt");
            fail();
        }catch (IOException e){
            assertNull(e.getMessage());
        }
    }
}
