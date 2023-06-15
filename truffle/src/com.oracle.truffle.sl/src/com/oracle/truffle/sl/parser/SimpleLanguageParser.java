/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
// Checkstyle: stop
//@formatter:off
package com.oracle.truffle.sl.parser;

// DO NOT MODIFY - generated from SimpleLanguage.g4 using "mx create-sl-parser"

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.graalvm.collections.Pair;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLRootNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.parser.SLParseError;

import static com.oracle.truffle.sl.parser.SimpleLanguageParserSupport.getMapSetPair;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings("all")
public class SimpleLanguageParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, WS=35, COMMENT=36, LINE_COMMENT=37, IDENTIFIER=38, 
		STRING_LITERAL=39, NUMERIC_LITERAL=40;
	public static final int
		RULE_simplelanguage = 0, RULE_function = 1, RULE_at_mark = 2, RULE_block = 3, 
		RULE_statement = 4, RULE_while_statement = 5, RULE_if_statement = 6, RULE_return_statement = 7, 
		RULE_expression = 8, RULE_logic_big_term = 9, RULE_logic_term = 10, RULE_logic_factor = 11, 
		RULE_arithmetic = 12, RULE_term = 13, RULE_factor = 14, RULE_member_expression = 15;
	private static String[] makeRuleNames() {
		return new String[] {
			"simplelanguage", "function", "at_mark", "block", "statement", "while_statement", 
			"if_statement", "return_statement", "expression", "logic_big_term", "logic_term", 
			"logic_factor", "arithmetic", "term", "factor", "member_expression"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'function'", "'('", "','", "')'", "'@'", "'{'", "'}'", "'break'", 
			"';'", "'continue'", "'debugger'", "'while'", "'if'", "'else'", "'return'", 
			"'delete'", "'insert'", "'replace'", "'||'", "'&&'", "'<'", "'<='", "'>'", 
			"'>='", "'=='", "'!='", "'+'", "'-'", "'*'", "'/'", "'='", "'.'", "'['", 
			"']'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, "WS", 
			"COMMENT", "LINE_COMMENT", "IDENTIFIER", "STRING_LITERAL", "NUMERIC_LITERAL"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
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
	public String getGrammarFileName() { return "SimpleLanguage.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


	SLNodeFactory factory;
	private Source source;

	private static final class BailoutErrorListener extends BaseErrorListener {
	    private final Source source;
	    BailoutErrorListener(Source source) {
	        this.source = source;
	    }
	    @Override
	    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
	        throwParseError(source, line, charPositionInLine, (Token) offendingSymbol, msg);
	    }
	}

	public void SemErr(Token token, String message) {
	    assert token != null;
	    throwParseError(source, token.getLine(), token.getCharPositionInLine(), token, message);
	}

	private static void throwParseError(Source source, int line, int charPositionInLine, Token token, String message) {
	    int col = charPositionInLine + 1;
	    String location = "-- line " + line + " col " + col + ": ";
	    int length = token == null ? 1 : Math.max(token.getStopIndex() - token.getStartIndex(), 0);
	    throw new SLParseError(source, line, col, length, String.format("Error(s) parsing script:%n" + location + message));
	}

	public static Pair<Map<String, RootCallTarget>, Set<String>> parseSL(SLLanguage language, Source source) {
	    SimpleLanguageLexer lexer = new SimpleLanguageLexer(CharStreams.fromString(source.getCharacters().toString()));
	    SimpleLanguageParser parser = new SimpleLanguageParser(new CommonTokenStream(lexer));
	    lexer.removeErrorListeners();
	    parser.removeErrorListeners();
	    BailoutErrorListener listener = new BailoutErrorListener(source);
	    lexer.addErrorListener(listener);
	    parser.addErrorListener(listener);
	    parser.factory = new SLNodeFactory(language, source);
	    parser.source = source;
	    parser.simplelanguage();

		return getMapSetPair(parser);
	}

	public SimpleLanguageParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class SimplelanguageContext extends ParserRuleContext {
		public List<FunctionContext> function() {
			return getRuleContexts(FunctionContext.class);
		}
		public FunctionContext function(int i) {
			return getRuleContext(FunctionContext.class,i);
		}
		public TerminalNode EOF() { return getToken(SimpleLanguageParser.EOF, 0); }
		public SimplelanguageContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simplelanguage; }
	}

	public final SimplelanguageContext simplelanguage() throws RecognitionException {
		SimplelanguageContext _localctx = new SimplelanguageContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_simplelanguage);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			function();
			setState(36);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(33);
				function();
				}
				}
				setState(38);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(39);
			match(EOF);
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

	public static class FunctionContext extends ParserRuleContext {
		public Token IDENTIFIER;
		public Token s;
		public At_markContext g;
		public BlockContext body;
		public List<TerminalNode> IDENTIFIER() { return getTokens(SimpleLanguageParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(SimpleLanguageParser.IDENTIFIER, i);
		}
		public At_markContext at_mark() {
			return getRuleContext(At_markContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_function);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			match(T__0);
			setState(42);
			_localctx.IDENTIFIER = match(IDENTIFIER);
			setState(43);
			_localctx.s = match(T__1);
			 factory.startFunction(_localctx.IDENTIFIER, _localctx.s); 
			setState(55);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(45);
				_localctx.IDENTIFIER = match(IDENTIFIER);
				 factory.addFormalParameter(_localctx.IDENTIFIER); 
				setState(52);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__2) {
					{
					{
					setState(47);
					match(T__2);
					setState(48);
					_localctx.IDENTIFIER = match(IDENTIFIER);
					 factory.addFormalParameter(_localctx.IDENTIFIER); 
					}
					}
					setState(54);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(57);
			match(T__3);
			setState(58);
			_localctx.g = at_mark();
			 if (_localctx.g.result) factory.dontFlag(); 
			setState(60);
			_localctx.body = block(false);
			 factory.finishFunction(_localctx.body.result); 
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

	public static class At_markContext extends ParserRuleContext {
		public boolean result;
		public At_markContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_at_mark; }
	}

	public final At_markContext at_mark() throws RecognitionException {
		At_markContext _localctx = new At_markContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_at_mark);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				{
				setState(63);
				match(T__4);
				 _localctx.result =  true; 
				}
				break;
			case T__5:
				{
				 _localctx.result =  false; 
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class BlockContext extends ParserRuleContext {
		public boolean inLoop;
		public SLStatementNode result;
		public Token s;
		public StatementContext statement;
		public Token e;
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public BlockContext(ParserRuleContext parent, int invokingState, boolean inLoop) {
			super(parent, invokingState);
			this.inLoop = inLoop;
		}
		@Override public int getRuleIndex() { return RULE_block; }
	}

	public final BlockContext block(boolean inLoop) throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState(), inLoop);
		enterRule(_localctx, 6, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			 factory.startBlock();
			                                                  List<SLStatementNode> body = new ArrayList<>(); 
			setState(69);
			_localctx.s = match(T__5);
			setState(75);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__7) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__14) | (1L << T__15) | (1L << T__16) | (1L << T__17) | (1L << IDENTIFIER) | (1L << STRING_LITERAL) | (1L << NUMERIC_LITERAL))) != 0)) {
				{
				{
				setState(70);
				_localctx.statement = statement(inLoop);
				 body.add(_localctx.statement.result); 
				}
				}
				setState(77);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(78);
			_localctx.e = match(T__6);
			 _localctx.result =  factory.finishBlock(body, _localctx.s.getStartIndex(), _localctx.e.getStopIndex() - _localctx.s.getStartIndex() + 1); 
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

	public static class StatementContext extends ParserRuleContext {
		public boolean inLoop;
		public SLStatementNode result;
		public While_statementContext while_statement;
		public Token b;
		public Token c;
		public If_statementContext if_statement;
		public Return_statementContext return_statement;
		public ExpressionContext expression;
		public Token d;
		public While_statementContext while_statement() {
			return getRuleContext(While_statementContext.class,0);
		}
		public If_statementContext if_statement() {
			return getRuleContext(If_statementContext.class,0);
		}
		public Return_statementContext return_statement() {
			return getRuleContext(Return_statementContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public StatementContext(ParserRuleContext parent, int invokingState, boolean inLoop) {
			super(parent, invokingState);
			this.inLoop = inLoop;
		}
		@Override public int getRuleIndex() { return RULE_statement; }
	}

	public final StatementContext statement(boolean inLoop) throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState(), inLoop);
		enterRule(_localctx, 8, RULE_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__11:
				{
				setState(81);
				_localctx.while_statement = while_statement();
				 _localctx.result =  _localctx.while_statement.result; 
				}
				break;
			case T__7:
				{
				setState(84);
				_localctx.b = match(T__7);
				 if (inLoop) { _localctx.result =  factory.createBreak(_localctx.b); } else { SemErr(_localctx.b, "break used outside of loop"); } 
				setState(86);
				match(T__8);
				}
				break;
			case T__9:
				{
				setState(87);
				_localctx.c = match(T__9);
				 if (inLoop) { _localctx.result =  factory.createContinue(_localctx.c); } else { SemErr(_localctx.c, "continue used outside of loop"); } 
				setState(89);
				match(T__8);
				}
				break;
			case T__12:
				{
				setState(90);
				_localctx.if_statement = if_statement(inLoop);
				 _localctx.result =  _localctx.if_statement.result; 
				}
				break;
			case T__14:
				{
				setState(93);
				_localctx.return_statement = return_statement();
				 _localctx.result =  _localctx.return_statement.result; 
				}
				break;
			case T__1:
			case T__15:
			case T__16:
			case T__17:
			case IDENTIFIER:
			case STRING_LITERAL:
			case NUMERIC_LITERAL:
				{
				setState(96);
				_localctx.expression = expression();
				setState(97);
				match(T__8);
				 _localctx.result =  _localctx.expression.result; 
				}
				break;
			case T__10:
				{
				setState(100);
				_localctx.d = match(T__10);
				 _localctx.result =  factory.createDebugger(_localctx.d); 
				setState(102);
				match(T__8);
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class While_statementContext extends ParserRuleContext {
		public SLStatementNode result;
		public Token w;
		public ExpressionContext condition;
		public BlockContext body;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public While_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_while_statement; }
	}

	public final While_statementContext while_statement() throws RecognitionException {
		While_statementContext _localctx = new While_statementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_while_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(105);
			_localctx.w = match(T__11);
			setState(106);
			match(T__1);
			setState(107);
			_localctx.condition = expression();
			setState(108);
			match(T__3);
			setState(109);
			_localctx.body = block(true);
			 _localctx.result =  factory.createWhile(_localctx.w, _localctx.condition.result, _localctx.body.result); 
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

	public static class If_statementContext extends ParserRuleContext {
		public boolean inLoop;
		public SLStatementNode result;
		public Token i;
		public ExpressionContext condition;
		public BlockContext then;
		public BlockContext block;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public List<BlockContext> block() {
			return getRuleContexts(BlockContext.class);
		}
		public BlockContext block(int i) {
			return getRuleContext(BlockContext.class,i);
		}
		public If_statementContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public If_statementContext(ParserRuleContext parent, int invokingState, boolean inLoop) {
			super(parent, invokingState);
			this.inLoop = inLoop;
		}
		@Override public int getRuleIndex() { return RULE_if_statement; }
	}

	public final If_statementContext if_statement(boolean inLoop) throws RecognitionException {
		If_statementContext _localctx = new If_statementContext(_ctx, getState(), inLoop);
		enterRule(_localctx, 12, RULE_if_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			_localctx.i = match(T__12);
			setState(113);
			match(T__1);
			setState(114);
			_localctx.condition = expression();
			setState(115);
			match(T__3);
			setState(116);
			_localctx.then = _localctx.block = block(inLoop);
			 SLStatementNode elsePart = null; 
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__13) {
				{
				setState(118);
				match(T__13);
				setState(119);
				_localctx.block = block(inLoop);
				 elsePart = _localctx.block.result; 
				}
			}

			 _localctx.result =  factory.createIf(_localctx.i, _localctx.condition.result, _localctx.then.result, elsePart); 
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

	public static class Return_statementContext extends ParserRuleContext {
		public SLStatementNode result;
		public Token r;
		public ExpressionContext expression;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Return_statementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_return_statement; }
	}

	public final Return_statementContext return_statement() throws RecognitionException {
		Return_statementContext _localctx = new Return_statementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_return_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			_localctx.r = match(T__14);
			 SLExpressionNode value = null; 
			setState(131);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__15) | (1L << T__16) | (1L << T__17) | (1L << IDENTIFIER) | (1L << STRING_LITERAL) | (1L << NUMERIC_LITERAL))) != 0)) {
				{
				setState(128);
				_localctx.expression = expression();
				 value = _localctx.expression.result; 
				}
			}

			 _localctx.result =  factory.createReturn(_localctx.r, value); 
			setState(134);
			match(T__8);
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

	public static class ExpressionContext extends ParserRuleContext {
		public SLExpressionNode result;
		public Token u;
		public Token deleted;
		public Logic_big_termContext logic_big_term;
		public TerminalNode NUMERIC_LITERAL() { return getToken(SimpleLanguageParser.NUMERIC_LITERAL, 0); }
		public Logic_big_termContext logic_big_term() {
			return getRuleContext(Logic_big_termContext.class,0);
		}
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	}

	public final ExpressionContext expression() throws RecognitionException {
		ExpressionContext _localctx = new ExpressionContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_expression);
		try {
			setState(165);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__15:
				enterOuterAlt(_localctx, 1);
				{
				setState(136);
				_localctx.u = match(T__15);
				setState(137);
				match(T__1);
				{
				setState(138);
				_localctx.deleted = match(NUMERIC_LITERAL);
				 _localctx.result =  factory.createDelete(_localctx.deleted); 
				}
				setState(141);
				match(T__3);
				}
				break;
			case T__16:
				enterOuterAlt(_localctx, 2);
				{
				setState(142);
				_localctx.u = match(T__16);
				setState(143);
				match(T__1);
				{
				 factory.startNewExp(); 
				setState(145);
				_localctx.logic_big_term = logic_big_term();
				 _localctx.result =  factory.createInsert(_localctx.logic_big_term.result); 
				}
				 factory.endNewExp(); 
				setState(149);
				match(T__3);
				}
				break;
			case T__17:
				enterOuterAlt(_localctx, 3);
				{
				setState(151);
				_localctx.u = match(T__17);
				setState(152);
				match(T__1);
				{
				setState(153);
				_localctx.deleted = match(NUMERIC_LITERAL);
				setState(154);
				match(T__2);
				 factory.startNewExp(); 
				setState(156);
				_localctx.logic_big_term = logic_big_term();
				 _localctx.result =  factory.createReplace(_localctx.deleted, _localctx.logic_big_term.result); 
				}
				 factory.endNewExp(); 
				setState(160);
				match(T__3);
				}
				break;
			case T__1:
			case IDENTIFIER:
			case STRING_LITERAL:
			case NUMERIC_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(162);
				_localctx.logic_big_term = logic_big_term();
				 _localctx.result =  _localctx.logic_big_term.result; 
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class Logic_big_termContext extends ParserRuleContext {
		public SLExpressionNode result;
		public Logic_termContext logic_term;
		public Token op;
		public List<Logic_termContext> logic_term() {
			return getRuleContexts(Logic_termContext.class);
		}
		public Logic_termContext logic_term(int i) {
			return getRuleContext(Logic_termContext.class,i);
		}
		public Logic_big_termContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logic_big_term; }
	}

	public final Logic_big_termContext logic_big_term() throws RecognitionException {
		Logic_big_termContext _localctx = new Logic_big_termContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_logic_big_term);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			_localctx.logic_term = logic_term();
			 _localctx.result =  _localctx.logic_term.result; 
			setState(175);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(169);
					_localctx.op = match(T__18);
					setState(170);
					_localctx.logic_term = logic_term();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.logic_term.result); 
					}
					} 
				}
				setState(177);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
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

	public static class Logic_termContext extends ParserRuleContext {
		public SLExpressionNode result;
		public Logic_factorContext logic_factor;
		public Token op;
		public List<Logic_factorContext> logic_factor() {
			return getRuleContexts(Logic_factorContext.class);
		}
		public Logic_factorContext logic_factor(int i) {
			return getRuleContext(Logic_factorContext.class,i);
		}
		public Logic_termContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logic_term; }
	}

	public final Logic_termContext logic_term() throws RecognitionException {
		Logic_termContext _localctx = new Logic_termContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_logic_term);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(178);
			_localctx.logic_factor = logic_factor();
			 _localctx.result =  _localctx.logic_factor.result; 
			setState(186);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(180);
					_localctx.op = match(T__19);
					setState(181);
					_localctx.logic_factor = logic_factor();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.logic_factor.result); 
					}
					} 
				}
				setState(188);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
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

	public static class Logic_factorContext extends ParserRuleContext {
		public SLExpressionNode result;
		public ArithmeticContext arithmetic;
		public Token op;
		public List<ArithmeticContext> arithmetic() {
			return getRuleContexts(ArithmeticContext.class);
		}
		public ArithmeticContext arithmetic(int i) {
			return getRuleContext(ArithmeticContext.class,i);
		}
		public Logic_factorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logic_factor; }
	}

	public final Logic_factorContext logic_factor() throws RecognitionException {
		Logic_factorContext _localctx = new Logic_factorContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_logic_factor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			_localctx.arithmetic = arithmetic();
			 _localctx.result =  _localctx.arithmetic.result; 
			setState(195);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(191);
				_localctx.op = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__20) | (1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24) | (1L << T__25))) != 0)) ) {
					_localctx.op = _errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(192);
				_localctx.arithmetic = arithmetic();
				 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.arithmetic.result); 
				}
				break;
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

	public static class ArithmeticContext extends ParserRuleContext {
		public SLExpressionNode result;
		public TermContext term;
		public Token op;
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public ArithmeticContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arithmetic; }
	}

	public final ArithmeticContext arithmetic() throws RecognitionException {
		ArithmeticContext _localctx = new ArithmeticContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_arithmetic);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			_localctx.term = term();
			 _localctx.result =  _localctx.term.result; 
			setState(205);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(199);
					_localctx.op = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==T__26 || _la==T__27) ) {
						_localctx.op = _errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(200);
					_localctx.term = term();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.term.result); 
					}
					} 
				}
				setState(207);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
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

	public static class TermContext extends ParserRuleContext {
		public SLExpressionNode result;
		public FactorContext factor;
		public Token op;
		public List<FactorContext> factor() {
			return getRuleContexts(FactorContext.class);
		}
		public FactorContext factor(int i) {
			return getRuleContext(FactorContext.class,i);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_term);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(208);
			_localctx.factor = factor();
			 _localctx.result =  _localctx.factor.result; 
			setState(216);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(210);
					_localctx.op = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==T__28 || _la==T__29) ) {
						_localctx.op = _errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(211);
					_localctx.factor = factor();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.factor.result); 
					}
					} 
				}
				setState(218);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
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

	public static class FactorContext extends ParserRuleContext {
		public SLExpressionNode result;
		public Token IDENTIFIER;
		public Member_expressionContext member_expression;
		public Token STRING_LITERAL;
		public Token NUMERIC_LITERAL;
		public Token s;
		public ExpressionContext expr;
		public Token e;
		public TerminalNode IDENTIFIER() { return getToken(SimpleLanguageParser.IDENTIFIER, 0); }
		public TerminalNode STRING_LITERAL() { return getToken(SimpleLanguageParser.STRING_LITERAL, 0); }
		public TerminalNode NUMERIC_LITERAL() { return getToken(SimpleLanguageParser.NUMERIC_LITERAL, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Member_expressionContext member_expression() {
			return getRuleContext(Member_expressionContext.class,0);
		}
		public FactorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_factor; }
	}

	public final FactorContext factor() throws RecognitionException {
		FactorContext _localctx = new FactorContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_factor);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(236);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				{
				setState(219);
				_localctx.IDENTIFIER = match(IDENTIFIER);
				 SLExpressionNode assignmentName = factory.createStringLiteral(_localctx.IDENTIFIER, false); 
				setState(225);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
				case 1:
					{
					setState(221);
					_localctx.member_expression = member_expression(null, null, assignmentName);
					 _localctx.result =  _localctx.member_expression.result; 
					}
					break;
				case 2:
					{
					 _localctx.result =  factory.createRead(assignmentName); 
					}
					break;
				}
				}
				break;
			case STRING_LITERAL:
				{
				setState(227);
				_localctx.STRING_LITERAL = match(STRING_LITERAL);
				 _localctx.result =  factory.createStringLiteral(_localctx.STRING_LITERAL, true); 
				}
				break;
			case NUMERIC_LITERAL:
				{
				setState(229);
				_localctx.NUMERIC_LITERAL = match(NUMERIC_LITERAL);
				 _localctx.result =  factory.createNumericLiteral(_localctx.NUMERIC_LITERAL); 
				}
				break;
			case T__1:
				{
				setState(231);
				_localctx.s = match(T__1);
				setState(232);
				_localctx.expr = expression();
				setState(233);
				_localctx.e = match(T__3);
				 _localctx.result =  factory.createParenExpression(_localctx.expr.result, _localctx.s.getStartIndex(), _localctx.e.getStopIndex() - _localctx.s.getStartIndex() + 1); 
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class Member_expressionContext extends ParserRuleContext {
		public SLExpressionNode r;
		public SLExpressionNode assignmentReceiver;
		public SLExpressionNode assignmentName;
		public SLExpressionNode result;
		public ExpressionContext expression;
		public Token e;
		public Token IDENTIFIER;
		public Member_expressionContext member_expression;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IDENTIFIER() { return getToken(SimpleLanguageParser.IDENTIFIER, 0); }
		public Member_expressionContext member_expression() {
			return getRuleContext(Member_expressionContext.class,0);
		}
		public Member_expressionContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public Member_expressionContext(ParserRuleContext parent, int invokingState, SLExpressionNode r, SLExpressionNode assignmentReceiver, SLExpressionNode assignmentName) {
			super(parent, invokingState);
			this.r = r;
			this.assignmentReceiver = assignmentReceiver;
			this.assignmentName = assignmentName;
		}
		@Override public int getRuleIndex() { return RULE_member_expression; }
	}

	public final Member_expressionContext member_expression(SLExpressionNode r,SLExpressionNode assignmentReceiver,SLExpressionNode assignmentName) throws RecognitionException {
		Member_expressionContext _localctx = new Member_expressionContext(_ctx, getState(), r, assignmentReceiver, assignmentName);
		enterRule(_localctx, 30, RULE_member_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			 SLExpressionNode receiver = r;
			                                                  SLExpressionNode nestedAssignmentName = null; 
			setState(270);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
				{
				setState(239);
				match(T__1);
				 List<SLExpressionNode> parameters = new ArrayList<>();
				                                                  if (receiver == null) {
				                                                      receiver = factory.createRead(assignmentName);
				                                                  } 
				setState(252);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__15) | (1L << T__16) | (1L << T__17) | (1L << IDENTIFIER) | (1L << STRING_LITERAL) | (1L << NUMERIC_LITERAL))) != 0)) {
					{
					setState(241);
					_localctx.expression = expression();
					 parameters.add(_localctx.expression.result); 
					setState(249);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__2) {
						{
						{
						setState(243);
						match(T__2);
						setState(244);
						_localctx.expression = expression();
						 parameters.add(_localctx.expression.result); 
						}
						}
						setState(251);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(254);
				_localctx.e = match(T__3);
				 _localctx.result =  factory.createCall(receiver, parameters, _localctx.e); 
				}
				break;
			case T__30:
				{
				setState(256);
				match(T__30);
				setState(257);
				_localctx.expression = expression();
				 if (assignmentName == null) {
				                                                      SemErr((_localctx.expression!=null?(_localctx.expression.start):null), "invalid assignment target");
				                                                  } else if (assignmentReceiver == null) {
				                                                      _localctx.result =  factory.createAssignment(assignmentName, _localctx.expression.result);
				                                                  } else {
				                                                      _localctx.result =  factory.createWriteProperty(assignmentReceiver, assignmentName, _localctx.expression.result);
				                                                  } 
				}
				break;
			case T__31:
				{
				setState(260);
				match(T__31);
				 if (receiver == null) {
				                                                       receiver = factory.createRead(assignmentName);
				                                                  } 
				setState(262);
				_localctx.IDENTIFIER = match(IDENTIFIER);
				 nestedAssignmentName = factory.createStringLiteral(_localctx.IDENTIFIER, false);
				                                                  _localctx.result =  factory.createReadProperty(receiver, nestedAssignmentName); 
				}
				break;
			case T__32:
				{
				setState(264);
				match(T__32);
				 if (receiver == null) {
				                                                      receiver = factory.createRead(assignmentName);
				                                                  } 
				setState(266);
				_localctx.expression = expression();
				 nestedAssignmentName = _localctx.expression.result;
				                                                  _localctx.result =  factory.createReadProperty(receiver, nestedAssignmentName); 
				setState(268);
				match(T__33);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(275);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(272);
				_localctx.member_expression = member_expression(_localctx.result, receiver, nestedAssignmentName);
				 _localctx.result =  _localctx.member_expression.result; 
				}
				break;
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3*\u0118\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\3\2\7"+
		"\2%\n\2\f\2\16\2(\13\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3"+
		"\65\n\3\f\3\16\38\13\3\5\3:\n\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\5"+
		"\4E\n\4\3\5\3\5\3\5\3\5\3\5\7\5L\n\5\f\5\16\5O\13\5\3\5\3\5\3\5\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\5\6j\n\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b"+
		"\3\b\3\b\3\b\3\b\3\b\5\b}\n\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\5\t\u0086\n"+
		"\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n\u00a8"+
		"\n\n\3\13\3\13\3\13\3\13\3\13\3\13\7\13\u00b0\n\13\f\13\16\13\u00b3\13"+
		"\13\3\f\3\f\3\f\3\f\3\f\3\f\7\f\u00bb\n\f\f\f\16\f\u00be\13\f\3\r\3\r"+
		"\3\r\3\r\3\r\3\r\5\r\u00c6\n\r\3\16\3\16\3\16\3\16\3\16\3\16\7\16\u00ce"+
		"\n\16\f\16\16\16\u00d1\13\16\3\17\3\17\3\17\3\17\3\17\3\17\7\17\u00d9"+
		"\n\17\f\17\16\17\u00dc\13\17\3\20\3\20\3\20\3\20\3\20\3\20\5\20\u00e4"+
		"\n\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20\u00ef\n\20\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\7\21\u00fa\n\21\f\21\16\21\u00fd"+
		"\13\21\5\21\u00ff\n\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\3\21\3\21\3\21\5\21\u0111\n\21\3\21\3\21\3\21\5\21"+
		"\u0116\n\21\3\21\2\2\22\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \2\5\3"+
		"\2\27\34\3\2\35\36\3\2\37 \2\u0126\2\"\3\2\2\2\4+\3\2\2\2\6D\3\2\2\2\b"+
		"F\3\2\2\2\ni\3\2\2\2\fk\3\2\2\2\16r\3\2\2\2\20\u0080\3\2\2\2\22\u00a7"+
		"\3\2\2\2\24\u00a9\3\2\2\2\26\u00b4\3\2\2\2\30\u00bf\3\2\2\2\32\u00c7\3"+
		"\2\2\2\34\u00d2\3\2\2\2\36\u00ee\3\2\2\2 \u00f0\3\2\2\2\"&\5\4\3\2#%\5"+
		"\4\3\2$#\3\2\2\2%(\3\2\2\2&$\3\2\2\2&\'\3\2\2\2\')\3\2\2\2(&\3\2\2\2)"+
		"*\7\2\2\3*\3\3\2\2\2+,\7\3\2\2,-\7(\2\2-.\7\4\2\2.9\b\3\1\2/\60\7(\2\2"+
		"\60\66\b\3\1\2\61\62\7\5\2\2\62\63\7(\2\2\63\65\b\3\1\2\64\61\3\2\2\2"+
		"\658\3\2\2\2\66\64\3\2\2\2\66\67\3\2\2\2\67:\3\2\2\28\66\3\2\2\29/\3\2"+
		"\2\29:\3\2\2\2:;\3\2\2\2;<\7\6\2\2<=\5\6\4\2=>\b\3\1\2>?\5\b\5\2?@\b\3"+
		"\1\2@\5\3\2\2\2AB\7\7\2\2BE\b\4\1\2CE\b\4\1\2DA\3\2\2\2DC\3\2\2\2E\7\3"+
		"\2\2\2FG\b\5\1\2GM\7\b\2\2HI\5\n\6\2IJ\b\5\1\2JL\3\2\2\2KH\3\2\2\2LO\3"+
		"\2\2\2MK\3\2\2\2MN\3\2\2\2NP\3\2\2\2OM\3\2\2\2PQ\7\t\2\2QR\b\5\1\2R\t"+
		"\3\2\2\2ST\5\f\7\2TU\b\6\1\2Uj\3\2\2\2VW\7\n\2\2WX\b\6\1\2Xj\7\13\2\2"+
		"YZ\7\f\2\2Z[\b\6\1\2[j\7\13\2\2\\]\5\16\b\2]^\b\6\1\2^j\3\2\2\2_`\5\20"+
		"\t\2`a\b\6\1\2aj\3\2\2\2bc\5\22\n\2cd\7\13\2\2de\b\6\1\2ej\3\2\2\2fg\7"+
		"\r\2\2gh\b\6\1\2hj\7\13\2\2iS\3\2\2\2iV\3\2\2\2iY\3\2\2\2i\\\3\2\2\2i"+
		"_\3\2\2\2ib\3\2\2\2if\3\2\2\2j\13\3\2\2\2kl\7\16\2\2lm\7\4\2\2mn\5\22"+
		"\n\2no\7\6\2\2op\5\b\5\2pq\b\7\1\2q\r\3\2\2\2rs\7\17\2\2st\7\4\2\2tu\5"+
		"\22\n\2uv\7\6\2\2vw\5\b\5\2w|\b\b\1\2xy\7\20\2\2yz\5\b\5\2z{\b\b\1\2{"+
		"}\3\2\2\2|x\3\2\2\2|}\3\2\2\2}~\3\2\2\2~\177\b\b\1\2\177\17\3\2\2\2\u0080"+
		"\u0081\7\21\2\2\u0081\u0085\b\t\1\2\u0082\u0083\5\22\n\2\u0083\u0084\b"+
		"\t\1\2\u0084\u0086\3\2\2\2\u0085\u0082\3\2\2\2\u0085\u0086\3\2\2\2\u0086"+
		"\u0087\3\2\2\2\u0087\u0088\b\t\1\2\u0088\u0089\7\13\2\2\u0089\21\3\2\2"+
		"\2\u008a\u008b\7\22\2\2\u008b\u008c\7\4\2\2\u008c\u008d\7*\2\2\u008d\u008e"+
		"\b\n\1\2\u008e\u008f\3\2\2\2\u008f\u00a8\7\6\2\2\u0090\u0091\7\23\2\2"+
		"\u0091\u0092\7\4\2\2\u0092\u0093\b\n\1\2\u0093\u0094\5\24\13\2\u0094\u0095"+
		"\b\n\1\2\u0095\u0096\3\2\2\2\u0096\u0097\b\n\1\2\u0097\u0098\7\6\2\2\u0098"+
		"\u00a8\3\2\2\2\u0099\u009a\7\24\2\2\u009a\u009b\7\4\2\2\u009b\u009c\7"+
		"*\2\2\u009c\u009d\7\5\2\2\u009d\u009e\b\n\1\2\u009e\u009f\5\24\13\2\u009f"+
		"\u00a0\b\n\1\2\u00a0\u00a1\3\2\2\2\u00a1\u00a2\b\n\1\2\u00a2\u00a3\7\6"+
		"\2\2\u00a3\u00a8\3\2\2\2\u00a4\u00a5\5\24\13\2\u00a5\u00a6\b\n\1\2\u00a6"+
		"\u00a8\3\2\2\2\u00a7\u008a\3\2\2\2\u00a7\u0090\3\2\2\2\u00a7\u0099\3\2"+
		"\2\2\u00a7\u00a4\3\2\2\2\u00a8\23\3\2\2\2\u00a9\u00aa\5\26\f\2\u00aa\u00b1"+
		"\b\13\1\2\u00ab\u00ac\7\25\2\2\u00ac\u00ad\5\26\f\2\u00ad\u00ae\b\13\1"+
		"\2\u00ae\u00b0\3\2\2\2\u00af\u00ab\3\2\2\2\u00b0\u00b3\3\2\2\2\u00b1\u00af"+
		"\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\25\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b4"+
		"\u00b5\5\30\r\2\u00b5\u00bc\b\f\1\2\u00b6\u00b7\7\26\2\2\u00b7\u00b8\5"+
		"\30\r\2\u00b8\u00b9\b\f\1\2\u00b9\u00bb\3\2\2\2\u00ba\u00b6\3\2\2\2\u00bb"+
		"\u00be\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\27\3\2\2"+
		"\2\u00be\u00bc\3\2\2\2\u00bf\u00c0\5\32\16\2\u00c0\u00c5\b\r\1\2\u00c1"+
		"\u00c2\t\2\2\2\u00c2\u00c3\5\32\16\2\u00c3\u00c4\b\r\1\2\u00c4\u00c6\3"+
		"\2\2\2\u00c5\u00c1\3\2\2\2\u00c5\u00c6\3\2\2\2\u00c6\31\3\2\2\2\u00c7"+
		"\u00c8\5\34\17\2\u00c8\u00cf\b\16\1\2\u00c9\u00ca\t\3\2\2\u00ca\u00cb"+
		"\5\34\17\2\u00cb\u00cc\b\16\1\2\u00cc\u00ce\3\2\2\2\u00cd\u00c9\3\2\2"+
		"\2\u00ce\u00d1\3\2\2\2\u00cf\u00cd\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0\33"+
		"\3\2\2\2\u00d1\u00cf\3\2\2\2\u00d2\u00d3\5\36\20\2\u00d3\u00da\b\17\1"+
		"\2\u00d4\u00d5\t\4\2\2\u00d5\u00d6\5\36\20\2\u00d6\u00d7\b\17\1\2\u00d7"+
		"\u00d9\3\2\2\2\u00d8\u00d4\3\2\2\2\u00d9\u00dc\3\2\2\2\u00da\u00d8\3\2"+
		"\2\2\u00da\u00db\3\2\2\2\u00db\35\3\2\2\2\u00dc\u00da\3\2\2\2\u00dd\u00de"+
		"\7(\2\2\u00de\u00e3\b\20\1\2\u00df\u00e0\5 \21\2\u00e0\u00e1\b\20\1\2"+
		"\u00e1\u00e4\3\2\2\2\u00e2\u00e4\b\20\1\2\u00e3\u00df\3\2\2\2\u00e3\u00e2"+
		"\3\2\2\2\u00e4\u00ef\3\2\2\2\u00e5\u00e6\7)\2\2\u00e6\u00ef\b\20\1\2\u00e7"+
		"\u00e8\7*\2\2\u00e8\u00ef\b\20\1\2\u00e9\u00ea\7\4\2\2\u00ea\u00eb\5\22"+
		"\n\2\u00eb\u00ec\7\6\2\2\u00ec\u00ed\b\20\1\2\u00ed\u00ef\3\2\2\2\u00ee"+
		"\u00dd\3\2\2\2\u00ee\u00e5\3\2\2\2\u00ee\u00e7\3\2\2\2\u00ee\u00e9\3\2"+
		"\2\2\u00ef\37\3\2\2\2\u00f0\u0110\b\21\1\2\u00f1\u00f2\7\4\2\2\u00f2\u00fe"+
		"\b\21\1\2\u00f3\u00f4\5\22\n\2\u00f4\u00fb\b\21\1\2\u00f5\u00f6\7\5\2"+
		"\2\u00f6\u00f7\5\22\n\2\u00f7\u00f8\b\21\1\2\u00f8\u00fa\3\2\2\2\u00f9"+
		"\u00f5\3\2\2\2\u00fa\u00fd\3\2\2\2\u00fb\u00f9\3\2\2\2\u00fb\u00fc\3\2"+
		"\2\2\u00fc\u00ff\3\2\2\2\u00fd\u00fb\3\2\2\2\u00fe\u00f3\3\2\2\2\u00fe"+
		"\u00ff\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0101\7\6\2\2\u0101\u0111\b\21"+
		"\1\2\u0102\u0103\7!\2\2\u0103\u0104\5\22\n\2\u0104\u0105\b\21\1\2\u0105"+
		"\u0111\3\2\2\2\u0106\u0107\7\"\2\2\u0107\u0108\b\21\1\2\u0108\u0109\7"+
		"(\2\2\u0109\u0111\b\21\1\2\u010a\u010b\7#\2\2\u010b\u010c\b\21\1\2\u010c"+
		"\u010d\5\22\n\2\u010d\u010e\b\21\1\2\u010e\u010f\7$\2\2\u010f\u0111\3"+
		"\2\2\2\u0110\u00f1\3\2\2\2\u0110\u0102\3\2\2\2\u0110\u0106\3\2\2\2\u0110"+
		"\u010a\3\2\2\2\u0111\u0115\3\2\2\2\u0112\u0113\5 \21\2\u0113\u0114\b\21"+
		"\1\2\u0114\u0116\3\2\2\2\u0115\u0112\3\2\2\2\u0115\u0116\3\2\2\2\u0116"+
		"!\3\2\2\2\26&\669DMi|\u0085\u00a7\u00b1\u00bc\u00c5\u00cf\u00da\u00e3"+
		"\u00ee\u00fb\u00fe\u0110\u0115";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
