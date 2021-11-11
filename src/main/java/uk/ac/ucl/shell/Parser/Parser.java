package uk.ac.ucl.shell.Parser;

import java.util.function.Function;

import uk.ac.ucl.shell.Parser.pack.parser.ParserInterface;
import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;
import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;


/**
 * This class is an implementation of the Monad
 * 
 * @param <T> the type of parsed value after parsing
 * @param parseFunction the function of parsing, must be delcared when 
 * delcaring a new Parser
 */
public class Parser<T> implements Monad<T>{
    private Function<String,MonadicValue<T,String>> parseFunction;

    public Parser(Function<String,MonadicValue<T,String>> function){
        parseFunction = function;
    }

    /**
     * Parses the input.
     * 
     * @param input the input which the parser going to parse, can be empty or null
     * @return a monadic value, can be an empty monadic vallue, not null
     */
    public MonadicValue<T,String> parse(String input){
        if (input == null){
            return new Pair<T,String>(null, "");
            //same as using parser zero
        }
        return (MonadicValue<T,String>)parseFunction.apply(input);
    }
}









