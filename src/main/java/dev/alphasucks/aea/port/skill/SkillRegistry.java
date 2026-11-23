package dev.alphasucks.aea.port.skill;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class SkillRegistry {
   private static final Logger LOG = LogUtils.getLogger();
   private static volatile Map<class_2960, SkillTree> trees = Map.of();
   private static volatile Map<class_2960, Skill> powerSkills = Map.of();
   private static volatile Map<class_2960, Skill> fileSkills = Map.of();
   private static volatile Map<class_2960, Skill> byId = Map.of();
   private static volatile Map<class_2960, List<class_2960>> children = Map.of();
   private static volatile List<class_2960> roots = List.of();
   private static volatile Map<class_2960, class_2960> rootCache = Map.of();
   private static volatile Map<class_2960, class_2960> orphanedSkills = Map.of();
   private static volatile Map<class_2960, class_2960> unknownPointTrees = Map.of();

   private SkillRegistry() {
   }

   public static void setTrees(Map<class_2960, SkillTree> loaded) {
      trees = Map.copyOf(loaded);
      rebuild();
   }

   public static void setPowerSkills(Map<class_2960, Skill> skills) {
      powerSkills = Map.copyOf(skills);
      rebuild();
   }

   public static void setFileSkills(Map<class_2960, Skill> skills) {
      fileSkills = Map.copyOf(skills);
      rebuild();
   }

   private static synchronized void rebuild() {
      Map<class_2960, SkillTree> treeMap = trees;
      Map<class_2960, Skill> candidates = new LinkedHashMap(fileSkills);
      candidates.putAll(powerSkills);
      Map<class_2960, class_2960> rc = new HashMap();
      Map<class_2960, class_2960> orphans = new LinkedHashMap();
      Map<class_2960, Skill> valid = new LinkedHashMap();

      for (Skill skill : candidates.values()) {
         class_2960 cur = skill.id();
         class_2960 root = null;
         int guard = 0;

         while (guard++ <= 256) {
            Skill s = (Skill)candidates.get(cur);
            if (s == null) {
               break;
            }

            class_2960 parent = s.parent();
            if (treeMap.containsKey(parent)) {
               root = parent;
               break;
            }

            if (!candidates.containsKey(parent)) {
               break;
            }

            cur = parent;
         }

         if (root == null) {
            orphans.put(skill.id(), skill.parent());
         } else {
            valid.put(skill.id(), skill);
            rc.put(skill.id(), root);
         }
      }

      Map<class_2960, List<class_2960>> kids = new HashMap();

      for (Skill skill : valid.values()) {
         ((List)kids.computeIfAbsent(skill.parent(), k -> new ArrayList())).add(skill.id());
      }

      Comparator<class_2960> byOrder = Comparator.comparingInt((class_2960 id) -> ((Skill)valid.get(id)).order()).thenComparing(class_2960::toString);

      for (List<class_2960> siblings : kids.values()) {
         siblings.sort(byOrder);
      }

      List<class_2960> rootList = new ArrayList(treeMap.keySet());
      rootList.sort(Comparator.comparingInt((class_2960 id) -> ((SkillTree)treeMap.get(id)).order()).thenComparing(class_2960::toString));
      Map<class_2960, class_2960> unknownTrees = new LinkedHashMap();

      for (Skill skill : valid.values()) {
         checkTrees(skill.id(), skill.cost().points(), treeMap, unknownTrees);
         checkTrees(skill.id(), skill.reward().points(), treeMap, unknownTrees);
      }

      orphanedSkills = Map.copyOf(orphans);
      unknownPointTrees = Map.copyOf(unknownTrees);
      byId = Map.copyOf(valid);
      children = Map.copyOf(kids);
      roots = List.copyOf(rootList);
      rootCache = Map.copyOf(rc);
   }

   private static void checkTrees(class_2960 skillId, List<SkillPoints> points, Map<class_2960, SkillTree> treeMap, Map<class_2960, class_2960> unknown) {
      for (SkillPoints entry : points) {
         if (entry.tree().isPresent() && !treeMap.containsKey(entry.tree().get())) {
            unknown.putIfAbsent(skillId, (class_2960)entry.tree().get());
         }
      }
   }

   public static void reportOrphanedSkills() {
      Map<class_2960, class_2960> orphans = orphanedSkills;

      for (Entry<class_2960, class_2960> e : orphans.entrySet()) {
         LOG.warn(
            "[Apoli] Skill {} has no skill tree at the top of its parent chain (parent '{}'); it will not appear anywhere. Point its 'parent' at a skill_trees file id or another skill.",
            e.getKey(),
            e.getValue()
         );
      }

      for (Entry<class_2960, class_2960> e : unknownPointTrees.entrySet()) {
         LOG.warn(
            "[Apoli] Skill {} uses points of skill tree '{}' in its cost or reward, but no skill tree with that id is loaded. Check the 'skill_tree' id.",
            e.getKey(),
            e.getValue()
         );
      }
   }

   @Nullable
   public static SkillTree tree(class_2960 id) {
      return (SkillTree)trees.get(id);
   }

   public static Collection<SkillTree> trees() {
      return trees.values();
   }

   @Nullable
   public static Skill get(class_2960 id) {
      return (Skill)byId.get(id);
   }

   public static Collection<Skill> all() {
      return byId.values();
   }

   public static List<class_2960> roots() {
      return roots;
   }

   public static List<class_2960> childrenOf(class_2960 id) {
      return (List<class_2960>)children.getOrDefault(id, List.of());
   }

   public static class_2960 rootOf(class_2960 id) {
      return (class_2960)rootCache.getOrDefault(id, id);
   }
}
