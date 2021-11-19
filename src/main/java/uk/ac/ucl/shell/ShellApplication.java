package uk.ac.ucl.shell;
import java.util.List;

public interface ShellApplication {

    String exec(List<String> appArgs) throws RuntimeException;
}
