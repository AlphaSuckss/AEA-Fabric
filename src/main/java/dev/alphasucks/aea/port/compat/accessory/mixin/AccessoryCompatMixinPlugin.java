package dev.alphasucks.aea.port.compat.accessory.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class AccessoryCompatMixinPlugin implements IMixinConfigPlugin {
   private static final boolean TRINKETS_PRESENT = classExists("dev.emi.trinkets.api.TrinketInventory");

   private static boolean classExists(String name) {
      String path = name.replace('.', '/') + ".class";
      return AccessoryCompatMixinPlugin.class.getClassLoader().getResource(path) != null;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return mixinClassName.contains(".trinkets.") ? TRINKETS_PRESENT : true;
   }

   public void onLoad(String mixinPackage) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
