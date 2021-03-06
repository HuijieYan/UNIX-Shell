package uk.ac.ucl.shell.lib.Parser.pack.type.pair;
import uk.ac.ucl.shell.lib.Parser.pack.type.MonadicValue;
/*
    Type V is the value.
    Type I is the rest of the input stream.
*/
public class Pair<V,I> implements MonadicValue<V,I>{
    private final V value;
    private final I inputStream;

    public Pair(V value, I inputStream){
        this.value = value;
        this.inputStream = inputStream;
    }

    public V getValue(){return value;}
    public I getInputStream(){return inputStream;}
    public Boolean isEmpty(){
        return value == null;
    }
}