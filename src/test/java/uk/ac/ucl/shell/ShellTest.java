package uk.ac.ucl.shell;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Scanner;

public class ShellTest {
    public ShellTest() {
    }


    @Test
    public void testShell() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out;
        out = new PipedOutputStream(in);
        Shell.eval("echo foo", new OutputStreamWriter(out));
        Scanner scn = new Scanner(in);
        assertEquals(scn.next(),"foo");
    }
    

}
