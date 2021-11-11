package uk.ac.ucl.shell.Parser;

import java.util.function.Function;
import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;
import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;

public class ParserOperation implements MonadOperation{
    public <T> Monad<T> result(final T value){
        return new Parser<T>(input->{return new Pair<>(value, input);});
    }

    /**
     * Monad's bind operation was interpreted as follows:
     *       1. Use the first parser to parse the input.
     *       2. If first parsing is unsuccessful return zero, otherwise apply 
     *          the function given to the parsed value of the result pair  
     *          after first parsing.
     *       3. The function given would return the second parser, use it to parse
     *          the rest of the input (given in the result pair after first parsing).
     *   
     * @param parser the first parser, not null
     * @param function the given function which takes in the parsed value and returns
     * the second parser, not null
     */   
    public <T,A> Monad<A> bind(final Monad<T> parser, final Function<T,Monad<A>> function){
        return new Parser<>(inp ->{
            MonadicValue<T,String> pair = parser.parse(inp);
            
            if (pair.isEmpty()){
                Monad<A> zero = this.zero();
                return zero.parse(inp);
            }
            return function.apply(pair.getValue()).parse(pair.getInputStream());
        });
    }
       
    public <T> Monad<T> zero(){
        return new Parser<T>(input -> {
            return new Pair<T,String>(null, "");
        });
    }
}