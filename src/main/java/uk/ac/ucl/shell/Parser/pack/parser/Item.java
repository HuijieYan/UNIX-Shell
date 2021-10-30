package uk.ac.ucl.shell.Parser.pack.parser;

import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;

public class Item implements ParserInterface<String,String>{
    private Pair<String,String> pair;

    public Pair<String,String> parse(String inputStream){
        if (inputStream.length() > 0){
            pair = new Pair<String,String>(Character.toString(inputStream.charAt(0)),inputStream.substring(1));
        }else{
            pair = new Pair<String,String>("","");
            //empty pair
        }
        return pair;
    }
}