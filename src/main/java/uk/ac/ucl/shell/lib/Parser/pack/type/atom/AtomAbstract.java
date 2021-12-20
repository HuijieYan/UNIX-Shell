package uk.ac.ucl.shell.lib.Parser.pack.type.atom;

import java.util.ArrayList;

abstract class AtomAbstract implements Atom{
    protected ArrayList<String> content = new ArrayList<>();
    public ArrayList<String> get(){
        return content;
    }
    public abstract boolean isRedirectionSymbol();
}
