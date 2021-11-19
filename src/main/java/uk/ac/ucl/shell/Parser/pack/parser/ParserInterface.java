package uk.ac.ucl.shell.Parser.pack.parser;
import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;
/*
    Type V is the value.
    Type I is the rest of the input stream.
*/
//The actual parser
public interface ParserInterface<V,I>{
    public Pair<V,I> parse(I inputStream);
}