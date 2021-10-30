package uk.ac.ucl.shell.Parser;

import java.util.function.Function;

import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;

public class Parser<T> implements Monad<T>{
    private Function<String,MonadicValue<T,String>> parserFunction;
    //parserFunction = inp -> (v,inp')

    public Parser(Function<String,MonadicValue<T,String>> function){
        parserFunction = function;
    }

    public MonadicValue<T,String> parse(String input){
        return (MonadicValue<T,String>)parserFunction.apply(input);
    }
}









