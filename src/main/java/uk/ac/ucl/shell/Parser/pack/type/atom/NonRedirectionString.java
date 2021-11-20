package uk.ac.ucl.shell.Parser.pack.type.atom;

import java.util.ArrayList;

public class NonRedirectionString extends AtomAbstract{

    public NonRedirectionString(ArrayList<String> list){
        content = list;
    }
    
    public boolean isRedirectionSymbol(){
        return false;
    }

    public ArrayList<String> get(){
        return content;
    }
}
