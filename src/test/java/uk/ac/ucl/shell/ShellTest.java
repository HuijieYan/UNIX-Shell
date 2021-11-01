package uk.ac.ucl.shell;

import org.junit.Test;

import uk.ac.ucl.shell.Shell_Monad;

import static org.junit.Assert.*;

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
        Shell_Monad.eval("echo foo", out);
        Scanner scn = new Scanner(in);
        assertEquals(scn.next(),"foo");
    }

    @Test
    public void testEcho() throws Exception {
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out;
        out = new PipedOutputStream(in);
        Shell_Monad.eval("echo hello", out);
        Scanner scn = new Scanner(in);
        //assertEquals(scn.next(),"hello");
        System.out.println("Returned from shell -> "+scn.next());        
    }
    

}
