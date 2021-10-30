package uk.ac.ucl.shell.Parser.pack.parser;


import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;

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