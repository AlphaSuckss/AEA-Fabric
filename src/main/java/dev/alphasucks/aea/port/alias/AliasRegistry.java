package dev.alphasucks.aea.port.alias;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.class_2960;

public final class AliasRegistry {
   private final Map<class_2960, class_2960> typeAliases = new HashMap();

   public void registerTypeAlias(class_2960 oldId, class_2960 canonical) {
      class_2960 existing = (class_2960)this.typeAliases.putIfAbsent(oldId, canonical);
      if (existing != null && !existing.equals(canonical)) {
         throw new IllegalStateException("Conflicting type alias: " + oldId + " -> " + existing + " vs " + canonical);
      }
   }

   public class_2960 resolve(class_2960 id) {
      return (class_2960)this.typeAliases.getOrDefault(id, id);
   }

   public Map<class_2960, class_2960> view() {
      return Collections.unmodifiableMap(this.typeAliases);
   }

   public int size() {
      return this.typeAliases.size();
   }
}
