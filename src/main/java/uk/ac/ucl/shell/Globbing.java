package uk.ac.ucl.shell;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


public class Globbing {

    /**
     * execution function of Globbing
     * @param pathShell currentDirectory of Shell
     * @param pattern pattern to do globing (eg. *.txt, *//*.txt)
     * @return globed result in a list of string.
     */
    public static List<String> exec(String pathShell, String pattern) {

        int index = pattern.indexOf("*");
        ArrayList<String> resultList = new ArrayList<>();
        if (index == -1) {
            resultList.add(pattern);
            return resultList;
        }
        
        //Split pattern into path and actual glob content
        String targetDir = pattern.substring(0, index);
        String glob = pattern.substring(index);
 
        //dealing with root
        String rootDir = findRootPath(pathShell, targetDir);
        boolean isAbsolute = Paths.get(targetDir).isAbsolute();
        return processGlobbing(pathShell, rootDir, isAbsolute, glob, pattern);
    }

    //file visitor
    /**
     * getFiles does the recursive search from given path
     * The function returns list of matched path.
     * @param directory Root directory to start the search
     * @param glob glob pattern (eg. *.txt)
     * @return List of matched path
     * @throws IOException if an I/O error occurs
     */
    public static List<Path> getFiles(final Path directory, final String glob) throws IOException {
        final var myFileVisitor = new GlobFileVisitor(glob);
        Files.walkFileTree(directory, myFileVisitor);
        return myFileVisitor.getMatchedFiles();
    }
    
    public static class GlobFileVisitor extends SimpleFileVisitor<Path> {
    
        private final PathMatcher pathMatcher;
        private final List<Path> matchedFiles = new ArrayList<>();
    
        /**
         * Constructor of GlobFileVisitor
         * @param glob pattern to be globed
         */
        public GlobFileVisitor(final String glob) {
            this.pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        }
    
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) {
            if (pathMatcher.matches(path)) {
                matchedFiles.add(path);
            }
            return FileVisitResult.CONTINUE;
        }
    
        /**
         * Function gets the list that contains matched path
         * @return list of matched path.
         */
        public List<Path> getMatchedFiles() {
            return matchedFiles;
        }
    }
    //

    /*
     * Help function of exec() that change Path into given directory
     * @param currentDirectory currentDirectory of Shell
     * @param target directory
     * @return New path changed from current Shell directory
     * @throw RuntimeException When failed to change the directory
     */
    private static String findRootPath(String currentDirectory, String dir) {
        try {
            File myFile = ShellUtil.getDir(currentDirectory, dir);
            currentDirectory = myFile.getPath();
            return currentDirectory;
        }catch (IOException e) {
            throw new RuntimeException("glob: fail to change the directory");
        }
    }

    /*
     * Help function of exec() that process the actual globbing
     * The function will do the actual globbing and add each matched result as an element into a list in proper format (relative/absolute)
     * IF no result is matched, add the original pattern into list
     * @param pathMask currentDirectory of Shell
     * @param rootDirectory Root directory for the actual recursive search.
     * @param isAbsolute True if this globbing pattern is using absolute path, false if using relative
     * @param glob pattern of globbing
     * @return List of matched result
     * @throw RuntimeException When failed to change the directory
     */    
    private static List<String> processGlobbing(String pathMask, String rootDirectory, boolean isAbsolute, String glob, String usrPattern) {

        ArrayList<String> processedList = new ArrayList<>();
        try {
            // Build the path pattern with glob syntax for further matching inside PathMatcher
            String pattern = Paths.get(rootDirectory)+ FileSystems.getDefault().getSeparator() + glob;

            //if on windows
            if (pattern.contains("\\")) {
                pattern = pattern.replace("\\", "\\\\");
            }

            List<Path> matchedRes = getFiles(Paths.get(rootDirectory), pattern);
            if (matchedRes.size() == 0) {
                processedList.add(usrPattern);
                return processedList;
            }

            for (Path curElem : matchedRes) {
                //if using relative path
                if (!isAbsolute) {
                    Path relativePath = Paths.get(pathMask).relativize(curElem);
                    processedList.add(relativePath.toString());
                } else {
                    String curStr = curElem.toString();
                    processedList.add(curStr);
                }                
            }

        } catch (IOException e) {
            return new ArrayList<>();
        }
        
        return processedList;
    }
}