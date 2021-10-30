package uk.ac.ucl.shell;
import java.io.IOException;
import java.util.List;

public interface ShellApplication {

    String exec(List<String> appArgs) throws IOException;
}
