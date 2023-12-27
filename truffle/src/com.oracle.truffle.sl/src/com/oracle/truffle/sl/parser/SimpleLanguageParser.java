/*
 * Copyright (c) 2012, 2021, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.sl.SLLanguage;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;

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
	static { RuntimeMetaData.checkVersion("4.12.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		WS=32, COMMENT=33, LINE_COMMENT=34, IDENTIFIER=35, STRING_LITERAL=36, 
		NUMERIC_LITERAL=37;
	public static final int
		RULE_simplelanguage = 0, RULE_function = 1, RULE_at_mark = 2, RULE_block = 3, 
		RULE_statement = 4, RULE_while_statement = 5, RULE_if_statement = 6, RULE_return_statement = 7, 
		RULE_expression = 8, RULE_logic_term = 9, RULE_logic_factor = 10, RULE_arithmetic = 11, 
		RULE_term = 12, RULE_factor = 13, RULE_member_expression = 14;
	private static String[] makeRuleNames() {
		return new String[] {
			"simplelanguage", "function", "at_mark", "block", "statement", "while_statement", 
			"if_statement", "return_statement", "expression", "logic_term", "logic_factor", 
			"arithmetic", "term", "factor", "member_expression"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'function'", "'('", "','", "')'", "'@'", "'{'", "'}'", "'break'", 
			"';'", "'continue'", "'debugger'", "'while'", "'if'", "'else'", "'return'", 
			"'||'", "'&&'", "'<'", "'<='", "'>'", "'>='", "'=='", "'!='", "'+'", 
			"'-'", "'*'", "'/'", "'='", "'.'", "'['", "']'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, "WS", "COMMENT", "LINE_COMMENT", 
			"IDENTIFIER", "STRING_LITERAL", "NUMERIC_LITERAL"
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

	public static Pair<Map<TruffleString, RootCallTarget>, Set<TruffleString>> parseSL(SLLanguage language, Source source, Map<String, Integer> functionMapping) {
	    SimpleLanguageLexer lexer = new SimpleLanguageLexer(CharStreams.fromString(source.getCharacters().toString()));
	    SimpleLanguageParser parser = new SimpleLanguageParser(new CommonTokenStream(lexer));
	    lexer.removeErrorListeners();
	    parser.removeErrorListeners();
	    BailoutErrorListener listener = new BailoutErrorListener(source);
	    lexer.addErrorListener(listener);
	    parser.addErrorListener(listener);
	    parser.factory = new SLNodeFactory(language, source, functionMapping);
	    parser.source = source;
	    parser.simplelanguage();

	    return getMapSetPair(parser);
	}

	public SimpleLanguageParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
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

	@SuppressWarnings("CheckReturnValue")
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
			_localctx.g = at_mark();
			 if (_localctx.g.result) factory.dontFlag(); 
			setState(58);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(64);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				{
				setState(61);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(67);
			_localctx.s = match(T__5);
			setState(73);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 240518216964L) != 0)) {
				{
				{
				setState(68);
				_localctx.statement = statement(inLoop);
				 body.add(_localctx.statement.result); 
				}
				}
				setState(75);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(76);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(101);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__11:
				{
				setState(79);
				_localctx.while_statement = while_statement();
				 _localctx.result =  _localctx.while_statement.result; 
				}
				break;
			case T__7:
				{
				setState(82);
				_localctx.b = match(T__7);
				 if (inLoop) { _localctx.result =  factory.createBreak(_localctx.b); } else { SemErr(_localctx.b, "break used outside of loop"); } 
				setState(84);
				match(T__8);
				}
				break;
			case T__9:
				{
				setState(85);
				_localctx.c = match(T__9);
				 if (inLoop) { _localctx.result =  factory.createContinue(_localctx.c); } else { SemErr(_localctx.c, "continue used outside of loop"); } 
				setState(87);
				match(T__8);
				}
				break;
			case T__12:
				{
				setState(88);
				_localctx.if_statement = if_statement(inLoop);
				 _localctx.result =  _localctx.if_statement.result; 
				}
				break;
			case T__14:
				{
				setState(91);
				_localctx.return_statement = return_statement();
				 _localctx.result =  _localctx.return_statement.result; 
				}
				break;
			case T__1:
			case IDENTIFIER:
			case STRING_LITERAL:
			case NUMERIC_LITERAL:
				{
				setState(94);
				_localctx.expression = expression();
				setState(95);
				match(T__8);
				 _localctx.result =  _localctx.expression.result; 
				}
				break;
			case T__10:
				{
				setState(98);
				_localctx.d = match(T__10);
				 _localctx.result =  factory.createDebugger(_localctx.d); 
				setState(100);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(103);
			_localctx.w = match(T__11);
			setState(104);
			match(T__1);
			setState(105);
			_localctx.condition = expression();
			setState(106);
			match(T__3);
			setState(107);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(110);
			_localctx.i = match(T__12);
			setState(111);
			match(T__1);
			setState(112);
			_localctx.condition = expression();
			setState(113);
			match(T__3);
			setState(114);
			_localctx.then = _localctx.block = block(inLoop);
			 SLStatementNode elsePart = null; 
			setState(120);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__13) {
				{
				setState(116);
				match(T__13);
				setState(117);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(124);
			_localctx.r = match(T__14);
			 SLExpressionNode value = null; 
			setState(129);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 240518168580L) != 0)) {
				{
				setState(126);
				_localctx.expression = expression();
				 value = _localctx.expression.result; 
				}
			}

			 _localctx.result =  factory.createReturn(_localctx.r, value); 
			setState(132);
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

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public SLExpressionNode result;
		public Logic_termContext logic_term;
		public Token op;
		public List<Logic_termContext> logic_term() {
			return getRuleContexts(Logic_termContext.class);
		}
		public Logic_termContext logic_term(int i) {
			return getRuleContext(Logic_termContext.class,i);
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
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(134);
			_localctx.logic_term = logic_term();
			 _localctx.result =  _localctx.logic_term.result; 
			setState(142);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(136);
					_localctx.op = match(T__15);
					setState(137);
					_localctx.logic_term = logic_term();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.logic_term.result); 
					}
					} 
				}
				setState(144);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(145);
			_localctx.logic_factor = logic_factor();
			 _localctx.result =  _localctx.logic_factor.result; 
			setState(153);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(147);
					_localctx.op = match(T__16);
					setState(148);
					_localctx.logic_factor = logic_factor();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.logic_factor.result); 
					}
					} 
				}
				setState(155);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(156);
			_localctx.arithmetic = arithmetic();
			 _localctx.result =  _localctx.arithmetic.result; 
			setState(162);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				{
				setState(158);
				_localctx.op = _input.LT(1);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 16515072L) != 0)) ) {
					_localctx.op = _errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(159);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(164);
			_localctx.term = term();
			 _localctx.result =  _localctx.term.result; 
			setState(172);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(166);
					_localctx.op = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==T__23 || _la==T__24) ) {
						_localctx.op = _errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(167);
					_localctx.term = term();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.term.result); 
					}
					} 
				}
				setState(174);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(175);
			_localctx.factor = factor();
			 _localctx.result =  _localctx.factor.result; 
			setState(183);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(177);
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
					setState(178);
					_localctx.factor = factor();
					 _localctx.result =  factory.createBinary(_localctx.op, _localctx.result, _localctx.factor.result); 
					}
					} 
				}
				setState(185);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(203);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				{
				setState(186);
				_localctx.IDENTIFIER = match(IDENTIFIER);
				 SLExpressionNode assignmentName = factory.createStringLiteral(_localctx.IDENTIFIER, false); 
				setState(192);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
				case 1:
					{
					setState(188);
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
				setState(194);
				_localctx.STRING_LITERAL = match(STRING_LITERAL);
				 _localctx.result =  factory.createStringLiteral(_localctx.STRING_LITERAL, true); 
				}
				break;
			case NUMERIC_LITERAL:
				{
				setState(196);
				_localctx.NUMERIC_LITERAL = match(NUMERIC_LITERAL);
				 _localctx.result =  factory.createNumericLiteral(_localctx.NUMERIC_LITERAL); 
				}
				break;
			case T__1:
				{
				setState(198);
				_localctx.s = match(T__1);
				setState(199);
				_localctx.expr = expression();
				setState(200);
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

	@SuppressWarnings("CheckReturnValue")
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
			setState(237);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__1:
				{
				setState(206);
				match(T__1);
				 List<SLExpressionNode> parameters = new ArrayList<>();
				                                                  if (receiver == null) {
				                                                      receiver = factory.createRead(assignmentName);
				                                                  } 
				setState(219);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 240518168580L) != 0)) {
					{
					setState(208);
					_localctx.expression = expression();
					 parameters.add(_localctx.expression.result); 
					setState(216);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==T__2) {
						{
						{
						setState(210);
						match(T__2);
						setState(211);
						_localctx.expression = expression();
						 parameters.add(_localctx.expression.result); 
						}
						}
						setState(218);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					}
				}

				setState(221);
				_localctx.e = match(T__3);
				 _localctx.result =  factory.createCall(receiver, parameters, _localctx.e); 
				}
				break;
			case T__27:
				{
				setState(223);
				match(T__27);
				setState(224);
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
			case T__28:
				{
				setState(227);
				match(T__28);
				 if (receiver == null) {
				                                                       receiver = factory.createRead(assignmentName);
				                                                  } 
				setState(229);
				_localctx.IDENTIFIER = match(IDENTIFIER);
				 nestedAssignmentName = factory.createStringLiteral(_localctx.IDENTIFIER, false);
				                                                  _localctx.result =  factory.createReadProperty(receiver, nestedAssignmentName); 
				}
				break;
			case T__29:
				{
				setState(231);
				match(T__29);
				 if (receiver == null) {
				                                                      receiver = factory.createRead(assignmentName);
				                                                  } 
				setState(233);
				_localctx.expression = expression();
				 nestedAssignmentName = _localctx.expression.result;
				                                                  _localctx.result =  factory.createReadProperty(receiver, nestedAssignmentName); 
				setState(235);
				match(T__30);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(242);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				{
				setState(239);
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
		"\u0004\u0001%\u00f5\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0001\u0000\u0001\u0000"+
		"\u0005\u0000!\b\u0000\n\u0000\f\u0000$\t\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0005\u00011\b\u0001\n\u0001\f\u0001"+
		"4\t\u0001\u0003\u00016\b\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0003"+
		"\u0002A\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0005\u0003H\b\u0003\n\u0003\f\u0003K\t\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003"+
		"\u0004f\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0006\u0003\u0006y\b\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u0082\b\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b"+
		"\u0001\b\u0005\b\u008d\b\b\n\b\f\b\u0090\t\b\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0001\t\u0005\t\u0098\b\t\n\t\f\t\u009b\t\t\u0001\n\u0001\n"+
		"\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u00a3\b\n\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b\u00ab\b\u000b"+
		"\n\u000b\f\u000b\u00ae\t\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f"+
		"\u0001\f\u0005\f\u00b6\b\f\n\f\f\f\u00b9\t\f\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0003\r\u00c1\b\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u00cc\b\r\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0005\u000e\u00d7\b\u000e\n\u000e\f\u000e\u00da\t\u000e"+
		"\u0003\u000e\u00dc\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0003\u000e\u00ee\b\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0003\u000e"+
		"\u00f3\b\u000e\u0001\u000e\u0000\u0000\u000f\u0000\u0002\u0004\u0006\b"+
		"\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u0000\u0003\u0001"+
		"\u0000\u0012\u0017\u0001\u0000\u0018\u0019\u0001\u0000\u001a\u001b\u0101"+
		"\u0000\u001e\u0001\u0000\u0000\u0000\u0002\'\u0001\u0000\u0000\u0000\u0004"+
		"@\u0001\u0000\u0000\u0000\u0006B\u0001\u0000\u0000\u0000\be\u0001\u0000"+
		"\u0000\u0000\ng\u0001\u0000\u0000\u0000\fn\u0001\u0000\u0000\u0000\u000e"+
		"|\u0001\u0000\u0000\u0000\u0010\u0086\u0001\u0000\u0000\u0000\u0012\u0091"+
		"\u0001\u0000\u0000\u0000\u0014\u009c\u0001\u0000\u0000\u0000\u0016\u00a4"+
		"\u0001\u0000\u0000\u0000\u0018\u00af\u0001\u0000\u0000\u0000\u001a\u00cb"+
		"\u0001\u0000\u0000\u0000\u001c\u00cd\u0001\u0000\u0000\u0000\u001e\"\u0003"+
		"\u0002\u0001\u0000\u001f!\u0003\u0002\u0001\u0000 \u001f\u0001\u0000\u0000"+
		"\u0000!$\u0001\u0000\u0000\u0000\" \u0001\u0000\u0000\u0000\"#\u0001\u0000"+
		"\u0000\u0000#%\u0001\u0000\u0000\u0000$\"\u0001\u0000\u0000\u0000%&\u0005"+
		"\u0000\u0000\u0001&\u0001\u0001\u0000\u0000\u0000\'(\u0005\u0001\u0000"+
		"\u0000()\u0005#\u0000\u0000)*\u0005\u0002\u0000\u0000*5\u0006\u0001\uffff"+
		"\uffff\u0000+,\u0005#\u0000\u0000,2\u0006\u0001\uffff\uffff\u0000-.\u0005"+
		"\u0003\u0000\u0000./\u0005#\u0000\u0000/1\u0006\u0001\uffff\uffff\u0000"+
		"0-\u0001\u0000\u0000\u000014\u0001\u0000\u0000\u000020\u0001\u0000\u0000"+
		"\u000023\u0001\u0000\u0000\u000036\u0001\u0000\u0000\u000042\u0001\u0000"+
		"\u0000\u00005+\u0001\u0000\u0000\u000056\u0001\u0000\u0000\u000067\u0001"+
		"\u0000\u0000\u000078\u0005\u0004\u0000\u000089\u0003\u0004\u0002\u0000"+
		"9:\u0006\u0001\uffff\uffff\u0000:;\u0003\u0006\u0003\u0000;<\u0006\u0001"+
		"\uffff\uffff\u0000<\u0003\u0001\u0000\u0000\u0000=>\u0005\u0005\u0000"+
		"\u0000>A\u0006\u0002\uffff\uffff\u0000?A\u0006\u0002\uffff\uffff\u0000"+
		"@=\u0001\u0000\u0000\u0000@?\u0001\u0000\u0000\u0000A\u0005\u0001\u0000"+
		"\u0000\u0000BC\u0006\u0003\uffff\uffff\u0000CI\u0005\u0006\u0000\u0000"+
		"DE\u0003\b\u0004\u0000EF\u0006\u0003\uffff\uffff\u0000FH\u0001\u0000\u0000"+
		"\u0000GD\u0001\u0000\u0000\u0000HK\u0001\u0000\u0000\u0000IG\u0001\u0000"+
		"\u0000\u0000IJ\u0001\u0000\u0000\u0000JL\u0001\u0000\u0000\u0000KI\u0001"+
		"\u0000\u0000\u0000LM\u0005\u0007\u0000\u0000MN\u0006\u0003\uffff\uffff"+
		"\u0000N\u0007\u0001\u0000\u0000\u0000OP\u0003\n\u0005\u0000PQ\u0006\u0004"+
		"\uffff\uffff\u0000Qf\u0001\u0000\u0000\u0000RS\u0005\b\u0000\u0000ST\u0006"+
		"\u0004\uffff\uffff\u0000Tf\u0005\t\u0000\u0000UV\u0005\n\u0000\u0000V"+
		"W\u0006\u0004\uffff\uffff\u0000Wf\u0005\t\u0000\u0000XY\u0003\f\u0006"+
		"\u0000YZ\u0006\u0004\uffff\uffff\u0000Zf\u0001\u0000\u0000\u0000[\\\u0003"+
		"\u000e\u0007\u0000\\]\u0006\u0004\uffff\uffff\u0000]f\u0001\u0000\u0000"+
		"\u0000^_\u0003\u0010\b\u0000_`\u0005\t\u0000\u0000`a\u0006\u0004\uffff"+
		"\uffff\u0000af\u0001\u0000\u0000\u0000bc\u0005\u000b\u0000\u0000cd\u0006"+
		"\u0004\uffff\uffff\u0000df\u0005\t\u0000\u0000eO\u0001\u0000\u0000\u0000"+
		"eR\u0001\u0000\u0000\u0000eU\u0001\u0000\u0000\u0000eX\u0001\u0000\u0000"+
		"\u0000e[\u0001\u0000\u0000\u0000e^\u0001\u0000\u0000\u0000eb\u0001\u0000"+
		"\u0000\u0000f\t\u0001\u0000\u0000\u0000gh\u0005\f\u0000\u0000hi\u0005"+
		"\u0002\u0000\u0000ij\u0003\u0010\b\u0000jk\u0005\u0004\u0000\u0000kl\u0003"+
		"\u0006\u0003\u0000lm\u0006\u0005\uffff\uffff\u0000m\u000b\u0001\u0000"+
		"\u0000\u0000no\u0005\r\u0000\u0000op\u0005\u0002\u0000\u0000pq\u0003\u0010"+
		"\b\u0000qr\u0005\u0004\u0000\u0000rs\u0003\u0006\u0003\u0000sx\u0006\u0006"+
		"\uffff\uffff\u0000tu\u0005\u000e\u0000\u0000uv\u0003\u0006\u0003\u0000"+
		"vw\u0006\u0006\uffff\uffff\u0000wy\u0001\u0000\u0000\u0000xt\u0001\u0000"+
		"\u0000\u0000xy\u0001\u0000\u0000\u0000yz\u0001\u0000\u0000\u0000z{\u0006"+
		"\u0006\uffff\uffff\u0000{\r\u0001\u0000\u0000\u0000|}\u0005\u000f\u0000"+
		"\u0000}\u0081\u0006\u0007\uffff\uffff\u0000~\u007f\u0003\u0010\b\u0000"+
		"\u007f\u0080\u0006\u0007\uffff\uffff\u0000\u0080\u0082\u0001\u0000\u0000"+
		"\u0000\u0081~\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000\u0000\u0000"+
		"\u0082\u0083\u0001\u0000\u0000\u0000\u0083\u0084\u0006\u0007\uffff\uffff"+
		"\u0000\u0084\u0085\u0005\t\u0000\u0000\u0085\u000f\u0001\u0000\u0000\u0000"+
		"\u0086\u0087\u0003\u0012\t\u0000\u0087\u008e\u0006\b\uffff\uffff\u0000"+
		"\u0088\u0089\u0005\u0010\u0000\u0000\u0089\u008a\u0003\u0012\t\u0000\u008a"+
		"\u008b\u0006\b\uffff\uffff\u0000\u008b\u008d\u0001\u0000\u0000\u0000\u008c"+
		"\u0088\u0001\u0000\u0000\u0000\u008d\u0090\u0001\u0000\u0000\u0000\u008e"+
		"\u008c\u0001\u0000\u0000\u0000\u008e\u008f\u0001\u0000\u0000\u0000\u008f"+
		"\u0011\u0001\u0000\u0000\u0000\u0090\u008e\u0001\u0000\u0000\u0000\u0091"+
		"\u0092\u0003\u0014\n\u0000\u0092\u0099\u0006\t\uffff\uffff\u0000\u0093"+
		"\u0094\u0005\u0011\u0000\u0000\u0094\u0095\u0003\u0014\n\u0000\u0095\u0096"+
		"\u0006\t\uffff\uffff\u0000\u0096\u0098\u0001\u0000\u0000\u0000\u0097\u0093"+
		"\u0001\u0000\u0000\u0000\u0098\u009b\u0001\u0000\u0000\u0000\u0099\u0097"+
		"\u0001\u0000\u0000\u0000\u0099\u009a\u0001\u0000\u0000\u0000\u009a\u0013"+
		"\u0001\u0000\u0000\u0000\u009b\u0099\u0001\u0000\u0000\u0000\u009c\u009d"+
		"\u0003\u0016\u000b\u0000\u009d\u00a2\u0006\n\uffff\uffff\u0000\u009e\u009f"+
		"\u0007\u0000\u0000\u0000\u009f\u00a0\u0003\u0016\u000b\u0000\u00a0\u00a1"+
		"\u0006\n\uffff\uffff\u0000\u00a1\u00a3\u0001\u0000\u0000\u0000\u00a2\u009e"+
		"\u0001\u0000\u0000\u0000\u00a2\u00a3\u0001\u0000\u0000\u0000\u00a3\u0015"+
		"\u0001\u0000\u0000\u0000\u00a4\u00a5\u0003\u0018\f\u0000\u00a5\u00ac\u0006"+
		"\u000b\uffff\uffff\u0000\u00a6\u00a7\u0007\u0001\u0000\u0000\u00a7\u00a8"+
		"\u0003\u0018\f\u0000\u00a8\u00a9\u0006\u000b\uffff\uffff\u0000\u00a9\u00ab"+
		"\u0001\u0000\u0000\u0000\u00aa\u00a6\u0001\u0000\u0000\u0000\u00ab\u00ae"+
		"\u0001\u0000\u0000\u0000\u00ac\u00aa\u0001\u0000\u0000\u0000\u00ac\u00ad"+
		"\u0001\u0000\u0000\u0000\u00ad\u0017\u0001\u0000\u0000\u0000\u00ae\u00ac"+
		"\u0001\u0000\u0000\u0000\u00af\u00b0\u0003\u001a\r\u0000\u00b0\u00b7\u0006"+
		"\f\uffff\uffff\u0000\u00b1\u00b2\u0007\u0002\u0000\u0000\u00b2\u00b3\u0003"+
		"\u001a\r\u0000\u00b3\u00b4\u0006\f\uffff\uffff\u0000\u00b4\u00b6\u0001"+
		"\u0000\u0000\u0000\u00b5\u00b1\u0001\u0000\u0000\u0000\u00b6\u00b9\u0001"+
		"\u0000\u0000\u0000\u00b7\u00b5\u0001\u0000\u0000\u0000\u00b7\u00b8\u0001"+
		"\u0000\u0000\u0000\u00b8\u0019\u0001\u0000\u0000\u0000\u00b9\u00b7\u0001"+
		"\u0000\u0000\u0000\u00ba\u00bb\u0005#\u0000\u0000\u00bb\u00c0\u0006\r"+
		"\uffff\uffff\u0000\u00bc\u00bd\u0003\u001c\u000e\u0000\u00bd\u00be\u0006"+
		"\r\uffff\uffff\u0000\u00be\u00c1\u0001\u0000\u0000\u0000\u00bf\u00c1\u0006"+
		"\r\uffff\uffff\u0000\u00c0\u00bc\u0001\u0000\u0000\u0000\u00c0\u00bf\u0001"+
		"\u0000\u0000\u0000\u00c1\u00cc\u0001\u0000\u0000\u0000\u00c2\u00c3\u0005"+
		"$\u0000\u0000\u00c3\u00cc\u0006\r\uffff\uffff\u0000\u00c4\u00c5\u0005"+
		"%\u0000\u0000\u00c5\u00cc\u0006\r\uffff\uffff\u0000\u00c6\u00c7\u0005"+
		"\u0002\u0000\u0000\u00c7\u00c8\u0003\u0010\b\u0000\u00c8\u00c9\u0005\u0004"+
		"\u0000\u0000\u00c9\u00ca\u0006\r\uffff\uffff\u0000\u00ca\u00cc\u0001\u0000"+
		"\u0000\u0000\u00cb\u00ba\u0001\u0000\u0000\u0000\u00cb\u00c2\u0001\u0000"+
		"\u0000\u0000\u00cb\u00c4\u0001\u0000\u0000\u0000\u00cb\u00c6\u0001\u0000"+
		"\u0000\u0000\u00cc\u001b\u0001\u0000\u0000\u0000\u00cd\u00ed\u0006\u000e"+
		"\uffff\uffff\u0000\u00ce\u00cf\u0005\u0002\u0000\u0000\u00cf\u00db\u0006"+
		"\u000e\uffff\uffff\u0000\u00d0\u00d1\u0003\u0010\b\u0000\u00d1\u00d8\u0006"+
		"\u000e\uffff\uffff\u0000\u00d2\u00d3\u0005\u0003\u0000\u0000\u00d3\u00d4"+
		"\u0003\u0010\b\u0000\u00d4\u00d5\u0006\u000e\uffff\uffff\u0000\u00d5\u00d7"+
		"\u0001\u0000\u0000\u0000\u00d6\u00d2\u0001\u0000\u0000\u0000\u00d7\u00da"+
		"\u0001\u0000\u0000\u0000\u00d8\u00d6\u0001\u0000\u0000\u0000\u00d8\u00d9"+
		"\u0001\u0000\u0000\u0000\u00d9\u00dc\u0001\u0000\u0000\u0000\u00da\u00d8"+
		"\u0001\u0000\u0000\u0000\u00db\u00d0\u0001\u0000\u0000\u0000\u00db\u00dc"+
		"\u0001\u0000\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000\u0000\u00dd\u00de"+
		"\u0005\u0004\u0000\u0000\u00de\u00ee\u0006\u000e\uffff\uffff\u0000\u00df"+
		"\u00e0\u0005\u001c\u0000\u0000\u00e0\u00e1\u0003\u0010\b\u0000\u00e1\u00e2"+
		"\u0006\u000e\uffff\uffff\u0000\u00e2\u00ee\u0001\u0000\u0000\u0000\u00e3"+
		"\u00e4\u0005\u001d\u0000\u0000\u00e4\u00e5\u0006\u000e\uffff\uffff\u0000"+
		"\u00e5\u00e6\u0005#\u0000\u0000\u00e6\u00ee\u0006\u000e\uffff\uffff\u0000"+
		"\u00e7\u00e8\u0005\u001e\u0000\u0000\u00e8\u00e9\u0006\u000e\uffff\uffff"+
		"\u0000\u00e9\u00ea\u0003\u0010\b\u0000\u00ea\u00eb\u0006\u000e\uffff\uffff"+
		"\u0000\u00eb\u00ec\u0005\u001f\u0000\u0000\u00ec\u00ee\u0001\u0000\u0000"+
		"\u0000\u00ed\u00ce\u0001\u0000\u0000\u0000\u00ed\u00df\u0001\u0000\u0000"+
		"\u0000\u00ed\u00e3\u0001\u0000\u0000\u0000\u00ed\u00e7\u0001\u0000\u0000"+
		"\u0000\u00ee\u00f2\u0001\u0000\u0000\u0000\u00ef\u00f0\u0003\u001c\u000e"+
		"\u0000\u00f0\u00f1\u0006\u000e\uffff\uffff\u0000\u00f1\u00f3\u0001\u0000"+
		"\u0000\u0000\u00f2\u00ef\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000"+
		"\u0000\u0000\u00f3\u001d\u0001\u0000\u0000\u0000\u0013\"25@Iex\u0081\u008e"+
		"\u0099\u00a2\u00ac\u00b7\u00c0\u00cb\u00d8\u00db\u00ed\u00f2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
