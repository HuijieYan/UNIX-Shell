package uk.ac.ucl.shell.Parser.pack.type.atom;

import java.util.ArrayList;

public interface Atom {
    public boolean isRedirectionSymbol();
    public ArrayList<String> get();
}
