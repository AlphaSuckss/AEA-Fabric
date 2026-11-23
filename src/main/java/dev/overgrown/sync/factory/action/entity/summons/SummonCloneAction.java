package dev.overgrown.sync.factory.action.entity.summons;

import dev.overgrown.sync.Sync;
import dev.overgrown.sync.factory.action.entity.summons.entities.clone.CloneEntity;
import dev.overgrown.sync.registry.entities.SyncEntityRegistry;
import io.github.apace100.apoli.data.ApoliDataTypes;
import io.github.apace100.apoli.power.factory.action.ActionFactory;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.data.SerializableData.Instance;
import java.util.function.Consumer;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1890;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3545;
import net.minecraft.class_3730;

public class SummonCloneAction {
   public static void action(Instance data, class_1297 entity) {
      if (entity instanceof class_1657 player) {
         boolean canSit = data.getBoolean("can_sit");
         boolean canAttack = data.getBoolean("can_attack");
         boolean followOwner = data.getBoolean("follow_owner");
         boolean inheritsEquipment = data.getBoolean("inherit_equipment");
         boolean inheritsEnchantments = data.getBoolean("inherit_enchantments");
         class_2960 wideTexture = data.get("wide_texture");
         class_2960 slimTexture = data.get("slim_texture");
         Consumer<class_3545<class_1297, class_1297>> bientityAction = data.get("bientity_action");
         CloneEntity clone = summon(player, canSit, followOwner, canAttack, inheritsEquipment, inheritsEnchantments);
         if (clone != null) {
            if (wideTexture != null) {
               clone.setCustomWideTexture(wideTexture);
            }

            if (slimTexture != null) {
               clone.setCustomSlimTexture(slimTexture);
            }

            if (bientityAction != null) {
               bientityAction.accept(new class_3545<>(player, clone));
            }
         }
      } else {
         Sync.LOGGER.warn("Attempted to summon clone of invalid entity. Only Players are compatible with this action type.");
      }
   }

   private static CloneEntity summon(
      class_1657 player, boolean canSit, boolean followOwner, boolean canAttack, boolean inheritsEquipment, boolean inheritsEnchantments
   ) {
      if (!(player.method_37908() instanceof class_3218 serverWorld)) {
         return null;
      } else {
         CloneEntity clone = new CloneEntity(SyncEntityRegistry.CLONE, serverWorld);
         clone.setCanSit(canSit);
         clone.setCanAttack(canAttack);
         clone.setFollowOwner(followOwner);
         clone.method_5808(player.method_23317(), player.method_23318(), player.method_23321(), player.method_5791(), 0.0F);
         // owner first: initialize() joins the owner's team
         clone.setOwnerUUID(player.method_5667());
         clone.method_5943(serverWorld, serverWorld.method_8404(player.method_24515()), class_3730.field_16463, null, null);
         clone.method_5665(player.method_5477());
         clone.method_5952(false);
         clone.method_5971();
         serverWorld.method_8649(clone);
         if (inheritsEquipment) {
            for (class_1304 slot : class_1304.values()) {
               if (!player.method_6118(slot).method_7960()) {
                  clone.method_5946(slot, 0.0F);
                  class_1799 copy = new class_1799(player.method_6118(slot).method_7909(), player.method_6118(slot).method_7947());
                  if (inheritsEnchantments) {
                     class_1890.method_8214(class_1890.method_8222(player.method_6118(slot)), copy);
                  }

                  clone.method_5673(slot, copy);
               }
            }
         }

         clone.updateWeaponGoals();
         return clone;
      }
   }

   public static ActionFactory<class_1297> getFactory() {
      return new ActionFactory<>(
         Sync.identifier("summon_clone"),
         new SerializableData()
            .add("can_sit", SerializableDataTypes.BOOLEAN, true)
            .add("can_attack", SerializableDataTypes.BOOLEAN, true)
            .add("follow_owner", SerializableDataTypes.BOOLEAN, true)
            .add("inherit_equipment", SerializableDataTypes.BOOLEAN, true)
            .add("inherit_enchantments", SerializableDataTypes.BOOLEAN, true)
            .add("wide_texture", SerializableDataTypes.IDENTIFIER, null)
            .add("slim_texture", SerializableDataTypes.IDENTIFIER, null)
            .add("bientity_action", ApoliDataTypes.BIENTITY_ACTION, null),
         SummonCloneAction::action
      );
   }
}
