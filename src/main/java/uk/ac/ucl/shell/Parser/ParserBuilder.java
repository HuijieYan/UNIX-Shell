package uk.ac.ucl.shell.Parser;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;

import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;
import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;

public class ParserBuilder {
    public ArrayList<Character> spaces = new ArrayList<>();
    public ParserBuilder(){
        spaces.add(' ');
        spaces.add('\t');
    }

    public <T> Monad<T> result(final T value){
        Function<String,MonadicValue<T,String>> result = input -> {
                return new Pair<>(value, input);
        };
        //Set a function that returns a pair of input and preset value
        return new Parser<T>(result);
    }

    public <T,A> Monad<A> bind(final Monad<T> parser, final Function<T,Monad<A>> function){
        return new Parser<>(inp ->{
            MonadicValue<T,String> pair = parser.parse(inp);
            //use the first parser to parse the input
            if (pair.isEmpty()){
                Monad<A> zero = this.zero();
                return zero.parse(inp);
            //eg. bind(item,/x->result x) "" would return zero if the first parsing has failed
            }
            return function.apply(pair.getValue()).parse(pair.getInputStream());
            //use the returned value to get the second parser and then parse the rest of the input
        });
    }

    public <T> Monad<T> zero(){
        return new Parser<T>(input -> {
            return new Pair<T,String>(null, "");
        });
        //return a parser that returns an empty pair no matter what the input is
    }

    public Monad<Character> item(){
        Function<String,MonadicValue<Character,String>> item = input -> {
            if (input.length() > 0){
                return new Pair<Character,String>(input.charAt(0), input.substring(1));
            }
            return new Pair<Character,String>(null, "");
            //return an empty pair
        };
        return new Parser<>(item);
        //returns a parser that will return the first chararcter of the input as value
    }

    public Monad<Character> sat(final Function<Character,Boolean> validation){
        Function<String,MonadicValue<Character,String>> satisfy = input ->{
            return this.bind(this.item(),value ->{
                if (validation.apply(value)){
                    return this.result(value);
                }else{
                    return this.zero();
                }
            }).parse(input);
        };
        return new Parser<>(satisfy);
    }

    public Monad<Character> isChar(final Character value){
    //identify if the first character of the input matches the value
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

    /*
    many p = p x bind /_ ->
            many p xs bind /_ -> 
            result (x:xs)
    eg. many char('a') "aab" = result(['a','a']) "b"
    */
    public <T> Monad<Deque<T>> many(final Monad<T> parser){
        // Function many would let the parser keep parsing the input until
        // the end of input is reached or the parsing has failed.
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

    public <T> Monad<Deque<T>> many1(final Monad<T> parser){
    // the parser must be parsed successfully at least once 
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

    public Monad<Character> anyCharacterExcept(final ArrayList<Character> exceptCharacters){
        Function<Character,Boolean> function = input->{
            for (char character : exceptCharacters){
                if (input == character){
                    return false;
                }
            }
            return true;
        };
        return this.sat(function);
    }
    
    /*
        symbolQuoted s = char s bind x/->
                        many1 anyCharacterExcept bind y/->
                        char s bind z/->
                        result(x:y:z) 
    */
    public Monad<String> symbolQuoted(char symbol){
    //assumes the content between symbols excludes the symbol
        return new Parser<>(input->{
            return this.bind(this.isChar(symbol), x->{
                ArrayList<Character> exception = new ArrayList<>();
                exception.add(symbol);
                exception.add('\n');
                return this.bind(this.many1(anyCharacterExcept(exception)), y->{
                    return this.bind(this.isChar(symbol),z->{
                        y.add(z);
                        y.addFirst(x);
                        StringBuilder builder = new StringBuilder();
                        for(char letter:y){
                            builder.append(letter);
                        }
                        return result(builder.toString());
                    });
                });
            }).parse(input);
        });
    }

    public Monad<String> symbolQuoted(char symbol,ArrayList<Character> exception){
    //exception is the list of unwanted characters except the symbol between the symbol
        return new Parser<>(input->{
            return this.bind(this.isChar(symbol), x->{
                exception.add(symbol);
                return this.bind(this.many1(anyCharacterExcept(exception)), y->{
                    return this.bind(this.isChar(symbol),z->{
                        y.add(z);
                        y.addFirst(x);
                        StringBuilder builder = new StringBuilder();
                        for(char letter:y){
                            builder.append(letter);
                        }
                        return result(builder.toString());
                    });
                });
            }).parse(input);
        });
    }

    public Monad<String> singleQuoted(){
        return new Parser<>(input->{
            return symbolQuoted('\'').parse(input);
        });
    }

    public Monad<String> backQuoted(){
        return new Parser<>(input->{
            return symbolQuoted('`').parse(input);
        });
    }

    public Monad<String> doubleQuoted(){
        return new Parser<>(input->{
            return this.bind(this.isChar('\"'), x->{
                return this.bind(many(or(backQuoted(),doubleQuotedContent())), y->{
                    return this.bind(this.isChar('\"'),z->{
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

    public Monad<String> doubleQuotedContent(){
    //parses a string that does not includes spaces, back quotes nor double quotes
        return new Parser<>(input->{
            ArrayList<Character> exception = new ArrayList<>();
            exception.add('`');
            exception.add('\"');
            exception.add('\n');
            return this.bind(this.many1(anyCharacterExcept(exception)), y->{
                //got a list of characters after parsing
                    StringBuilder builder = new StringBuilder();
                    for(char letter:y){
                        builder.append(letter);
                    }
                    return result(builder.toString());
                }).parse(input);
        });
    }

    public Monad<String> quoted(){
        return new Parser<>(input->{
            Monad<String> p = this.or(this.backQuoted(),this.doubleQuoted());
            return this.or(this.singleQuoted(),p).parse(input);
        });
    }

    private Monad<String> nonKeyword(){
    //parses any string containing characters except for newlines, single quotes, double quotes, backquotes, semicolons and vertical bars
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
        //parses any string containing characters except for white spaces,newlines, quotes, less than, greater than, semicolons and vertical bars
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
    lexSeq = pSepp (lexPipe ++ lexCall) lexCommand ';'
    */
    /*
    public Monad<String> lexSeq(){
        return this.pSepp(this.or(this.lexPipe(),this.lexCall()), lexCommand(), ';');
    }
    
    public Monad<String> lexCall(){
        return new Parser<>(input->{
            return this.bind(this.many(this.or(quoted(), nonKeyword())),strings->{
                StringBuilder builder = new StringBuilder();
                for (String str:strings){
                    builder.append(str);
                }
                return this.result(builder.toString());
            }).parse(input);
        });
    }
    */
    /*
    pSepp p1 p2 symbol = p1 bind x/->
                isChar(symbol) bind y/->
                p2 bind z/->
                result (x:y:z)
    */
    /*
    private Monad<String> pSepp(Monad<String> parser1,Monad<String> parser2,char symbol){
        return new Parser<>(input->{
            return this.bind(parser1, x->{
                return this.bind(isChar(symbol), y->{
                    return this.bind(parser2,z->{
                        StringBuilder builder = new StringBuilder(x);
                        builder.append(y);
                        builder.append(z);
                        return this.result(builder.toString());
                    });
                });
            }).parse(input);
        });
    }
    */

    /*
    lexPipe = (pSepp lexCall lexPipe '|') ++ (pSepplexCall lexCall lexCall '|')
    */
    /*
    public Monad<String> lexPipe(){
        return new Parser<>(input->{
            return this.or(this.pSepp(this.lexCall(), this.lexPipe(), '|'),
            this.pSepp(this.lexCall(), this.lexCall(), '|')).parse(input);
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
    /*
    public Monad<String> lexCommand(){
        return new Parser<>(input->{
            Monad<String> command = this.or(this.or(this.lexSeq(),this.lexPipe()),this.lexCall());
            return command.parse(input);
        });
    }
    */

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

    //public Monad<ArrayList<ArrayList<ArrayList<String>>>>

    /*
        parseCall = many (isChar(' ') ++ isChar('\t')) bind x/->

    */
    public Monad<ArrayList<String>> parseCall(){
        return new Parser<>(input->{
            return this.bind(this.manySpaces(), spaces1->{
                return this.bind(this.many(this.bind(this.redirection(),x->{return this.bind(this.manySpaces(),spaces3->{return this.result(x);});})), redirections->{
                    return this.bind(this.argument(), argument->{
                        return this.bind(this.many(this.bind(this.manySpaces(),spaces4->{return this.atom();})), atoms->{
                            return this.bind(this.manySpaces(), spaces2->{
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
        Monad<ArrayList<ArrayList<ArrayList<String>>>> sat = p.lexCommand();
        //Monad<ArrayList<String>> sat = p.parseCall();
        //System.out.println("Result: "+sat.parse("   < abc > abc abc sdf abc").getValue());
        System.out.println("Input:    < abc > abc abc sdf abc;abc|bbc|abc;abc");
        System.out.println("Result: "+sat.parse("   < abc > abc abc sdf abc;abc|bbc|abc;abc").getValue());
        System.out.println("Input Left: "+sat.parse("   < abc > abc abc sdf abc;abc|abc|abc;abc").getInputStream());
    }
}
