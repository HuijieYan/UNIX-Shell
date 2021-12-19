// Generated from uk/ac/ucl/shell/ShellGrammar.g4 by ANTLR 4.7
package uk.ac.ucl.shell;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ShellGrammarParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ShellGrammarVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ShellGrammarParser#command}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCommand(ShellGrammarParser.CommandContext ctx);
	/**
	 * Visit a parse tree produced by {@link ShellGrammarParser#atomicCommand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomicCommand(ShellGrammarParser.AtomicCommandContext ctx);
}