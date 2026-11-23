package dev.alphasucks.aea.port.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.alphasucks.aea.port.tick.TickRateVanilla;
import dev.alphasucks.aea.port.tick.TickRates;
import dev.alphasucks.aea.port.tick.TickScope;
import dev.alphasucks.aea.port.tick.TickState;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.class_1297;
import net.minecraft.class_1923;
import net.minecraft.class_1937;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2172;
import net.minecraft.class_2186;
import net.minecraft.class_2245;
import net.minecraft.class_2264;
import net.minecraft.class_2265;
import net.minecraft.class_2267;
import net.minecraft.class_2300;
import net.minecraft.class_2561;
import net.minecraft.class_3218;
import net.minecraft.class_4076;
import net.minecraft.class_5321;

public final class ApoliTickCommand {
   private static final int MAX_CHUNKS = 4096;
   private static final SimpleCommandExceptionType TOO_MANY_CHUNKS = new SimpleCommandExceptionType(
      class_2561.method_43470("That covers more than 4096 chunks; narrow the area.")
   );
   private static final String[] RATE_SUGGESTIONS = new String[]{"20", "reset"};
   private static final String[] STEP_SUGGESTIONS = new String[]{"1t", "1s"};
   private static final String[] SPRINT_SUGGESTIONS = new String[]{"60s", "1d", "3d"};

   private ApoliTickCommand() {
   }

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      dispatcher.register(branches("tick"));
      dispatcher.register(branches("apoli:tick"));
   }

   private static LiteralArgumentBuilder<class_2168> branches(String name) {
      return (LiteralArgumentBuilder<class_2168>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247(name)
               .requires(ApoliPermissions.require("apoli.command.tick", 2)))
            .then(class_2170.method_9247("entity").then(entityTargets())))
         .then(class_2170.method_9247("chunk").then(chunkTargets()));
   }

   private static ArgumentBuilder<class_2168, ?> entityTargets() {
      RequiredArgumentBuilder<class_2168, class_2300> entities = class_2170.method_9244("entities", class_2186.method_9306());
      attach(entities, ApoliTickCommand::entityTargets0);
      return entities;
   }

   private static ArgumentBuilder<class_2168, ?> chunkTargets() {
      RequiredArgumentBuilder<class_2168, class_2267> from = class_2170.method_9244("from", class_2264.method_9701());
      attach(from, ApoliTickCommand::chunkTargetsFrom);
      RequiredArgumentBuilder<class_2168, class_2267> to = class_2170.method_9244("to", class_2264.method_9701());
      attach(to, ApoliTickCommand::chunkTargetsTo);
      from.then(to);
      RequiredArgumentBuilder<class_2168, Float> radius = class_2170.method_9244("radius", FloatArgumentType.floatArg(0.0F));
      attach(radius, ApoliTickCommand::chunkTargetsRadius);
      from.then(class_2170.method_9247("radius").then(radius));
      return from;
   }

   private static void attach(ArgumentBuilder<class_2168, ?> builder, ApoliTickCommand.Resolver resolver) {
      builder.then(class_2170.method_9247("query").executes(ctx -> run(ctx, resolver, ApoliTickCommand::query)));
      builder.then(
         ((LiteralArgumentBuilder)class_2170.method_9247("rate")
               .then(class_2170.method_9247("reset").executes(ctx -> run(ctx, resolver, ApoliTickCommand::reset))))
            .then(
               class_2170.method_9244("rate", FloatArgumentType.floatArg(1.0F, 10000.0F))
                  .suggests((ctx, sb) -> class_2172.method_9253(RATE_SUGGESTIONS, sb))
                  .executes(ctx -> {
                     int rate = Math.round(FloatArgumentType.getFloat(ctx, "rate"));
                     return run(ctx, resolver, (source, targets) -> rate(source, targets, rate));
                  })
            )
      );
      builder.then(class_2170.method_9247("freeze").executes(ctx -> run(ctx, resolver, (source, targets) -> freeze(source, targets, true))));
      builder.then(class_2170.method_9247("unfreeze").executes(ctx -> run(ctx, resolver, (source, targets) -> freeze(source, targets, false))));
      builder.then(
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)class_2170.method_9247("step")
                  .executes(ctx -> run(ctx, resolver, (source, targets) -> step(source, targets, 1))))
               .then(class_2170.method_9247("stop").executes(ctx -> run(ctx, resolver, (source, targets) -> step(source, targets, 0)))))
            .then(
               class_2170.method_9244("time", class_2245.method_48287(1)).suggests((ctx, sb) -> class_2172.method_9253(STEP_SUGGESTIONS, sb)).executes(ctx -> {
                  int time = IntegerArgumentType.getInteger(ctx, "time");
                  return run(ctx, resolver, (source, targets) -> step(source, targets, time));
               })
            )
      );
      builder.then(
         ((LiteralArgumentBuilder)class_2170.method_9247("sprint")
               .then(class_2170.method_9247("stop").executes(ctx -> run(ctx, resolver, (source, targets) -> sprint(source, targets, 0)))))
            .then(
               class_2170.method_9244("time", class_2245.method_48287(1))
                  .suggests((ctx, sb) -> class_2172.method_9253(SPRINT_SUGGESTIONS, sb))
                  .executes(ctx -> {
                     int time = IntegerArgumentType.getInteger(ctx, "time");
                     return run(ctx, resolver, (source, targets) -> sprint(source, targets, time));
                  })
            )
      );
   }

   private static int run(CommandContext<class_2168> ctx, ApoliTickCommand.Resolver resolver, ApoliTickCommand.Op op) throws CommandSyntaxException {
      ApoliTickCommand.Targets targets = resolver.resolve(ctx);
      if (targets.size() == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No targets matched."));
         return 0;
      } else {
         return op.apply((class_2168)ctx.getSource(), targets);
      }
   }

   private static ApoliTickCommand.Targets entityTargets0(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      Collection<? extends class_1297> entities = class_2186.method_9317(ctx, "entities");
      return new ApoliTickCommand.Targets(List.copyOf(entities), null, ((class_2168)ctx.getSource()).method_9225().method_27983());
   }

   private static ApoliTickCommand.Targets chunkTargetsFrom(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2265 from = class_2264.method_9702(ctx, "from");
      LongArrayList chunks = new LongArrayList(1);
      chunks.add(from.method_34873().method_8324());
      return new ApoliTickCommand.Targets(null, chunks, ((class_2168)ctx.getSource()).method_9225().method_27983());
   }

   private static ApoliTickCommand.Targets chunkTargetsTo(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2265 from = class_2264.method_9702(ctx, "from");
      class_2265 to = class_2264.method_9702(ctx, "to");
      class_1923 a = from.method_34873();
      class_1923 b = to.method_34873();
      int minX = Math.min(a.field_9181, b.field_9181);
      int maxX = Math.max(a.field_9181, b.field_9181);
      int minZ = Math.min(a.field_9180, b.field_9180);
      int maxZ = Math.max(a.field_9180, b.field_9180);
      long total = (long)(maxX - minX + 1) * (maxZ - minZ + 1);
      if (total > 4096L) {
         throw TOO_MANY_CHUNKS.create();
      }

      LongArrayList chunks = new LongArrayList((int)total);

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            chunks.add(class_1923.method_8331(x, z));
         }
      }

      return new ApoliTickCommand.Targets(null, chunks, ((class_2168)ctx.getSource()).method_9225().method_27983());
   }

   private static ApoliTickCommand.Targets chunkTargetsRadius(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2265 centre = class_2264.method_9702(ctx, "from");
      float radius = FloatArgumentType.getFloat(ctx, "radius");
      int minX = class_4076.method_18675((int)Math.floor(centre.comp_638() - radius));
      int maxX = class_4076.method_18675((int)Math.ceil(centre.comp_638() + radius));
      int minZ = class_4076.method_18675((int)Math.floor(centre.comp_639() - radius));
      int maxZ = class_4076.method_18675((int)Math.ceil(centre.comp_639() + radius));
      long total = (long)(maxX - minX + 1) * (maxZ - minZ + 1);
      if (total > 4096L) {
         throw TOO_MANY_CHUNKS.create();
      }

      LongArrayList chunks = new LongArrayList();
      double radiusSq = (double)radius * radius;

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            int minBlockX = x << 4;
            int minBlockZ = z << 4;
            int nearestX = Math.max(minBlockX, Math.min(centre.comp_638(), minBlockX + 15));
            int nearestZ = Math.max(minBlockZ, Math.min(centre.comp_639(), minBlockZ + 15));
            double dx = nearestX - centre.comp_638();
            double dz = nearestZ - centre.comp_639();
            if (dx * dx + dz * dz <= radiusSq) {
               chunks.add(class_1923.method_8331(x, z));
            }
         }
      }

      return new ApoliTickCommand.Targets(null, chunks, ((class_2168)ctx.getSource()).method_9225().method_27983());
   }

   private static TickState stateOf(ApoliTickCommand.Targets targets, int index) {
      return targets.entities() != null
         ? TickRates.entityState((class_1297)targets.entities().get(index))
         : TickRates.chunkState(targets.dimension(), targets.chunks().getLong(index));
   }

   private static TickState peek(ApoliTickCommand.Targets targets, int index) {
      return targets.entities() != null
         ? TickRates.peekEntity((class_1297)targets.entities().get(index))
         : TickRates.peekChunk(targets.dimension(), targets.chunks().getLong(index));
   }

   private static int rate(class_2168 source, ApoliTickCommand.Targets targets, int rate) {
      int base = TickRateVanilla.rate(source.method_9211());
      int i = 0;

      for (int n = targets.size(); i < n; i++) {
         TickState state = stateOf(targets, i);
         state.setRate(rate);
         state.setExpiry(-1L);
      }

      int count = targets.size();
      String noun = targets.noun();
      source.method_9226(() -> class_2561.method_43470("Set the tick rate of " + count + " " + noun + " to " + rate + " per second."), true);
      if (rate > base) {
         source.method_9226(
            () -> class_2561.method_43470(
               "Note: the server itself runs at " + base + " per second, so anything above that ticks normally."
            ),
            false
         );
      }

      return count;
   }

   private static int reset(class_2168 source, ApoliTickCommand.Targets targets) {
      int i = 0;

      for (int n = targets.size(); i < n; i++) {
         if (targets.entities() != null) {
            TickRates.clearEntity((class_1297)targets.entities().get(i));
         } else {
            TickRates.clearChunk(targets.dimension(), targets.chunks().getLong(i));
         }
      }

      int count = targets.size();
      String noun = targets.noun();
      source.method_9226(() -> class_2561.method_43470("Reset the tick rate of " + count + " " + noun + "."), true);
      return count;
   }

   private static int freeze(class_2168 source, ApoliTickCommand.Targets targets, boolean frozen) {
      int i = 0;

      for (int n = targets.size(); i < n; i++) {
         TickState state = stateOf(targets, i);
         state.setFrozen(frozen);
         state.setExpiry(-1L);
         if (!frozen && state.isDefault()) {
            if (targets.entities() != null) {
               TickRates.clearEntity((class_1297)targets.entities().get(i));
            } else {
               TickRates.clearChunk(targets.dimension(), targets.chunks().getLong(i));
            }
         }
      }

      int count = targets.size();
      String noun = targets.noun();
      source.method_9226(() -> class_2561.method_43470(count + " " + noun + (frozen ? " have been frozen." : " have been unfrozen.")), true);
      return count;
   }

   private static int step(class_2168 source, ApoliTickCommand.Targets targets, int ticks) {
      int i = 0;

      for (int n = targets.size(); i < n; i++) {
         TickState state = peek(targets, i);
         if (state == null || !state.frozen() || state.sprintTicks() > 0) {
            source.method_9213(class_2561.method_43470("Every target must be frozen and not sprinting before it can step."));
            return 0;
         }
      }

      i = 0;

      for (int n = targets.size(); i < n; i++) {
         stateOf(targets, i).setStepTicks(ticks);
      }

      int count = targets.size();
      String noun = targets.noun();
      source.method_9226(
         () -> class_2561.method_43470(ticks > 0 ? count + " " + noun + " will step " + ticks + " ticks." : count + " " + noun + " have stopped stepping."), true
      );
      return count;
   }

   private static int sprint(class_2168 source, ApoliTickCommand.Targets targets, int ticks) {
      int i = 0;

      for (int n = targets.size(); i < n; i++) {
         TickState state = peek(targets, i);
         if (state != null && state.stepTicks() > 0) {
            source.method_9213(class_2561.method_43470("A target that is stepping cannot sprint."));
            return 0;
         }
      }

      i = 0;

      for (int n = targets.size(); i < n; i++) {
         TickState state = stateOf(targets, i);
         state.setSprintTicks(ticks);
         state.setExpiry(-1L);
      }

      int count = targets.size();
      String noun = targets.noun();
      source.method_9226(
         () -> class_2561.method_43470(ticks > 0 ? count + " " + noun + " will sprint for " + ticks + " ticks." : count + " " + noun + " have stopped sprinting."),
         true
      );
      return count;
   }

   private static int query(class_2168 source, ApoliTickCommand.Targets targets) {
      int base = TickRateVanilla.rate(source.method_9211());
      StringBuilder sb = new StringBuilder("The tick rates of the specified ").append(targets.noun()).append(":");
      int first = base;
      int i = 0;

      for (int n = targets.size(); i < n; i++) {
         int rate;
         String label;
         if (targets.entities() != null) {
            class_1297 entity = (class_1297)targets.entities().get(i);
            rate = TickRates.effectiveRate(entity, TickScope.ENTITY);
            label = entity.method_5864().method_5897().getString() + " " + entity.method_5820();
         } else {
            long pos = targets.chunks().getLong(i);
            class_3218 level = source.method_9225();
            rate = TickRates.effectiveChunkRate(level, pos, base);
            label = "Chunk " + new class_1923(pos);
         }

         if (i == 0) {
            first = rate;
         }

         sb.append('\n').append(label).append(" - ").append(rate == 0 ? "frozen" : rate + " per second");
      }

      String text = sb.toString();
      source.method_9226(() -> class_2561.method_43470(text), false);
      return first;
   }

   private interface Op {
      int apply(class_2168 var1, ApoliTickCommand.Targets var2);
   }

   private interface Resolver {
      ApoliTickCommand.Targets resolve(CommandContext<class_2168> var1) throws CommandSyntaxException;
   }

   private record Targets(List<? extends class_1297> entities, LongArrayList chunks, class_5321<class_1937> dimension) {
      int size() {
         return this.entities != null ? this.entities.size() : this.chunks.size();
      }

      String noun() {
         return this.entities != null ? (this.size() == 1 ? "entity" : "entities") : (this.size() == 1 ? "chunk" : "chunks");
      }
   }
}
