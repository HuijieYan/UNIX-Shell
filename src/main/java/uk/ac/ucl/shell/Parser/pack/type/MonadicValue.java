package pack.type;
/*
    Type V is the value.
    Type I is the rest of the input stream.
*/
public interface MonadicValue<V,I>{
    public V getValue();
    public I getInputStream();
    public Boolean isEmpty();
}