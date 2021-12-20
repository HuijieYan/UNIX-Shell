package uk.ac.ucl.shell.Parser.pack.type.atom;

import java.util.ArrayList;

public interface Atom {
    boolean isRedirectionSymbol();
    ArrayList<String> get();
}
