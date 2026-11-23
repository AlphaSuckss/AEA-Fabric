package dev.alphasucks.aea.port.action.builtin.entity;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.compat.skills.SkillsCompat;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.skill.SkillRegistry;
import dev.alphasucks.aea.port.skill.SkillTrees;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

public final class GrantSkillTreeAction implements ActionType<EntityCtx, GrantSkillTreeAction.Cfg> {
   @Override
   public MapCodec<GrantSkillTreeAction.Cfg> codec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(IdCodecs.ID.fieldOf("skill_tree").forGetter(GrantSkillTreeAction.Cfg::skillTree)).apply(i, GrantSkillTreeAction.Cfg::new)
      );
   }

   public void run(GrantSkillTreeAction.Cfg cfg, EntityCtx ctx) {
      if (ctx.entity() instanceof class_3222 player) {
         if (SkillRegistry.tree(cfg.skillTree()) != null) {
            SkillTrees.grantTree(player, cfg.skillTree());
         } else {
            if (ModCompat.PUFFISH_SKILLS) {
               SkillsCompat.unlockCategory(player, cfg.skillTree());
            }
         }
      }
   }

   public record Cfg(class_2960 skillTree) {
   }
}
