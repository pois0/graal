package com.oracle.truffle.sl.parser;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.sl.nodes.controlflow.SLBlockNode;
import org.graalvm.collections.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

final class SimpleLanguageParserSupport {
    private final static Random rand = new Random();
    private static boolean rotate = true;

    static Pair<Map<String, RootCallTarget>, Set<String>> getMapSetPair(SimpleLanguageParser parser) {
        ArrayList<Pair<String, SLBlockNode>> flaggableFunctions = parser.factory.flaggableFunctions;

        // TODO
        if (rotate) {
            System.out.println("Parser: 1st!");
            rotate = false;
            return Pair.create(parser.factory.getAllFunctions(), parser.factory.getFunctionContainsNewNode());
        }
        System.out.println("Parser: 2nd!");
        rotate = true;

        int statementSize = 0;
        for (Pair<String,SLBlockNode> f : flaggableFunctions) {
            statementSize += f.getRight().getSize();
        }

        int stmtRand = rand.nextInt(statementSize);
        int tmp = 0;
        for (Pair<String,SLBlockNode> f : flaggableFunctions) {
            SLBlockNode right = f.getRight();
            int fSize = right.getSize();
            if (stmtRand < tmp + fSize) {
                right.handleAsReplaced(stmtRand - tmp);
                HashSet<String> modifiedFuncs = new HashSet<>();
                modifiedFuncs.add(f.getLeft());
                return Pair.create(parser.factory.getAllFunctions(), modifiedFuncs);
            }
            tmp += fSize;
        }

        throw new IllegalStateException();
    }
}
