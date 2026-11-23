package dev.alphasucks.aea.port.skill;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.alphasucks.aea.port.loader.IdWildcards;
import dev.alphasucks.aea.port.loader.JsonDataFiles;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_3695;
import net.minecraft.class_4080;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public final class SkillTreeLoader extends class_4080<Map<class_2960, JsonElement>> {
   private static final Logger LOG = LogUtils.getLogger();
   private static final String DIR = "skill_trees";

   // the original class carried javac bridges from class_4080's intermediary names to prepare/apply
   @Override
   protected Map<class_2960, JsonElement> method_18789(class_3300 manager, class_3695 profiler) {
      return this.prepare(manager, profiler);
   }

   @Override
   protected void method_18788(Map<class_2960, JsonElement> data, class_3300 manager, class_3695 profiler) {
      this.apply(data, manager, profiler);
   }

   @NotNull
   protected Map<class_2960, JsonElement> prepare(class_3300 resourceManager, class_3695 profilerFiller) {
      return JsonDataFiles.load(resourceManager, "skill_trees", LOG);
   }

   protected void apply(Map<class_2960, JsonElement> data, class_3300 rm, class_3695 profiler) {
      Map<class_2960, SkillTree> trees = new HashMap();
      Map<class_2960, Skill> skills = new HashMap();

      for (Entry<class_2960, JsonElement> e : data.entrySet()) {
         class_2960 id = (class_2960)e.getKey();
         Dynamic<JsonElement> file = IdWildcards.apply(new Dynamic(JsonOps.INSTANCE, (JsonElement)e.getValue()), id);
         if (file.getMapValues().result().isEmpty()) {
            LOG.error("[Apoli] Skill tree file {} is empty or not a JSON object — skipping.", id);
         } else if (file.get("parent").result().isPresent()) {
            Dynamic<JsonElement> power = (Dynamic<JsonElement>)file.get("power").result().orElse(null);
            if (power != null && file.get("powers").result().isEmpty()) {
               file = file.remove("power").set("powers", power);
            }

            Skill.fileCodec(id)
               .codec()
               .parse(file)
               .resultOrPartial(err -> LOG.error("[Apoli] Failed to parse skill {}: {}", id, err))
               .ifPresent(skill -> skills.put(id, skill));
         } else {
            SkillTree.codec(id)
               .codec()
               .parse(file)
               .resultOrPartial(err -> LOG.error("[Apoli] Failed to parse skill tree {}: {}", id, err))
               .ifPresent(tree -> trees.put(id, tree));
         }
      }

      SkillRegistry.setTrees(trees);
      SkillRegistry.setFileSkills(skills);
      LOG.info("[Apoli] Loaded {} skill tree(s) and {} skill(s) from skill_trees files.", trees.size(), skills.size());
   }
}
