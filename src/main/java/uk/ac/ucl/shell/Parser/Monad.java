package uk.ac.ucl.shell.Parser;

import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;

public interface Monad<T>{
    public MonadicValue<T,String> parse(String input);
}