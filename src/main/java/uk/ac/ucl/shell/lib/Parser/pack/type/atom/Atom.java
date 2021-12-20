package uk.ac.ucl.shell.lib.Parser.pack.type.atom;

import java.util.ArrayList;

public interface Atom {
    boolean isRedirectionSymbol();
    ArrayList<String> get();
}
