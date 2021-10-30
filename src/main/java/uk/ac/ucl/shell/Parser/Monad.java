import pack.type.MonadicValue;
import pack.parser.ParserInterface;
import java.util.function.Function;

public interface Monad<T>{
    public MonadicValue<T,String> parse(String input);
}