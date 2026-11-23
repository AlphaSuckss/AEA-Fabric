package dev.alphasucks.aea.port.script;

import dev.alphasucks.aea.port.Apoli;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ApoliScripts {
   private static final EnumSet<ApoliScripts.Kind> ACTION_KINDS = EnumSet.of(
      ApoliScripts.Kind.ENTITY_ACTION,
      new ApoliScripts.Kind[]{
         ApoliScripts.Kind.BIENTITY_ACTION,
         ApoliScripts.Kind.BLOCK_ACTION,
         ApoliScripts.Kind.ITEM_ACTION,
         ApoliScripts.Kind.POWER_ADDED,
         ApoliScripts.Kind.POWER_REMOVED
      }
   );
   private static final Map<ApoliScripts.Key, Consumer<ScriptCtx>> ACTIONS = new ConcurrentHashMap();
   private static final Map<ApoliScripts.Key, Predicate<ScriptCtx>> CONDITIONS = new ConcurrentHashMap();
   private static final Map<ApoliScripts.Kind, ApoliScripts.EventDispatcher> EVENT_ACTIONS = new ConcurrentHashMap();
   private static final Map<ApoliScripts.Kind, ApoliScripts.EventPredicate> EVENT_CONDITIONS = new ConcurrentHashMap();
   @Nullable
   private static volatile ScriptBackend backend;

   private ApoliScripts() {
   }

   public static void setEventDispatcher(ApoliScripts.Kind kind, ApoliScripts.EventDispatcher dispatcher) {
      EVENT_ACTIONS.put(kind, dispatcher);
   }

   public static void setEventPredicate(ApoliScripts.Kind kind, ApoliScripts.EventPredicate predicate) {
      EVENT_CONDITIONS.put(kind, predicate);
   }

   public static void setBackend(ScriptBackend next) {
      backend = next;
      Apoli.LOGGER.info("[Apoli] Script backend installed: {}", next.name());
   }

   @Nullable
   public static ScriptBackend backend() {
      return backend;
   }

   public static void registerAction(ApoliScripts.Kind kind, class_2960 id, Consumer<ScriptCtx> handler) {
      if (!ACTION_KINDS.contains(kind)) {
         throw new IllegalArgumentException(kind + " is not an action kind");
      }

      ACTIONS.put(new ApoliScripts.Key(kind, id), handler);
   }

   public static void registerCondition(ApoliScripts.Kind kind, class_2960 id, Predicate<ScriptCtx> handler) {
      CONDITIONS.put(new ApoliScripts.Key(kind, id), handler);
   }

   @Nullable
   public static Consumer<ScriptCtx> action(ApoliScripts.Kind kind, class_2960 id) {
      return (Consumer<ScriptCtx>)ACTIONS.get(new ApoliScripts.Key(kind, id));
   }

   @Nullable
   public static Predicate<ScriptCtx> condition(ApoliScripts.Kind kind, class_2960 id) {
      return (Predicate<ScriptCtx>)CONDITIONS.get(new ApoliScripts.Key(kind, id));
   }

   public static void run(ApoliScripts.Kind kind, class_2960 id, ScriptCtx ctx) {
      Consumer<ScriptCtx> handler = (Consumer<ScriptCtx>)ACTIONS.get(new ApoliScripts.Key(kind, id));
      if (handler != null) {
         try {
            handler.accept(ctx);
         } catch (Throwable t) {
            ScriptWarnings.failed(kind, id, t);
         }
      } else {
         ApoliScripts.EventDispatcher dispatcher = (ApoliScripts.EventDispatcher)EVENT_ACTIONS.get(kind);
         if (dispatcher != null) {
            try {
               if (dispatcher.dispatch(id, ctx)) {
                  return;
               }
            } catch (Throwable t) {
               ScriptWarnings.failed(kind, id, t);
               return;
            }
         }

         ScriptBackend engine = backend;
         if (engine != null && engine.available() && engine.has(id)) {
            try {
               engine.execute(id, ctx);
            } catch (Throwable t) {
               ScriptWarnings.failed(kind, id, t);
            }
         } else {
            ScriptWarnings.missing(kind, id);
         }
      }
   }

   public static boolean test(ApoliScripts.Kind kind, class_2960 id, ScriptCtx ctx, boolean fallback) {
      Predicate<ScriptCtx> handler = (Predicate<ScriptCtx>)CONDITIONS.get(new ApoliScripts.Key(kind, id));
      if (handler != null) {
         try {
            return handler.test(ctx);
         } catch (Throwable t) {
            ScriptWarnings.failed(kind, id, t);
            return fallback;
         }
      } else {
         ApoliScripts.EventPredicate predicate = (ApoliScripts.EventPredicate)EVENT_CONDITIONS.get(kind);
         if (predicate != null) {
            try {
               Boolean result = predicate.test(id, ctx);
               if (result != null) {
                  return result;
               }
            } catch (Throwable t) {
               ScriptWarnings.failed(kind, id, t);
               return fallback;
            }
         }

         ScriptBackend engine = backend;
         if (engine != null && engine.available() && engine.has(id)) {
            try {
               return truthy(engine.execute(id, ctx), fallback);
            } catch (Throwable t) {
               ScriptWarnings.failed(kind, id, t);
               return fallback;
            }
         } else {
            ScriptWarnings.missing(kind, id);
            return fallback;
         }
      }
   }

   private static boolean truthy(@Nullable Object value, boolean fallback) {
      if (value == null) {
         return fallback;
      } else if (value instanceof Boolean bool) {
         return bool;
      } else if (value instanceof Number number) {
         return number.doubleValue() != 0.0;
      } else {
         return value instanceof CharSequence text ? !text.isEmpty() : true;
      }
   }

   public static void clear() {
      ACTIONS.clear();
      CONDITIONS.clear();
      ScriptWarnings.reset();
   }

   public static int registeredCount() {
      return ACTIONS.size() + CONDITIONS.size();
   }

   @FunctionalInterface
   public interface EventDispatcher {
      boolean dispatch(class_2960 var1, ScriptCtx var2);
   }

   @FunctionalInterface
   public interface EventPredicate {
      @Nullable
      Boolean test(class_2960 var1, ScriptCtx var2);
   }

   private record Key(ApoliScripts.Kind kind, class_2960 id) {
   }

   public enum Kind {
      ENTITY_ACTION,
      BIENTITY_ACTION,
      BLOCK_ACTION,
      ITEM_ACTION,
      ENTITY_CONDITION,
      BIENTITY_CONDITION,
      BLOCK_CONDITION,
      ITEM_CONDITION,
      POWER_ADDED,
      POWER_REMOVED;
   }
}
