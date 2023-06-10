function parser(tokens, tokenLen) {
  current = 0;

  ast = new();
  ast.type = "Program";
  ast.body = new();

  bodyLen = 0;

  while (current < tokenLen) {
    whileVar = parserWalk(tokens, current);
    current = whileVar.current;
    ast.body[bodyLen] = whileVar.result;
    bodyLen = bodyLen + 1;
  }

  ast.bodyLen = bodyLen;

  return ast;
}

function parserWalk(tokens, current) {
  token = tokens[current];
  if (token.type == "number") {
    return parserGenResult("Number", token.value, current + 1);
  }

  if (token.type == "string") {
    return parserGenResult("String", token.value, current + 1);
  }

    current = current + 1;
    token = tokens[current];

    node = new();
    node.current = current;
    node.result = new();
    node.result.type = "Call";
    node.result.name = token.value;
    node.result.params = new();

    current = current + 1;
    token = tokens[current];

    params = new();
    paramIndex = 0;
    while (token.type != ")") {
      whileVar = parserWalk(tokens, current);
      current = whileVar.current;
      params[paramIndex] = whileVar.result;
      paramIndex = paramIndex + 1;
      token = tokens[current];
    }

    node.current = current + 1;
    node.result.params = params;
    node.result.paramLen = paramIndex;

    return node;
}

function parserGenResult(type, value, current) {
  ret = new();
  ret.current = current;
  ret.result = new();
  ret.result.type = type;
  ret.result.value = value;

  return ret;
}

function traverser(ast) {
  return traverseNode(ast, null());
}

function traverseNode(node, parent) {
  if (node.type == "Number") {
    enterNumber(node, parent);
  }

  if (node.type == "String") {
    enterString(node, parent);
  }

  if (node.type == "Call") {
    expr = enterCall(node, parent);
    traverseArray(node.params, node.paramLen, node);
  }

  if (node.type == "Program") {
    traverseArray(node.body, node.bodyLen, node);
  }
}

function traverseArray(array, arrayLen, parent) {
  i = 0;
  while (i < arrayLen) {
    traverseNode(array[i], parent);
    i = i + 1;
  }
}

function transformer(ast) {
  newAst = new();
  newAst.type = "Program";
  newAst.body = new();

  ast._context = newAst.body;
  ast._contextLen = 0;
  traverser(ast);
  newAst.bodyLen = ast._contextLen;

  return newAst;
}

function enterNumber(node, parent) {
  tmp = new();
  tmp.type = "Number";
  tmp.value = node.value;
  ctxLen = parent._contextLen;
  parent._context[ctxLen] = tmp;
  parent._contextLen = ctxLen + 1;
}

function enterString(node, parent) {
  tmp = new();
  tmp.type = "String";
  tmp.value = node.value;
  ctxLen = parent._contextLen;
  parent._context[ctxLen] = tmp;
  parent._contextLen = ctxLen + 1;
}

function enterCall(node, parent) {
  expr = new();
  expr.type = "Call";
  expr.callee = new();
  expr.callee.type = "Ident";
  expr.callee.name = node.name;
  expr.args = new();
  expr.argLen = node.paramLen;
  node._context = expr.args;
  node._contextLen = 0;

  if (parent.type != "Call") {
    exprTmp = new();
    exprTmp.type = "Expression";
    exprTmp.expression = expr;
    expr = exprTmp;
  }

  ctxLen = parent._contextLen;
  parent._context[ctxLen] = expr;
  parent._contextLen = ctxLen + 1;
  return expr;
}

function codeGenerator(node) {
  if (node.type == "Program") {
    if (node.bodyLen == 0) {
      return "";
    }

    if (node.bodyLen == 1) {
      return codeGenerator(node.body[0]);
    }

    i = 0;
    acc = "";

    while (i < node.bodyLen - 1) {
      acc = acc + codeGenerator(node.body[i]) + "|";
      i = i + 1;
    }

    return acc + codeGenerator(node.body[i]) + "|";
  }

  if (node.type == "Expression") {
    return codeGenerator(node.expression) + ";";
  }

  if (node.type == "Call") {
    acc = codeGenerator(node.callee) + "(";

    len = node.argLen;
    if (len == 0) {
      return ")";
    }

    if (len == 1) {
      return acc + codeGenerator(node.args[0]) + ")";
    }

    i = 0;

    while (i < len - 1) {
      acc = acc + codeGenerator(node.args[i]) + ",";
      i = i + 1;
    }

    return acc + codeGenerator(node.args[i]) + ")";
  }

  if (node.type == "Ident") {
    return node.name;
  }

  if (node.type == "Number") {
    return node.value;
  }

  if (node.type == "String") {
    return "'" + node.value + "'";
  }
}

function compiler(input, len) {
  ast = parser(input, len);
  newAst = transformer(ast);
  output = codeGenerator(newAst);
  return output;
}

function main() {
  tokens = new();
  tokens[0] = startParen();
  tokens[1] = name("add");
  tokens[2] = number("2");
  tokens[3] = startParen();
  tokens[4] = name("subtract");
  tokens[5] = number(4);
  tokens[6] = number(2);
  tokens[7] = endParen();
  tokens[8] = endParen();
  println(compiler(tokens, 9));
}

function startParen() {
  ret = new();
  ret.type = "(";
  return ret;
}

function endParen() {
  ret = new();
  ret.type = ")";
  return ret;
}

function number(value) {
  ret = new();
  ret.type = "number";
  ret.value = value;
  return ret;
}

function name(value) {
  ret = new();
  ret.type = "name";
  ret.value = value;
  return ret;
}

function string(value) {
  ret = new();
  ret.type = "string";
  ret.value = value;
  return ret;
}

function null() {}
