package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.rope.RopeAnchor;
import dev.alphasucks.aea.port.rope.RopeEndpointSource;
import dev.alphasucks.aea.port.rope.RopeManager;
import dev.alphasucks.aea.port.rope.RopeParams;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1937;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import org.jetbrains.annotations.Nullable;

public final class AttachRopeAction implements ActionType<EntityCtx, AttachRopeAction.Cfg> {
   public static final MapCodec<AttachRopeAction.Cfg> CODEC = RecordCodecBuilder.mapCodec(
      i -> i.group(
            RopeEndpointSource.CODEC.codec().optionalFieldOf("from", RopeEndpointSource.self()).forGetter(AttachRopeAction.Cfg::from),
            RopeEndpointSource.CODEC.codec().optionalFieldOf("to", RopeEndpointSource.raycastBlock(30.0F)).forGetter(AttachRopeAction.Cfg::to),
            Codec.STRING.optionalFieldOf("slot").forGetter(AttachRopeAction.Cfg::slot),
            IdCodecs.ID.optionalFieldOf("texture", Apoli.id("textures/rope/rope.png")).forGetter(AttachRopeAction.Cfg::texture),
            Codec.BOOL.optionalFieldOf("toggle", true).forGetter(AttachRopeAction.Cfg::toggle),
            RopeParams.CODEC.forGetter(AttachRopeAction.Cfg::params)
         )
         .apply(i, AttachRopeAction.Cfg::new)
   );

   @Override
   public MapCodec<AttachRopeAction.Cfg> codec() {
      return CODEC;
   }

   public void run(AttachRopeAction.Cfg cfg, EntityCtx ctx) {
      apply(cfg, ctx.raw(), null, ctx.level());
   }

   static void apply(AttachRopeAction.Cfg cfg, @Nullable class_1297 actor, @Nullable class_1297 target, class_1937 level) {
      if (actor != null && level instanceof class_3218 serverLevel) {
         if (!cfg.toggle || !cfg.slot.isEmpty() || !RopeManager.detachControllable(actor.method_5667())) {
            RopeAnchor from = cfg.from.resolve(actor, target, level);
            RopeAnchor to = cfg.to.resolve(actor, target, level);
            if (from != null && to != null) {
               RopeManager.create(serverLevel, from, to, (String)cfg.slot.orElse(null), actor.method_5667(), cfg.params, cfg.texture);
            }
         }
      }
   }

   public static final class BiEntity implements ActionType<BiEntityCtx, AttachRopeAction.Cfg> {
      @Override
      public MapCodec<AttachRopeAction.Cfg> codec() {
         return AttachRopeAction.CODEC;
      }

      public void run(AttachRopeAction.Cfg cfg, BiEntityCtx ctx) {
         AttachRopeAction.apply(cfg, ctx.actor(), ctx.target(), ctx.level());
      }
   }

   public record Cfg(RopeEndpointSource from, RopeEndpointSource to, Optional<String> slot, class_2960 texture, boolean toggle, RopeParams params) {
   }
}
