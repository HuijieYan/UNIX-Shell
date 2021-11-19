package uk.ac.ucl.shell.Parser;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

import uk.ac.ucl.shell.Parser.pack.command.*;
import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;
import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;

public class ParserBuilder {
    public ArrayList<Character> spaces = new ArrayList<>();
    private MonadOperation op = new ParserOperation();
    
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
/*
    public Monad<String> word(){
        Function<String,MonadicValue<String,String>> func = input->{
            if (input.equals("")){
                return this.result("").parse("");
            }
            Monad<String> neWord = this.newWord();
            return or(neWord,this.result("")).parse(input);
        };
        return new Parser<>(func);
    }

    private Monad<String> newWord(){
    // a private parser
        Function<String,MonadicValue<String,String>> neWordFunc = input ->{
            Function<Character,Monad<String>> bindFunction = character ->{
                Function<String,Monad<String>> func = inp ->{
                    StringBuilder str = new StringBuilder(character);
                    return result(str.append(inp).toString());
                    //result (character:inp)
                };
                return this.bind(this.word(), func);
                //bind word /inp-> result (character:inp)
            };
            return this.bind(or(this.isLowerChar(),this.isUpperChar()), bindFunction).parse(input);
        };
        return new Parser<>(neWordFunc);
    }
    */

    /*
    many p = p x bind /_ ->
            many p xs bind /_ -> 
            result (x:xs)
    eg. many char('a') "aab" = result(['a','a']) "b"
    */
    /*
    public <T> Monad<ArrayList<T>> many(final Monad<T> parser){
    // Function many would let the parser keep parsing the input until
    // the end of input is reached or the parsing has failed.
        Function<String,MonadicValue<ArrayList<T>,String>> function = input->{
            String inputStream = input;
            ArrayList<T> values = new ArrayList<>();
            while (inputStream.length()>0){
                MonadicValue<T,String> pair = parser.parse(inputStream);  
                if (pair.isEmpty()){
                //if parse failed
                    break;
                }
                inputStream = pair.getInputStream();
                values.add(pair.getValue());       
            }
            return this.result(values).parse(inputStream);
        };
        return new Parser<>(function);
    }
    //need refactoring
    */

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
     * @param parser parser returns a list of strings which are the content between the symbol,
     *               this list can be empty
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
        return new Parser<>(input->{
            ArrayList<Character> exception = new ArrayList<>();
            exception.add('`');
            exception.add('\"');
            exception.add('\n');
            Monad<Deque<String>> doubleQuotedContent = this.many(this.or(this.backQuoted(),this.quotedContent(exception)));
            /*
                double quoted content can be many strings that can be backquoted string or 
                strings that does not includes new lines, back quotes nor double quotes
            */
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

/*
    public Monad<ArrayList<ArrayList<ArrayList<String>>>> lexSeq(){
        return this.pSepp(this.or(this.lexPipe(),this.lexCall()), lexCommand(), ';');
    }

    private Monad<ArrayList<ArrayList<ArrayList<String>>>> pSepp(Monad<ArrayList<ArrayList<ArrayList<String>>>> parser1,Monad<ArrayList<ArrayList<ArrayList<String>>>> parser2,char symbol){
        return new Parser<>(input->{
            return this.bind(parser1, x->{
                return this.bind(isChar(symbol), y->{
                    return this.bind(parser2,z->{
                        x.addAll(z);
                        return this.result(x);
                    });
                });
            }).parse(input);
        });
    }

    public Monad<ArrayList<ArrayList<ArrayList<String>>>> lexPipe(){
        return new Parser<>(input->{
            return this.or(this.pipe(this.lexCall(), this.lexPipe(), '|'),
            this.pipe(this.lexCall(), this.lexCall(), '|')).parse(input);
        });
    }

    private Monad<ArrayList<ArrayList<ArrayList<String>>>> pipe(Monad<ArrayList<ArrayList<ArrayList<String>>>> parser1,Monad<ArrayList<ArrayList<ArrayList<String>>>> parser2,char symbol){
        return new Parser<>(input->{
            return this.bind(parser1, x->{
                return this.bind(isChar(symbol), y->{
                    return this.bind(parser2,z->{
                        x.get(0).addAll(z.get(0));
                        return this.result(x);
                    });
                });
            }).parse(input);
        });
    }

    public Monad<ArrayList<ArrayList<ArrayList<String>>>> lexCall(){
        return new Parser<>(input->{
            return this.bind(this.many(this.or(quoted(), nonKeyword())),strings->{
                StringBuilder builder = new StringBuilder();
                ArrayList<ArrayList<ArrayList<String>>> arr = new ArrayList<>();
                for (String str:strings){
                    builder.append(str);
                }
                arr.add(new ArrayList<ArrayList<String>>());
                arr.get(0).add(this.parseCall().parse(builder.toString()).getValue());
                return this.result(arr);
            }).parse(input);
        });
    }

    public Monad<ArrayList<ArrayList<ArrayList<String>>>> lexCommand(){
        return new Parser<>(input->{
            Monad<ArrayList<ArrayList<ArrayList<String>>>> command = this.or(this.or(this.lexSeq(),this.lexPipe()),this.lexCall());
            return command.parse(input);
        });
    }
*/
    /*
        parseCall = many (isChar(' ') ++ isChar('\t')) bind x/->
    */
    /*
    public Monad<ArrayList<String>> parseCall(){
        return new Parser<>(input->{
            return this.bind(this.manySpaces(), emptySpaces1->{
                return this.bind(this.many(this.bind(this.redirection(),x->{return this.bind(this.manySpaces(),spaces3->{return this.result(x);});})), redirections->{
                    return this.bind(this.argument(), argument->{
                        return this.bind(this.many(this.bind(this.manySpaces(),spaces4->{return this.atom();})), atoms->{
                            return this.bind(this.manySpaces(), emptySpaces2->{
                                ArrayList<String> result = new ArrayList<>();
                                for (ArrayList<String> redirection:redirections){
                                    result.addAll(redirection);
                                }
                                result.add(argument);
                                for (ArrayList<String> atom:atoms){
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

    private Monad<ArrayList<String>> redirection(){
        return new Parser<>(input->{
            return this.bind(this.or(this.isChar('<'),this.isChar('>')), symbol->{
                return this.bind(this.manySpaces(),spaces->{
                    return this.bind(this.argument(),argument->{
                        ArrayList<String> result = new ArrayList<>();
                        result.add(Character.toString(symbol));
                        result.add(argument);
                        return this.result(result);
                    });
                });
            }).parse(input);
        });
    }

    private Monad<ArrayList<String>> atom(){
        return new Parser<>(input->{
            return this.or(this.redirection(),this.bind(this.argument(),argument->{
                ArrayList<String> result = new ArrayList<>();
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

    private Monad<String> argument(){
        return new Parser<>(input->{
            return this.bind(this.many1(this.or(this.quoted(), this.unQuoted())),ls->{
                StringBuilder builder = new StringBuilder();
                for (String str:ls){
                    builder.append(str);
                }
                return this.result(builder.toString());
            }).parse(input);
        });
    }
    */

    /*
        To avoid left factoring, command has changed to:
        <command> ::= <seq> | <pipe> | <call>
        <pipe> ::= <call> "|" <pipe> | <call> "|" <call>
        <seq>  ::= (<pipe>|<call>) ";" <command>
        <call> ::= ( <non-keyword> | <quoted> ) *
    */
    public Monad<ArrayList<Command>> parseCommand(){
        return new Parser<>(input->{
            Monad<ArrayList<Command>> command = this.or(this.or(this.seq(),this.pipe()),this.call());
            return command.parse(input);
        });
    }

    public Monad<ArrayList<Command>> seq(){
        return this.sepBySymbol(this.or(this.pipe(),this.call()), this.parseCommand(), ';',(commandList1,commandList2)->{
            commandList1.addAll(commandList2);
            return this.result(commandList1);
        });
    }

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

    public Monad<ArrayList<Command>> pipe(){
        return new Parser<>(input->{
            Monad<ArrayList<Command>> callThenPipe = this.sepBySymbol(this.call(), this.pipe(), '|',(commandList1,commandList2)->{
                Command pipe = new Pipe(commandList1,commandList2.get(0).getCommands());
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

    public Monad<ArrayList<Command>> call(){
        return new Parser<>(input->{
            return this.bind(this.many(this.or(quoted(), nonKeyword())),strings->{
                StringBuilder builder = new StringBuilder();
                ArrayList<Command> arr = new ArrayList<>();
                for (String str:strings){
                    builder.append(str);
                }
                ArrayList<String> result = this.parseCall().parse(builder.toString()).getValue();
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

    /*
        parseCall = many (isChar(' ') ++ isChar('\t')) bind x/->
    */
    public Monad<ArrayList<String>> parseCall(){
        return new Parser<>(input->{
            return this.bind(this.manySpaces(), emptySpaces1->{
                return this.bind(this.many(this.bind(this.redirection(),x->{return this.bind(this.manySpaces(),spaces3->{return this.result(x);});})), redirections->{
                    return this.bind(this.argument(), argument->{
                        return this.bind(this.many(this.bind(this.manySpaces(),spaces4->{return this.atom();})), atoms->{
                            return this.bind(this.manySpaces(), emptySpaces2->{
                                ArrayList<String> result = new ArrayList<>();
                                for (ArrayList<String> redirection:redirections){
                                    result.addAll(redirection);
                                }
                                result.add(argument);
                                for (ArrayList<String> atom:atoms){
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

    private Monad<ArrayList<String>> redirection(){
        return new Parser<>(input->{
            return this.bind(this.or(this.isChar('<'),this.isChar('>')), symbol->{
                return this.bind(this.manySpaces(),spaces->{
                    return this.bind(this.argument(),argument->{
                        ArrayList<String> result = new ArrayList<>();
                        result.add(Character.toString(symbol));
                        result.add(argument);
                        return this.result(result);
                    });
                });
            }).parse(input);
        });
    }

    private Monad<ArrayList<String>> atom(){
        return new Parser<>(input->{
            return this.or(this.redirection(),this.bind(this.argument(),argument->{
                ArrayList<String> result = new ArrayList<>();
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

    private Monad<String> argument(){
        return new Parser<>(input->{
            return this.bind(this.many1(this.or(this.quoted(), this.unQuoted())),ls->{
                StringBuilder builder = new StringBuilder();
                for (String str:ls){
                    builder.append(str);
                }
                return this.result(builder.toString());
            }).parse(input);
        });
    }
    public static void main(String[] args) {
        ParserBuilder p = new ParserBuilder();
        Monad<ArrayList<Command>> sat = p.parseCommand();
        //Monad<ArrayList<String>> sat = p.parseCall();
        //System.out.println("Result: "+sat.parse("   < abc > abc abc sdf abc").getValue());
        System.out.println("Input:    < abc > abc abc sdf abc;abc|bbc|abc;abc");
        System.out.println("Result: "+sat.parse("cat test1.txt | > test5.txt").getValue());
        System.out.println("Input Left: "+sat.parse("cat test1.txt | > test5.txt").getInputStream());
    }
}