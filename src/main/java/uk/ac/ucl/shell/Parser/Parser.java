import java.util.function.Function;

import pack.parser.Item;
import pack.parser.Result;
import pack.parser.Zero;
import pack.parser.ParserInterface;
import pack.type.MonadicValue;
import pack.type.pair.Pair;

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









