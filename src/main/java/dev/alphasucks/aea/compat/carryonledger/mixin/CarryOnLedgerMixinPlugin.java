package dev.alphasucks.aea.compat.carryonledger.mixin;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Applies the Carry On -> Ledger mixins only when both mods are installed. */
public class CarryOnLedgerMixinPlugin implements IMixinConfigPlugin {
    private static final boolean ACTIVE = FabricLoader.getInstance().isModLoaded("carryon") && FabricLoader.getInstance().isModLoaded("ledger");

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return ACTIVE;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
