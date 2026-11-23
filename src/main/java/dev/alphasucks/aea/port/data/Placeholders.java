package dev.alphasucks.aea.port.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.class_156;
import org.jetbrains.annotations.Nullable;

public final class Placeholders {
   private static final DecimalFormat NUMBER_FORMAT = class_156.method_654(new DecimalFormat("#"), format -> {
      format.setMaximumFractionDigits(15);
      format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
   });

   private Placeholders() {
   }

   public static DataResult<List<String>> parameters(String owner, Dynamic<?> template, Optional<List<String>> declared) {
      Set<String> found = new LinkedHashSet();
      if (declared.isEmpty()) {
         scan(template, null, found);
         return DataResult.success(List.copyOf(found));
      }

      List<String> names = List.copyOf(new LinkedHashSet((Collection)declared.get()));

      for (String name : names) {
         if (name.isEmpty() || name.indexOf(91) >= 0 || name.indexOf(93) >= 0) {
            return DataResult.error(() -> owner + " declares the parameter \"" + name + "\", and a parameter name cannot be empty or contain [ or ]");
         }
      }

      scan(template, names, found);

      for (String name : names) {
         if (!found.contains(name)) {
            return DataResult.error(() -> owner + " declares parameter [" + name + "] but never uses it");
         }
      }

      return DataResult.success(names);
   }

   public static <T> T substitute(DynamicOps<T> ops, T input, List<String> parameters, Map<String, Dynamic<?>> arguments) {
      if (input != null && !parameters.isEmpty()) {
         Optional<String> text = ops.getStringValue(input).result();
         if (text.isPresent()) {
            return substituteText(ops, input, (String)text.get(), parameters, arguments);
         }

         Optional<Stream<Pair<T, T>>> entries = ops.getMapValues(input).result();
         if (entries.isPresent()) {
            List<Pair<T, T>> pairs = ((Stream)entries.get()).toList();
            List<Pair<T, T>> rebuilt = null;

            for (int i = 0; i < pairs.size(); i++) {
               Pair<T, T> pair = (Pair<T, T>)pairs.get(i);
               T value = substitute(ops, (T)pair.getSecond(), parameters, arguments);
               if (value != pair.getSecond() && rebuilt == null) {
                  rebuilt = new ArrayList(pairs.subList(0, i));
               }

               if (rebuilt != null) {
                  rebuilt.add(value == pair.getSecond() ? pair : Pair.of(pair.getFirst(), value));
               }
            }

            return (T)(rebuilt == null ? input : ops.createMap(rebuilt.stream()));
         } else {
            Optional<Stream<T>> values = ops.getStream(input).result();
            if (values.isPresent()) {
               List<T> items = ((Stream)values.get()).toList();
               List<T> rebuilt = null;

               for (int i = 0; i < items.size(); i++) {
                  T item = (T)items.get(i);
                  T value = substitute(ops, item, parameters, arguments);
                  if (value != item && rebuilt == null) {
                     rebuilt = new ArrayList(items.subList(0, i));
                  }

                  if (rebuilt != null) {
                     rebuilt.add(value);
                  }
               }

               return (T)(rebuilt == null ? input : ops.createList(rebuilt.stream()));
            } else {
               return input;
            }
         }
      } else {
         return input;
      }
   }

   public static String stringify(Dynamic<?> value) {
      return stringifyDynamic(value);
   }

   private static <T> String stringifyDynamic(Dynamic<T> value) {
      return stringify(value.getOps(), value.getValue());
   }

   private static <T> String stringify(DynamicOps<T> ops, T raw) {
      Optional<String> text = ops.getStringValue(raw).result();
      if (text.isPresent()) {
         return (String)text.get();
      }

      Optional<Boolean> flag = ops.getBooleanValue(raw).result();
      if (flag.isPresent() && ops.createBoolean((Boolean)flag.get()).equals(raw)) {
         return ((Boolean)flag.get()).toString();
      }

      Optional<Number> number = ops.getNumberValue(raw).result();
      if (number.isPresent()) {
         synchronized (NUMBER_FORMAT) {
            return NUMBER_FORMAT.format(number.get());
         }
      } else {
         return String.valueOf(raw);
      }
   }

   private static <T> T substituteText(DynamicOps<T> ops, T input, String text, List<String> parameters, Map<String, Dynamic<?>> arguments) {
      int open = text.indexOf(91);
      if (open < 0) {
         return input;
      }

      if (open == 0 && text.charAt(text.length() - 1) == ']') {
         String whole = declaredAt(text, 0, parameters);
         Dynamic<?> value = whole != null && whole.length() + 2 == text.length() ? (Dynamic)arguments.get(whole) : null;
         if (value != null) {
            return (T)value.convert(ops).getValue();
         }
      }

      StringBuilder out = null;
      int copied = 0;

      while (open >= 0) {
         String name = declaredAt(text, open, parameters);
         Dynamic<?> value = name == null ? null : (Dynamic)arguments.get(name);
         if (value == null) {
            open = text.indexOf(91, open + 1);
         } else {
            if (out == null) {
               out = new StringBuilder(text.length() + 16);
            }

            out.append(text, copied, open).append(stringify(value));
            copied = open + name.length() + 2;
            open = text.indexOf(91, copied);
         }
      }

      return (T)(out == null ? input : ops.createString(out.append(text, copied, text.length()).toString()));
   }

   private static <T> void scan(Dynamic<T> template, @Nullable List<String> declared, Set<String> out) {
      scan(template.getOps(), template.getValue(), declared, out);
   }

   private static <T> void scan(DynamicOps<T> ops, T input, @Nullable List<String> declared, Set<String> out) {
      if (input != null) {
         Optional<String> text = ops.getStringValue(input).result();
         if (text.isPresent()) {
            scanText((String)text.get(), declared, out);
         } else {
            Optional<Stream<Pair<T, T>>> entries = ops.getMapValues(input).result();
            if (entries.isPresent()) {
               ((Stream<Pair<T, T>>)entries.get()).forEach(entry -> scan(ops, entry.getSecond(), declared, out));
            } else {
               ops.getStream(input).result().ifPresent(values -> values.forEach(value -> scan(ops, value, declared, out)));
            }
         }
      }
   }

   private static void scanText(String text, @Nullable List<String> declared, Set<String> out) {
      int open = text.indexOf(91);

      while (open >= 0) {
         String name = declared == null ? identifierAt(text, open) : declaredAt(text, open, declared);
         if (name == null) {
            open = text.indexOf(91, open + 1);
         } else {
            out.add(name);
            open = text.indexOf(91, open + name.length() + 2);
         }
      }
   }

   @Nullable
   private static String identifierAt(String text, int open) {
      int start = open + 1;
      if (start < text.length() && isNameStart(text.charAt(start))) {
         int end = start + 1;

         while (end < text.length() && isNamePart(text.charAt(end))) {
            end++;
         }

         return end < text.length() && text.charAt(end) == ']' ? text.substring(start, end) : null;
      } else {
         return null;
      }
   }

   @Nullable
   private static String declaredAt(String text, int open, List<String> names) {
      for (int i = 0; i < names.size(); i++) {
         String name = (String)names.get(i);
         int close = open + 1 + name.length();
         if (close < text.length() && text.charAt(close) == ']' && text.startsWith(name, open + 1)) {
            return name;
         }
      }

      return null;
   }

   private static boolean isNameStart(char c) {
      return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
   }

   private static boolean isNamePart(char c) {
      return isNameStart(c) || c >= '0' && c <= '9' || c == '-' || c == '.';
   }
}
