package uk.ac.ucl.shell;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

import java.io.*;
import java.util.Scanner;

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
        File subDir = tempFolder.newFolder("subDir");
        tempFolder.newFolder(".subDir");
        currentDir = subDir.getParent();

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


    @Test
    public void testShell() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        OutputStreamWriter writer = new OutputStreamWriter(out);
        Shell.eval("echo foo", writer, currentDir);
        Scanner scn = new Scanner(in);
        assertEquals(scn.next(),"foo");
    }

    @Test
    public void testEcho() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        OutputStreamWriter writer = new OutputStreamWriter(out);
        Shell.eval("echo hello", writer, currentDir);
        Scanner scn = new Scanner(in);
        assertEquals(scn.next(),"hello");
    }
}
