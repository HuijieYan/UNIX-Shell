package uk.ac.ucl.shell;

import java.util.ArrayList;
import java.util.Deque;
import java.util.function.BiFunction;

import uk.ac.ucl.shell.Parser.Monad;
import uk.ac.ucl.shell.Parser.Parser;
import uk.ac.ucl.shell.Parser.ParserBuilder;
import uk.ac.ucl.shell.Parser.pack.command.Call;
import uk.ac.ucl.shell.Parser.pack.command.Command;
import uk.ac.ucl.shell.Parser.pack.command.Pipe;
import uk.ac.ucl.shell.Parser.pack.type.MonadicValue;
import uk.ac.ucl.shell.Parser.pack.type.atom.Atom;
import uk.ac.ucl.shell.Parser.pack.type.atom.NonRedirectionString;
import uk.ac.ucl.shell.Parser.pack.type.atom.RedirectionSymbol;
import uk.ac.ucl.shell.Parser.pack.type.pair.Pair;


public class ShellParser {
    private final ArrayList<Character> spaces = new ArrayList<>();
    private final ParserBuilder parserBuilder = new ParserBuilder();

    public ShellParser(){
        spaces.add(' ');
        spaces.add('\t');
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
            return parserBuilder.bind(parserBuilder.isChar(symbol), x->{
                return parserBuilder.bind(parser, y->{
                    return parserBuilder.bind(parserBuilder.isChar(symbol),z->{
                        StringBuilder builder = new StringBuilder();
                        builder.append(x);
                        builder.append(y);
                        builder.append(z);

                        return parserBuilder.result(builder.toString());
                    });
                });
            }).parse(input);
        });
    }

    public Monad<String> symbolQuoted(Monad<Deque<String>> parser,char symbol){
    //This is a different version of symbolQuoted parser, the content between
    //given symbol is a combination of several strings.
    //swap the order of argument for method overloading
        return new Parser<>(input->{
            return parserBuilder.bind(parserBuilder.isChar(symbol), x->{
                return parserBuilder.bind(parser, y->{
                    return parserBuilder.bind(parserBuilder.isChar(symbol),z->{
                        StringBuilder builder = new StringBuilder();
                        builder.append(x);
                        for(String str:y){
                            builder.append(str);
                        }
                        builder.append(z);

                        return parserBuilder.result(builder.toString());
                    });
                });
            }).parse(input);
        });
    }

    /**
     * For parsing strings that are between quoted symbols, it allows any characters except
     * characters given by the parameter.
     *
     * @param exception a list of characters that should not appear in the input, can be empty
     */
    public Monad<String> quotedContent(ArrayList<Character> exception){
        return new Parser<>(input->{
            return parserBuilder.bind(parserBuilder.many1(parserBuilder.anyCharacterExcept(exception)), y->{
                StringBuilder builder = new StringBuilder();
                for(char letter:y){
                    builder.append(letter);
                }

                return parserBuilder.result(builder.toString());
            }).parse(input);
        });
    }

    public Monad<String> singleQuoted(){
        return new Parser<>(input->{
            ArrayList<Character> exception = new ArrayList<>();
            exception.add('\'');
            exception.add('\n');
            Monad<String> contentParser = this.quotedContent(exception);

            return this.symbolQuoted('\'',contentParser).parse(input);
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
        double-quoted content can be many strings that can be back-quoted string or
        strings that does not include new lines, back quotes nor double quotes
    */
        return new Parser<>(input->{
            ArrayList<Character> exception = new ArrayList<>();
            exception.add('`');
            exception.add('\"');
            exception.add('\n');
            Monad<Deque<String>> doubleQuotedContent = parserBuilder.many(parserBuilder.or(this.backQuoted(),this.quotedContent(exception)));

            return this.symbolQuoted(doubleQuotedContent,'\"').parse(input);
        });
    }

    public Monad<String> quoted(){
        return new Parser<>(input->{
            Monad<String> backQuotedOrDoubleQuoted = parserBuilder.or(this.backQuoted(),this.doubleQuoted());
            return parserBuilder.or(this.singleQuoted(),backQuotedOrDoubleQuoted).parse(input);
        });
    }

    private Monad<String> nonKeyword(){
    //parses a string containing any characters except for newlines, single quotes, double quotes,
    // back quotes, semicolons and vertical bars
        return new Parser<>(input->{
            ArrayList<Character> ls = new ArrayList<>();
            ls.add('\n');
            ls.add('\'');
            ls.add('\"');
            ls.add('`');
            ls.add(';');
            ls.add('|');

            return parserBuilder.bind(parserBuilder.many1(parserBuilder.anyCharacterExcept(ls)),chars->{
                //got a list of characters after parsing
                StringBuilder builder = new StringBuilder();
                for (char ch:chars){
                    builder.append(ch);
                }

                return parserBuilder.result(builder.toString());
            }).parse(input);
        });
    }

    public Monad<String> unQuoted(){
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

            return parserBuilder.bind(parserBuilder.many1(parserBuilder.anyCharacterExcept(ls)),chars->{
                //got a list of characters after parsing
                StringBuilder builder = new StringBuilder();
                for (char ch:chars){
                    builder.append(ch);
                }

                return parserBuilder.result(builder.toString());
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
     * Call or Pipe, thus the list actually represents the sequence of commands,
     * list can be empty but not null
     */
    public Monad<ArrayList<Command>> parseCommand(){
        return new Parser<>(input->{
            Monad<ArrayList<Command>> command = parserBuilder.or(parserBuilder.or(this.seq(),this.pipe()),this.call());
            return command.parse(input);
        });
    }

    /**
     * A generalisation parser for grammars such as pipe and seq, to let this parser successfully parse
     * the input must be successfully parsed by following parsers in this order: parser1, isChar(symbol),
     * parser2. Then the result value would be returned by a way defined by parameter function.
     *
     * @param parser1/parser2 returns a list of commands as parsed value, parser cannot be null,
     * list can be empty but not null
     * @param symbol symbol that separates string of parser1 and string of parser2, not empty nor null
     * @param function takes in the parsed value of both parsers, returns a Monad that returns a list
     * of commands
     * @return returns what the function returns, list can be null indicating parsing unsuccessful
     * but not empty
     */
    private Monad<ArrayList<Command>> sepBySymbol(Monad<ArrayList<Command>> parser1,Monad<ArrayList<Command>> parser2,
                                                  char symbol,BiFunction<ArrayList<Command>,ArrayList<Command>,
                                                    Monad<ArrayList<Command>>> function){
        return new Parser<>(input->{
            return parserBuilder.bind(parser1, x->{
                return parserBuilder.bind(parserBuilder.isChar(symbol), y->{
                    return parserBuilder.bind(parser2,z->{
                        return function.apply(x, z);
                    });
                });
            }).parse(input);
        });
    }

    public Monad<ArrayList<Command>> seq(){
        return this.sepBySymbol(parserBuilder.or(this.pipe(),this.call()), this.parseCommand(), ';',(commandList1,commandList2)->{
            commandList1.addAll(commandList2);
            return parserBuilder.result(commandList1);
        });
    }

    public Monad<ArrayList<Command>> pipe(){
        return new Parser<>(input->{
            Monad<ArrayList<Command>> callThenPipe = this.sepBySymbol(this.call(), this.pipe(), '|',(commandList1,commandList2)->{
                Command pipe = new Pipe(commandList1, ((Pipe)commandList2.get(0)).getCommands());
                // commandList2 is a list with Pipe command only, so we get the list of calls in the Pipe object and then
                // put it into the constructor to create a new Pipe object
                ArrayList<Command> arr = new ArrayList<>();
                arr.add(pipe);
                return parserBuilder.result(arr);
            });

            Monad<ArrayList<Command>> callThenCall = this.sepBySymbol(this.call(), this.call(), '|',(commandList1,commandList2)->{
                Command pipe = new Pipe(commandList1,commandList2);
                ArrayList<Command> arr = new ArrayList<>();
                arr.add(pipe);
                return parserBuilder.result(arr);
            });

            return parserBuilder.or(callThenPipe,callThenCall).parse(input);
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
            return parserBuilder.bind(parserBuilder.many(parserBuilder.or(quoted(), nonKeyword())),strings->{
                ArrayList<Command> arr = new ArrayList<>();
                ArrayList<Atom> value = callGetAtoms(strings);
                if (value == null){
                    return parserBuilder.zero();
                }

                Command call = new Call(value);
                arr.add(call);

                return parserBuilder.result(arr);
            }).parse(input);
        });
    }

    private ArrayList<Atom> callGetAtoms(Deque<String> strings){
        StringBuilder builder = new StringBuilder();
        for (String str:strings){
            builder.append(str);
        }

        MonadicValue<ArrayList<Atom>,String> result = this.parseCall().parse(builder.toString());
        ArrayList<Atom> value = result.getValue();
        String restOfInput = result.getInputStream();
        if (value == null || restOfInput.length() > 0){
            //We cannot tolerate call commands that are not being fully parsed because the string
            //provided to the parser suits the grammar <call> ::= ( <non-keyword> | <quoted> ) *.
            //When we still have input left, this means parsing has failed.
            return null;
        }

        return value;
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
            return parserBuilder.bind(this.manySpaces(), emptySpaces1->{
                return parserBuilder.bind(this.redirectionWithSpaces(), redirections->{
                    return parserBuilder.bind(this.parseArgument(), argument->{
                        return parserBuilder.bind(this.atomWithSpaces(), atoms->{
                            return parserBuilder.bind(this.manySpaces(), emptySpaces2->{
                                ArrayList<Atom> result = new ArrayList<>();
                                for (ArrayList<Atom> redirection:redirections){
                                    result.addAll(redirection);
                                }

                                result.add(argument);

                                for (ArrayList<Atom> atom:atoms){
                                    result.addAll(atom);
                                }

                                return parserBuilder.result(result);
                            });
                        });
                    });
                });
            }).parse(input);
        });
    }

    private Monad<Deque<ArrayList<Atom>>> atomWithSpaces(){
        return parserBuilder.many(parserBuilder.bind(this.many1Spaces(),spaces->{
            return parserBuilder.bind(this.atom(),x->{
                return parserBuilder.result(x);
            });
        }));
    }

    private Monad<Deque<ArrayList<Atom>>> redirectionWithSpaces(){
    //represents [<redirection> <whitespace>] in BNF
        return parserBuilder.many(parserBuilder.bind(this.redirection(),x->{
            return parserBuilder.bind(this.many1Spaces(),spaces->{
                return parserBuilder.result(x);
            });
        }));
    }

    public Monad<ArrayList<Atom>> redirection(){
    //represents <redirection> ::= "<" [ <whitespace> ] <argument> in BNF
        return new Parser<>(input->{
            return parserBuilder.bind(parserBuilder.or(parserBuilder.isChar('<'),parserBuilder.isChar('>')), symbol->{
                return parserBuilder.bind(this.manySpaces(),spaces->{
                    return parserBuilder.bind(this.argument(),argument->{
                        ArrayList<Atom> result = new ArrayList<>();
                        Atom symbolAtom = new RedirectionSymbol(symbol);
                        Atom argumentAtom = new NonRedirectionString(argument);

                        result.add(symbolAtom);
                        result.add(argumentAtom);

                        return parserBuilder.result(result);
                    });
                });
            }).parse(input);
        });
    }

    public Monad<ArrayList<Atom>> atom(){
    //represents <atom> ::= <redirection> | <argument> in BNF
        return new Parser<>(input->{
            return parserBuilder.or(this.redirection(),parserBuilder.bind(this.parseArgument(),argument->{
                ArrayList<Atom> result = new ArrayList<>();
                result.add(argument);

                return parserBuilder.result(result);
            })).parse(input);
        });
    }

    private Monad<Deque<Character>> many1Spaces(){
        return new Parser<>(input->{
            return parserBuilder.many1(parserBuilder.or(parserBuilder.isChar(' '),parserBuilder.isChar('\t'))).parse(input);
        });
    }

    private Monad<Deque<Character>> manySpaces(){
    //parses multiple spaces formed by whitespaces and tabs(\t)
        return new Parser<>(input->{
            return parserBuilder.many(parserBuilder.or(parserBuilder.isChar(' '),parserBuilder.isChar('\t'))).parse(input);
        });
    }

    public Monad<Atom> parseArgument(){
        return parserBuilder.bind(this.argument(),argument->{
            Atom argumentAtom = new NonRedirectionString(argument);
            return parserBuilder.result(argumentAtom);
        });
    }

    private Monad<ArrayList<String>> argument(){
    //represents <argument> ::= ( <quoted> | <unquoted> )+
        return new Parser<>(input->{
            return parserBuilder.bind(parserBuilder.many1(parserBuilder.or(this.quoted(), this.unQuoted())),ls->{
                ArrayList<String> list = new ArrayList<>(ls);
                return parserBuilder.result(list);
            }).parse(input);
        });
    }

    public Monad<ArrayList<String>> decodeDoubleQuoted(){
    //Takes in a string between double quotes and returns a list of double-quoted content and back quoted string
        return new Parser<>(input->{
            ArrayList<Character> exception = new ArrayList<>();
            exception.add('`');
            exception.add('\"');
            exception.add('\n');
            Monad<Deque<String>> doubleQuotedContent = parserBuilder.many(parserBuilder.or(this.backQuoted(),this.quotedContent(exception)));

            ArrayList<String> result = new ArrayList<>(doubleQuotedContent.parse(input).getValue());
            return parserBuilder.result(result).parse(input);
        });
    }
}
