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
import java.util.HashSet;import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLRootNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.nodes.controlflow.SLBlockNode;import com.oracle.truffle.sl.parser.SLParseError;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import org.graalvm.collections.Pair;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Random;import java.util.Set;import static com.oracle.truffle.sl.parser.SimpleLanguageParserSupport.getMapSetPair;

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
		T__31=32, T__32=33, WS=34, COMMENT=35, LINE_COMMENT=36, IDENTIFIER=37, 
		STRING_LITERAL=38, NUMERIC_LITERAL=39;
	public static final int
		RULE_simplelanguage = 0, RULE_function = 1, RULE_block = 2, RULE_statement = 3, 
		RULE_while_statement = 4, RULE_if_statement = 5, RULE_return_statement = 6, 
		RULE_expression = 7, RULE_logic_big_term = 8, RULE_logic_term = 9, RULE_logic_factor = 10, 
		RULE_arithmetic = 11, RULE_term = 12, RULE_factor = 13, RULE_member_expression = 14;
	private static String[] makeRuleNames() {
		return new String[] {
			"simplelanguage", "function", "block", "statement", "while_statement", 
			"if_statement", "return_statement", "expression", "logic_big_term", "logic_term", 
			"logic_factor", "arithmetic", "term", "factor", "member_expression"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'function'", "'('", "','", "')'", "'{'", "'}'", "'break'", "';'", 
			"'continue'", "'debugger'", "'while'", "'if'", "'else'", "'return'", 
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
			null, null, null, null, null, null, null, null, null, null, "WS", "COMMENT", 
			"LINE_COMMENT", "IDENTIFIER", "STRING_LITERAL", "NUMERIC_LITERAL"
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
			setState(30);
			function();
			setState(34);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(31);
				function();
				}
				}
				setState(36);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(37);
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
		public BlockContext body;
		public List<TerminalNode> IDENTIFIER() { return getTokens(SimpleLanguageParser.IDENTIFIER); }
		public TerminalNode IDENTIFIER(int i) {
			return getToken(SimpleLanguageParser.IDENTIFIER, i);
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
			setState(39);
			match(T__0);
			setState(40);
			_localctx.IDENTIFIER = match(IDENTIFIER);
			setState(41);
			_localctx.s = match(T__1);
			 factory.startFunction(_localctx.IDENTIFIER, _localctx.s); 
			setState(53);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==IDENTIFIER) {
				{
				setState(43);
				_localctx.IDENTIFIER = match(IDENTIFIER);
				 factory.addFormalParameter(_localctx.IDENTIFIER); 
				setState(50);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==T__2) {
					{
					{
					setState(45);
					match(T__2);
					setState(46);
					_localctx.IDENTIFIER = match(IDENTIFIER);
					 factory.addFormalParameter(_localctx.IDENTIFIER); 
					}
					}
					setState(52);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
			}

			setState(55);
			match(T__3);
			setState(56);
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
		enterRule(_localctx, 4, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			 factory.startBlock();
			                                                  List<SLStatementNode> body = new ArrayList<>(); 
			setState(60);
			_localctx.s = match(T__4);
			setState(66);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__6) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << T__16) | (1L << IDENTIFIER) | (1L << STRING_LITERAL) | (1L << NUMERIC_LITERAL))) != 0)) {
				{
				{
				setState(61);
				_localctx.statement = statement(inLoop);
				 body.add(_localctx.statement.result); 
				}
				}
				setState(68);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(69);
			_localctx.e = match(T__5);
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
		enterRule(_localctx, 6, RULE_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(94);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__10:
				{
				setState(72);
				_localctx.while_statement = while_statement();
				 _localctx.result =  _localctx.while_statement.result; 
				}
				break;
			case T__6:
				{
				setState(75);
				_localctx.b = match(T__6);
				 if (inLoop) { _localctx.result =  factory.createBreak(_localctx.b); } else { SemErr(_localctx.b, "break used outside of loop"); } 
				setState(77);
				match(T__7);
				}
				break;
			case T__8:
				{
				setState(78);
				_localctx.c = match(T__8);
				 if (inLoop) { _localctx.result =  factory.createContinue(_localctx.c); } else { SemErr(_localctx.c, "continue used outside of loop"); } 
				setState(80);
				match(T__7);
				}
				break;
			case T__11:
				{
				setState(81);
				_localctx.if_statement = if_statement(inLoop);
				 _localctx.result =  _localctx.if_statement.result; 
				}
				break;
			case T__13:
				{
				setState(84);
				_localctx.return_statement = return_statement();
				 _localctx.result =  _localctx.return_statement.result; 
				}
				break;
			case T__1:
			case T__14:
			case T__15:
			case T__16:
			case IDENTIFIER:
			case STRING_LITERAL:
			case NUMERIC_LITERAL:
				{
				setState(87);
				_localctx.expression = expression();
				setState(88);
				match(T__7);
				 _localctx.result =  _localctx.expression.result; 
				}
				break;
			case T__9:
				{
				setState(91);
				_localctx.d = match(T__9);
				 _localctx.result =  factory.createDebugger(_localctx.d); 
				setState(93);
				match(T__7);
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
		enterRule(_localctx, 8, RULE_while_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			_localctx.w = match(T__10);
			setState(97);
			match(T__1);
			setState(98);
			_localctx.condition = expression();
			setState(99);
			match(T__3);
			setState(100);
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
		enterRule(_localctx, 10, RULE_if_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			_localctx.i = match(T__11);
			setState(104);
			match(T__1);
			setState(105);
			_localctx.condition = expression();
			setState(106);
			match(T__3);
			setState(107);
			_localctx.then = _localctx.block = block(inLoop);
			 SLStatementNode elsePart = null; 
			setState(113);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__12) {
				{
				setState(109);
				match(T__12);
				setState(110);
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
		enterRule(_localctx, 12, RULE_return_statement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			_localctx.r = match(T__13);
			 SLExpressionNode value = null; 
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__14) | (1L << T__15) | (1L << T__16) | (1L << IDENTIFIER) | (1L << STRING_LITERAL) | (1L << NUMERIC_LITERAL))) != 0)) {
				{
				setState(119);
				_localctx.expression = expression();
				 value = _localctx.expression.result; 
				}
			}

			 _localctx.result =  factory.createReturn(_localctx.r, value); 
			setState(125);
			match(T__7);
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
		enterRule(_localctx, 14, RULE_expression);
		try {
			setState(156);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__14:
				enterOuterAlt(_localctx, 1);
				{
				setState(127);
				_localctx.u = match(T__14);
				setState(128);
				match(T__1);
				{
				setState(129);
				_localctx.deleted = match(NUMERIC_LITERAL);
				 _localctx.result =  factory.createDelete(_localctx.deleted); 
				}
				setState(132);
				match(T__3);
				}
				break;
			case T__15:
				enterOuterAlt(_localctx, 2);
				{
				setState(133);
				_localctx.u = match(T__15);
				setState(134);
				match(T__1);
				{
				 factory.startNewExp(); 
				setState(136);
				_localctx.logic_big_term = logic_big_term();
				 _localctx.result =  factory.createInsert(_localctx.logic_big_term.result); 
				}
				 factory.endNewExp(); 
				setState(140);
				match(T__3);
				}
				break;
			case T__16:
				enterOuterAlt(_localctx, 3);
				{
				setState(142);
				_localctx.u = match(T__16);
				setState(143);
				match(T__1);
				{
				setState(144);
				_localctx.deleted = match(NUMERIC_LITERAL);
				setState(145);
				match(T__2);
				 factory.startNewExp(); 
				setState(147);
				_localctx.logic_big_term = logic_big_term();
				 _localctx.result =  factory.createReplace(_localctx.deleted, _localctx.logic_big_term.result); 
				}
				 factory.endNewExp(); 
				setState(151);
				match(T__3);
				}
				break;
			case T__1:
			case IDENTIFIER:
			case STRING_LITERAL:
			case NUMERIC_LITERAL:
				enterOuterAlt(_localctx, 4);
				{
				setState(153);
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
		enterRule(_localctx, 16, RULE_logic_big_term);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			_localctx.logic_term = logic_term();
			 _localctx.result =  _localctx.logic_term.result; 
			setState(166);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(160);
					_localctx.op = match(T__17);
					setState(161);
					_localctx.logic_term = logic_term();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.logic_term.result); 
					}
					} 
				}
				setState(168);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
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
		enterRule(_localctx, 18, RULE_logic_term);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(169);
			_localctx.logic_factor = logic_factor();
			 _localctx.result =  _localctx.logic_factor.result; 
			setState(177);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(171);
					_localctx.op = match(T__18);
					setState(172);
					_localctx.logic_factor = logic_factor();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.logic_factor.result); 
					}
					} 
				}
				setState(179);
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
		enterRule(_localctx, 20, RULE_logic_factor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(180);
			_localctx.arithmetic = arithmetic();
			 _localctx.result =  _localctx.arithmetic.result; 
			setState(186);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				{
				setState(182);
				_localctx.op = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__19) | (1L << T__20) | (1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24))) != 0)) ) {
					_localctx.op = _errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(183);
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
		enterRule(_localctx, 22, RULE_arithmetic);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(188);
			_localctx.term = term();
			 _localctx.result =  _localctx.term.result; 
			setState(196);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(190);
					_localctx.op = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==T__25 || _la==T__26) ) {
						_localctx.op = _errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(191);
					_localctx.term = term();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.term.result); 
					}
					} 
				}
				setState(198);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
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
		enterRule(_localctx, 24, RULE_term);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(199);
			_localctx.factor = factor();
			 _localctx.result =  _localctx.factor.result; 
			setState(207);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(201);
					_localctx.op = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==T__27 || _la==T__28) ) {
						_localctx.op = _errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(202);
					_localctx.factor = factor();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.factor.result); 
					}
					} 
				}
				setState(209);
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
		enterRule(_localctx, 26, RULE_factor);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(227);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				{
				setState(210);
				_localctx.IDENTIFIER = match(IDENTIFIER);
				 SLExpressionNode assignmentName = factory.createStringLiteral(_localctx.IDENTIFIER, false); 
				setState(216);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
				case 1:
					{
					setState(212);
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
				setState(218);
				_localctx.STRING_LITERAL = match(STRING_LITERAL);
				 _localctx.result =  factory.createStringLiteral(_localctx.STRING_LITERAL, true); 
				}
				break;
			case NUMERIC_LITERAL:
				{
				setState(220);
				_localctx.NUMERIC_LITERAL = match(NUMERIC_LITERAL);
				 _localctx.result =  factory.createNumericLiteral(_localctx.NUMERIC_LITERAL); 
				}
				break;
			case T__1:
				{
				setState(222);
				_localctx.s = match(T__1);
				setState(223);
				_localctx.expr = expression();
				setState(224);
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
		enterRule(_localctx, 28, RULE_member_expression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			 SLExpressionNode receiver = r;
			                                                  SLExpressionNode nestedAssignmentName = null; 
			setState(261);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
				{
				setState(230);
				match(T__1);
				 List<SLExpressionNode> parameters = new ArrayList<>();
				                                                  if (receiver == null) {
				                                                      receiver = factory.createRead(assignmentName);
				                                                  } 
				setState(243);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__14) | (1L << T__15) | (1L << T__16) | (1L << IDENTIFIER) | (1L << STRING_LITERAL) | (1L << NUMERIC_LITERAL))) != 0)) {
					{
					setState(232);
					_localctx.expression = expression();
					 parameters.add(_localctx.expression.result); 
					setState(240);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__2) {
						{
						{
						setState(234);
						match(T__2);
						setState(235);
						_localctx.expression = expression();
						 parameters.add(_localctx.expression.result); 
						}
						}
						setState(242);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(245);
				_localctx.e = match(T__3);
				 _localctx.result =  factory.createCall(receiver, parameters, _localctx.e); 
				}
				break;
			case T__29:
				{
				setState(247);
				match(T__29);
				setState(248);
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
			case T__30:
				{
				setState(251);
				match(T__30);
				 if (receiver == null) {
				                                                       receiver = factory.createRead(assignmentName);
				                                                  } 
				setState(253);
				_localctx.IDENTIFIER = match(IDENTIFIER);
				 nestedAssignmentName = factory.createStringLiteral(_localctx.IDENTIFIER, false);
				                                                  _localctx.result =  factory.createReadProperty(receiver, nestedAssignmentName); 
				}
				break;
			case T__31:
				{
				setState(255);
				match(T__31);
				 if (receiver == null) {
				                                                      receiver = factory.createRead(assignmentName);
				                                                  } 
				setState(257);
				_localctx.expression = expression();
				 nestedAssignmentName = _localctx.expression.result;
				                                                  _localctx.result =  factory.createReadProperty(receiver, nestedAssignmentName); 
				setState(259);
				match(T__32);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(266);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				{
				setState(263);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3)\u010f\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\3\2\3\2\7\2#\n\2\f\2"+
		"\16\2&\13\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3\63\n\3\f\3"+
		"\16\3\66\13\3\5\38\n\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\7\4C\n\4\f"+
		"\4\16\4F\13\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5a\n\5\3\6\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\5\7t\n\7\3\7\3\7"+
		"\3\b\3\b\3\b\3\b\3\b\5\b}\n\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3"+
		"\t\3\t\3\t\3\t\3\t\5\t\u009f\n\t\3\n\3\n\3\n\3\n\3\n\3\n\7\n\u00a7\n\n"+
		"\f\n\16\n\u00aa\13\n\3\13\3\13\3\13\3\13\3\13\3\13\7\13\u00b2\n\13\f\13"+
		"\16\13\u00b5\13\13\3\f\3\f\3\f\3\f\3\f\3\f\5\f\u00bd\n\f\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\7\r\u00c5\n\r\f\r\16\r\u00c8\13\r\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\7\16\u00d0\n\16\f\16\16\16\u00d3\13\16\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\5\17\u00db\n\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\5\17"+
		"\u00e6\n\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\7\20\u00f1\n"+
		"\20\f\20\16\20\u00f4\13\20\5\20\u00f6\n\20\3\20\3\20\3\20\3\20\3\20\3"+
		"\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20\u0108\n\20"+
		"\3\20\3\20\3\20\5\20\u010d\n\20\3\20\2\2\21\2\4\6\b\n\f\16\20\22\24\26"+
		"\30\32\34\36\2\5\3\2\26\33\3\2\34\35\3\2\36\37\2\u011d\2 \3\2\2\2\4)\3"+
		"\2\2\2\6=\3\2\2\2\b`\3\2\2\2\nb\3\2\2\2\fi\3\2\2\2\16w\3\2\2\2\20\u009e"+
		"\3\2\2\2\22\u00a0\3\2\2\2\24\u00ab\3\2\2\2\26\u00b6\3\2\2\2\30\u00be\3"+
		"\2\2\2\32\u00c9\3\2\2\2\34\u00e5\3\2\2\2\36\u00e7\3\2\2\2 $\5\4\3\2!#"+
		"\5\4\3\2\"!\3\2\2\2#&\3\2\2\2$\"\3\2\2\2$%\3\2\2\2%\'\3\2\2\2&$\3\2\2"+
		"\2\'(\7\2\2\3(\3\3\2\2\2)*\7\3\2\2*+\7\'\2\2+,\7\4\2\2,\67\b\3\1\2-.\7"+
		"\'\2\2.\64\b\3\1\2/\60\7\5\2\2\60\61\7\'\2\2\61\63\b\3\1\2\62/\3\2\2\2"+
		"\63\66\3\2\2\2\64\62\3\2\2\2\64\65\3\2\2\2\658\3\2\2\2\66\64\3\2\2\2\67"+
		"-\3\2\2\2\678\3\2\2\289\3\2\2\29:\7\6\2\2:;\5\6\4\2;<\b\3\1\2<\5\3\2\2"+
		"\2=>\b\4\1\2>D\7\7\2\2?@\5\b\5\2@A\b\4\1\2AC\3\2\2\2B?\3\2\2\2CF\3\2\2"+
		"\2DB\3\2\2\2DE\3\2\2\2EG\3\2\2\2FD\3\2\2\2GH\7\b\2\2HI\b\4\1\2I\7\3\2"+
		"\2\2JK\5\n\6\2KL\b\5\1\2La\3\2\2\2MN\7\t\2\2NO\b\5\1\2Oa\7\n\2\2PQ\7\13"+
		"\2\2QR\b\5\1\2Ra\7\n\2\2ST\5\f\7\2TU\b\5\1\2Ua\3\2\2\2VW\5\16\b\2WX\b"+
		"\5\1\2Xa\3\2\2\2YZ\5\20\t\2Z[\7\n\2\2[\\\b\5\1\2\\a\3\2\2\2]^\7\f\2\2"+
		"^_\b\5\1\2_a\7\n\2\2`J\3\2\2\2`M\3\2\2\2`P\3\2\2\2`S\3\2\2\2`V\3\2\2\2"+
		"`Y\3\2\2\2`]\3\2\2\2a\t\3\2\2\2bc\7\r\2\2cd\7\4\2\2de\5\20\t\2ef\7\6\2"+
		"\2fg\5\6\4\2gh\b\6\1\2h\13\3\2\2\2ij\7\16\2\2jk\7\4\2\2kl\5\20\t\2lm\7"+
		"\6\2\2mn\5\6\4\2ns\b\7\1\2op\7\17\2\2pq\5\6\4\2qr\b\7\1\2rt\3\2\2\2so"+
		"\3\2\2\2st\3\2\2\2tu\3\2\2\2uv\b\7\1\2v\r\3\2\2\2wx\7\20\2\2x|\b\b\1\2"+
		"yz\5\20\t\2z{\b\b\1\2{}\3\2\2\2|y\3\2\2\2|}\3\2\2\2}~\3\2\2\2~\177\b\b"+
		"\1\2\177\u0080\7\n\2\2\u0080\17\3\2\2\2\u0081\u0082\7\21\2\2\u0082\u0083"+
		"\7\4\2\2\u0083\u0084\7)\2\2\u0084\u0085\b\t\1\2\u0085\u0086\3\2\2\2\u0086"+
		"\u009f\7\6\2\2\u0087\u0088\7\22\2\2\u0088\u0089\7\4\2\2\u0089\u008a\b"+
		"\t\1\2\u008a\u008b\5\22\n\2\u008b\u008c\b\t\1\2\u008c\u008d\3\2\2\2\u008d"+
		"\u008e\b\t\1\2\u008e\u008f\7\6\2\2\u008f\u009f\3\2\2\2\u0090\u0091\7\23"+
		"\2\2\u0091\u0092\7\4\2\2\u0092\u0093\7)\2\2\u0093\u0094\7\5\2\2\u0094"+
		"\u0095\b\t\1\2\u0095\u0096\5\22\n\2\u0096\u0097\b\t\1\2\u0097\u0098\3"+
		"\2\2\2\u0098\u0099\b\t\1\2\u0099\u009a\7\6\2\2\u009a\u009f\3\2\2\2\u009b"+
		"\u009c\5\22\n\2\u009c\u009d\b\t\1\2\u009d\u009f\3\2\2\2\u009e\u0081\3"+
		"\2\2\2\u009e\u0087\3\2\2\2\u009e\u0090\3\2\2\2\u009e\u009b\3\2\2\2\u009f"+
		"\21\3\2\2\2\u00a0\u00a1\5\24\13\2\u00a1\u00a8\b\n\1\2\u00a2\u00a3\7\24"+
		"\2\2\u00a3\u00a4\5\24\13\2\u00a4\u00a5\b\n\1\2\u00a5\u00a7\3\2\2\2\u00a6"+
		"\u00a2\3\2\2\2\u00a7\u00aa\3\2\2\2\u00a8\u00a6\3\2\2\2\u00a8\u00a9\3\2"+
		"\2\2\u00a9\23\3\2\2\2\u00aa\u00a8\3\2\2\2\u00ab\u00ac\5\26\f\2\u00ac\u00b3"+
		"\b\13\1\2\u00ad\u00ae\7\25\2\2\u00ae\u00af\5\26\f\2\u00af\u00b0\b\13\1"+
		"\2\u00b0\u00b2\3\2\2\2\u00b1\u00ad\3\2\2\2\u00b2\u00b5\3\2\2\2\u00b3\u00b1"+
		"\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b4\25\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b6"+
		"\u00b7\5\30\r\2\u00b7\u00bc\b\f\1\2\u00b8\u00b9\t\2\2\2\u00b9\u00ba\5"+
		"\30\r\2\u00ba\u00bb\b\f\1\2\u00bb\u00bd\3\2\2\2\u00bc\u00b8\3\2\2\2\u00bc"+
		"\u00bd\3\2\2\2\u00bd\27\3\2\2\2\u00be\u00bf\5\32\16\2\u00bf\u00c6\b\r"+
		"\1\2\u00c0\u00c1\t\3\2\2\u00c1\u00c2\5\32\16\2\u00c2\u00c3\b\r\1\2\u00c3"+
		"\u00c5\3\2\2\2\u00c4\u00c0\3\2\2\2\u00c5\u00c8\3\2\2\2\u00c6\u00c4\3\2"+
		"\2\2\u00c6\u00c7\3\2\2\2\u00c7\31\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c9\u00ca"+
		"\5\34\17\2\u00ca\u00d1\b\16\1\2\u00cb\u00cc\t\4\2\2\u00cc\u00cd\5\34\17"+
		"\2\u00cd\u00ce\b\16\1\2\u00ce\u00d0\3\2\2\2\u00cf\u00cb\3\2\2\2\u00d0"+
		"\u00d3\3\2\2\2\u00d1\u00cf\3\2\2\2\u00d1\u00d2\3\2\2\2\u00d2\33\3\2\2"+
		"\2\u00d3\u00d1\3\2\2\2\u00d4\u00d5\7\'\2\2\u00d5\u00da\b\17\1\2\u00d6"+
		"\u00d7\5\36\20\2\u00d7\u00d8\b\17\1\2\u00d8\u00db\3\2\2\2\u00d9\u00db"+
		"\b\17\1\2\u00da\u00d6\3\2\2\2\u00da\u00d9\3\2\2\2\u00db\u00e6\3\2\2\2"+
		"\u00dc\u00dd\7(\2\2\u00dd\u00e6\b\17\1\2\u00de\u00df\7)\2\2\u00df\u00e6"+
		"\b\17\1\2\u00e0\u00e1\7\4\2\2\u00e1\u00e2\5\20\t\2\u00e2\u00e3\7\6\2\2"+
		"\u00e3\u00e4\b\17\1\2\u00e4\u00e6\3\2\2\2\u00e5\u00d4\3\2\2\2\u00e5\u00dc"+
		"\3\2\2\2\u00e5\u00de\3\2\2\2\u00e5\u00e0\3\2\2\2\u00e6\35\3\2\2\2\u00e7"+
		"\u0107\b\20\1\2\u00e8\u00e9\7\4\2\2\u00e9\u00f5\b\20\1\2\u00ea\u00eb\5"+
		"\20\t\2\u00eb\u00f2\b\20\1\2\u00ec\u00ed\7\5\2\2\u00ed\u00ee\5\20\t\2"+
		"\u00ee\u00ef\b\20\1\2\u00ef\u00f1\3\2\2\2\u00f0\u00ec\3\2\2\2\u00f1\u00f4"+
		"\3\2\2\2\u00f2\u00f0\3\2\2\2\u00f2\u00f3\3\2\2\2\u00f3\u00f6\3\2\2\2\u00f4"+
		"\u00f2\3\2\2\2\u00f5\u00ea\3\2\2\2\u00f5\u00f6\3\2\2\2\u00f6\u00f7\3\2"+
		"\2\2\u00f7\u00f8\7\6\2\2\u00f8\u0108\b\20\1\2\u00f9\u00fa\7 \2\2\u00fa"+
		"\u00fb\5\20\t\2\u00fb\u00fc\b\20\1\2\u00fc\u0108\3\2\2\2\u00fd\u00fe\7"+
		"!\2\2\u00fe\u00ff\b\20\1\2\u00ff\u0100\7\'\2\2\u0100\u0108\b\20\1\2\u0101"+
		"\u0102\7\"\2\2\u0102\u0103\b\20\1\2\u0103\u0104\5\20\t\2\u0104\u0105\b"+
		"\20\1\2\u0105\u0106\7#\2\2\u0106\u0108\3\2\2\2\u0107\u00e8\3\2\2\2\u0107"+
		"\u00f9\3\2\2\2\u0107\u00fd\3\2\2\2\u0107\u0101\3\2\2\2\u0108\u010c\3\2"+
		"\2\2\u0109\u010a\5\36\20\2\u010a\u010b\b\20\1\2\u010b\u010d\3\2\2\2\u010c"+
		"\u0109\3\2\2\2\u010c\u010d\3\2\2\2\u010d\37\3\2\2\2\25$\64\67D`s|\u009e"+
		"\u00a8\u00b3\u00bc\u00c6\u00d1\u00da\u00e5\u00f2\u00f5\u0107\u010c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
