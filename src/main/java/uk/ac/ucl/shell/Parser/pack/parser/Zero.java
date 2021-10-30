package pack.parser;
import pack.type.pair.Pair;
import pack.parser.ParserInterface;
/*
    Type V is the value.
    Type I is the rest of the input stream.
*/

public class Zero implements ParserInterface<String,String>{
    public Pair<String,String> parse(){
        return new Pair<String,String>("","");
        //empty pair no matter what input is
    }

    public Pair<String,String> parse(String inputStream){
        return new Pair<String,String>("","");
        //empty pair no matter what input is
    }
}