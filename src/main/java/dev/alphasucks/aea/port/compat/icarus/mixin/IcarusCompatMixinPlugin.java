package dev.alphasucks.aea.port.compat.icarus.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class IcarusCompatMixinPlugin implements IMixinConfigPlugin {
   private static final boolean ICARUS_PRESENT = classExists("dev.cammiescorner.icarus.api.IcarusPlayerValues");

   private static boolean classExists(String name) {
      String path = name.replace('.', '/') + ".class";
      return IcarusCompatMixinPlugin.class.getClassLoader().getResource(path) != null;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return ICARUS_PRESENT && classExists(targetClassName);
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
