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

    public static List<String> exec(String pathShell, String pattern) throws IOException {

        int index = pattern.indexOf("*");
        ArrayList<String> resultList = new ArrayList<>();
        if (index == -1) {
            resultList.add(pattern);
            return resultList;
        }
        
        String targetDir = pattern.substring(0, index);
        String glob = pattern.substring(index, pattern.length());
 
        //dealing with root
        String rootDir = findRootPath(pathShell, targetDir);
        boolean isAbsolute = Paths.get(targetDir).isAbsolute();

        return processGlobbing(pathShell, rootDir, isAbsolute, glob);
    }

    //file visitor
    public static List<Path> getFiles(final Path directory, final String glob) throws IOException {
        final var myFileVisitor = new GlobFileVisitor(glob);
        Files.walkFileTree(directory, myFileVisitor);
    
        return myFileVisitor.getMatchedFiles();
    }
    
    public static class GlobFileVisitor extends SimpleFileVisitor<Path> {
    
        private final PathMatcher pathMatcher;
        private List<Path> matchedFiles = new ArrayList<>();
    
        public GlobFileVisitor(final String glob) {
            this.pathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
        }
    
        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
            if (pathMatcher.matches(path)) {
                matchedFiles.add(path);
            }
            return FileVisitResult.CONTINUE;
        }
    
        public List<Path> getMatchedFiles() {
            return matchedFiles;
        }
    }
    //

    public static String findRootPath(String currentDirectory, String dir) {
        try {
            File myFile = ShellUtil.getDir(currentDirectory, dir);
            currentDirectory = myFile.getPath();
            return currentDirectory;
        }catch (RuntimeException ex) {
            throw new RuntimeException("glob: fail to change the directory");
        }
    }

    public static List<String> processGlobbing(String pathMask, String rootDirectory, boolean isAbsolute, String glob) {

        ArrayList<String> processedList = new ArrayList<>();
        try {
            String pattern = Paths.get(rootDirectory)+ FileSystems.getDefault().getSeparator() + glob;
            List<Path> matchedRes = getFiles(Paths.get(rootDirectory), pattern);
            if (matchedRes.size() == 0) {
                processedList.add(glob);
                return processedList;
            }

            for (Path curElem : matchedRes) {
                //if using relative
                if (!isAbsolute) {
                    Path relativePath = Paths.get(pathMask).relativize(curElem);
                    if (!relativePath.toFile().isFile()) {
                        relativePath = relativePath.resolve(curElem.getFileName());
                    }
                    processedList.add(relativePath.toString());
                } else {
                    String curStr = curElem.toString();
                    processedList.add(curStr.toString());
                }                
            }

        } catch (IOException e) {
            return new ArrayList<>();
        }
        
        return processedList;
    }
}