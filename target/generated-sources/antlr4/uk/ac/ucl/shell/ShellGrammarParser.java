// Generated from uk/ac/ucl/shell/ShellGrammar.g4 by ANTLR 4.7
package uk.ac.ucl.shell;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ShellGrammarParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, NONSPECIAL=2, DOUBLEQUOTED=3, SINGLEQUOTED=4;
	public static final int
		RULE_command = 0, RULE_atomicCommand = 1;
	public static final String[] ruleNames = {
		"command", "atomicCommand"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "';'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, "NONSPECIAL", "DOUBLEQUOTED", "SINGLEQUOTED"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "ShellGrammar.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ShellGrammarParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class CommandContext extends ParserRuleContext {
		public List<AtomicCommandContext> atomicCommand() {
			return getRuleContexts(AtomicCommandContext.class);
		}
		public AtomicCommandContext atomicCommand(int i) {
			return getRuleContext(AtomicCommandContext.class,i);
		}
		public CommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_command; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ShellGrammarVisitor ) return ((ShellGrammarVisitor<? extends T>)visitor).visitCommand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CommandContext command() throws RecognitionException {
		CommandContext _localctx = new CommandContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_command);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(4);
			atomicCommand();
			setState(9);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(5);
				match(T__0);
				setState(6);
				atomicCommand();
				}
				}
				setState(11);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AtomicCommandContext extends ParserRuleContext {
		public List<TerminalNode> NONSPECIAL() { return getTokens(ShellGrammarParser.NONSPECIAL); }
		public TerminalNode NONSPECIAL(int i) {
			return getToken(ShellGrammarParser.NONSPECIAL, i);
		}
		public List<TerminalNode> DOUBLEQUOTED() { return getTokens(ShellGrammarParser.DOUBLEQUOTED); }
		public TerminalNode DOUBLEQUOTED(int i) {
			return getToken(ShellGrammarParser.DOUBLEQUOTED, i);
		}
		public List<TerminalNode> SINGLEQUOTED() { return getTokens(ShellGrammarParser.SINGLEQUOTED); }
		public TerminalNode SINGLEQUOTED(int i) {
			return getToken(ShellGrammarParser.SINGLEQUOTED, i);
		}
		public AtomicCommandContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atomicCommand; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ShellGrammarVisitor ) return ((ShellGrammarVisitor<? extends T>)visitor).visitAtomicCommand(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AtomicCommandContext atomicCommand() throws RecognitionException {
		AtomicCommandContext _localctx = new AtomicCommandContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_atomicCommand);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(13); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(12);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NONSPECIAL) | (1L << DOUBLEQUOTED) | (1L << SINGLEQUOTED))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(15); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << NONSPECIAL) | (1L << DOUBLEQUOTED) | (1L << SINGLEQUOTED))) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\6\24\4\2\t\2\4\3"+
		"\t\3\3\2\3\2\3\2\7\2\n\n\2\f\2\16\2\r\13\2\3\3\6\3\20\n\3\r\3\16\3\21"+
		"\3\3\2\2\4\2\4\2\3\3\2\4\6\2\23\2\6\3\2\2\2\4\17\3\2\2\2\6\13\5\4\3\2"+
		"\7\b\7\3\2\2\b\n\5\4\3\2\t\7\3\2\2\2\n\r\3\2\2\2\13\t\3\2\2\2\13\f\3\2"+
		"\2\2\f\3\3\2\2\2\r\13\3\2\2\2\16\20\t\2\2\2\17\16\3\2\2\2\20\21\3\2\2"+
		"\2\21\17\3\2\2\2\21\22\3\2\2\2\22\5\3\2\2\2\4\13\21";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}