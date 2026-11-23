package dev.alphasucks.aea.port.power.builtin;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.data.CameraAnimation;
import dev.alphasucks.aea.port.data.CameraKeyframe;
import dev.alphasucks.aea.port.data.CameraLook;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.power.PowerType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_1297;
import net.minecraft.class_2300;
import net.minecraft.class_2303;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

public final class ModifyCameraPower extends PowerType<ModifyCameraPower.Config> {
   public static final class_2960 CANONICAL = Apoli.id("modify_camera");
   private static final int RETARGET_TICKS = 20;
   private static final double TRACK_RANGE_SQ = 65536.0;
   private static final Map<String, Optional<class_2300>> SELECTORS = new ConcurrentHashMap();
   private static final MapCodec<ModifyCameraPower.Config> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            ModifyCameraPower.Transform.MAP_CODEC.forGetter(ModifyCameraPower.Config::transform),
            CameraLook.CODEC.optionalFieldOf("look", CameraLook.ANCHOR).forGetter(ModifyCameraPower.Config::look),
            Codec.BOOL.optionalFieldOf("follow", true).forGetter(ModifyCameraPower.Config::follow),
            IdCodecs.ID.optionalFieldOf("set").forGetter(ModifyCameraPower.Config::set),
            Codec.STRING.optionalFieldOf("selector").forGetter(ModifyCameraPower.Config::selector),
            Codec.BOOL.optionalFieldOf("collision", true).forGetter(ModifyCameraPower.Config::collision),
            LoggedOptionalField.of("keyframes", CameraKeyframe.CODEC.listOf(), List.of()).forGetter(ModifyCameraPower.Config::keyframes),
            Codec.BOOL.optionalFieldOf("loop", false).forGetter(ModifyCameraPower.Config::loop),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(ModifyCameraPower.Config::priority)
         )
         .apply(i, ModifyCameraPower.Config::new)
   );

   @Override
   public MapCodec<ModifyCameraPower.Config> configCodec() {
      return CODEC;
   }

   @Override
   public boolean resolvesForClient() {
      return true;
   }

   public int clientTarget(class_2960 powerId, ModifyCameraPower.Config cfg, class_3222 player, int current) {
      if (!cfg.targetsOther()) {
         return -1;
      }

      if (current >= 0 && player.field_13995.method_3780() % 20 != 0) {
         class_1297 existing = player.method_37908().method_8469(current);
         if (valid(player, existing)) {
            return current;
         }
      }

      class_1297 found = cfg.set().isPresent() ? nearestMember(player, (class_2960)cfg.set().get()) : null;
      if (found == null && cfg.selector().isPresent()) {
         found = firstSelected(player, (String)cfg.selector().get());
      }

      return found == null ? -1 : found.method_5628();
   }

   private static boolean valid(class_3222 player, @Nullable class_1297 entity) {
      return entity != null && !entity.method_31481() && entity.method_37908() == player.method_37908() && entity.method_5858(player) <= 65536.0;
   }

   @Nullable
   private static class_1297 nearestMember(class_3222 player, class_2960 setId) {
      List<UUID> members = EntitySetPower.iterationOrder(player, setId, false);
      class_1297 best = null;
      double bestDistance = Double.MAX_VALUE;
      int i = 0;

      for (int n = members.size(); i < n; i++) {
         class_1297 member = EntitySetPower.resolveEntity(player.field_13995, (UUID)members.get(i));
         if (valid(player, member) && member != player) {
            double distance = member.method_5858(player);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = member;
            }
         }
      }

      return best;
   }

   @Nullable
   private static class_1297 firstSelected(class_3222 player, String selector) {
      Optional<class_2300> parsed = (Optional<class_2300>)SELECTORS.computeIfAbsent(selector, ModifyCameraPower::parse);
      if (parsed.isEmpty()) {
         return null;
      }

      try {
         List<? extends class_1297> found = ((class_2300)parsed.get()).method_9816(player.method_5671().method_9206(2));
         int i = 0;

         for (int n = found.size(); i < n; i++) {
            class_1297 entity = (class_1297)found.get(i);
            if (entity != player && valid(player, entity)) {
               return entity;
            }
         }

         return null;
      } catch (Exception ignored) {
         return null;
      }
   }

   private static Optional<class_2300> parse(String selector) {
      try {
         return Optional.of(new class_2303(new StringReader(selector), true).method_9882());
      } catch (Exception e) {
         Apoli.LOGGER.warn("[Apoli] apoli:modify_camera could not parse selector '{}': {}", selector, e.getMessage());
         return Optional.empty();
      }
   }

   public record Config(
      ModifyCameraPower.Transform transform,
      CameraLook look,
      boolean follow,
      Optional<class_2960> set,
      Optional<String> selector,
      boolean collision,
      List<CameraKeyframe> keyframes,
      boolean loop,
      int priority,
      CameraAnimation animation
   ) {
      Config(
         ModifyCameraPower.Transform transform,
         CameraLook look,
         boolean follow,
         Optional<class_2960> set,
         Optional<String> selector,
         boolean collision,
         List<CameraKeyframe> keyframes,
         boolean loop,
         int priority
      ) {
         this(transform, look, follow, set, selector, collision, keyframes, loop, priority, CameraAnimation.of(keyframes, loop));
      }

      public boolean targetsOther() {
         return this.set.isPresent() || this.selector.isPresent();
      }
   }

   public record Transform(Space space, Expression x, Expression y, Expression z, Expression pitch, Expression yaw, Expression roll, Expression fov) {
      public static final MapCodec<ModifyCameraPower.Transform> MAP_CODEC = RecordCodecBuilder.mapCodec(
         i -> i.group(
               Space.CODEC.optionalFieldOf("space", Space.LOCAL).forGetter(ModifyCameraPower.Transform::space),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("x", Expression.constant(0.0)).forGetter(ModifyCameraPower.Transform::x),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("y", Expression.constant(0.0)).forGetter(ModifyCameraPower.Transform::y),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("z", Expression.constant(0.0)).forGetter(ModifyCameraPower.Transform::z),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("pitch", Expression.constant(0.0)).forGetter(ModifyCameraPower.Transform::pitch),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("yaw", Expression.constant(0.0)).forGetter(ModifyCameraPower.Transform::yaw),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("roll", Expression.constant(0.0)).forGetter(ModifyCameraPower.Transform::roll),
               Expression.FLOAT_OR_EXPR.optionalFieldOf("fov", Expression.constant(1.0)).forGetter(ModifyCameraPower.Transform::fov)
            )
            .apply(i, ModifyCameraPower.Transform::new)
      );
   }
}
