package dev.alphasucks.aea.port.data.expr;

import java.util.List;
import java.util.UUID;
import net.minecraft.class_1309;
import net.minecraft.class_1320;
import net.minecraft.class_1329;
import net.minecraft.class_2960;
import net.minecraft.class_5131;
import net.minecraft.class_7923;

public final class ExprAttributes {
   private static final String QUERIES = "get, base, default, min, max, has, modifier, has_modifier";

   private ExprAttributes() {
   }

   public static ExprVars.ResolvedVar query(List<String> args, String name, int at) throws ExprParseException {
      if (!args.isEmpty() && !((String)args.get(0)).isEmpty()) {
         String rawId = unquote((String)args.get(0));
         class_2960 id = class_2960.method_12829(rawId);
         if (id == null) {
            throw new ExprParseException("'" + rawId + "' is not a valid attribute id", at);
         }

         class_1320 attribute = (class_1320)class_7923.field_41190.method_17966(id).orElse(null);
         if (attribute == null) {
            throw new ExprParseException("unknown attribute '" + id + "'", at);
         }

         String query = args.size() > 1 ? unquote((String)args.get(1)) : "get";
         boolean modifierQuery = query.equals("modifier") || query.equals("has_modifier");
         int allowed = modifierQuery ? 3 : 2;
         if (args.size() > allowed) {
            throw new ExprParseException(name + "[id, " + query + "] takes at most " + allowed + " arguments, but got " + args.size(), at);
         }

         if (modifierQuery) {
            if (args.size() >= 3 && !((String)args.get(2)).isEmpty()) {
               String rawModifier = unquote((String)args.get(2));

               UUID modifier;
               try {
                  modifier = UUID.fromString(rawModifier);
               } catch (IllegalArgumentException e) {
                  throw new ExprParseException("'" + rawModifier + "' is not a valid modifier UUID", at);
               }

               return query.equals("modifier") ? modifierAmount(attribute, modifier) : hasModifier(attribute, modifier);
            } else {
               throw new ExprParseException(name + "[id, " + query + ", <modifier uuid>] is missing the modifier UUID", at);
            }
         } else {
            return switch (query) {
               case "get" -> new ExprVars.ResolvedVar((e, c, l, v) -> {
                  if (e instanceof class_1309 living) {
                     class_5131 map = living.method_6127();
                     return map.method_27306(attribute) ? map.method_26852(attribute) : 0.0;
                  } else {
                     return 0.0;
                  }
               }, false);
               case "base" -> new ExprVars.ResolvedVar((e, c, l, v) -> {
                  if (e instanceof class_1309 living) {
                     class_5131 map = living.method_6127();
                     return map.method_27306(attribute) ? map.method_26856(attribute) : 0.0;
                  } else {
                     return 0.0;
                  }
               }, false);
               case "has" -> new ExprVars.ResolvedVar(
                  (e, c, l, v) -> e instanceof class_1309 living && living.method_6127().method_27306(attribute) ? 1 : 0, false
               );
               case "default" -> constant(attribute.method_6169());
               case "min" -> constant(attribute instanceof class_1329 ranged ? ranged.method_35061() : -Double.MAX_VALUE);
               case "max" -> constant(attribute instanceof class_1329 ranged ? ranged.method_35062() : Double.MAX_VALUE);
               default -> throw new ExprParseException(
                  "'" + query + "' is not an attribute query (get, base, default, min, max, has, modifier, has_modifier)", at
               );
            };
         }
      } else {
         throw new ExprParseException(name + "[id, query] expects an attribute id", at);
      }
   }

   private static ExprVars.ResolvedVar modifierAmount(class_1320 attribute, UUID modifier) {
      return new ExprVars.ResolvedVar((e, c, l, v) -> {
         if (e instanceof class_1309 living) {
            class_5131 map = living.method_6127();
            return map.method_27305(attribute, modifier) ? map.method_27307(attribute, modifier) : 0.0;
         } else {
            return 0.0;
         }
      }, false);
   }

   private static ExprVars.ResolvedVar hasModifier(class_1320 attribute, UUID modifier) {
      return new ExprVars.ResolvedVar((e, c, l, v) -> e instanceof class_1309 living && living.method_6127().method_27305(attribute, modifier) ? 1 : 0, false);
   }

   private static ExprVars.ResolvedVar constant(double value) {
      return new ExprVars.ResolvedVar((e, c, l, v) -> value, false);
   }

   private static String unquote(String arg) {
      int last = arg.length() - 1;
      if (last > 0) {
         char first = arg.charAt(0);
         if ((first == '\'' || first == '"') && arg.charAt(last) == first) {
            return arg.substring(1, last).trim();
         }
      }

      return arg;
   }
}
