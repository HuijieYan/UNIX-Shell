package pack.parser;
import pack.type.pair.Pair;
import pack.parser.ParserInterface;
/*
    Type V is the value.
    Type I is the rest of the input stream.
*/

public class Result<V,I> implements ParserInterface<V,I>{
    private Pair<V,I> pair;
    private V value;

    public Result(V value){
        this.value = value;
    }

    public Pair<V,I> parse(I inputStream){
        pair = new Pair<>(value, inputStream);
        return pair;
    }
}