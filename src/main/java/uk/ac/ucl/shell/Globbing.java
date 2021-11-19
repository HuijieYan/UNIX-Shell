package uk.ac.ucl.shell;

/**
 * Sample code that finds files that match the specified glob pattern.
 * For more information on what constitutes a glob pattern, see
 * https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
 *
 * The file or directories that match the pattern are printed to
 * standard out.  The number of matches is also printed.
 *
 * When executing this application, you must put the glob pattern
 * in quotes, so the shell will not expand any wild cards:
 *              java Find . -name "*.java"
 */

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;


public class Globbing {

    public static class Finder
        extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private final OutputStreamWriter writer;
        private int numMatches = 0;

        Finder(String pattern, OutputStreamWriter writer) {
            this.writer = writer;
            matcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + pattern);
        }

        // Compares the glob pattern against
        // the file or directory name.
        void find(Path file) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                numMatches++;
                //writer.println(file);
                try {
                    writer.write(file.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Prints the total number of
        // matches to standard out.
        void done() {
            System.out.println("Matched: "
                + numMatches);
        }

        // Invoke the pattern matching
        // method on each file.
        @Override
        public FileVisitResult visitFile(Path file,
                BasicFileAttributes attrs) {
            find(file);
            return CONTINUE;
        }

        // Invoke the pattern matching
        // method on each directory.
        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) {
            find(dir);
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file,
                IOException exc) {
            System.err.println(exc);
            return CONTINUE;
        }
    }

    // static void usage() {
    //     System.err.println("java Find <path>" +
    //         " -name \"<glob_pattern>\"");
    //     System.exit(-1);
    // }

    public static String exec(Path dir, String glob) throws IOException {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(output);
        Finder finder = new Finder(glob, writer);
        Files.walkFileTree(dir, finder);
        //finder.done(); 
        return output.toString();
    }

    // public static void main(String[] args)
    //     throws IOException {

    //     if (args.length < 3 || !args[1].equals("-name"))
    //         usage();

    //     Path startingDir = Paths.get(args[0]);
    //     String pattern = args[2];

    //     Finder finder = new Finder(pattern);
    //     Files.walkFileTree(startingDir, finder);
    //     //finder.done();
    // }
}