package dev.alphasucks.aea.port.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;

public final class JsonTextParser {
   private static final int EOF = -1;
   private static final JsonPrimitive TRUE = new JsonPrimitive(true);
   private static final JsonPrimitive FALSE = new JsonPrimitive(false);
   private static final JsonPrimitive NAN = new JsonPrimitive(Double.NaN);
   private static final JsonPrimitive POSITIVE_INFINITY = new JsonPrimitive(Double.POSITIVE_INFINITY);
   private static final JsonPrimitive NEGATIVE_INFINITY = new JsonPrimitive(Double.NEGATIVE_INFINITY);
   private final char[] text;
   private final int end;
   private final JsonFormat format;
   private int pos;
   private JsonElement[] containers = new JsonElement[16];
   private String[] names = new String[16];
   private int depth;

   private JsonTextParser(char[] text, int start, int end, JsonFormat format) {
      this.text = text;
      this.pos = start;
      this.end = end;
      this.format = format;
   }

   public static JsonTextParser.Parsed parse(String text, JsonFormat format) {
      char[] chars = text.toCharArray();
      return new JsonTextParser(chars, 0, chars.length, format).document();
   }

   public static JsonTextParser.Parsed read(InputStream in, JsonFormat format) throws IOException {
      return read(in.readAllBytes(), format);
   }

   public static JsonTextParser.Parsed read(byte[] utf8, JsonFormat format) {
      CharBuffer chars = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(utf8));
      return new JsonTextParser(chars.array(), chars.arrayOffset() + chars.position(), chars.arrayOffset() + chars.limit(), format).document();
   }

   private JsonTextParser.Parsed document() {
      if (this.pos < this.end && this.text[this.pos] == '\ufeff') {
         this.pos++;
      }

      int first = this.next();
      if (first == -1) {
         throw this.error("The file has no value in it");
      }

      JsonElement value = this.value(first);
      int trailing = this.trailingContent();
      return new JsonTextParser.Parsed(value, trailing < 0 ? 0 : this.lineOf(trailing));
   }

   private JsonElement value(int first) {
      int c = first;

      label57:
      while (true) {
         JsonElement done;
         if (c == 123) {
            int n = this.next();
            if (n != 125) {
               this.push(new JsonObject());
               this.names[this.depth - 1] = this.name(n);
               this.colon();
               c = this.next();
               continue;
            }

            done = new JsonObject();
         } else if (c == 91) {
            int n = this.next();
            if (n != 93) {
               this.push(new JsonArray());
               c = n;
               continue;
            }

            done = new JsonArray();
         } else {
            done = this.scalar(c);
         }

         while (this.depth != 0) {
            JsonElement parent = this.containers[this.depth - 1];
            if (parent instanceof JsonArray array) {
               array.add(done);
               int n = this.next();
               if (n == 44) {
                  n = this.next();
                  if (n != 93) {
                     c = n;
                     continue label57;
                  }

                  this.trailingComma();
                  done = this.pop();
               } else {
                  if (n != 93) {
                     throw this.unexpected(n, "Expected ',' or ']' after an array element");
                  }

                  done = this.pop();
               }
            } else {
               ((JsonObject)parent).add(this.names[this.depth - 1], done);
               int n = this.next();
               if (n == 44) {
                  n = this.next();
                  if (n != 125) {
                     this.names[this.depth - 1] = this.name(n);
                     this.colon();
                     c = this.next();
                     continue label57;
                  }

                  this.trailingComma();
                  done = this.pop();
               } else {
                  if (n != 125) {
                     throw this.unexpected(n, "Expected ',' or '}' after a value");
                  }

                  done = this.pop();
               }
            }
         }

         return done;
      }
   }

   private String name(int c) {
      if (c == 34) {
         return this.string('"');
      }

      if (c == 39) {
         this.requireJson5("single-quoted names");
         return this.string('\'');
      }

      if (c == -1) {
         throw this.error("The file ends inside an object");
      }

      if (!this.startsUnquotedName((char)c)) {
         this.pos--;
         throw this.error("Expected a name, found " + describe(c));
      }

      if (this.format != JsonFormat.JSON5) {
         this.pos--;
         throw this.error("Use a .json5 file for names without quotes");
      }

      int start = this.pos - 1;
      int i = this.pos;

      while (i < this.end && !this.endsUnquotedName(i)) {
         i++;
      }

      this.pos = i;
      return new String(this.text, start, i - start);
   }

   private void colon() {
      int c = this.next();
      if (c != 58) {
         throw this.unexpected(c, "Expected ':' after the name");
      }
   }

   private JsonElement scalar(int c) {
      switch (c) {
         case -1:
            throw this.error("Expected a value, but the file ends here");
         case 34:
            return new JsonPrimitive(this.string('"'));
         case 39:
            this.requireJson5("single-quoted strings");
            return new JsonPrimitive(this.string('\''));
         case 44:
         case 58:
         case 93:
         case 125:
            this.pos--;
            throw this.error("Expected a value, found " + describe(c));
         default:
            this.pos--;
            return this.literal();
      }
   }

   private JsonElement literal() {
      char c = this.text[this.pos];
      if ((c == 't' || c == 'T') && this.keyword("true")) {
         return TRUE;
      } else if ((c == 'f' || c == 'F') && this.keyword("false")) {
         return FALSE;
      } else {
         return (JsonElement)((c == 'n' || c == 'N') && this.keyword("null") ? JsonNull.INSTANCE : this.number());
      }
   }

   private boolean keyword(String word) {
      int length = word.length();
      if (this.pos + length > this.end) {
         return false;
      }

      for (int i = 1; i < length; i++) {
         char c = this.text[this.pos + i];
         char lower = word.charAt(i);
         if (c != lower && c != lower - ' ') {
            return false;
         }
      }

      int after = this.pos + length;
      if (after < this.end && !this.terminates(after)) {
         if (Character.isLetterOrDigit(this.text[after])) {
            return false;
         }

         this.terminated(after);
      }

      this.pos = after;
      return true;
   }

   private JsonElement number() {
      int start = this.pos;
      int i = this.pos;
      char c = this.text[i];
      boolean negative = false;
      if (c == '+' || c == '-') {
         if (c == '+') {
            this.requireJson5("a leading '+'");
         }

         negative = c == '-';
         if (++i >= this.end) {
            this.pos = i;
            throw this.error("Expected a number after the sign");
         }

         c = this.text[i];
      }

      if (c != 'I' && c != 'N') {
         if (c != '0' || i + 1 >= this.end || this.text[i + 1] != 'x' && this.text[i + 1] != 'X') {
            int integerStart = i;

            while (i < this.end && isDigit(this.text[i])) {
               i++;
            }

            int integerDigits = i - integerStart;
            if (integerDigits != 0 || i < this.end && this.text[i] == '.') {
               if (integerDigits > 1 && this.text[integerStart] == '0') {
                  this.pos = integerStart;
                  throw this.error("Numbers can't have leading zeros");
               }

               int fractionDigits = -1;
               boolean exponent = false;
               if (i < this.end && this.text[i] == '.') {
                  int fractionStart = ++i;

                  while (i < this.end && isDigit(this.text[i])) {
                     i++;
                  }

                  fractionDigits = i - fractionStart;
                  if (integerDigits == 0 && fractionDigits == 0) {
                     this.pos = i;
                     throw this.error("Expected digits around the decimal point");
                  }

                  if (integerDigits == 0) {
                     this.requireJson5("a number starting with '.'");
                  }

                  if (fractionDigits == 0) {
                     this.requireJson5("a number ending with '.'");
                  }
               }

               if (i < this.end && (this.text[i] == 'e' || this.text[i] == 'E')) {
                  exponent = true;
                  i++;
                  if (i < this.end && (this.text[i] == '+' || this.text[i] == '-')) {
                     i++;
                  }

                  int exponentStart = i;

                  while (i < this.end && isDigit(this.text[i])) {
                     i++;
                  }

                  if (i == exponentStart) {
                     this.pos = i;
                     throw this.error("Expected digits in the exponent");
                  }
               }

               this.terminated(i);
               this.pos = i;
               boolean integral = fractionDigits <= 0 && !exponent;
               return new JsonPrimitive(new JsonTextParser.LazyNumber(this.numberText(start, i, integerStart, integerDigits, fractionDigits), integral));
            } else {
               this.pos = i;
               if (i > start) {
                  throw this.error("Expected a number after the sign");
               } else {
                  throw this.unquoted(c);
               }
            }
         } else {
            this.requireJson5("hexadecimal numbers");
            int digits = i + 2;
            int j = digits;

            while (j < this.end && Character.digit(this.text[j], 16) >= 0) {
               j++;
            }

            if (j == digits) {
               this.pos = j;
               throw this.error("Expected hexadecimal digits after '0x'");
            } else {
               this.terminated(j);
               this.pos = j;
               String hex = new String(this.text, digits, j - digits);
               String decimal = j - digits <= 15
                  ? Long.toString(negative ? -Long.parseLong(hex, 16) : Long.parseLong(hex, 16))
                  : (negative ? new BigInteger(hex, 16).negate() : new BigInteger(hex, 16)).toString();
               return new JsonPrimitive(new JsonTextParser.LazyNumber(decimal, true));
            }
         }
      } else {
         String word = c == 'I' ? "Infinity" : "NaN";
         if (!this.text(i, word)) {
            this.pos = i;
            throw this.unquoted(c);
         } else {
            this.requireJson5(word);
            this.terminated(i + word.length());
            this.pos = i + word.length();
            if (c == 'N') {
               return NAN;
            } else {
               return negative ? NEGATIVE_INFINITY : POSITIVE_INFINITY;
            }
         }
      }
   }

   private String numberText(int start, int stop, int integerStart, int integerDigits, int fractionDigits) {
      int from = this.text[start] == '+' ? start + 1 : start;
      if (integerDigits > 0 && fractionDigits != 0) {
         return new String(this.text, from, stop - from);
      }

      StringBuilder out = new StringBuilder(stop - from + 1);
      out.append(this.text, from, integerStart - from);
      if (integerDigits == 0) {
         out.append('0');
      }

      int point = integerStart + integerDigits;
      out.append(this.text, integerStart, integerDigits);
      if (fractionDigits == 0) {
         out.append(this.text, point + 1, stop - point - 1);
      } else {
         out.append(this.text, point, stop - point);
      }

      return out.toString();
   }

   private boolean text(int at, String word) {
      if (at + word.length() > this.end) {
         return false;
      }

      for (int i = 0; i < word.length(); i++) {
         if (this.text[at + i] != word.charAt(i)) {
            return false;
         }
      }

      return true;
   }

   private void terminated(int at) {
      if (at < this.end && !this.terminates(at)) {
         this.pos = at;
         char c = this.text[at];
         if (c != '/' || at + 1 >= this.end || this.text[at + 1] != '/' && this.text[at + 1] != '*') {
            throw this.error("Unexpected " + describe(c) + " after the value");
         } else {
            throw this.error("Use a .jsonc or .json5 file for comments");
         }
      }
   }

   private boolean terminates(int at) {
      char c = this.text[at];
      switch (c) {
         case '\t':
         case '\n':
         case '\f':
         case '\r':
         case ' ':
         case ',':
         case ':':
         case '[':
         case ']':
         case '{':
         case '}':
            return true;
         case '\u000b':
            return this.format == JsonFormat.JSON5;
         case '/':
            return this.format.allowsComments() && at + 1 < this.end && (this.text[at + 1] == '/' || this.text[at + 1] == '*');
         default:
            return this.format == JsonFormat.JSON5 && c > 127 && isExtendedSpace(c);
      }
   }

   private String string(char quote) {
      int start = this.pos;

      for (int i = start; i < this.end; i++) {
         char c = this.text[i];
         if (c == quote) {
            this.pos = i + 1;
            return new String(this.text, start, i - start);
         }

         if (c == '\\') {
            return this.escaped(quote, start, i);
         }
      }

      this.pos = start - 1;
      throw this.error("A string is never closed");
   }

   private String escaped(char quote, int start, int from) {
      StringBuilder out = new StringBuilder(from - start + 16);
      out.append(this.text, start, from - start);
      int i = from;

      while (i < this.end) {
         char c = this.text[i++];
         if (c == quote) {
            this.pos = i;
            return out.toString();
         }

         if (c != '\\') {
            out.append(c);
         } else {
            if (i < this.end) {
               char escape = this.text[i++];
               switch (escape) {
                  case '\n':
                  case '\r':
                  case '"':
                  case '\'':
                  case '/':
                  case '\\':
                     out.append(escape);
                     continue;
                  case 'b':
                     out.append('\b');
                     continue;
                  case 'f':
                     out.append('\f');
                     continue;
                  case 'n':
                     out.append('\n');
                     continue;
                  case 'r':
                     out.append('\r');
                     continue;
                  case 't':
                     out.append('\t');
                     continue;
                  case 'u':
                     if (i + 4 > this.end) {
                        this.pos = i - 2;
                        throw this.error("A \\u escape needs four hexadecimal digits");
                     }

                     int value = 0;

                     for (int k = 0; k < 4; k++) {
                        int digit = Character.digit(this.text[i + k], 16);
                        if (digit < 0 || this.text[i + k] > 127) {
                           this.pos = i - 2;
                           throw this.error("A \\u escape needs four hexadecimal digits");
                        }

                        value = value << 4 | digit;
                     }

                     out.append((char)value);
                     i += 4;
                     continue;
                  default:
                     this.pos = i - 2;
                     throw this.error("Unknown escape sequence \\" + escape);
               }
            }
            break;
         }
      }

      this.pos = start - 1;
      throw this.error("A string is never closed");
   }

   private int next() {
      while (this.pos < this.end) {
         char c = this.text[this.pos++];
         if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
            if (c == '/') {
               if (this.pos >= this.end) {
                  return c;
               }

               char after = this.text[this.pos];
               if (after != '/' && after != '*') {
                  return c;
               }

               if (!this.format.allowsComments()) {
                  this.pos--;
                  throw this.error("Use a .jsonc or .json5 file for comments");
               }

               this.skipComment(after);
            } else {
               if (c == '\f' || c == 11 || c > 127 && isExtendedSpace(c)) {
                  if (this.format == JsonFormat.JSON5) {
                     continue;
                  }

                  this.pos--;
                  throw this.error("Use a .json5 file for U+" + hex(c) + " as whitespace");
               }

               return c;
            }
         }
      }

      return -1;
   }

   private void skipComment(char kind) {
      int open = this.pos - 1;
      this.pos++;
      if (kind != '/') {
         while (this.pos + 1 < this.end) {
            if (this.text[this.pos] == '*' && this.text[this.pos + 1] == '/') {
               this.pos += 2;
               return;
            }

            this.pos++;
         }

         this.pos = open;
         throw this.error("A /* comment is never closed");
      } else {
         while (this.pos < this.end) {
            char c = this.text[this.pos++];
            if (c == '\n' || c == '\r') {
               return;
            }
         }
      }
   }

   private int trailingContent() {
      int i = this.pos;

      while (true) {
         if (i >= this.end) {
            return -1;
         }

         char c = this.text[i];
         if (c != ' ' && c != '\n' && c != '\r' && c != '\t' && (this.format != JsonFormat.JSON5 || c != '\f' && c != 11 && (c <= 127 || !isExtendedSpace(c)))) {
            if (c != '/' || i + 1 >= this.end || !this.format.allowsComments()) {
               break;
            }

            if (this.text[i + 1] == '/') {
               i += 2;

               while (i < this.end && this.text[i] != '\n' && this.text[i] != '\r') {
                  i++;
               }
            } else {
               if (this.text[i + 1] != '*') {
                  break;
               }

               int close = this.indexOfCommentEnd(i + 2);
               if (close < 0) {
                  return i;
               }

               i = close + 2;
            }
         } else {
            i++;
         }
      }

      return i;
   }

   private int indexOfCommentEnd(int from) {
      for (int i = from; i + 1 < this.end; i++) {
         if (this.text[i] == '*' && this.text[i + 1] == '/') {
            return i;
         }
      }

      return -1;
   }

   private void trailingComma() {
      if (!this.format.allowsComments()) {
         this.pos--;
         throw this.error("Use a .jsonc or .json5 file for trailing commas");
      }
   }

   private void requireJson5(String what) {
      if (this.format != JsonFormat.JSON5) {
         throw this.error("Use a .json5 file for " + what);
      }
   }

   private void push(JsonElement container) {
      if (this.depth == this.containers.length) {
         this.containers = (JsonElement[])Arrays.copyOf(this.containers, this.depth * 2);
         this.names = (String[])Arrays.copyOf(this.names, this.depth * 2);
      }

      this.containers[this.depth] = container;
      this.names[this.depth] = null;
      this.depth++;
   }

   private JsonElement pop() {
      this.depth--;
      JsonElement container = this.containers[this.depth];
      this.containers[this.depth] = null;
      this.names[this.depth] = null;
      return container;
   }

   private boolean startsUnquotedName(char c) {
      switch (c) {
         case '"':
         case '#':
         case '\'':
         case ',':
         case '/':
         case ':':
         case ';':
         case '=':
         case '[':
         case '\\':
         case ']':
         case '{':
         case '}':
            return false;
         default:
            return c > ' ';
      }
   }

   private boolean endsUnquotedName(int at) {
      char c = this.text[at];
      switch (c) {
         case '\t':
         case '\n':
         case '\u000b':
         case '\f':
         case '\r':
         case ' ':
         case ',':
         case ':':
         case '[':
         case ']':
         case '{':
         case '}':
            return true;
         case '/':
            return at + 1 < this.end && (this.text[at + 1] == '/' || this.text[at + 1] == '*');
         default:
            return c > 127 && isExtendedSpace(c);
      }
   }

   private static boolean isExtendedSpace(char c) {
      return c == 160 || c == 8232 || c == 8233 || c == '\ufeff' || Character.getType(c) == 12;
   }

   private static boolean isDigit(char c) {
      return c >= '0' && c <= '9';
   }

   private JsonParseException unquoted(char c) {
      return Character.isLetter(c) ? this.error("Text values need quotes around them") : this.error("Unexpected " + describe(c));
   }

   private JsonParseException unexpected(int c, String message) {
      if (c == -1) {
         return this.error("The file ends before every object and array is closed");
      }

      this.pos--;
      return this.error(message + ", found " + describe(c));
   }

   private static String describe(int c) {
      if (c == -1) {
         return "the end of the file";
      } else {
         return c < 32 ? "U+" + hex((char)c) : "'" + (char)c + "'";
      }
   }

   private static String hex(char c) {
      String digits = Integer.toHexString(c).toUpperCase(Locale.ROOT);
      return "0000".substring(digits.length()) + digits;
   }

   private int lineOf(int offset) {
      int line = 1;

      for (int i = 0; i < offset && i < this.end; i++) {
         if (this.text[i] == '\n') {
            line++;
         }
      }

      return line;
   }

   private JsonParseException error(String message) {
      int at = Math.max(0, Math.min(this.pos, this.end));
      int line = 1;
      int lineStart = 0;

      for (int i = 0; i < at; i++) {
         if (this.text[i] == '\n') {
            line++;
            lineStart = i + 1;
         }
      }

      return new JsonParseException(message + " at line " + line + " column " + (at - lineStart + 1) + " path " + this.path());
   }

   private String path() {
      StringBuilder out = new StringBuilder("$");

      for (int d = 0; d < this.depth; d++) {
         if (this.containers[d] instanceof JsonArray array) {
            out.append('[').append(array.size()).append(']');
         } else {
            out.append('.');
            if (this.names[d] != null) {
               out.append(this.names[d]);
            }
         }
      }

      return out.toString();
   }

   private static final class LazyNumber extends Number {
      private final String value;
      private final boolean integral;

      private LazyNumber(String value, boolean integral) {
         this.value = value;
         this.integral = integral;
      }

      public int intValue() {
         if (this.integral) {
            try {
               return Integer.parseInt(this.value);
            } catch (NumberFormatException e) {
               try {
                  return (int)Long.parseLong(this.value);
               } catch (NumberFormatException var3) {
               }
            }
         }

         return new BigDecimal(this.value).intValue();
      }

      public long longValue() {
         if (this.integral) {
            try {
               return Long.parseLong(this.value);
            } catch (NumberFormatException var2) {
            }
         }

         return new BigDecimal(this.value).longValue();
      }

      public float floatValue() {
         return Float.parseFloat(this.value);
      }

      public double doubleValue() {
         return Double.parseDouble(this.value);
      }

      public String toString() {
         return this.value;
      }

      public int hashCode() {
         return this.value.hashCode();
      }

      public boolean equals(Object other) {
         return other == this || other instanceof JsonTextParser.LazyNumber number && this.value.equals(number.value);
      }
   }

   public record Parsed(JsonElement value, int trailingLine) {
   }
}
