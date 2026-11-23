package dev.alphasucks.aea.compat.spell_engine.mixin;

import dev.alphasucks.aea.compat.spell_engine.SpellEngineVersion;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Applies {@code aea-spell_engine.mixins.json} only when Spell Engine 0.x is installed. Spell Engine 1.x for 1.20.1 is
 * a different API; see {@link SpellEngineVersion}.
 */
public class SpellEngineMixinPlugin implements IMixinConfigPlugin {
    private static final boolean ACTIVE = SpellEngineVersion.supported();

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return ACTIVE; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
    @Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
