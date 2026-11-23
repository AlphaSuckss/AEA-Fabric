package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.data.Space;
import dev.alphasucks.aea.port.data.Vector;
import dev.alphasucks.aea.port.entity.ApoliEntities;
import dev.alphasucks.aea.port.entity.summon.MinionEntity;
import dev.alphasucks.aea.port.entity.summon.Summons;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3218;

public final class SummonMinionAction implements ActionType<EntityCtx, SummonMinionAction.Cfg> {
   @Override
   public MapCodec<SummonMinionAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               IdCodecs.ID.optionalFieldOf("texture").forGetter(SummonMinionAction.Cfg::texture),
               Codec.BOOL.optionalFieldOf("follow_owner", false).forGetter(SummonMinionAction.Cfg::followOwner),
               Vector.CODEC.optionalFieldOf("follow_offset").forGetter(SummonMinionAction.Cfg::followOffset),
               Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(SummonMinionAction.Cfg::scale),
               Codec.BOOL.optionalFieldOf("invulnerable", false).forGetter(SummonMinionAction.Cfg::invulnerable),
               Codec.INT.optionalFieldOf("max_life_ticks", 1200).forGetter(SummonMinionAction.Cfg::maxLifeTicks),
               IdCodecs.ID.optionalFieldOf("summon_id").forGetter(SummonMinionAction.Cfg::summonId),
               IdCodecs.ID.listOf().optionalFieldOf("powers", List.of()).forGetter(SummonMinionAction.Cfg::powers),
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(SummonMinionAction.Cfg::bientityAction),
               Vector.CODEC.optionalFieldOf("offset", Vector.ZERO).forGetter(SummonMinionAction.Cfg::offset),
               Space.CODEC.optionalFieldOf("space", Space.WORLD).forGetter(SummonMinionAction.Cfg::space)
            )
            .apply(i, SummonMinionAction.Cfg::new)
      );
   }

   public void run(SummonMinionAction.Cfg cfg, EntityCtx ctx) {
      class_1309 owner = ctx.living();
      if (owner != null && ctx.level() instanceof class_3218 level) {
         MinionEntity minion = ApoliEntities.minionType().method_5883(level);
         if (minion != null) {
            minion.setOwner(owner);
            cfg.texture.ifPresent(minion::setTexture);
            minion.setScale(cfg.scale);
            minion.method_5684(cfg.invulnerable);
            minion.setMaxLifeTime(cfg.maxLifeTicks);
            cfg.summonId.ifPresent(minion::setSummonId);
            class_243 pos = cfg.space.position(owner, cfg.offset);
            if (cfg.followOffset.isPresent()) {
               Vector offset = (Vector)cfg.followOffset.get();
               if (cfg.followOwner) {
                  minion.setFollowOffset(offset.x(), offset.y(), offset.z());
               }

               pos = pos.method_1031(offset.x(), offset.y(), offset.z());
            }

            minion.setFollowingOwner(cfg.followOwner);
            minion.method_5808(pos.field_1352, pos.field_1351, pos.field_1350, owner.method_5791(), owner.method_36455());
            level.method_8649(minion);
            Summons.grantPowers(minion, cfg.powers);
            cfg.bientityAction.ifPresent(action -> action.run(new BiEntityCtx(owner, minion, level)));
            Summons.resendIfMoved(minion, pos);
         }
      }
   }

   public record Cfg(
      Optional<class_2960> texture,
      boolean followOwner,
      Optional<Vector> followOffset,
      float scale,
      boolean invulnerable,
      int maxLifeTicks,
      Optional<class_2960> summonId,
      List<class_2960> powers,
      Optional<BiEntityAction> bientityAction,
      Vector offset,
      Space space
   ) {
   }
}
