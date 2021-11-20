package uk.ac.ucl.shell.Parser.pack.type.atom;

import java.util.ArrayList;

abstract class AtomAbstract implements Atom{
    protected ArrayList<String> content;
    public ArrayList<String> get(){
        return content;
    }
    public abstract boolean isRedirectionSymbol();
}
