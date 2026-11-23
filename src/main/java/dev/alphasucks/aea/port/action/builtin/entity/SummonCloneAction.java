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
import dev.alphasucks.aea.port.entity.summon.CloneEntity;
import dev.alphasucks.aea.port.entity.summon.Summons;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_3218;

public final class SummonCloneAction implements ActionType<EntityCtx, SummonCloneAction.Cfg> {
   @Override
   public MapCodec<SummonCloneAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               Codec.BOOL.optionalFieldOf("can_attack", true).forGetter(SummonCloneAction.Cfg::canAttack),
               Codec.BOOL.optionalFieldOf("can_sit", true).forGetter(SummonCloneAction.Cfg::canSit),
               Codec.BOOL.optionalFieldOf("follow_owner", true).forGetter(SummonCloneAction.Cfg::followOwner),
               Codec.BOOL.optionalFieldOf("inherit_equipment", true).forGetter(SummonCloneAction.Cfg::inheritEquipment),
               Codec.BOOL.optionalFieldOf("inherit_enchantments", true).forGetter(SummonCloneAction.Cfg::inheritEnchantments),
               Codec.BOOL.optionalFieldOf("slim", false).forGetter(SummonCloneAction.Cfg::slim),
               IdCodecs.ID.optionalFieldOf("wide_texture").forGetter(SummonCloneAction.Cfg::wideTexture),
               IdCodecs.ID.optionalFieldOf("slim_texture").forGetter(SummonCloneAction.Cfg::slimTexture),
               Codec.INT.optionalFieldOf("max_life_ticks", 1200).forGetter(SummonCloneAction.Cfg::maxLifeTicks),
               IdCodecs.ID.optionalFieldOf("summon_id").forGetter(SummonCloneAction.Cfg::summonId),
               IdCodecs.ID.listOf().optionalFieldOf("powers", List.of()).forGetter(SummonCloneAction.Cfg::powers),
               LoggedOptionalField.of("bientity_action", BiEntityAction.CODEC).forGetter(SummonCloneAction.Cfg::bientityAction),
               Vector.CODEC.optionalFieldOf("offset", Vector.ZERO).forGetter(SummonCloneAction.Cfg::offset),
               Space.CODEC.optionalFieldOf("space", Space.WORLD).forGetter(SummonCloneAction.Cfg::space)
            )
            .apply(i, SummonCloneAction.Cfg::new)
      );
   }

   public void run(SummonCloneAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_1657 owner && ctx.level() instanceof class_3218 level) {
         CloneEntity clone = (CloneEntity)ApoliEntities.cloneType().method_5883(level);
         if (clone != null) {
            clone.setOwner(owner);
            clone.setCanAttack(cfg.canAttack);
            clone.setCanSit(cfg.canSit);
            clone.setFollowOwner(cfg.followOwner);
            clone.setSlim(cfg.slim);
            cfg.wideTexture.ifPresent(clone::setWideTexture);
            cfg.slimTexture.ifPresent(clone::setSlimTexture);
            clone.setMaxLifeTime(cfg.maxLifeTicks);
            cfg.summonId.ifPresent(clone::setSummonId);
            clone.method_5665(owner.method_5477());
            class_243 pos = cfg.space.position(owner, cfg.offset);
            clone.method_5808(pos.field_1352, pos.field_1351, pos.field_1350, owner.method_5791(), 0.0F);
            class_243 spawnedAt = clone.method_19538();
            if (cfg.inheritEquipment) {
               for (class_1304 slot : class_1304.values()) {
                  class_1799 equipped = owner.method_6118(slot);
                  if (!equipped.method_7960()) {
                     class_1799 copy = cfg.inheritEnchantments ? equipped.method_7972() : new class_1799(equipped.method_7909(), equipped.method_7947());
                     clone.method_5946(slot, 0.0F);
                     clone.method_5673(slot, copy);
                  }
               }
            }

            level.method_8649(clone);
            clone.updateWeaponGoals();
            Summons.grantPowers(clone, cfg.powers);
            cfg.bientityAction.ifPresent(action -> action.run(new BiEntityCtx(owner, clone, level)));
            Summons.resendIfMoved(clone, spawnedAt);
         }
      }
   }

   public record Cfg(
      boolean canAttack,
      boolean canSit,
      boolean followOwner,
      boolean inheritEquipment,
      boolean inheritEnchantments,
      boolean slim,
      Optional<class_2960> wideTexture,
      Optional<class_2960> slimTexture,
      int maxLifeTicks,
      Optional<class_2960> summonId,
      List<class_2960> powers,
      Optional<BiEntityAction> bientityAction,
      Vector offset,
      Space space
   ) {
   }
}
