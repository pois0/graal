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

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings("all")
public class SimpleLanguageLexer extends Lexer {
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
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
			"T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "T__24", 
			"T__25", "T__26", "T__27", "T__28", "T__29", "T__30", "T__31", "T__32", 
			"WS", "COMMENT", "LINE_COMMENT", "LETTER", "NON_ZERO_DIGIT", "DIGIT", 
			"HEX_DIGIT", "OCT_DIGIT", "BINARY_DIGIT", "TAB", "STRING_CHAR", "IDENTIFIER", 
			"STRING_LITERAL", "NUMERIC_LITERAL"
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


	public SimpleLanguageLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "SimpleLanguage.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2)\u012c\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3"+
		"\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\22\3\23\3\23\3\23\3\24\3\24\3\24\3\25\3\25\3\26\3\26\3\26\3\27\3\27"+
		"\3\30\3\30\3\30\3\31\3\31\3\31\3\32\3\32\3\32\3\33\3\33\3\34\3\34\3\35"+
		"\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\"\3#\6#\u00e1\n#\r#\16#\u00e2"+
		"\3#\3#\3$\3$\3$\3$\7$\u00eb\n$\f$\16$\u00ee\13$\3$\3$\3$\3$\3$\3%\3%\3"+
		"%\3%\7%\u00f9\n%\f%\16%\u00fc\13%\3%\3%\3&\5&\u0101\n&\3\'\3\'\3(\3(\3"+
		")\5)\u0108\n)\3*\3*\3+\3+\3,\3,\3-\3-\3.\3.\3.\7.\u0115\n.\f.\16.\u0118"+
		"\13.\3/\3/\7/\u011c\n/\f/\16/\u011f\13/\3/\3/\3\60\3\60\3\60\7\60\u0126"+
		"\n\60\f\60\16\60\u0129\13\60\5\60\u012b\n\60\3\u00ec\2\61\3\3\5\4\7\5"+
		"\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23"+
		"%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\"C#E$G"+
		"%I&K\2M\2O\2Q\2S\2U\2W\2Y\2[\'](_)\3\2\n\5\2\13\f\16\17\"\"\4\2\f\f\17"+
		"\17\6\2&&C\\aac|\3\2\63;\3\2\62;\5\2\62;CHch\3\2\629\6\2\f\f\17\17$$^"+
		"^\2\u012b\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2"+
		"\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2"+
		"\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2"+
		"\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2"+
		"\2G\3\2\2\2\2I\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\3a\3\2\2\2\5j"+
		"\3\2\2\2\7l\3\2\2\2\tn\3\2\2\2\13p\3\2\2\2\rr\3\2\2\2\17t\3\2\2\2\21z"+
		"\3\2\2\2\23|\3\2\2\2\25\u0085\3\2\2\2\27\u008e\3\2\2\2\31\u0094\3\2\2"+
		"\2\33\u0097\3\2\2\2\35\u009c\3\2\2\2\37\u00a3\3\2\2\2!\u00aa\3\2\2\2#"+
		"\u00b1\3\2\2\2%\u00b9\3\2\2\2\'\u00bc\3\2\2\2)\u00bf\3\2\2\2+\u00c1\3"+
		"\2\2\2-\u00c4\3\2\2\2/\u00c6\3\2\2\2\61\u00c9\3\2\2\2\63\u00cc\3\2\2\2"+
		"\65\u00cf\3\2\2\2\67\u00d1\3\2\2\29\u00d3\3\2\2\2;\u00d5\3\2\2\2=\u00d7"+
		"\3\2\2\2?\u00d9\3\2\2\2A\u00db\3\2\2\2C\u00dd\3\2\2\2E\u00e0\3\2\2\2G"+
		"\u00e6\3\2\2\2I\u00f4\3\2\2\2K\u0100\3\2\2\2M\u0102\3\2\2\2O\u0104\3\2"+
		"\2\2Q\u0107\3\2\2\2S\u0109\3\2\2\2U\u010b\3\2\2\2W\u010d\3\2\2\2Y\u010f"+
		"\3\2\2\2[\u0111\3\2\2\2]\u0119\3\2\2\2_\u012a\3\2\2\2ab\7h\2\2bc\7w\2"+
		"\2cd\7p\2\2de\7e\2\2ef\7v\2\2fg\7k\2\2gh\7q\2\2hi\7p\2\2i\4\3\2\2\2jk"+
		"\7*\2\2k\6\3\2\2\2lm\7.\2\2m\b\3\2\2\2no\7+\2\2o\n\3\2\2\2pq\7}\2\2q\f"+
		"\3\2\2\2rs\7\177\2\2s\16\3\2\2\2tu\7d\2\2uv\7t\2\2vw\7g\2\2wx\7c\2\2x"+
		"y\7m\2\2y\20\3\2\2\2z{\7=\2\2{\22\3\2\2\2|}\7e\2\2}~\7q\2\2~\177\7p\2"+
		"\2\177\u0080\7v\2\2\u0080\u0081\7k\2\2\u0081\u0082\7p\2\2\u0082\u0083"+
		"\7w\2\2\u0083\u0084\7g\2\2\u0084\24\3\2\2\2\u0085\u0086\7f\2\2\u0086\u0087"+
		"\7g\2\2\u0087\u0088\7d\2\2\u0088\u0089\7w\2\2\u0089\u008a\7i\2\2\u008a"+
		"\u008b\7i\2\2\u008b\u008c\7g\2\2\u008c\u008d\7t\2\2\u008d\26\3\2\2\2\u008e"+
		"\u008f\7y\2\2\u008f\u0090\7j\2\2\u0090\u0091\7k\2\2\u0091\u0092\7n\2\2"+
		"\u0092\u0093\7g\2\2\u0093\30\3\2\2\2\u0094\u0095\7k\2\2\u0095\u0096\7"+
		"h\2\2\u0096\32\3\2\2\2\u0097\u0098\7g\2\2\u0098\u0099\7n\2\2\u0099\u009a"+
		"\7u\2\2\u009a\u009b\7g\2\2\u009b\34\3\2\2\2\u009c\u009d\7t\2\2\u009d\u009e"+
		"\7g\2\2\u009e\u009f\7v\2\2\u009f\u00a0\7w\2\2\u00a0\u00a1\7t\2\2\u00a1"+
		"\u00a2\7p\2\2\u00a2\36\3\2\2\2\u00a3\u00a4\7f\2\2\u00a4\u00a5\7g\2\2\u00a5"+
		"\u00a6\7n\2\2\u00a6\u00a7\7g\2\2\u00a7\u00a8\7v\2\2\u00a8\u00a9\7g\2\2"+
		"\u00a9 \3\2\2\2\u00aa\u00ab\7k\2\2\u00ab\u00ac\7p\2\2\u00ac\u00ad\7u\2"+
		"\2\u00ad\u00ae\7g\2\2\u00ae\u00af\7t\2\2\u00af\u00b0\7v\2\2\u00b0\"\3"+
		"\2\2\2\u00b1\u00b2\7t\2\2\u00b2\u00b3\7g\2\2\u00b3\u00b4\7r\2\2\u00b4"+
		"\u00b5\7n\2\2\u00b5\u00b6\7c\2\2\u00b6\u00b7\7e\2\2\u00b7\u00b8\7g\2\2"+
		"\u00b8$\3\2\2\2\u00b9\u00ba\7~\2\2\u00ba\u00bb\7~\2\2\u00bb&\3\2\2\2\u00bc"+
		"\u00bd\7(\2\2\u00bd\u00be\7(\2\2\u00be(\3\2\2\2\u00bf\u00c0\7>\2\2\u00c0"+
		"*\3\2\2\2\u00c1\u00c2\7>\2\2\u00c2\u00c3\7?\2\2\u00c3,\3\2\2\2\u00c4\u00c5"+
		"\7@\2\2\u00c5.\3\2\2\2\u00c6\u00c7\7@\2\2\u00c7\u00c8\7?\2\2\u00c8\60"+
		"\3\2\2\2\u00c9\u00ca\7?\2\2\u00ca\u00cb\7?\2\2\u00cb\62\3\2\2\2\u00cc"+
		"\u00cd\7#\2\2\u00cd\u00ce\7?\2\2\u00ce\64\3\2\2\2\u00cf\u00d0\7-\2\2\u00d0"+
		"\66\3\2\2\2\u00d1\u00d2\7/\2\2\u00d28\3\2\2\2\u00d3\u00d4\7,\2\2\u00d4"+
		":\3\2\2\2\u00d5\u00d6\7\61\2\2\u00d6<\3\2\2\2\u00d7\u00d8\7?\2\2\u00d8"+
		">\3\2\2\2\u00d9\u00da\7\60\2\2\u00da@\3\2\2\2\u00db\u00dc\7]\2\2\u00dc"+
		"B\3\2\2\2\u00dd\u00de\7_\2\2\u00deD\3\2\2\2\u00df\u00e1\t\2\2\2\u00e0"+
		"\u00df\3\2\2\2\u00e1\u00e2\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e2\u00e3\3\2"+
		"\2\2\u00e3\u00e4\3\2\2\2\u00e4\u00e5\b#\2\2\u00e5F\3\2\2\2\u00e6\u00e7"+
		"\7\61\2\2\u00e7\u00e8\7,\2\2\u00e8\u00ec\3\2\2\2\u00e9\u00eb\13\2\2\2"+
		"\u00ea\u00e9\3\2\2\2\u00eb\u00ee\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ec\u00ea"+
		"\3\2\2\2\u00ed\u00ef\3\2\2\2\u00ee\u00ec\3\2\2\2\u00ef\u00f0\7,\2\2\u00f0"+
		"\u00f1\7\61\2\2\u00f1\u00f2\3\2\2\2\u00f2\u00f3\b$\2\2\u00f3H\3\2\2\2"+
		"\u00f4\u00f5\7\61\2\2\u00f5\u00f6\7\61\2\2\u00f6\u00fa\3\2\2\2\u00f7\u00f9"+
		"\n\3\2\2\u00f8\u00f7\3\2\2\2\u00f9\u00fc\3\2\2\2\u00fa\u00f8\3\2\2\2\u00fa"+
		"\u00fb\3\2\2\2\u00fb\u00fd\3\2\2\2\u00fc\u00fa\3\2\2\2\u00fd\u00fe\b%"+
		"\2\2\u00feJ\3\2\2\2\u00ff\u0101\t\4\2\2\u0100\u00ff\3\2\2\2\u0101L\3\2"+
		"\2\2\u0102\u0103\t\5\2\2\u0103N\3\2\2\2\u0104\u0105\t\6\2\2\u0105P\3\2"+
		"\2\2\u0106\u0108\t\7\2\2\u0107\u0106\3\2\2\2\u0108R\3\2\2\2\u0109\u010a"+
		"\t\b\2\2\u010aT\3\2\2\2\u010b\u010c\4\62\63\2\u010cV\3\2\2\2\u010d\u010e"+
		"\7\13\2\2\u010eX\3\2\2\2\u010f\u0110\n\t\2\2\u0110Z\3\2\2\2\u0111\u0116"+
		"\5K&\2\u0112\u0115\5K&\2\u0113\u0115\5O(\2\u0114\u0112\3\2\2\2\u0114\u0113"+
		"\3\2\2\2\u0115\u0118\3\2\2\2\u0116\u0114\3\2\2\2\u0116\u0117\3\2\2\2\u0117"+
		"\\\3\2\2\2\u0118\u0116\3\2\2\2\u0119\u011d\7$\2\2\u011a\u011c\5Y-\2\u011b"+
		"\u011a\3\2\2\2\u011c\u011f\3\2\2\2\u011d\u011b\3\2\2\2\u011d\u011e\3\2"+
		"\2\2\u011e\u0120\3\2\2\2\u011f\u011d\3\2\2\2\u0120\u0121\7$\2\2\u0121"+
		"^\3\2\2\2\u0122\u012b\7\62\2\2\u0123\u0127\5M\'\2\u0124\u0126\5O(\2\u0125"+
		"\u0124\3\2\2\2\u0126\u0129\3\2\2\2\u0127\u0125\3\2\2\2\u0127\u0128\3\2"+
		"\2\2\u0128\u012b\3\2\2\2\u0129\u0127\3\2\2\2\u012a\u0122\3\2\2\2\u012a"+
		"\u0123\3\2\2\2\u012b`\3\2\2\2\r\2\u00e2\u00ec\u00fa\u0100\u0107\u0114"+
		"\u0116\u011d\u0127\u012a\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
