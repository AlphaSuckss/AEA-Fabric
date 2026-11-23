package dev.alphasucks.aea.port.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.alphasucks.aea.port.mixin.command.EntitySelectorOptionsAccessor;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.skill.SkillDataAttachment;
import dev.alphasucks.aea.port.skill.SkillRegistry;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2172;
import net.minecraft.class_2303;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class ApoliSelectorOptions {
   private static final DynamicCommandExceptionType NOT_INSTALLED = new DynamicCommandExceptionType(
      name -> class_2561.method_43470("The '" + name + "' selector option needs the Origins mod installed.")
   );
   private static final Map<String, ApoliSelectorOptions.Handler> EXTERNAL = new ConcurrentHashMap();

   private ApoliSelectorOptions() {
   }

   public static void register(String name, ApoliSelectorOptions.Handler handler) {
      EXTERNAL.put(name, handler);
   }

   public static void bootstrap() {
      // AEA: Overgrown's Origins registered these; here they read Origins 1.10.2's origin component.
      EXTERNAL.putIfAbsent("origin", new ApoliSelectorOptions.OriginHandler(false));
      EXTERNAL.putIfAbsent("origin_layer", new ApoliSelectorOptions.OriginHandler(true));
      option("power", "Entities holding an Apoli power", () -> new ApoliSelectorOptions.PowerHandler(false));
      option("suppressed_power", "Entities whose Apoli power is suppressed", () -> new ApoliSelectorOptions.PowerHandler(true));
      option("skill", "Players who purchased a skill", ApoliSelectorOptions::skillHandler);
      option("origin", "Players with an origin (needs Origins)", () -> (ApoliSelectorOptions.Handler)EXTERNAL.get("origin"));
      option("origin_layer", "Players who chose an origin in a layer (needs Origins)", () -> (ApoliSelectorOptions.Handler)EXTERNAL.get("origin_layer"));
   }

   private static void option(String name, String tooltip, Supplier<ApoliSelectorOptions.Handler> lookup) {
      EntitySelectorOptionsAccessor.apoli$register(name, parser -> handle(name, lookup, parser), parser -> true, class_2561.method_43470(tooltip));
   }

   private static void handle(String name, Supplier<ApoliSelectorOptions.Handler> lookup, class_2303 parser) throws CommandSyntaxException {
      ApoliSelectorOptions.Handler handler = (ApoliSelectorOptions.Handler)lookup.get();
      parser.method_9875((builder, consumer) -> {
         ApoliSelectorOptions.Handler current = (ApoliSelectorOptions.Handler)lookup.get();
         return current == null ? builder.buildFuture() : class_2172.method_9270(current.suggestions(), builder);
      });
      boolean inverted = parser.method_9892();
      int start = parser.method_9835().getCursor();
      class_2960 value = class_2960.method_12835(parser.method_9835());
      if (handler == null) {
         parser.method_9835().setCursor(start);
         throw NOT_INSTALLED.createWithContext(parser.method_9835(), name);
      }

      Predicate<class_1297> predicate = handler.predicate(value);
      parser.method_9916(inverted ? predicate.negate() : predicate);
   }

   @Nullable
   private static ApoliSelectorOptions.Handler skillHandler() {
      return new ApoliSelectorOptions.Handler() {
         @Override
         public Predicate<class_1297> predicate(class_2960 value) {
            return entity -> entity instanceof class_1657 player && SkillDataAttachment.get(player).isPurchased(value);
         }

         @Override
         public Collection<class_2960> suggestions() {
            List<class_2960> ids = new ArrayList();
            SkillRegistry.all().forEach(skill -> ids.add(skill.id()));
            return ids;
         }
      };
   }

   public interface Handler {
      Predicate<class_1297> predicate(class_2960 var1);

      Collection<class_2960> suggestions();
   }

   /** AEA: origin / origin_layer options over Origins 1.10.2 ({@code ModComponents.ORIGIN}). */
   private record OriginHandler(boolean layer) implements ApoliSelectorOptions.Handler {
      @Override
      public Predicate<class_1297> predicate(class_2960 value) {
         return entity -> {
            if (!(entity instanceof class_1657 player)) {
               return false;
            }
            OriginComponent component = ModComponents.ORIGIN.getNullable(player);
            if (component == null) {
               return false;
            }
            for (Map.Entry<OriginLayer, Origin> e : component.getOrigins().entrySet()) {
               if (e.getKey() == null || e.getValue() == null || e.getValue() == Origin.EMPTY) {
                  continue;
               }
               if (this.layer ? value.equals(e.getKey().getIdentifier()) : value.equals(e.getValue().getIdentifier())) {
                  return true;
               }
            }
            return false;
         };
      }

      @Override
      public Collection<class_2960> suggestions() {
         List<class_2960> ids = new ArrayList<>();
         if (this.layer) {
            for (OriginLayer l : OriginLayers.getLayers()) {
               ids.add(l.getIdentifier());
            }
         } else {
            OriginRegistry.identifiers().forEach(ids::add);
         }
         return ids;
      }
   }

   private record PowerHandler(boolean suppressed) implements ApoliSelectorOptions.Handler {
      @Override
      public Predicate<class_1297> predicate(class_2960 value) {
         return entity -> {
            PowerContainer container = PowerContainer.of(entity);
            return container != null && container.hasPower(value) ? !this.suppressed || container.isSuppressed(value) : false;
         };
      }

      @Override
      public Collection<class_2960> suggestions() {
         return ApoliPowers.view().keySet();
      }
   }
}
