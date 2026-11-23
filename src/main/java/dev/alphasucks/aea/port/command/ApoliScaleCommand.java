package dev.alphasucks.aea.port.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.alphasucks.aea.port.scale.ScaleEasing;
import dev.alphasucks.aea.port.scale.ScaleOperation;
import dev.alphasucks.aea.port.scale.ScaleState;
import dev.alphasucks.aea.port.scale.ScaleType;
import dev.alphasucks.aea.port.scale.ScaleTypeCodec;
import dev.alphasucks.aea.port.scale.ScaleTypes;
import dev.alphasucks.aea.port.scale.Scales;
import java.util.Collection;
import java.util.Locale;
import net.minecraft.class_1297;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2172;
import net.minecraft.class_2186;
import net.minecraft.class_2232;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

public final class ApoliScaleCommand {
   private static final SuggestionProvider<class_2168> SCALE_TYPES = (ctx, builder) -> class_2172.method_9270(ScaleTypes.ids(), builder);
   private static final SuggestionProvider<class_2168> EASINGS = (ctx, builder) -> {
      for (ScaleEasing easing : ScaleEasing.values()) {
         builder.suggest(easing.method_15434());
      }

      return builder.buildFuture();
   };

   private ApoliScaleCommand() {
   }

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      LiteralArgumentBuilder<class_2168> root = (LiteralArgumentBuilder<class_2168>)class_2170.method_9247("apoli:scale")
         .requires(ApoliPermissions.require("apoli.command.scale", 2));
      root.then(
         class_2170.method_9247("get")
            .then(
               ((RequiredArgumentBuilder)class_2170.method_9244("targets", class_2186.method_9306()).executes(ctx -> get(ctx, ScaleTypes.BASE)))
                  .then(class_2170.method_9244("scale_type", class_2232.method_9441()).suggests(SCALE_TYPES).executes(ctx -> get(ctx, type(ctx))))
            )
      );

      for (ScaleOperation operation : ScaleOperation.values()) {
         root.then(operation(operation));
      }

      root.then(class_2170.method_9247("reset").then(class_2170.method_9244("targets", class_2186.method_9306()).executes(ApoliScaleCommand::reset)));
      root.then(class_2170.method_9247("list").executes(ApoliScaleCommand::list));
      dispatcher.register(root);
   }

   private static LiteralArgumentBuilder<class_2168> operation(ScaleOperation operation) {
      RequiredArgumentBuilder<class_2168, Float> value = (RequiredArgumentBuilder<class_2168, Float>)((RequiredArgumentBuilder)class_2170.method_9244(
               "value", FloatArgumentType.floatArg(1.0E-4F, 10000.0F)
            )
            .executes(ctx -> apply(ctx, operation, 0, ScaleEasing.LINEAR)))
         .then(
            ((RequiredArgumentBuilder)class_2170.method_9244("ticks", IntegerArgumentType.integer(0, 32767))
                  .executes(ctx -> apply(ctx, operation, IntegerArgumentType.getInteger(ctx, "ticks"), ScaleEasing.LINEAR)))
               .then(
                  class_2170.method_9244("easing", class_2232.method_9441())
                     .suggests(EASINGS)
                     .executes(ctx -> apply(ctx, operation, IntegerArgumentType.getInteger(ctx, "ticks"), easing(ctx)))
               )
         );
      return (LiteralArgumentBuilder<class_2168>)class_2170.method_9247(operation.method_15434())
         .then(
            class_2170.method_9244("targets", class_2186.method_9306())
               .then(class_2170.method_9244("scale_type", class_2232.method_9441()).suggests(SCALE_TYPES).then(value))
         );
   }

   private static ScaleType type(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 id = class_2232.method_9443(ctx, "scale_type");
      ScaleType type = ScaleTypeCodec.resolve(id);
      if (type == null) {
         throw new SimpleCommandExceptionType(class_2561.method_43470("Unknown scale type: " + id)).create();
      } else {
         return type;
      }
   }

   private static ScaleEasing easing(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 id = class_2232.method_9443(ctx, "easing");

      for (ScaleEasing easing : ScaleEasing.values()) {
         if (easing.method_15434().equals(id.method_12832())) {
            return easing;
         }
      }

      throw new SimpleCommandExceptionType(class_2561.method_43470("Unknown easing: " + id)).create();
   }

   private static int get(CommandContext<class_2168> ctx, ScaleType type) throws CommandSyntaxException {
      Collection<? extends class_1297> targets = class_2186.method_9317(ctx, "targets");
      int last = 0;

      for (class_1297 entity : targets) {
         float value = Scales.value(entity, type);
         last = (int)(value * 100.0F);
         ((class_2168)ctx.getSource())
            .method_9226(
               () -> class_2561.method_43470(
                  entity.method_5477().getString() + " " + type.id() + " = " + String.format(Locale.ROOT, "%.4f", new Object[]{value})
               ),
               false
            );
      }

      return last;
   }

   private static int apply(CommandContext<class_2168> ctx, ScaleOperation operation, int ticks, ScaleEasing easing) throws CommandSyntaxException {
      Collection<? extends class_1297> targets = class_2186.method_9317(ctx, "targets");
      ScaleType type = type(ctx);
      float argument = FloatArgumentType.getFloat(ctx, "value");

      for (class_1297 entity : targets) {
         ScaleState state = Scales.stateOf(entity);
         float current = state == null ? 1.0F : state.target(type);
         Scales.set(entity, type, operation.apply(current, argument), ticks, easing);
      }

      int count = targets.size();
      ((class_2168)ctx.getSource())
         .method_9226(() -> class_2561.method_43470("Set " + type.id() + " on " + count + " entit" + (count == 1 ? "y" : "ies")), true);
      return count;
   }

   private static int reset(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      Collection<? extends class_1297> targets = class_2186.method_9317(ctx, "targets");

      for (class_1297 entity : targets) {
         Scales.reset(entity);
      }

      int count = targets.size();
      ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("Reset every scale on " + count + " entit" + (count == 1 ? "y" : "ies")), true);
      return count;
   }

   private static int list(CommandContext<class_2168> ctx) {
      StringBuilder builder = new StringBuilder();

      for (ScaleType type : ScaleTypes.all()) {
         if (builder.length() > 0) {
            builder.append(", ");
         }

         builder.append(type.id());
      }

      ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(builder.toString()), false);
      return ScaleTypes.count();
   }
}
