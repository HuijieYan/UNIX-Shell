package uk.ac.ucl.shell.Parser.pack.type.atom;

public class RedirectionSymbol extends AtomAbstract{
    private final boolean greaterThan;
    //indicates whether the symbol is > or <

    public RedirectionSymbol(char symbol){
        greaterThan = symbol == '>';
        content.add(Character.toString(symbol));
    }

    public boolean isRedirectionSymbol(){
        return true;
    }

    public boolean isTowardsNext(){
        return greaterThan;
    }
}
