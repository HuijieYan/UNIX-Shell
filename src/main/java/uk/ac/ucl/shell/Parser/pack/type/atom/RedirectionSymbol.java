package uk.ac.ucl.shell.Parser.pack.type.atom;

public class RedirectionSymbol extends AtomAbstract{
    private boolean greaterThan;
    //indicates whether the symbol is > or <

    public RedirectionSymbol(char symbol){
        if (symbol == '>'){
            greaterThan = true;
        }else{
            greaterThan = false;
        }
        content.add(Character.toString(symbol));
    }

    public boolean isRedirectionSymbol(){
        return true;
    }

    public boolean isTowardsNext(){
        return greaterThan;
    }
}
