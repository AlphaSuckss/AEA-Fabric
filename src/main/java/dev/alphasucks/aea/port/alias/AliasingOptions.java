package dev.alphasucks.aea.port.alias;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.class_2960;

public record AliasingOptions(List<class_2960> typeAliases, Map<String, String> fieldAliases, Map<class_2960, Map<String, String>> typeAliasDefaults) {
   public static final AliasingOptions NONE = new AliasingOptions(List.of(), Map.of(), Map.of());

   public static AliasingOptions none() {
      return NONE;
   }

   public static AliasingOptions.Builder builder() {
      return new AliasingOptions.Builder();
   }

   public static final class Builder {
      private final List<class_2960> typeAliases = new ArrayList();
      private final Map<String, String> fieldAliases = new HashMap();
      private final Map<class_2960, Map<String, String>> typeAliasDefaults = new HashMap();

      public AliasingOptions.Builder addTypeAlias(class_2960 oldId) {
         this.typeAliases.add(oldId);
         return this;
      }

      public AliasingOptions.Builder addTypeAlias(String oldId) {
         return this.addTypeAlias(class_2960.method_12829(oldId));
      }

      public AliasingOptions.Builder addTypeAlias(class_2960 oldId, Map<String, String> defaults) {
         this.typeAliases.add(oldId);
         this.typeAliasDefaults.put(oldId, Map.copyOf(defaults));
         return this;
      }

      public AliasingOptions.Builder addTypeAlias(String oldId, Map<String, String> defaults) {
         return this.addTypeAlias(class_2960.method_12829(oldId), defaults);
      }

      public AliasingOptions.Builder renameField(String oldName, String newName) {
         this.fieldAliases.put(oldName, newName);
         return this;
      }

      public AliasingOptions build() {
         return new AliasingOptions(List.copyOf(this.typeAliases), Map.copyOf(this.fieldAliases), Map.copyOf(this.typeAliasDefaults));
      }
   }
}
