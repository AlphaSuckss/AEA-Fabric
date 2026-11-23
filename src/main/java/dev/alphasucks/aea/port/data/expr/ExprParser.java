package dev.alphasucks.aea.port.data.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import net.minecraft.class_2960;

public final class ExprParser {
   private static final int MAX_DEPTH = 256;
   private final String src;
   private final Function<String, ExprVars.ResolvedVar> resolver;
   private int pos;
   private int depth;
   private boolean needsContainer;
   private boolean needsPeer;
   private ExprParser.T tokType;
   private boolean bracketFnIdent;
   private String tokText = "";
   private double tokNum;
   private int tokStart;
   private static final Map<String, ExprParser.BracketFnBuilder> BRACKET_FUNCTIONS = new HashMap();
   private static final Map<String, ExprParser.IdFnBuilder> ID_FUNCTIONS = new HashMap();
   private static final Map<String, ExprParser.FnBuilder> FUNCTIONS = new HashMap();
   private static final Set<String> GENERATOR_NAMES = Set.of(
      new String[]{
         "Uni",
         "Nor",
         "Int",
         "nat",
         "Nat",
         "Int1",
         "Int2",
         "Int3",
         "Int4",
         "Int5",
         "Int6",
         "Int7",
         "Int8",
         "Int9",
         "nat1",
         "nat2",
         "nat3",
         "nat4",
         "nat5",
         "nat6",
         "nat7",
         "nat8",
         "nat9",
         "Nat1",
         "Nat2",
         "Nat3",
         "Nat4",
         "Nat5",
         "Nat6",
         "Nat7",
         "Nat8",
         "Nat9"
      }
   );

   private ExprParser(String src, Function<String, ExprVars.ResolvedVar> resolver) {
      this.src = src;
      this.resolver = resolver;
   }

   public static ExprParser.Result parse(String src, Function<String, ExprVars.ResolvedVar> resolver) throws ExprParseException {
      ExprParser p = new ExprParser(src, resolver);
      p.advance();
      ExprNode root = p.parseExpression();
      p.expect(ExprParser.T.EOF, "end of expression");
      return new ExprParser.Result(root, p.needsContainer, p.needsPeer);
   }

   private ExprNode parseExpression() throws ExprParseException {
      if (++this.depth > 256) {
         throw new ExprParseException("expression too deeply nested", this.tokStart);
      }

      ExprNode node = this.parseOr();
      this.depth--;
      return node;
   }

   private ExprNode parseOr() throws ExprParseException {
      ExprNode left;
      for (left = this.parseAnd();
         this.tokType == ExprParser.T.OP && (this.tokText.equals("||") || this.tokText.equals("|"));
         left = fold(new ExprNodes.OrN(new ExprNode[]{left, this.parseAnd()}))
      ) {
         this.advance();
      }

      return left;
   }

   private ExprNode parseAnd() throws ExprParseException {
      ExprNode left;
      for (left = this.parseComparison();
         this.tokType == ExprParser.T.OP && (this.tokText.equals("&&") || this.tokText.equals("&"));
         left = fold(new ExprNodes.AndN(new ExprNode[]{left, this.parseComparison()}))
      ) {
         this.advance();
      }

      return left;
   }

   private ExprNode parseComparison() throws ExprParseException {
      ExprNode left = this.parseAdditive();

      while (this.tokType == ExprParser.T.OP) {
         ExprNodes.Op op = switch (this.tokText) {
            case "==", "=" -> ExprNodes.Op.EQ;
            case "!=", "<>" -> ExprNodes.Op.NE;
            case "<" -> ExprNodes.Op.LT;
            case "<=" -> ExprNodes.Op.LE;
            case ">" -> ExprNodes.Op.GT;
            case ">=" -> ExprNodes.Op.GE;
            default -> null;
         };
         if (op == null) {
            break;
         }

         this.advance();
         left = fold(new ExprNodes.Bin(op, left, this.parseAdditive()));
      }

      return left;
   }

   private ExprNode parseAdditive() throws ExprParseException {
      ExprNode left = this.parseTerm();

      while (this.tokType == ExprParser.T.OP && (this.tokText.equals("+") || this.tokText.equals("-"))) {
         ExprNodes.Op op = this.tokText.equals("+") ? ExprNodes.Op.ADD : ExprNodes.Op.SUB;
         this.advance();
         left = fold(new ExprNodes.Bin(op, left, this.parseTerm()));
      }

      return left;
   }

   private ExprNode parseTerm() throws ExprParseException {
      ExprNode left = this.parseUnary();

      while (true) {
         while (this.tokType != ExprParser.T.OP) {
            if (this.tokType != ExprParser.T.NUM
               && this.tokType != ExprParser.T.IDENT
               && this.tokType != ExprParser.T.GEN
               && this.tokType != ExprParser.T.LPAREN) {
               return left;
            }

            left = fold(new ExprNodes.Bin(ExprNodes.Op.MUL, left, this.parseUnary()));
         }
         ExprNodes.Op op = switch (this.tokText) {
            case "*", "×" -> ExprNodes.Op.MUL;
            case "/", "÷" -> ExprNodes.Op.DIV;
            case "\\" -> ExprNodes.Op.IDIV;
            case "#" -> ExprNodes.Op.MOD;
            default -> null;
         };
         if (op == null) {
            return left;
         }

         this.advance();
         left = fold(new ExprNodes.Bin(op, left, this.parseUnary()));
      }
   }

   private ExprNode parseUnary() throws ExprParseException {
      if (this.tokType == ExprParser.T.OP && this.tokText.equals("-")) {
         this.advance();
         return fold(new ExprNodes.Neg(this.parseUnary()));
      } else if (this.tokType == ExprParser.T.OP && this.tokText.equals("+")) {
         this.advance();
         return this.parseUnary();
      } else {
         return this.parsePower();
      }
   }

   private ExprNode parsePower() throws ExprParseException {
      ExprNode base = this.parsePostfix();
      if (this.tokType == ExprParser.T.OP && this.tokText.equals("^^")) {
         this.advance();
         return fold(new ExprNodes.Tetra(base, this.parseUnary()));
      } else if (this.tokType == ExprParser.T.OP && this.tokText.equals("^")) {
         this.advance();
         return fold(new ExprNodes.Bin(ExprNodes.Op.POW, base, this.parseUnary()));
      } else {
         return base;
      }
   }

   private ExprNode parsePostfix() throws ExprParseException {
      ExprNode node = this.parsePrimary();

      while (this.tokType == ExprParser.T.OP && (this.tokText.equals("!") || this.tokText.equals("%"))) {
         node = this.tokText.equals("!") ? fold(new ExprNodes.Fact(node)) : fold(new ExprNodes.Bin(ExprNodes.Op.MUL, node, new ExprNodes.Const(0.01)));
         this.advance();
      }

      return node;
   }

   private ExprNode parsePrimary() throws ExprParseException {
      switch (this.tokType) {
         case NUM:
            ExprNode n = new ExprNodes.Const(this.tokNum);
            this.advance();
            return n;
         case IDENT:
            String name = this.tokText;
            int at = this.tokStart;
            this.advance();
            if (this.tokType == ExprParser.T.LPAREN) {
               return this.functionCall(name, at);
            } else {
               if (this.tokType == ExprParser.T.LBRACKET) {
                  ExprParser.BracketFnBuilder bracket = (ExprParser.BracketFnBuilder)BRACKET_FUNCTIONS.get(name);
                  if (bracket != null) {
                     return this.bracketFunctionCall(name, at, bracket);
                  }

                  return this.indexedVariable(name, at);
               }

               return this.variable(name, at);
            }
         case GEN:
            ExprNode gen = this.generator(this.tokText, this.tokStart);
            this.advance();
            return gen;
         case OP:
         default:
            throw new ExprParseException("unexpected token '" + this.tokText + "'", this.tokStart);
         case LPAREN:
            this.advance();
            ExprNode inner = this.parseExpression();
            this.expect(ExprParser.T.RPAREN, "')'");
            this.advance();
            return inner;
      }
   }

   private ExprNode functionCall(String name, int at) throws ExprParseException {
      ExprParser.IdFnBuilder idBuilder = (ExprParser.IdFnBuilder)ID_FUNCTIONS.get(name);
      if (idBuilder != null) {
         return this.idFunctionCall(name, at, idBuilder);
      }

      this.advance();
      List<ExprNode> args = new ArrayList(4);
      if (this.tokType != ExprParser.T.RPAREN) {
         args.add(this.parseExpression());

         while (this.tokType == ExprParser.T.COMMA) {
            this.advance();
            args.add(this.parseExpression());
         }
      }

      this.expect(ExprParser.T.RPAREN, "')'");
      this.advance();
      ExprParser.FnBuilder builder = (ExprParser.FnBuilder)FUNCTIONS.get(name);
      if (builder == null) {
         throw new ExprParseException("unknown function '" + name + "'", at);
      } else {
         return fold(builder.build(args, name, at));
      }
   }

   private ExprNode idFunctionCall(String name, int at, ExprParser.IdFnBuilder builder) throws ExprParseException {
      this.advance();
      this.expect(ExprParser.T.IDENT, "an id like 'namespace:path'");
      class_2960 id = class_2960.method_12829(this.tokText);
      if (id == null) {
         throw new ExprParseException("'" + this.tokText + "' is not a valid id", this.tokStart);
      }

      this.advance();
      List<ExprNode> args = new ArrayList(2);

      while (this.tokType == ExprParser.T.COMMA) {
         this.advance();
         args.add(this.parseExpression());
      }

      this.expect(ExprParser.T.RPAREN, "')'");
      this.advance();
      ExprVars.ResolvedVar rv = builder.build(id, args, name, at);
      this.needsContainer = this.needsContainer | rv.needsContainer();
      this.needsPeer = this.needsPeer | rv.needsPeer();
      return new ExprNodes.Var(rv.accessor());
   }

   private ExprNode bracketFunctionCall(String name, int at, ExprParser.BracketFnBuilder builder) throws ExprParseException {
      int open = this.tokStart;
      int close = this.matchingBracket(open);
      List<String> args = splitArguments(this.src.substring(open + 1, close));
      this.pos = close + 1;
      this.advance();
      ExprVars.ResolvedVar rv = builder.build(args, name, at);
      this.needsContainer = this.needsContainer | rv.needsContainer();
      this.needsPeer = this.needsPeer | rv.needsPeer();
      return new ExprNodes.Var(rv.accessor());
   }

   private int matchingBracket(int open) throws ExprParseException {
      int level = 0;
      char quote = 0;

      for (int i = open; i < this.src.length(); i++) {
         char c = this.src.charAt(i);
         if (quote != 0) {
            if (c == '\\') {
               i++;
            } else if (c == quote) {
               quote = 0;
            }
         } else {
            switch (c) {
               case '"':
               case '\'':
                  quote = c;
                  break;
               case '(':
               case '[':
               case '{':
                  level++;
                  break;
               case ')':
               case ']':
               case '}':
                  if (--level == 0) {
                     if (c != ']') {
                        throw new ExprParseException("expected ']' but found '" + c + "'", i);
                     }

                     return i;
                  }
            }
         }
      }

      throw new ExprParseException("unterminated '['", open);
   }

   private static List<String> splitArguments(String inner) {
      List<String> args = new ArrayList(4);
      int level = 0;
      char quote = 0;
      int start = 0;

      for (int i = 0; i < inner.length(); i++) {
         char c = inner.charAt(i);
         if (quote != 0) {
            if (c == '\\') {
               i++;
            } else if (c == quote) {
               quote = 0;
            }
         } else {
            switch (c) {
               case '"':
               case '\'':
                  quote = c;
                  break;
               case '(':
               case '[':
               case '{':
                  level++;
                  break;
               case ')':
               case ']':
               case '}':
                  level--;
                  break;
               case ',':
                  if (level == 0) {
                     args.add(inner.substring(start, i).trim());
                     start = i + 1;
                  }
            }
         }
      }

      String tail = inner.substring(start).trim();
      if (!tail.isEmpty() || !args.isEmpty()) {
         args.add(tail);
      }

      return args;
   }

   public static ExprParser.Nested compileNested(String source, String name, int at) throws ExprParseException {
      try {
         ExprParser.Result result = parse(source, ExprVars::resolve);
         return new ExprParser.Nested(result.root(), result.needsContainer(), result.needsPeer());
      } catch (ExprParseException e) {
         throw new ExprParseException(name + " argument \"" + source + "\": " + e.getMessage(), at);
      }
   }

   public static void registerBracketFunction(String name, ExprParser.BracketFnBuilder builder) {
      BRACKET_FUNCTIONS.put(name, builder);
   }

   public static void registerIdFunction(String name, ExprParser.IdFnBuilder builder) {
      ID_FUNCTIONS.put(name, builder);
   }

   private ExprNode variable(String name, int at) throws ExprParseException {
      switch (name) {
         case "pi":
            return new ExprNodes.Const(Math.PI);
         case "e":
            return new ExprNodes.Const(Math.E);
         default:
            ExprVars.ResolvedVar rv = (ExprVars.ResolvedVar)this.resolver.apply(name);
            if (rv == null) {
               throw new ExprParseException("unknown variable '" + name + "'", at);
            } else {
               this.needsContainer = this.needsContainer | rv.needsContainer();
               this.needsPeer = this.needsPeer | rv.needsPeer();
               return new ExprNodes.Var(rv.accessor());
            }
      }
   }

   private ExprNode indexedVariable(String name, int at) throws ExprParseException {
      class_2960 id = name.indexOf(58) < 0 ? null : class_2960.method_12829(name);
      if (id == null) {
         throw new ExprParseException("'" + name + "' is not a resource id like 'namespace:path', so it cannot be indexed with [...]", at);
      }

      this.advance();
      ExprNode index = this.parseExpression();
      this.expect(ExprParser.T.RBRACKET, "']'");
      this.advance();
      ExprVars.ResolvedVar rv = ExprVars.resolveIndexed(id, index);
      this.needsContainer = this.needsContainer | rv.needsContainer();
      this.needsPeer = this.needsPeer | rv.needsPeer();
      return new ExprNodes.Var(rv.accessor());
   }

   private ExprNode generator(String name, int at) throws ExprParseException {
      ExprNode gen = switch (name) {
         case "Uni" -> new ExprNodes.RUni(new ExprNodes.Const(0.0), new ExprNodes.Const(1.0));
         case "Nor" -> new ExprNodes.RNor(new ExprNodes.Const(0.0), new ExprNodes.Const(1.0));
         case "Int" -> boundedGen(-2147483648L, 2147483647L);
         case "nat" -> boundedGen(0L, 2147483647L);
         case "Nat" -> boundedGen(1L, 2147483647L);
         default -> null;
      };
      if (gen != null) {
         return gen;
      } else {
         long scale = powerOfTenSuffix(name, "Int");
         if (scale > 0L) {
            return boundedGen(-scale, scale);
         } else {
            scale = powerOfTenSuffix(name, "nat");
            if (scale > 0L) {
               return boundedGen(0L, scale);
            } else {
               scale = powerOfTenSuffix(name, "Nat");
               if (scale > 0L) {
                  return boundedGen(1L, scale);
               } else {
                  throw new ExprParseException("unknown random generator '[" + name + "]'", at);
               }
            }
         }
      }
   }

   private static long powerOfTenSuffix(String name, String prefix) {
      if (name.startsWith(prefix) && name.length() == prefix.length() + 1) {
         char digit = name.charAt(prefix.length());
         if (digit >= '1' && digit <= '9') {
            long scale = 1L;

            for (int i = 0; i < digit - '0'; i++) {
               scale *= 10L;
            }

            return scale;
         } else {
            return -1L;
         }
      } else {
         return -1L;
      }
   }

   private static ExprNode boundedGen(long lo, long hi) {
      return new ExprNodes.RUnid(new ExprNodes.Const(lo), new ExprNodes.Const(hi));
   }

   private static ExprNode fold(ExprNode node) {
      return node.isConstant() && !(node instanceof ExprNodes.Const) ? new ExprNodes.Const(node.eval(null, null, null, 0.0)) : node;
   }

   private void expect(ExprParser.T type, String what) throws ExprParseException {
      if (this.tokType != type) {
         throw new ExprParseException("expected " + what + " but found '" + this.tokText + "'", this.tokStart);
      }
   }

   private static void fn1(DoubleUnaryOperator fn, String... names) {
      ExprParser.FnBuilder b = (args, name, at) -> {
         if (args.size() != 1) {
            throw new ExprParseException(name + " expects 1 argument", at);
         } else {
            return new ExprNodes.Fn1(fn, (ExprNode)args.get(0));
         }
      };

      for (String n : names) {
         FUNCTIONS.put(n, b);
      }
   }

   private static void fn2(DoubleBinaryOperator fn, String... names) {
      ExprParser.FnBuilder b = (args, name, at) -> {
         if (args.size() != 2) {
            throw new ExprParseException(name + " expects 2 arguments", at);
         } else {
            return new ExprNodes.Fn2(fn, (ExprNode)args.get(0), (ExprNode)args.get(1));
         }
      };

      for (String n : names) {
         FUNCTIONS.put(n, b);
      }
   }

   private static void fn3(ExprNodes.DoubleTernaryOperator fn, String... names) {
      ExprParser.FnBuilder b = (args, name, at) -> {
         if (args.size() != 3) {
            throw new ExprParseException(name + " expects 3 arguments", at);
         } else {
            return new ExprNodes.Fn3(fn, (ExprNode)args.get(0), (ExprNode)args.get(1), (ExprNode)args.get(2));
         }
      };

      for (String n : names) {
         FUNCTIONS.put(n, b);
      }
   }

   private static ExprNode[] atLeast(int n, List<ExprNode> args, String name, int at) throws ExprParseException {
      if (args.size() < n) {
         throw new ExprParseException(name + " expects at least " + n + " argument(s)", at);
      } else {
         return (ExprNode[])args.toArray(new ExprNode[0]);
      }
   }

   private static double nthRoot(double n, double x) {
      return x < 0.0 && n == Math.rint(n) && ((long)n & 1L) == 1L ? -Math.pow(-x, 1.0 / n) : Math.pow(x, 1.0 / n);
   }

   private void advance() throws ExprParseException {
      boolean afterBracketFn = this.bracketFnIdent;
      this.bracketFnIdent = false;

      while (this.pos < this.src.length() && Character.isWhitespace(this.src.charAt(this.pos))) {
         this.pos++;
      }

      this.tokStart = this.pos;
      if (this.pos >= this.src.length()) {
         this.tokType = ExprParser.T.EOF;
         this.tokText = "<end>";
      } else {
         char c = this.src.charAt(this.pos);
         if (!Character.isDigit(c) && (c != '.' || this.pos + 1 >= this.src.length() || !Character.isDigit(this.src.charAt(this.pos + 1)))) {
            if (Character.isLetter(c) || c == '_') {
               this.scanIdent();
            } else if (c != '[') {
               if (c == ']') {
                  this.tokType = ExprParser.T.RBRACKET;
                  this.tokText = "]";
                  this.pos++;
               } else {
                  switch (c) {
                     case '(':
                        this.tokType = ExprParser.T.LPAREN;
                        this.tokText = "(";
                        this.pos++;
                        break;
                     case ')':
                        this.tokType = ExprParser.T.RPAREN;
                        this.tokText = ")";
                        this.pos++;
                        break;
                     case '*':
                     case '+':
                     default:
                        this.scanOperator(c);
                        break;
                     case ',':
                        this.tokType = ExprParser.T.COMMA;
                        this.tokText = ",";
                        this.pos++;
                  }
               }
            } else {
               if (!afterBracketFn && this.looksLikeGenerator()) {
                  this.scanGenerator();
               } else {
                  this.tokType = ExprParser.T.LBRACKET;
                  this.tokText = "[";
                  this.pos++;
               }
            }
         } else {
            this.scanNumber();
         }
      }
   }

   private void scanOperator(char c) throws ExprParseException {
      String two = this.pos + 1 < this.src.length() ? this.src.substring(this.pos, this.pos + 2) : "";
      switch (two) {
         case "^^":
         case "==":
         case "!=":
         case "<>":
         case "<=":
         case ">=":
         case "&&":
         case "||":
            this.tokType = ExprParser.T.OP;
            this.tokText = two;
            this.pos += 2;
            return;
         default:
            switch (c) {
               case '!':
               case '#':
               case '%':
               case '&':
               case '*':
               case '+':
               case '-':
               case '/':
               case '<':
               case '=':
               case '>':
               case '\\':
               case '^':
               case '|':
               case '×':
               case '÷':
                  this.tokType = ExprParser.T.OP;
                  this.tokText = String.valueOf(c);
                  this.pos++;
                  return;
               default:
                  throw new ExprParseException("unexpected character '" + c + "'", this.pos);
            }
      }
   }

   private void scanNumber() throws ExprParseException {
      int start = this.pos;

      while (this.pos < this.src.length() && Character.isDigit(this.src.charAt(this.pos))) {
         this.pos++;
      }

      boolean intOnly = this.pos > start;
      if (intOnly && this.pos + 1 < this.src.length() && this.src.charAt(this.pos) == '_' && Character.isDigit(this.src.charAt(this.pos + 1))) {
         double first = Double.parseDouble(this.src.substring(start, this.pos));
         this.pos++;
         int s2 = this.pos;

         while (this.pos < this.src.length() && Character.isDigit(this.src.charAt(this.pos))) {
            this.pos++;
         }

         double second = Double.parseDouble(this.src.substring(s2, this.pos));
         if (this.pos + 1 < this.src.length() && this.src.charAt(this.pos) == '_' && Character.isDigit(this.src.charAt(this.pos + 1))) {
            this.pos++;
            int s3 = this.pos;

            while (this.pos < this.src.length() && Character.isDigit(this.src.charAt(this.pos))) {
               this.pos++;
            }

            double third = Double.parseDouble(this.src.substring(s3, this.pos));
            this.tokNum = first + second / third;
         } else {
            this.tokNum = first / second;
         }

         this.tokType = ExprParser.T.NUM;
         this.tokText = this.src.substring(start, this.pos);
      } else {
         if (this.pos < this.src.length() && this.src.charAt(this.pos) == '.') {
            this.pos++;

            while (this.pos < this.src.length() && Character.isDigit(this.src.charAt(this.pos))) {
               this.pos++;
            }
         }

         if (this.pos < this.src.length() && (this.src.charAt(this.pos) == 'e' || this.src.charAt(this.pos) == 'E')) {
            int mark = this.pos++;
            if (this.pos < this.src.length() && (this.src.charAt(this.pos) == '+' || this.src.charAt(this.pos) == '-')) {
               this.pos++;
            }

            if (this.pos < this.src.length() && Character.isDigit(this.src.charAt(this.pos))) {
               while (this.pos < this.src.length() && Character.isDigit(this.src.charAt(this.pos))) {
                  this.pos++;
               }
            } else {
               this.pos = mark;
            }
         }

         this.tokText = this.src.substring(start, this.pos);

         try {
            this.tokNum = Double.parseDouble(this.tokText);
         } catch (NumberFormatException e) {
            throw new ExprParseException("invalid number '" + this.tokText + "'", start);
         }

         this.tokType = ExprParser.T.NUM;
      }
   }

   private void scanIdent() {
      int start = this.pos;

      while (this.pos < this.src.length() && (Character.isLetterOrDigit(this.src.charAt(this.pos)) || this.src.charAt(this.pos) == '_')) {
         this.pos++;
      }

      if (this.pos + 1 < this.src.length() && this.src.charAt(this.pos) == ':' && isResourcePathChar(this.src.charAt(this.pos + 1))) {
         this.pos++;

         while (this.pos < this.src.length() && isResourcePathChar(this.src.charAt(this.pos))) {
            this.pos++;
         }
      }

      this.tokType = ExprParser.T.IDENT;
      this.tokText = this.src.substring(start, this.pos);
      this.bracketFnIdent = BRACKET_FUNCTIONS.containsKey(this.tokText);
   }

   private static boolean isResourcePathChar(char c) {
      return c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '.' || c == '/' || c == '-';
   }

   private boolean looksLikeGenerator() {
      int end = this.src.indexOf(93, this.pos + 1);
      return end >= 0 && end - this.pos - 1 <= 4 ? GENERATOR_NAMES.contains(this.src.substring(this.pos + 1, end)) : false;
   }

   private void scanGenerator() throws ExprParseException {
      int start = this.pos++;
      int nameStart = this.pos;

      while (this.pos < this.src.length() && this.src.charAt(this.pos) != ']') {
         if (this.pos - nameStart > 16) {
            throw new ExprParseException("unterminated random generator", start);
         }

         this.pos++;
      }

      if (this.pos >= this.src.length()) {
         throw new ExprParseException("unterminated random generator", start);
      }

      this.tokText = this.src.substring(nameStart, this.pos);
      this.pos++;
      if (this.tokText.isEmpty()) {
         throw new ExprParseException("empty random generator '[]'", start);
      }

      this.tokType = ExprParser.T.GEN;
   }

   static {
      fn1(Math::sin, "sin");
      fn1(Math::cos, "cos");
      fn1(Math::tan, "tan", "tg");
      fn1(v -> 1.0 / Math.cos(v), "sec");
      fn1(v -> 1.0 / Math.sin(v), "csc", "cosec");
      fn1(v -> 1.0 / Math.tan(v), "cot", "ctg", "ctan");
      fn1(Math::asin, "asin", "arsin", "arcsin");
      fn1(Math::acos, "acos", "arcos", "arccos");
      fn1(Math::atan, "atan", "atg", "arctg", "arctan");
      fn1(Math::sinh, "sinh");
      fn1(Math::cosh, "cosh");
      fn1(Math::tanh, "tanh", "tgh", "th");
      fn1(v -> 1.0 / Math.tanh(v), "coth", "ctgh", "cth");
      fn1(v -> 1.0 / Math.cosh(v), "sech");
      fn1(v -> 1.0 / Math.sinh(v), "csch", "cosech");
      fn1(v -> Math.log(v + Math.sqrt(v * v + 1.0)), "asinh", "arsinh", "arcsinh");
      fn1(v -> Math.log(v + Math.sqrt(v * v - 1.0)), "acosh", "arcosh", "arccosh");
      fn1(v -> 0.5 * Math.log((1.0 + v) / (1.0 - v)), "atanh", "arctanh", "artanh");
      fn1(Math::log, "ln");
      fn1(v -> Math.log(v) / Math.log(2.0), "log2");
      fn1(Math::log10, "lg", "log10");
      fn1(Math::exp, "exp");
      fn1(Math::sqrt, "sqrt");
      fn1(Math::cbrt, "cbrt");
      fn1(Math::abs, "abs");
      fn1(Math::floor, "floor");
      fn1(Math::ceil, "ceil");
      fn1(v -> Math.round(v), "round");
      fn1(Math::signum, "sign", "sgn");
      fn1(Math::toDegrees, "deg");
      fn1(Math::toRadians, "rad");
      fn1(v -> v == 0.0 ? 1.0 : 0.0, "not");
      fn2((a, b) -> a % b, "mod");
      fn2(Math::pow, "pow");
      fn2(Math::atan2, "atan2");
      fn2(Math::hypot, "hypot");
      fn2((base, x) -> Math.log(x) / Math.log(base), "log");
      fn2(ExprParser::nthRoot, "root");
      fn3((v, lo, hi) -> Math.min(hi, Math.max(lo, v)), "clamp");
      fn3((a, b, t) -> a + (b - a) * t, "lerp");
      FUNCTIONS.put("if", (ExprParser.FnBuilder)(args, name, at) -> {
         if (args.size() != 3) {
            throw new ExprParseException("if expects 3 arguments (condition, then, else)", at);
         } else {
            return new ExprNodes.If((ExprNode)args.get(0), (ExprNode)args.get(1), (ExprNode)args.get(2));
         }
      });
      FUNCTIONS.put("min", (ExprParser.FnBuilder)(args, name, at) -> new ExprNodes.MinN(atLeast(1, args, name, at)));
      FUNCTIONS.put("max", (ExprParser.FnBuilder)(args, name, at) -> new ExprNodes.MaxN(atLeast(1, args, name, at)));
      FUNCTIONS.put("avg", (ExprParser.FnBuilder)(args, name, at) -> new ExprNodes.AvgN(atLeast(1, args, name, at)));
      FUNCTIONS.put("mean", (ExprParser.FnBuilder)FUNCTIONS.get("avg"));
      FUNCTIONS.put("and", (ExprParser.FnBuilder)(args, name, at) -> new ExprNodes.AndN(atLeast(2, args, name, at)));
      FUNCTIONS.put("or", (ExprParser.FnBuilder)(args, name, at) -> new ExprNodes.OrN(atLeast(2, args, name, at)));
      FUNCTIONS.put("rList", (ExprParser.FnBuilder)(args, name, at) -> new ExprNodes.RList(atLeast(1, args, name, at)));
      FUNCTIONS.put("rUni", (ExprParser.FnBuilder)(args, name, at) -> {
         if (args.size() != 2) {
            throw new ExprParseException("rUni expects 2 arguments", at);
         } else {
            return new ExprNodes.RUni((ExprNode)args.get(0), (ExprNode)args.get(1));
         }
      });
      FUNCTIONS.put("rUnid", (ExprParser.FnBuilder)(args, name, at) -> {
         if (args.size() != 2) {
            throw new ExprParseException("rUnid expects 2 arguments", at);
         } else {
            return new ExprNodes.RUnid((ExprNode)args.get(0), (ExprNode)args.get(1));
         }
      });
      FUNCTIONS.put("rNor", (ExprParser.FnBuilder)(args, name, at) -> {
         if (args.size() != 2) {
            throw new ExprParseException("rNor expects 2 arguments", at);
         } else {
            return new ExprNodes.RNor((ExprNode)args.get(0), (ExprNode)args.get(1));
         }
      });
      ExprVars.load();
   }

   @FunctionalInterface
   public interface BracketFnBuilder {
      ExprVars.ResolvedVar build(List<String> var1, String var2, int var3) throws ExprParseException;
   }

   @FunctionalInterface
   private interface FnBuilder {
      ExprNode build(List<ExprNode> var1, String var2, int var3) throws ExprParseException;
   }

   @FunctionalInterface
   public interface IdFnBuilder {
      ExprVars.ResolvedVar build(class_2960 var1, List<ExprNode> var2, String var3, int var4) throws ExprParseException;
   }

   public record Nested(ExprNode node, boolean needsContainer, boolean needsPeer) {
   }

   public record Result(ExprNode root, boolean needsContainer, boolean needsPeer) {
   }

   private enum T {
      NUM,
      IDENT,
      GEN,
      OP,
      LPAREN,
      RPAREN,
      LBRACKET,
      RBRACKET,
      COMMA,
      EOF;
   }
}
