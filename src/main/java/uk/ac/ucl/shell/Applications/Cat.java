package uk.ac.ucl.shell.Applications;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import uk.ac.ucl.shell.ShellApplication;
import uk.ac.ucl.shell.ShellUtil;

public class Cat implements ShellApplication {
    private String currentDirectory;
    private BufferedReader reader;
    private OutputStreamWriter writer;

    public Cat(String currentDirectory, BufferedReader reader, OutputStreamWriter writer) {
        this.currentDirectory = currentDirectory;
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public String exec(List<String> appArgs) throws RuntimeException {
        if (appArgs.isEmpty()) {
            if(this.reader == null){
                throw new RuntimeException("cat: no data from pipe or redirection and can not find file to read");
            }
            try {
                writeToBuffer(this.reader);
            }catch (Exception e){
                throw new RuntimeException("cat: fail to read from pipe or redirection");
            }

        } else {
            Charset encoding = StandardCharsets.UTF_8;
            for(String file : appArgs){
                try {
                    writeToBuffer(Files.newBufferedReader(ShellUtil.getPath(currentDirectory, file), encoding));
                }catch (IOException e){
                    throw new RuntimeException("cat: can not open " + file);
                }
            }

        }

        return currentDirectory;
    }

    private void writeToBuffer(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line + System.getProperty("line.separator"));
        }
        writer.flush();
    }

}