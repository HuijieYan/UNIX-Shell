package uk.ac.ucl.shell.lib.Parser.pack.monad;

import uk.ac.ucl.shell.lib.Parser.pack.type.MonadicValue;

public interface Monad<T>{
    MonadicValue<T,String> parse(String input);
}