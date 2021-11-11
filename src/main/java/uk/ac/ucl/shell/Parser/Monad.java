package uk.ac.ucl.shell.Parser;

import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;
import uk.ac.ucl.shell.Parser.pack.parser.ParserInterface;
import java.util.function.Function;

public interface Monad<T>{
    public MonadicValue<T,String> parse(String input);
}