package uk.ac.ucl.shell.Parser;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;

import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;
import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;

public class ParserOperation {

    public <T> Monad<T> result(final T value){
        return new Parser<T>(input->{return new Pair<>(value, input);});
    }


    /**
     * Monad's bind operation was interpreted as follows:
     *       1. Use the first parser to parse the input.
     *       2. If first parsing is unsuccessful return zero, otherwise apply 
     *          the function given to the parsed value of the result pair  
     *          after first parsing.
     *       3. The function given would return the second parser, use it to parse
     *          the rest of the input (given in the result pair after first parsing).
     *   
     * @param parser the first parser, not null
     * @param function the given function which takes in the parsed value and returns
     * the second parser, not null
     */   
    public <T,A> Monad<A> bind(final Monad<T> parser, final Function<T,Monad<A>> function){
        return new Parser<>(inp ->{
            MonadicValue<T,String> pair = parser.parse(inp);
            
            if (pair.isEmpty()){
                Monad<A> zero = this.zero();
                return zero.parse(inp);
            }
            return function.apply(pair.getValue()).parse(pair.getInputStream());
        });
    }
    
    /**
     * Parser zero returns an empty monadic value (defined by MonadicValue class),
     * the input can be null or empty
     */   
    public <T> Monad<T> zero(){
        return new Parser<T>(input -> {
            return new Pair<T,String>(null, "");
        });
    }

    public Monad<Character> item(){
    //returns the first character of input string, string can be empty
        return new Parser<>(input->{
            if (input.length() > 0){
                return new Pair<Character,String>(input.charAt(0), input.substring(1));
            }

            Monad<Character> zero = this.zero();
            return zero.parse(input);
        });
    }

    /**
     * Parser sat takes a string and checks whether the first character satisfies
     * given requirement, the string cannot be null but can be empty.
     *  
     * @param validation the given requirement which the first cahracter of the 
     * input string must satisfy.
     */   
    public Monad<Character> sat(final Function<Character,Boolean> validation){
        return new Parser<>(input -> {
            return this.bind(this.item(),value ->{
                if (validation.apply(value)){
                    return this.result(value);
                }else{
                    return this.zero();
                }
            }).parse(input);
        });
    }

    public Monad<Character> isChar(final Character value){
    //Identifies if the first character of the input matches the value
        return this.sat(input->{
            return input.equals(value);
        });
    }

    public Monad<Character> isDigit(){
    //Assumed the shell is using ascii
        return this.sat(input->{
            int ascii = (int) input;
            return ascii >= ((int) '0') && ascii <= ((int) '9');
        });
    }

    public Monad<Character> isLowerChar(){
    //Assumed the shell is using ascii
        return this.sat(input->{
            int ascii = (int) input;
            return ascii >= ((int) 'a') && ascii <= ((int) 'z');
        });
    }

    public Monad<Character> isUpperChar(){
    //Assumed the shell is using ascii
        return this.sat(input->{
            int ascii = (int) input;
            return ascii >= ((int) 'A') && ascii <= ((int) 'Z');
        });
    }

    public Monad<Character> isAnyLetter(){
        return or(isLowerChar(), isUpperChar());
    }

    /**
     * Parser many let the given parser keep parsing the input until the end
     * of input is reached or the parsing has failed, then it returns a list
     * of parsed result which can be empty (if there has not been a successful 
     * parsing) and the rest of the input.
     * 
     * @param parser the given parser which will parse the input many times
     */
    public <T> Monad<Deque<T>> many(final Monad<T> parser){
    // 
            return new Parser<>(input ->{
                Deque<T> empty = new LinkedList<T>();
                return this.or(this.bind(parser,x->{
                    return this.bind(this.many(parser),xs ->{
                        xs.addFirst(x);
                        return this.result(xs);
                    });
                }),this.result(empty)).parse(input);
            });
    }
    //see if can do dag

    /**
     * Parser many1 is nearly the same as parser many except the given parser
     * must have successfully parsed the input at least once.
     */
    public <T> Monad<Deque<T>> many1(final Monad<T> parser){ 
        return new Parser<>(input ->{
            return this.bind(parser,x->{
                return this.bind(this.many(parser),xs ->{
                    xs.addFirst(x);
                    return this.result(xs);
                });
            }).parse(input);
        });
    }


    public <T> Monad<T> or(final Monad<T> parser1, final Monad<T> parser2){
    // this is a deterministic combinator
        Function<String,MonadicValue<T,String>> function = input ->{
            MonadicValue<T,String> pair = parser1.parse(input);
            if (pair.isEmpty()){
                return parser2.parse(input);
                //only returns the second parsing result when the first parsing is unsuccessful
            }
            return pair;
        };
        return new Parser<>(function);
    }

    public Monad<Character> anyCharacterExcept(final ArrayList<Character> exceptions){
    //parses any character with exceptions given by the array list
        Function<Character,Boolean> function = input->{
            for (char character : exceptions){
                if (input == character){
                    return false;
                }
            }
            return true;
        };
        return this.sat(function);
    }
}