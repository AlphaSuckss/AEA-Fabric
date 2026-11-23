package dev.alphasucks.aea.port.power.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.Shape;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.dev.DevParticles;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.tick.TickRateVanilla;
import dev.alphasucks.aea.port.tick.TickRates;
import dev.alphasucks.aea.port.tick.TickScope;
import dev.alphasucks.aea.port.tick.TickState;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.class_1297;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3542;
import net.minecraft.server.MinecraftServer;

public final class ModifyTickRatePower extends PowerType<ModifyTickRatePower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_tick_rate");

   @Override
   public MapCodec<ModifyTickRatePower.Config> configCodec() {
      return RecordCodecBuilder.<ModifyTickRatePower.Config>mapCodec(
            i -> i.group(
                  ModifyTickRatePower.Target.CODEC.optionalFieldOf("target", ModifyTickRatePower.Target.SELF).forGetter(ModifyTickRatePower.Config::target),
                  Expression.INT_OR_EXPR.optionalFieldOf("rate").forGetter(ModifyTickRatePower.Config::rate),
                  Codec.BOOL.optionalFieldOf("frozen", false).forGetter(ModifyTickRatePower.Config::frozen),
                  Vector.SCALAR_OR_VECTOR.optionalFieldOf("radius", Vector.uniform(16.0F)).forGetter(ModifyTickRatePower.Config::radius),
                  Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(ModifyTickRatePower.Config::shape),
                  Shape.HOLLOW_THICKNESS.forGetter(ModifyTickRatePower.Config::hollowThickness),
                  LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ModifyTickRatePower.Config::bientityCondition),
                  Codec.BOOL.optionalFieldOf("include_self").forGetter(ModifyTickRatePower.Config::includeSelf),
                  Codec.BOOL.optionalFieldOf("affect_chunks", false).forGetter(ModifyTickRatePower.Config::affectChunks),
                  Codec.INT.optionalFieldOf("interval", 1).forGetter(ModifyTickRatePower.Config::interval)
               )
               .apply(i, ModifyTickRatePower.Config::new)
         )
         .xmap(ModifyTickRatePower::reportIgnoredFilter, Function.identity());
   }

   private static ModifyTickRatePower.Config reportIgnoredFilter(ModifyTickRatePower.Config cfg) {
      if (cfg.bientityCondition.isPresent() && cfg.target != ModifyTickRatePower.Target.AREA) {
         String where = LoggedOptionalField.context();
         Apoli.LOGGER
            .warn(
               "[Apoli] apoli:modify_tick_rate{} sets 'bientity_condition' together with \"target\": \"{}\" — the filter only picks which entities an \"area\" affects, so it does nothing here and every entity in scope is affected.",
               where == null ? "" : " of " + where,
               cfg.target.method_15434()
            );
      }

      return cfg;
   }

   @Override
   public boolean ticksNonLivingEntities() {
      return true;
   }

   public void tick(class_2960 powerId, ModifyTickRatePower.Config cfg, PowerContainer holder) {
      class_1297 owner = holder.rawOwner();
      if (owner.method_37908() instanceof class_3218 level) {
         MinecraftServer server = level.method_8503();
         if (server != null) {
            if (conditionHolds(powerId, owner, level)) {
               int interval = Math.max(1, cfg.interval);
               if (interval <= 1 || owner.field_6012 % interval == Math.floorMod(powerId.hashCode(), interval)) {
                  long expiry = server.method_3780() + interval + 2L;
                  switch (cfg.target) {
                     case SELF:
                        this.write(TickRates.stateFor(owner, TickScope.ENTITY), cfg, owner, expiry);
                        break;
                     case CHUNK:
                        this.write(TickRates.chunkState(level.method_27983(), owner.method_31476().method_8324()), cfg, owner, expiry);
                        break;
                     case DIMENSION:
                        this.write(TickRates.dimensionState(level.method_27983()), cfg, owner, expiry);
                        break;
                     case SERVER:
                        TickRates.requestServer(
                           server,
                           (Float)cfg.rate.map(expression -> (float)expression.eval(owner)).orElse(TickRateVanilla.exactRate(server)),
                           cfg.frozen,
                           interval + 2
                        );
                        break;
                     case AREA:
                        this.applyArea(cfg, owner, level, expiry);
                  }

                  if (exemptsOwner(cfg)) {
                     TickRates.exempt(owner, expiry);
                  }
               }
            }
         }
      }
   }

   private static boolean exemptsOwner(ModifyTickRatePower.Config cfg) {
      return switch (cfg.target) {
         case SELF, SERVER -> false;
         case CHUNK, DIMENSION -> cfg.includeSelf.isPresent() && !cfg.includeSelf.get();
         case AREA -> cfg.affectChunks && !cfg.includeSelf.orElse(false);
      };
   }

   private void applyArea(ModifyTickRatePower.Config cfg, class_1297 owner, class_3218 level, long expiry) {
      class_243 origin = owner.method_19538();
      int rx = (int)Math.ceil(cfg.radius.x());
      int ry = (int)Math.ceil(cfg.radius.y());
      int rz = (int)Math.ceil(cfg.radius.z());
      int boxHalf = Math.max(rx, Math.max(ry, rz));
      class_2338 center = class_2338.method_49638(origin);
      class_238 box = class_238.method_30048(origin, boxHalf * 2.0, boxHalf * 2.0, boxHalf * 2.0);
      if (DevParticles.due(level)) {
         DevParticles.outlineShape(level, origin, cfg.shape, cfg.radius.x(), cfg.radius.y(), cfg.radius.z());
      }

      double thickness = cfg.shape.thickness(cfg.hollowThickness, owner);
      List<class_1297> nearby = level.method_8333(
         (class_1297)null,
         box,
         target -> {
            if (target == owner) {
               return (Boolean)cfg.includeSelf.orElse(false);
            }

            class_2338 tp = target.method_24515();
            return cfg.shape
               .contains(
                  tp.method_10263() - center.method_10263(),
                  tp.method_10264() - center.method_10264(),
                  tp.method_10260() - center.method_10260(),
                  rx,
                  ry,
                  rz,
                  thickness
               );
         }
      );
      boolean filtered = cfg.bientityCondition.isPresent();

      for (int i = 0; i < nearby.size(); i++) {
         class_1297 target = (class_1297)nearby.get(i);
         if (!filtered || target == owner || ((BiEntityCondition)cfg.bientityCondition.get()).test(BiEntityCtx.of(owner, target, level))) {
            this.write(TickRates.stateFor(target, TickScope.ENTITY), cfg, owner, expiry);
         } else if (cfg.affectChunks) {
            TickRates.exempt(target, expiry);
         }
      }

      if (cfg.affectChunks) {
         int minX = center.method_10263() - rx;
         int maxX = center.method_10263() + rx;
         int minZ = center.method_10260() - rz;
         int maxZ = center.method_10260() + rz;

         for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
               this.write(TickRates.chunkState(level.method_27983(), class_1923.method_8331(cx, cz)), cfg, owner, expiry);
            }
         }
      }
   }

   private void write(TickState state, ModifyTickRatePower.Config cfg, class_1297 owner, long expiry) {
      state.setRate((Integer)cfg.rate.map(expression -> Math.max(0, expression.evalInt(owner))).orElse(-1));
      state.setFrozen(cfg.frozen);
      state.setExpiry(expiry);
   }

   private static boolean conditionHolds(class_2960 powerId, class_1297 owner, class_3218 level) {
      Power power = ApoliPowers.get(powerId);
      return power != null && !power.condition().isEmpty() ? ((EntityCondition)power.condition().get()).test(new EntityCtx(owner, level)) : true;
   }

   public record Config(
      ModifyTickRatePower.Target target,
      Optional<Expression> rate,
      boolean frozen,
      Vector radius,
      Shape shape,
      Expression hollowThickness,
      Optional<BiEntityCondition> bientityCondition,
      Optional<Boolean> includeSelf,
      boolean affectChunks,
      int interval
   ) {
   }

   public enum Target implements class_3542 {
      SELF("self"),
      CHUNK("chunk"),
      DIMENSION("dimension"),
      SERVER("server"),
      AREA("area");

      public static final Codec<ModifyTickRatePower.Target> CODEC = class_3542.method_28140(ModifyTickRatePower.Target::values);
      private final String name;

      Target(String name) {
         this.name = name;
      }

      @Override
      public String method_15434() {
         return this.name;
      }
   }
}
