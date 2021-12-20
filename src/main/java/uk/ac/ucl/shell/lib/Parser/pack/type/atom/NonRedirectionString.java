package uk.ac.ucl.shell.lib.Parser.pack.type.atom;

import java.util.ArrayList;

public class NonRedirectionString extends AtomAbstract{
    private boolean canBeGlob;
    public NonRedirectionString(ArrayList<String> list){
        content = list;
    }
    
    public boolean isRedirectionSymbol(){
        return false;
    }

    public ArrayList<String> get(){
        return content;
    }

    public boolean canBeGlob(){
        return this.canBeGlob;
    }

    public void setCanBeGlob(boolean canBeGlob){
        this.canBeGlob = canBeGlob;
    }
}
