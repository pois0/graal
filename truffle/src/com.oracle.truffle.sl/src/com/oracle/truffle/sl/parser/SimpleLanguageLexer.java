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
			"T__25", "T__26", "T__27", "T__28", "T__29", "T__30", "WS", "COMMENT", 
			"LINE_COMMENT", "LETTER", "NON_ZERO_DIGIT", "DIGIT", "HEX_DIGIT", "OCT_DIGIT", 
			"BINARY_DIGIT", "TAB", "STRING_CHAR", "IDENTIFIER", "STRING_LITERAL", 
			"NUMERIC_LITERAL"
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
		"\u0004\u0000%\u0112\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002"+
		"\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002"+
		"\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002"+
		"\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002"+
		"\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002"+
		"\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002"+
		"\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007"+
		"!\u0002\"\u0007\"\u0002#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007"+
		"&\u0002\'\u0007\'\u0002(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007"+
		"+\u0002,\u0007,\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001"+
		"\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001"+
		"\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001"+
		"\u0017\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u001a\u0001"+
		"\u001a\u0001\u001b\u0001\u001b\u0001\u001c\u0001\u001c\u0001\u001d\u0001"+
		"\u001d\u0001\u001e\u0001\u001e\u0001\u001f\u0004\u001f\u00c7\b\u001f\u000b"+
		"\u001f\f\u001f\u00c8\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0001"+
		" \u0005 \u00d1\b \n \f \u00d4\t \u0001 \u0001 \u0001 \u0001 \u0001 \u0001"+
		"!\u0001!\u0001!\u0001!\u0005!\u00df\b!\n!\f!\u00e2\t!\u0001!\u0001!\u0001"+
		"\"\u0003\"\u00e7\b\"\u0001#\u0001#\u0001$\u0001$\u0001%\u0003%\u00ee\b"+
		"%\u0001&\u0001&\u0001\'\u0001\'\u0001(\u0001(\u0001)\u0001)\u0001*\u0001"+
		"*\u0001*\u0005*\u00fb\b*\n*\f*\u00fe\t*\u0001+\u0001+\u0005+\u0102\b+"+
		"\n+\f+\u0105\t+\u0001+\u0001+\u0001,\u0001,\u0001,\u0005,\u010c\b,\n,"+
		"\f,\u010f\t,\u0003,\u0111\b,\u0001\u00d2\u0000-\u0001\u0001\u0003\u0002"+
		"\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\t\u0013"+
		"\n\u0015\u000b\u0017\f\u0019\r\u001b\u000e\u001d\u000f\u001f\u0010!\u0011"+
		"#\u0012%\u0013\'\u0014)\u0015+\u0016-\u0017/\u00181\u00193\u001a5\u001b"+
		"7\u001c9\u001d;\u001e=\u001f? A!C\"E\u0000G\u0000I\u0000K\u0000M\u0000"+
		"O\u0000Q\u0000S\u0000U#W$Y%\u0001\u0000\b\u0003\u0000\t\n\f\r  \u0002"+
		"\u0000\n\n\r\r\u0004\u0000$$AZ__az\u0001\u000019\u0001\u000009\u0003\u0000"+
		"09AFaf\u0001\u000007\u0003\u0000\n\n\r\r\"\"\u0111\u0000\u0001\u0001\u0000"+
		"\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005\u0001\u0000"+
		"\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001\u0000\u0000"+
		"\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000\u0000\u0000"+
		"\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000\u0000\u0000"+
		"\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000\u0000\u0000"+
		"\u0000\u0017\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000"+
		"\u0000\u001b\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000"+
		"\u0000\u001f\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0000"+
		"#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0000\'\u0001"+
		"\u0000\u0000\u0000\u0000)\u0001\u0000\u0000\u0000\u0000+\u0001\u0000\u0000"+
		"\u0000\u0000-\u0001\u0000\u0000\u0000\u0000/\u0001\u0000\u0000\u0000\u0000"+
		"1\u0001\u0000\u0000\u0000\u00003\u0001\u0000\u0000\u0000\u00005\u0001"+
		"\u0000\u0000\u0000\u00007\u0001\u0000\u0000\u0000\u00009\u0001\u0000\u0000"+
		"\u0000\u0000;\u0001\u0000\u0000\u0000\u0000=\u0001\u0000\u0000\u0000\u0000"+
		"?\u0001\u0000\u0000\u0000\u0000A\u0001\u0000\u0000\u0000\u0000C\u0001"+
		"\u0000\u0000\u0000\u0000U\u0001\u0000\u0000\u0000\u0000W\u0001\u0000\u0000"+
		"\u0000\u0000Y\u0001\u0000\u0000\u0000\u0001[\u0001\u0000\u0000\u0000\u0003"+
		"d\u0001\u0000\u0000\u0000\u0005f\u0001\u0000\u0000\u0000\u0007h\u0001"+
		"\u0000\u0000\u0000\tj\u0001\u0000\u0000\u0000\u000bl\u0001\u0000\u0000"+
		"\u0000\rn\u0001\u0000\u0000\u0000\u000fp\u0001\u0000\u0000\u0000\u0011"+
		"v\u0001\u0000\u0000\u0000\u0013x\u0001\u0000\u0000\u0000\u0015\u0081\u0001"+
		"\u0000\u0000\u0000\u0017\u008a\u0001\u0000\u0000\u0000\u0019\u0090\u0001"+
		"\u0000\u0000\u0000\u001b\u0093\u0001\u0000\u0000\u0000\u001d\u0098\u0001"+
		"\u0000\u0000\u0000\u001f\u009f\u0001\u0000\u0000\u0000!\u00a2\u0001\u0000"+
		"\u0000\u0000#\u00a5\u0001\u0000\u0000\u0000%\u00a7\u0001\u0000\u0000\u0000"+
		"\'\u00aa\u0001\u0000\u0000\u0000)\u00ac\u0001\u0000\u0000\u0000+\u00af"+
		"\u0001\u0000\u0000\u0000-\u00b2\u0001\u0000\u0000\u0000/\u00b5\u0001\u0000"+
		"\u0000\u00001\u00b7\u0001\u0000\u0000\u00003\u00b9\u0001\u0000\u0000\u0000"+
		"5\u00bb\u0001\u0000\u0000\u00007\u00bd\u0001\u0000\u0000\u00009\u00bf"+
		"\u0001\u0000\u0000\u0000;\u00c1\u0001\u0000\u0000\u0000=\u00c3\u0001\u0000"+
		"\u0000\u0000?\u00c6\u0001\u0000\u0000\u0000A\u00cc\u0001\u0000\u0000\u0000"+
		"C\u00da\u0001\u0000\u0000\u0000E\u00e6\u0001\u0000\u0000\u0000G\u00e8"+
		"\u0001\u0000\u0000\u0000I\u00ea\u0001\u0000\u0000\u0000K\u00ed\u0001\u0000"+
		"\u0000\u0000M\u00ef\u0001\u0000\u0000\u0000O\u00f1\u0001\u0000\u0000\u0000"+
		"Q\u00f3\u0001\u0000\u0000\u0000S\u00f5\u0001\u0000\u0000\u0000U\u00f7"+
		"\u0001\u0000\u0000\u0000W\u00ff\u0001\u0000\u0000\u0000Y\u0110\u0001\u0000"+
		"\u0000\u0000[\\\u0005f\u0000\u0000\\]\u0005u\u0000\u0000]^\u0005n\u0000"+
		"\u0000^_\u0005c\u0000\u0000_`\u0005t\u0000\u0000`a\u0005i\u0000\u0000"+
		"ab\u0005o\u0000\u0000bc\u0005n\u0000\u0000c\u0002\u0001\u0000\u0000\u0000"+
		"de\u0005(\u0000\u0000e\u0004\u0001\u0000\u0000\u0000fg\u0005,\u0000\u0000"+
		"g\u0006\u0001\u0000\u0000\u0000hi\u0005)\u0000\u0000i\b\u0001\u0000\u0000"+
		"\u0000jk\u0005@\u0000\u0000k\n\u0001\u0000\u0000\u0000lm\u0005{\u0000"+
		"\u0000m\f\u0001\u0000\u0000\u0000no\u0005}\u0000\u0000o\u000e\u0001\u0000"+
		"\u0000\u0000pq\u0005b\u0000\u0000qr\u0005r\u0000\u0000rs\u0005e\u0000"+
		"\u0000st\u0005a\u0000\u0000tu\u0005k\u0000\u0000u\u0010\u0001\u0000\u0000"+
		"\u0000vw\u0005;\u0000\u0000w\u0012\u0001\u0000\u0000\u0000xy\u0005c\u0000"+
		"\u0000yz\u0005o\u0000\u0000z{\u0005n\u0000\u0000{|\u0005t\u0000\u0000"+
		"|}\u0005i\u0000\u0000}~\u0005n\u0000\u0000~\u007f\u0005u\u0000\u0000\u007f"+
		"\u0080\u0005e\u0000\u0000\u0080\u0014\u0001\u0000\u0000\u0000\u0081\u0082"+
		"\u0005d\u0000\u0000\u0082\u0083\u0005e\u0000\u0000\u0083\u0084\u0005b"+
		"\u0000\u0000\u0084\u0085\u0005u\u0000\u0000\u0085\u0086\u0005g\u0000\u0000"+
		"\u0086\u0087\u0005g\u0000\u0000\u0087\u0088\u0005e\u0000\u0000\u0088\u0089"+
		"\u0005r\u0000\u0000\u0089\u0016\u0001\u0000\u0000\u0000\u008a\u008b\u0005"+
		"w\u0000\u0000\u008b\u008c\u0005h\u0000\u0000\u008c\u008d\u0005i\u0000"+
		"\u0000\u008d\u008e\u0005l\u0000\u0000\u008e\u008f\u0005e\u0000\u0000\u008f"+
		"\u0018\u0001\u0000\u0000\u0000\u0090\u0091\u0005i\u0000\u0000\u0091\u0092"+
		"\u0005f\u0000\u0000\u0092\u001a\u0001\u0000\u0000\u0000\u0093\u0094\u0005"+
		"e\u0000\u0000\u0094\u0095\u0005l\u0000\u0000\u0095\u0096\u0005s\u0000"+
		"\u0000\u0096\u0097\u0005e\u0000\u0000\u0097\u001c\u0001\u0000\u0000\u0000"+
		"\u0098\u0099\u0005r\u0000\u0000\u0099\u009a\u0005e\u0000\u0000\u009a\u009b"+
		"\u0005t\u0000\u0000\u009b\u009c\u0005u\u0000\u0000\u009c\u009d\u0005r"+
		"\u0000\u0000\u009d\u009e\u0005n\u0000\u0000\u009e\u001e\u0001\u0000\u0000"+
		"\u0000\u009f\u00a0\u0005|\u0000\u0000\u00a0\u00a1\u0005|\u0000\u0000\u00a1"+
		" \u0001\u0000\u0000\u0000\u00a2\u00a3\u0005&\u0000\u0000\u00a3\u00a4\u0005"+
		"&\u0000\u0000\u00a4\"\u0001\u0000\u0000\u0000\u00a5\u00a6\u0005<\u0000"+
		"\u0000\u00a6$\u0001\u0000\u0000\u0000\u00a7\u00a8\u0005<\u0000\u0000\u00a8"+
		"\u00a9\u0005=\u0000\u0000\u00a9&\u0001\u0000\u0000\u0000\u00aa\u00ab\u0005"+
		">\u0000\u0000\u00ab(\u0001\u0000\u0000\u0000\u00ac\u00ad\u0005>\u0000"+
		"\u0000\u00ad\u00ae\u0005=\u0000\u0000\u00ae*\u0001\u0000\u0000\u0000\u00af"+
		"\u00b0\u0005=\u0000\u0000\u00b0\u00b1\u0005=\u0000\u0000\u00b1,\u0001"+
		"\u0000\u0000\u0000\u00b2\u00b3\u0005!\u0000\u0000\u00b3\u00b4\u0005=\u0000"+
		"\u0000\u00b4.\u0001\u0000\u0000\u0000\u00b5\u00b6\u0005+\u0000\u0000\u00b6"+
		"0\u0001\u0000\u0000\u0000\u00b7\u00b8\u0005-\u0000\u0000\u00b82\u0001"+
		"\u0000\u0000\u0000\u00b9\u00ba\u0005*\u0000\u0000\u00ba4\u0001\u0000\u0000"+
		"\u0000\u00bb\u00bc\u0005/\u0000\u0000\u00bc6\u0001\u0000\u0000\u0000\u00bd"+
		"\u00be\u0005=\u0000\u0000\u00be8\u0001\u0000\u0000\u0000\u00bf\u00c0\u0005"+
		".\u0000\u0000\u00c0:\u0001\u0000\u0000\u0000\u00c1\u00c2\u0005[\u0000"+
		"\u0000\u00c2<\u0001\u0000\u0000\u0000\u00c3\u00c4\u0005]\u0000\u0000\u00c4"+
		">\u0001\u0000\u0000\u0000\u00c5\u00c7\u0007\u0000\u0000\u0000\u00c6\u00c5"+
		"\u0001\u0000\u0000\u0000\u00c7\u00c8\u0001\u0000\u0000\u0000\u00c8\u00c6"+
		"\u0001\u0000\u0000\u0000\u00c8\u00c9\u0001\u0000\u0000\u0000\u00c9\u00ca"+
		"\u0001\u0000\u0000\u0000\u00ca\u00cb\u0006\u001f\u0000\u0000\u00cb@\u0001"+
		"\u0000\u0000\u0000\u00cc\u00cd\u0005/\u0000\u0000\u00cd\u00ce\u0005*\u0000"+
		"\u0000\u00ce\u00d2\u0001\u0000\u0000\u0000\u00cf\u00d1\t\u0000\u0000\u0000"+
		"\u00d0\u00cf\u0001\u0000\u0000\u0000\u00d1\u00d4\u0001\u0000\u0000\u0000"+
		"\u00d2\u00d3\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001\u0000\u0000\u0000"+
		"\u00d3\u00d5\u0001\u0000\u0000\u0000\u00d4\u00d2\u0001\u0000\u0000\u0000"+
		"\u00d5\u00d6\u0005*\u0000\u0000\u00d6\u00d7\u0005/\u0000\u0000\u00d7\u00d8"+
		"\u0001\u0000\u0000\u0000\u00d8\u00d9\u0006 \u0000\u0000\u00d9B\u0001\u0000"+
		"\u0000\u0000\u00da\u00db\u0005/\u0000\u0000\u00db\u00dc\u0005/\u0000\u0000"+
		"\u00dc\u00e0\u0001\u0000\u0000\u0000\u00dd\u00df\b\u0001\u0000\u0000\u00de"+
		"\u00dd\u0001\u0000\u0000\u0000\u00df\u00e2\u0001\u0000\u0000\u0000\u00e0"+
		"\u00de\u0001\u0000\u0000\u0000\u00e0\u00e1\u0001\u0000\u0000\u0000\u00e1"+
		"\u00e3\u0001\u0000\u0000\u0000\u00e2\u00e0\u0001\u0000\u0000\u0000\u00e3"+
		"\u00e4\u0006!\u0000\u0000\u00e4D\u0001\u0000\u0000\u0000\u00e5\u00e7\u0007"+
		"\u0002\u0000\u0000\u00e6\u00e5\u0001\u0000\u0000\u0000\u00e7F\u0001\u0000"+
		"\u0000\u0000\u00e8\u00e9\u0007\u0003\u0000\u0000\u00e9H\u0001\u0000\u0000"+
		"\u0000\u00ea\u00eb\u0007\u0004\u0000\u0000\u00ebJ\u0001\u0000\u0000\u0000"+
		"\u00ec\u00ee\u0007\u0005\u0000\u0000\u00ed\u00ec\u0001\u0000\u0000\u0000"+
		"\u00eeL\u0001\u0000\u0000\u0000\u00ef\u00f0\u0007\u0006\u0000\u0000\u00f0"+
		"N\u0001\u0000\u0000\u0000\u00f1\u00f2\u000201\u0000\u00f2P\u0001\u0000"+
		"\u0000\u0000\u00f3\u00f4\u0005\t\u0000\u0000\u00f4R\u0001\u0000\u0000"+
		"\u0000\u00f5\u00f6\b\u0007\u0000\u0000\u00f6T\u0001\u0000\u0000\u0000"+
		"\u00f7\u00fc\u0003E\"\u0000\u00f8\u00fb\u0003E\"\u0000\u00f9\u00fb\u0003"+
		"I$\u0000\u00fa\u00f8\u0001\u0000\u0000\u0000\u00fa\u00f9\u0001\u0000\u0000"+
		"\u0000\u00fb\u00fe\u0001\u0000\u0000\u0000\u00fc\u00fa\u0001\u0000\u0000"+
		"\u0000\u00fc\u00fd\u0001\u0000\u0000\u0000\u00fdV\u0001\u0000\u0000\u0000"+
		"\u00fe\u00fc\u0001\u0000\u0000\u0000\u00ff\u0103\u0005\"\u0000\u0000\u0100"+
		"\u0102\u0003S)\u0000\u0101\u0100\u0001\u0000\u0000\u0000\u0102\u0105\u0001"+
		"\u0000\u0000\u0000\u0103\u0101\u0001\u0000\u0000\u0000\u0103\u0104\u0001"+
		"\u0000\u0000\u0000\u0104\u0106\u0001\u0000\u0000\u0000\u0105\u0103\u0001"+
		"\u0000\u0000\u0000\u0106\u0107\u0005\"\u0000\u0000\u0107X\u0001\u0000"+
		"\u0000\u0000\u0108\u0111\u00050\u0000\u0000\u0109\u010d\u0003G#\u0000"+
		"\u010a\u010c\u0003I$\u0000\u010b\u010a\u0001\u0000\u0000\u0000\u010c\u010f"+
		"\u0001\u0000\u0000\u0000\u010d\u010b\u0001\u0000\u0000\u0000\u010d\u010e"+
		"\u0001\u0000\u0000\u0000\u010e\u0111\u0001\u0000\u0000\u0000\u010f\u010d"+
		"\u0001\u0000\u0000\u0000\u0110\u0108\u0001\u0000\u0000\u0000\u0110\u0109"+
		"\u0001\u0000\u0000\u0000\u0111Z\u0001\u0000\u0000\u0000\u000b\u0000\u00c8"+
		"\u00d2\u00e0\u00e6\u00ed\u00fa\u00fc\u0103\u010d\u0110\u0001\u0006\u0000"+
		"\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}
