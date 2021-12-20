package uk.ac.ucl.shell.lib.Parser.pack.type;
/*
    Type V is the value.
    Type I is the rest of the input stream.
*/
public interface MonadicValue<V,I>{
    V getValue();
    I getInputStream();
    Boolean isEmpty();
}