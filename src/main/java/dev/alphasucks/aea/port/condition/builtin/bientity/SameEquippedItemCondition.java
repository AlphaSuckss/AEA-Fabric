package dev.alphasucks.aea.port.condition.builtin.bientity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.data.EquipmentSlot;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.class_1309;
import net.minecraft.class_1799;

public final class SameEquippedItemCondition implements ConditionType<BiEntityCtx, SameEquippedItemCondition.Cfg> {
   private static final List<EquipmentSlot> MAINHAND = List.of(EquipmentSlot.MAINHAND);

   @Override
   public MapCodec<SameEquippedItemCondition.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               SingleOrList.of(EquipmentSlot.CODEC).optionalFieldOf("equipment_slot", MAINHAND).forGetter(SameEquippedItemCondition.Cfg::equipmentSlot),
               LoggedOptionalField.strict("target_equipment_slot", SingleOrList.of(EquipmentSlot.CODEC))
                  .forGetter(SameEquippedItemCondition.Cfg::targetEquipmentSlot),
               Codec.BOOL.optionalFieldOf("compare_id", true).forGetter(SameEquippedItemCondition.Cfg::compareId),
               Codec.BOOL.optionalFieldOf("compare_count", false).forGetter(SameEquippedItemCondition.Cfg::compareCount),
               Codec.BOOL.optionalFieldOf("compare_tag", false).forGetter(SameEquippedItemCondition.Cfg::compareTag),
               Codec.BOOL.optionalFieldOf("allow_empty", false).forGetter(SameEquippedItemCondition.Cfg::allowEmpty),
               LoggedOptionalField.strict("item_condition", ItemCondition.CODEC).forGetter(SameEquippedItemCondition.Cfg::itemCondition)
            )
            .apply(i, SameEquippedItemCondition.Cfg::new)
      );
   }

   public boolean test(SameEquippedItemCondition.Cfg cfg, BiEntityCtx ctx) {
      class_1309 actor = ctx.livingActor();
      class_1309 target = ctx.livingTarget();
      if (actor != null && target != null) {
         List<EquipmentSlot> mineSlots = cfg.equipmentSlot();
         List<EquipmentSlot> theirSlots = (List<EquipmentSlot>)cfg.targetEquipmentSlot().orElse(mineSlots);

         for (int a = 0; a < mineSlots.size(); a++) {
            class_1799 mine = actor.method_6118(((EquipmentSlot)mineSlots.get(a)).vanilla());
            if (!mine.method_7960() || cfg.allowEmpty()) {
               for (int b = 0; b < theirSlots.size(); b++) {
                  class_1799 theirs = target.method_6118(((EquipmentSlot)theirSlots.get(b)).vanilla());
                  if ((!theirs.method_7960() || cfg.allowEmpty())
                     && same(cfg, mine, theirs)
                     && (!cfg.itemCondition().isPresent() || ((ItemCondition)cfg.itemCondition().get()).test(new ItemCtx(mine, ctx.level(), actor)))) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean same(SameEquippedItemCondition.Cfg cfg, class_1799 mine, class_1799 theirs) {
      if (mine.method_7960() != theirs.method_7960()) {
         return false;
      } else if (cfg.compareId() && !class_1799.method_7984(mine, theirs)) {
         return false;
      } else {
         return cfg.compareCount() && mine.method_7947() != theirs.method_7947() ? false : !cfg.compareTag() || sameData(mine, theirs);
      }
   }

   private static boolean sameData(class_1799 mine, class_1799 theirs) {
      return Objects.equals(mine.method_7969(), theirs.method_7969());
   }

   public record Cfg(
      List<EquipmentSlot> equipmentSlot,
      Optional<List<EquipmentSlot>> targetEquipmentSlot,
      boolean compareId,
      boolean compareCount,
      boolean compareTag,
      boolean allowEmpty,
      Optional<ItemCondition> itemCondition
   ) {
   }
}
