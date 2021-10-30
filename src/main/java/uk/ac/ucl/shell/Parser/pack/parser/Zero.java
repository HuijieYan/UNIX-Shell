package uk.ac.ucl.shell.Parser.pack.parser;

import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;

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