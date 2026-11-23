package dev.alphasucks.aea.macro.mixin;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/** Function macros are built in; when the standalone Function Macro Backport is installed, AEA leaves them to it. */
public class MacroMixinPlugin implements IMixinConfigPlugin {
    private static final boolean ACTIVE = !FabricLoader.getInstance().isModLoaded("functionmacrobackport");

    @Override
    public void onLoad(String mixinPackage) {
        if (!ACTIVE) {
            LoggerFactory.getLogger("AEA").warn("[AEA] Function Macro Backport is installed: AEA's built-in function macros are off. Remove that jar to use AEA's (fixed) version.");
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return ACTIVE;
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
