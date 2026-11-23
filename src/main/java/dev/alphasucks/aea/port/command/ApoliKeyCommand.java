package dev.alphasucks.aea.port.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.alphasucks.aea.port.data.ScrollDirection;
import dev.alphasucks.aea.port.keybind.HeldKeys;
import dev.alphasucks.aea.port.keybind.KeyDispatch;
import dev.alphasucks.aea.port.power.PowerKeys;
import dev.alphasucks.aea.port.power.builtin.ActionOnScrollWheelPower;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_124;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2172;
import net.minecraft.class_2186;
import net.minecraft.class_2561;

public final class ApoliKeyCommand {
   private static final List<String> VANILLA_KEYS = List.of(
      new String[]{
         "key.attack",
         "key.use",
         "key.jump",
         "key.sneak",
         "key.sprint",
         "key.forward",
         "key.back",
         "key.left",
         "key.right",
         "key.drop",
         "key.inventory",
         "key.swapOffhand",
         "key.pickItem",
         "key.chat",
         "key.command",
         "key.playerlist",
         "key.togglePerspective",
         "key.smoothCamera",
         "key.hotbar.1",
         "key.hotbar.2",
         "key.hotbar.3",
         "key.hotbar.4",
         "key.hotbar.5",
         "key.hotbar.6",
         "key.hotbar.7",
         "key.hotbar.8",
         "key.hotbar.9"
      }
   );
   private static final SuggestionProvider<class_2168> KEY_SUGGESTIONS = (ctx, builder) -> {
      Set<String> keys = new LinkedHashSet(PowerKeys.knownKeys());
      keys.addAll(VANILLA_KEYS);
      return class_2172.method_9265(keys, builder);
   };

   private ApoliKeyCommand() {
   }

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247(
                              "apoli:key"
                           )
                           .requires(ApoliPermissions.require("apoli.command.key", 2)))
                        .then(
                           class_2170.method_9247("press")
                              .then(
                                 class_2170.method_9244("targets", class_2186.method_9306())
                                    .then(
                                       ((RequiredArgumentBuilder)class_2170.method_9244("key", StringArgumentType.string())
                                             .suggests(KEY_SUGGESTIONS)
                                             .executes(ctx -> press(ctx, 1)))
                                          .then(
                                             class_2170.method_9244("duration", IntegerArgumentType.integer(1))
                                                .executes(ctx -> press(ctx, IntegerArgumentType.getInteger(ctx, "duration")))
                                          )
                                    )
                              )
                        ))
                     .then(
                        class_2170.method_9247("release")
                           .then(
                              class_2170.method_9244("targets", class_2186.method_9306())
                                 .then(class_2170.method_9244("key", StringArgumentType.string()).suggests(KEY_SUGGESTIONS).executes(ApoliKeyCommand::release))
                           )
                     ))
                  .then(class_2170.method_9247("clear").then(class_2170.method_9244("targets", class_2186.method_9306()).executes(ApoliKeyCommand::clear))))
               .then(class_2170.method_9247("list").then(class_2170.method_9244("targets", class_2186.method_9306()).executes(ApoliKeyCommand::list))))
            .then(
               class_2170.method_9247("scroll")
                  .then(
                     ((RequiredArgumentBuilder)class_2170.method_9244("targets", class_2186.method_9306())
                           .then(
                              ((LiteralArgumentBuilder)class_2170.method_9247("up").executes(ctx -> scroll(ctx, ScrollDirection.UP, 1)))
                                 .then(
                                    class_2170.method_9244("count", IntegerArgumentType.integer(1, 64))
                                       .executes(ctx -> scroll(ctx, ScrollDirection.UP, IntegerArgumentType.getInteger(ctx, "count")))
                                 )
                           ))
                        .then(
                           ((LiteralArgumentBuilder)class_2170.method_9247("down").executes(ctx -> scroll(ctx, ScrollDirection.DOWN, 1)))
                              .then(
                                 class_2170.method_9244("count", IntegerArgumentType.integer(1, 64))
                                    .executes(ctx -> scroll(ctx, ScrollDirection.DOWN, IntegerArgumentType.getInteger(ctx, "count")))
                              )
                        )
                  )
            )
      );
   }

   private static int press(CommandContext<class_2168> ctx, int duration) throws CommandSyntaxException {
      Collection<? extends class_1297> targets = class_2186.method_9317(ctx, "targets");
      String key = StringArgumentType.getString(ctx, "key");

      for (class_1297 target : targets) {
         KeyDispatch.force(target, key, duration, false);
      }

      int count = targets.size();
      ((class_2168)ctx.getSource())
         .method_9226(() -> class_2561.method_43470("Held " + key + " for " + duration + " tick(s) on " + count + " entit" + (count == 1 ? "y" : "ies")), true);
      return count;
   }

   private static int release(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      Collection<? extends class_1297> targets = class_2186.method_9317(ctx, "targets");
      String key = StringArgumentType.getString(ctx, "key");

      for (class_1297 target : targets) {
         KeyDispatch.force(target, key, 1, true);
      }

      int count = targets.size();
      ((class_2168)ctx.getSource())
         .method_9226(() -> class_2561.method_43470("Released " + key + " on " + count + " entit" + (count == 1 ? "y" : "ies")), true);
      return count;
   }

   private static int clear(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      Collection<? extends class_1297> targets = class_2186.method_9317(ctx, "targets");
      int cleared = 0;

      for (class_1297 target : targets) {
         Set<String> forced = new LinkedHashSet(HeldKeys.forcedKeys(target.method_5667()));
         if (!forced.isEmpty()) {
            for (String key : forced) {
               KeyDispatch.force(target, key, 1, true);
            }

            cleared++;
         }
      }

      int total = cleared;
      ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("Cleared forced keys on " + total + " entit" + (total == 1 ? "y" : "ies")), true);
      return cleared;
   }

   private static int list(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      Collection<? extends class_1297> targets = class_2186.method_9317(ctx, "targets");
      int found = 0;

      for (class_1297 target : targets) {
         Set<String> held = HeldKeys.serverHeldSet(target.method_5667());
         Set<String> forced = HeldKeys.forcedKeys(target.method_5667());
         if (held.isEmpty()) {
            ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(name(target) + ": no keys held").method_27692(class_124.field_1080), false);
         } else {
            found++;
            List<String> sorted = new ArrayList(held);
            Collections.sort(sorted);
            StringBuilder line = new StringBuilder(name(target)).append(": ");

            for (int i = 0; i < sorted.size(); i++) {
               if (i > 0) {
                  line.append(", ");
               }

               line.append((String)sorted.get(i));
               if (forced.contains(sorted.get(i))) {
                  line.append(" (forced)");
               }
            }

            String text = line.toString();
            ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(text), false);
         }
      }

      return found;
   }

   private static int scroll(CommandContext<class_2168> ctx, ScrollDirection direction, int count) throws CommandSyntaxException {
      Collection<? extends class_1297> targets = class_2186.method_9317(ctx, "targets");
      int fired = 0;

      for (class_1297 target : targets) {
         fired += ActionOnScrollWheelPower.scroll(target, direction, count);
      }

      int total = fired;
      ((class_2168)ctx.getSource())
         .method_9226(() -> class_2561.method_43470("Scrolled " + direction.getSerializedName() + " ×" + count + " — " + total + " power(s) fired"), true);
      return fired;
   }

   private static String name(class_1297 entity) {
      return entity instanceof class_1657 ? entity.method_5477().getString() : entity.method_5477().getString() + "#" + entity.method_5628();
   }
}
