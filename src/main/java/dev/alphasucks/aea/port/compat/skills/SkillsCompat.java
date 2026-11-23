package dev.alphasucks.aea.port.compat.skills;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.power.PowerContainer;
import java.util.List;
import java.util.UUID;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.puffish.skillsmod.api.SkillsAPI;

public final class SkillsCompat {
   private static final String SOURCE_PREFIX = "skills_reward/";

   private SkillsCompat() {
   }

   public static void init() {
      PowerReward.register();
      ModifyResourceReward.register();
   }

   static class_2960 newSource() {
      return Apoli.id("skills_reward/" + UUID.randomUUID());
   }

   static boolean isRewardSource(class_2960 source) {
      return "apoli".equals(source.method_12836()) && source.method_12832().startsWith("skills_reward/");
   }

   public static boolean hasCategory(class_2960 category) {
      return SkillsAPI.getCategory(category).isPresent();
   }

   public static void unlockCategory(class_3222 player, class_2960 category) {
      player.field_13995.execute(() -> SkillsAPI.getCategory(category).ifPresent(c -> c.unlock(player)));
   }

   public static void lockCategory(class_3222 player, class_2960 category) {
      player.field_13995.execute(() -> SkillsAPI.getCategory(category).ifPresent(c -> c.lock(player)));
   }

   public static void onJoin(class_3222 player) {
      PowerContainer container = PowerContainer.of(player);
      if (container != null) {
         for (class_2960 power : List.copyOf(container.allPowers())) {
            for (class_2960 source : container.sourcesOf(power)) {
               if (isRewardSource(source)) {
                  container.removePower(power, source);
               }
            }
         }
      }

      SkillsAPI.updateRewards(player, PowerReward.ID);
      SkillsAPI.updateRewards(player, ModifyResourceReward.ID);
   }
}
