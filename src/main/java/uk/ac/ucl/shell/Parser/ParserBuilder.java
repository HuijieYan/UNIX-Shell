package uk.ac.ucl.shell.Parser;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.ac.ucl.shell.Parser.pack.command.*;
import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;
import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;
import uk.ac.ucl.shell.Parser.pack.type.atom.*;

public class ParserBuilder {
    public ArrayList<Character> spaces = new ArrayList<>();
    
    public ParserBuilder(){
        spaces.add(' ');
        spaces.add('\t');
    }

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
            return (ascii >= ((int) '0') && ascii <= ((int) '9'));
        });
    }

    public Monad<Character> isLowerChar(){
    //Assumed the shell is using ascii
        return this.sat(input->{
            int ascii = (int) input;
            return (ascii >= ((int) 'a') && ascii <= ((int) 'z'));
        });
    }

    public Monad<Character> isUpperChar(){
    //Assumed the shell is using ascii
        return this.sat(input->{
            int ascii = (int) input;
            return (ascii >= ((int) 'A') && ascii <= ((int) 'Z'));
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
    
    /**
     * Parser symbolQuoted aiming to parse a string with given symbol at the start and the end
     * of the string, for example, string "This is a symbol quoted string" will have " as the
     * symbol.
     * 
     * @param symbol the symbol at the start and the end of string, not null nor empty
     * @param parser the parser which parses the content between the symbols.
     */
    public Monad<String> symbolQuoted(char symbol, Monad<String> parser){
    //assumes the content between symbols excludes the symbol
        return new Parser<>(input->{
            return this.bind(this.isChar(symbol), x->{
                ArrayList<Character> exception = new ArrayList<>();
                exception.add(symbol);
                exception.add('\n');
                return this.bind(parser, y->{
                    return this.bind(this.isChar(symbol),z->{
                        StringBuilder builder = new StringBuilder();
                        builder.append(x);
                        builder.append(y);
                        builder.append(z);
                        return result(builder.toString());
                    });
                });
            }).parse(input);
        });
    }

    /**
     * This is a different version of symbolQuoted parser, the content between the symbol is
     * a combination of serval strings.
     * 
     * @param parser parser returns a list of strings which are the content between 
     *               the symbol, this list can be empty
     */
    public Monad<String> symbolQuoted(Monad<Deque<String>> parser,char symbol){
    //swap the order of argument for method overloading
            return new Parser<>(input->{
                return this.bind(this.isChar(symbol), x->{
                    return this.bind(parser, y->{
                        return this.bind(this.isChar(symbol),z->{
                            StringBuilder builder = new StringBuilder();
                            builder.append(x);
                            for(String str:y){
                                builder.append(str);
                            }
                            builder.append(z);
                            return result(builder.toString());
                        });
                    });
                }).parse(input);
            });
        }

    public Monad<String> quotedContent(ArrayList<Character> exception){
            return new Parser<>(input->{
                return this.bind(this.many1(anyCharacterExcept(exception)), y->{
                        StringBuilder builder = new StringBuilder();
                        for(char letter:y){
                            builder.append(letter);
                        }
                        return result(builder.toString());
                    }).parse(input);
            });
        }

    public Monad<String> singleQuoted(){
        return new Parser<>(input->{
            ArrayList<Character> exception = new ArrayList<>();
            exception.add('\'');
            exception.add('\n');
            Monad<String> contentParser = this.quotedContent(exception);
            return symbolQuoted('\'',contentParser).parse(input);
        });
    }

    public Monad<String> backQuoted(){
        return new Parser<>(input->{
            ArrayList<Character> exception = new ArrayList<>();
            exception.add('`');
            exception.add('\n');
            Monad<String> contentParser = this.quotedContent(exception);
            return symbolQuoted('`',contentParser).parse(input);
        });
    }
    
    public Monad<String> doubleQuoted(){
    /*
        double quoted content can be many strings that can be backquoted string or 
        strings that does not includes new lines, back quotes nor double quotes
    */
        return new Parser<>(input->{
            ArrayList<Character> exception = new ArrayList<>();
            exception.add('`');
            exception.add('\"');
            exception.add('\n');
            Monad<Deque<String>> doubleQuotedContent = this.many(this.or(this.backQuoted(),this.quotedContent(exception)));
            
            return this.symbolQuoted(doubleQuotedContent,'\"').parse(input);
        });
    }

    public Monad<String> quoted(){
        return new Parser<>(input->{
            Monad<String> p = this.or(this.backQuoted(),this.doubleQuoted());
            return this.or(this.singleQuoted(),p).parse(input);
        });
    }

    private Monad<String> nonKeyword(){
    //parses a string containing any characters except for newlines, single quotes, double quotes,
    // backquotes, semicolons and vertical bars
        return new Parser<>(input->{
            ArrayList<Character> ls = new ArrayList<>();
            ls.add('\n');
            ls.add('\'');
            ls.add('\"');
            ls.add('`');
            ls.add(';');
            ls.add('|');
            return this.bind(this.many1(this.anyCharacterExcept(ls)),chars->{
                //got a list of characters after parsing
                StringBuilder builder = new StringBuilder();
                for (char ch:chars){
                    builder.append(ch);
                }
                return this.result(builder.toString());
            }).parse(input);
        });
    }

    private Monad<String> unQuoted(){
    //parses a string containing any characters except for white spaces,newlines, quotes, less than,
    // greater than, semicolons and vertical bars
            return new Parser<>(input->{
                ArrayList<Character> ls = new ArrayList<>(this.spaces);
                ls.add('\n');
                ls.add('\'');
                ls.add('\"');
                ls.add('`');
                ls.add(';');
                ls.add('|');
                ls.add('<');
                ls.add('>');
                return this.bind(this.many1(this.anyCharacterExcept(ls)),chars->{
                    //got a list of characters after parsing
                    StringBuilder builder = new StringBuilder();
                    for (char ch:chars){
                        builder.append(ch);
                    }
                    return this.result(builder.toString());
                }).parse(input);
            });
        }

    /**
     * To avoid left factoring, command's BNF has changed to:
     *  <command> ::= <seq> | <pipe> | <call>
     *  <pipe> ::= <call> "|" <pipe> | <call> "|" <call>
     *  <seq>  ::= (<pipe>|<call>) ";" <command>
     *  (<call>'s BNF will be discussed in parseCall() and call())
     * @return a Monad that returns a list of Command object, they will be either
     * Call or Pipe, thus the list actually represents the sequece of commands,
     * list can be empty but not null 
     */
    public Monad<ArrayList<Command>> parseCommand(){
        return new Parser<>(input->{
            Monad<ArrayList<Command>> command = this.or(this.or(this.seq(),this.pipe()),this.call());
            return command.parse(input);
        });
    }

    /**
     * A generalisation parser for grammars such as pipe and seq, to let this parser sucessfully parse 
     * the input must be successfully parsed by following parser in order: parser1, isChar(symbol),
     * parser.
     * @param parser1/parser2 returns a list of commands as parsed value, parser cannnot be null,
     * list can be empty but not null
     * @param symbol symbol that separates string of parser1 and string of parser2, not empty nor null
     * @param function takes in the parsed value of both parsers, returns a Monad that returns a list 
     * of commands
     * @return returns what the function returns, list can be null indicating parsing unsuccessful 
     * but not empty
     */
    private Monad<ArrayList<Command>> sepBySymbol(Monad<ArrayList<Command>> parser1,Monad<ArrayList<Command>> parser2,
                    char symbol,BiFunction<ArrayList<Command>,ArrayList<Command>,Monad<ArrayList<Command>>> function){
        return new Parser<>(input->{
            return this.bind(parser1, x->{
                return this.bind(isChar(symbol), y->{
                    return this.bind(parser2,z->{
                        return function.apply(x, z);
                    });
                });
            }).parse(input);
        });
    }

    public Monad<ArrayList<Command>> seq(){
        return this.sepBySymbol(this.or(this.pipe(),this.call()), this.parseCommand(), ';',(commandList1,commandList2)->{
            commandList1.addAll(commandList2);
            return this.result(commandList1);
        });
    }

    public Monad<ArrayList<Command>> pipe(){
        return new Parser<>(input->{
            Monad<ArrayList<Command>> callThenPipe = this.sepBySymbol(this.call(), this.pipe(), '|',(commandList1,commandList2)->{
                Command pipe = new Pipe(commandList1,commandList2.get(0).getCommands());
                // commandList2 is a list with Pipe command only, so we get the list of calls in the Pipe object and then
                // put it into the constructor to create a new Pipe object 
                ArrayList<Command> arr = new ArrayList<>();
                arr.add(pipe);
                return this.result(arr);
            });
            Monad<ArrayList<Command>> callThenCall = this.sepBySymbol(this.call(), this.call(), '|',(commandList1,commandList2)->{
                Command pipe = new Pipe(commandList1,commandList2);
                ArrayList<Command> arr = new ArrayList<>();
                arr.add(pipe);
                return this.result(arr);
            });
            return this.or(callThenPipe,callThenCall).parse(input);
        });
    }

    /**
     * call() parses call command, it first makes the input goes through a lexer that uses BNF:
     * <call> ::= ( <non-keyword> | <quoted> ) *
     * Then put the parsed value into the actual parser parseCall(), it then takes the parsed 
     * value of parsedCall() to create an Call object before putting this object to a new list
     * 
     * @return a Monad that returns an arraylist with Call object in it, list can be null which
     * indicates parsing unsuccessful but not empty
     */
    public Monad<ArrayList<Command>> call(){
        return new Parser<>(input->{
            return this.bind(this.many(this.or(quoted(), nonKeyword())),strings->{
                StringBuilder builder = new StringBuilder();
                ArrayList<Command> arr = new ArrayList<>();
                for (String str:strings){
                    builder.append(str);
                }

                ArrayList<Atom> result = this.parseCall().parse(builder.toString()).getValue();
                if (result == null){
                    Monad<ArrayList<Command>> zero = this.zero();
                    return zero;
                }
                Command call = new Call(result);
                arr.add(call);
                return this.result(arr);
            }).parse(input);
        });
    }

    /**
     * Parsing call follows the following BNF:
     * <call> ::= [ <whitespace> ]*[ <redirection> <whitespace> ]* <argument> [ <whitespace> <atom> ]* [ <whitespace> ]*
     * <atom> ::= <redirection> | <argument>
     * <argument> ::= ( <quoted> | <unquoted> )+
     * <redirection> ::= "<" [ <whitespace> ]* <argument> | ">" [ <whitespace> ]* <argument>
     * 
     * <argument> would be a string, <redirection> would be a list of strings with first string is either < or > and second
     * string is argument. <atom> would be a list of strings. <whitespace> would be ignored and would not be included in 
     * the string nor list. 
     * @return a Monad that returns an arraylist of strings, list can be null but not empty.
     */
    public Monad<ArrayList<Atom>> parseCall(){
        return new Parser<>(input->{
            return this.bind(this.manySpaces(), emptySpaces1->{
                return this.bind(this.many(this.bind(this.redirection(),x->{return this.bind(this.manySpaces(),spaces3->{return this.result(x);});})), redirections->{
                    return this.bind(this.parseArgument(), argument->{
                        return this.bind(this.many(this.bind(this.manySpaces(),spaces4->{return this.atom();})), atoms->{
                            return this.bind(this.manySpaces(), emptySpaces2->{
                                ArrayList<Atom> result = new ArrayList<>();
                                for (ArrayList<Atom> redirection:redirections){
                                    result.addAll(redirection);
                                }
                                result.add(argument);
                                for (ArrayList<Atom> atom:atoms){
                                    result.addAll(atom);
                                }
                                return this.result(result);
                            });
                        });
                    });
                });
            }).parse(input);
        });
    }

    private Monad<ArrayList<Atom>> redirection(){
        return new Parser<>(input->{
            return this.bind(this.or(this.isChar('<'),this.isChar('>')), symbol->{
                return this.bind(this.manySpaces(),spaces->{
                    return this.bind(this.argument(),argument->{
                        ArrayList<Atom> result = new ArrayList<>();
                        Atom symbolAtom = new RedirectionSymbol(symbol);
                        Atom argumentAtom = new NonRedirectionString(argument);
                        result.add(symbolAtom);
                        result.add(argumentAtom);
                        return this.result(result);
                    });
                });
            }).parse(input);
        });
    }

    private Monad<ArrayList<Atom>> atom(){
        return new Parser<>(input->{
            return this.or(this.redirection(),this.bind(this.parseArgument(),argument->{
                ArrayList<Atom> result = new ArrayList<>();
                result.add(argument);
                return this.result(result);
            })).parse(input);
        });
    }

    private Monad<Deque<Character>> manySpaces(){
        return new Parser<>(input->{
            return this.many(this.or(this.isChar(' '),this.isChar('\t'))).parse(input);
        });
    }

    private Monad<Atom> parseArgument(){
        return this.bind(this.argument(),argument->{
            Atom argumentAtom = new NonRedirectionString(argument);
            return this.result(argumentAtom);
        });
    }

    private Monad<ArrayList<String>> argument(){
        return new Parser<>(input->{
            return this.bind(this.many1(this.or(this.quoted(), this.unQuoted())),ls->{
                ArrayList<String> list = new ArrayList<>(ls);
                return this.result(list);
            }).parse(input);
        });
    }

    /**
     * takes in a double quoted string and returns a list of double quoted content and back quoted string
     * @param input the input can be empty but not null, the outtermost douvle quote is already removed,
     * when passed into the parser
     */

    public Monad<ArrayList<String>> decodeDoubleQuoted(){
            return new Parser<>(input->{
                ArrayList<Character> exception = new ArrayList<>();
                exception.add('`');
                exception.add('\"');
                exception.add('\n');
                Monad<Deque<String>> doubleQuotedContent = this.many(this.or(this.backQuoted(),this.quotedContent(exception)));
                
                ArrayList<String> result = new ArrayList<>(doubleQuotedContent.parse(input).getValue());
                return this.result(result).parse(input);
            });
        }

    public static void main(String[] args) {
        ParserBuilder b = new ParserBuilder();
        ArrayList<String> l = b.decodeDoubleQuoted().parse("abc`echo 123`def").getValue();
        System.out.println(l);
    }
}