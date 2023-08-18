package com.oracle.truffle.sl.runtime.diffexec;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.sl.nodes.SLExpressionNode;
import com.oracle.truffle.sl.nodes.SLStatementNode;
import com.oracle.truffle.sl.runtime.SLObject;

public interface ExecutionHistoryOperator<T> {
    void onReadArgument(String argName);
    void onReadLocalVariable(String varName);
    void onReadObjectField(Object receiver, String fieldName);
    void onReadArrayElement(Object array, long index);

    void onGenerateObject(SLObject object);

    void onUpdateLocalVariable(String varName, Object value);
    void onUpdateObjectField(Object receiver, String fieldName, Object value);
    void onUpdateArrayElement(Object array, long index, Object value);

    void rewriteLocalVariable(String varName, Object value, NodeIdentifier identifier);
    void rewriteObjectField(Object receiver, String fieldName, Object value, NodeIdentifier identifier, boolean fieldChanged);
    void rewriteArrayElement(Object array, long index, Object value, NodeIdentifier identifier, boolean fieldChanged);

    void deleteHistory(NodeIdentifier identifier);

    T onEnterExpression(NodeIdentifier identifier);
    void onReturnValue(NodeIdentifier identifier, T onEnterKey, Object result);
    void onReturnExceptional(NodeIdentifier identifier, T onEnterKey, Throwable exception);

    void onEnterFunctionDuringExec(NodeIdentifier caller, String functionName, int argLen);
    void onEnterFunctionDuringCalc(NodeIdentifier caller, String functionName, boolean argFlags);
    void onExitFunction(boolean duringCalc);
    void onEnterLoop(NodeIdentifier identifier);
    void onEnterNextIteration();
    void onExitLoop();

    void calcVoid(VirtualFrame frame, SLStatementNode node);
    <U> CalcResult.Generic<U> calcGeneric(VirtualFrame frame, SLExpressionNode calleeNode);
    CalcResult.Boolean calcBoolean(VirtualFrame frame, SLExpressionNode callerNode, SLExpressionNode calleeNode);
    CalcResult.Long calcLong(VirtualFrame frame, SLExpressionNode callerNode, SLExpressionNode calleeNode);

    Object getVariableValue(String varName, NodeIdentifier identifier);
    Object getObjectFieldValue(Object receiver, String fieldName, NodeIdentifier identifier);
    Object getArrayElementValue(Object array, long index, Object value);
}
