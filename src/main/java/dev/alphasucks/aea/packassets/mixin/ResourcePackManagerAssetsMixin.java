package dev.alphasucks.aea.packassets.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.packassets.client.ClientPackAssets;
import java.util.Map;
import net.minecraft.class_310;
import net.minecraft.class_3283;
import net.minecraft.class_3288;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Offers the downloaded data pack assets to the client's resource pack list on every scan. Only the game's own
 * resource pack manager is touched; data pack managers (which share the class) are left alone.
 */
@Mixin(class_3283.class)
public abstract class ResourcePackManagerAssetsMixin {
    @ModifyReturnValue(method = "method_29212", at = @At("RETURN"))
    private Map<String, class_3288> aea$addDataPackAssets(Map<String, class_3288> original) {
        class_310 client = class_310.method_1551();
        if (client == null || client.method_1520() != (Object) this) {
            return original;
        }
        class_3288 profile = ClientPackAssets.profile();
        if (profile == null || original.containsKey(profile.method_14463())) {
            return original;
        }
        return ImmutableMap.<String, class_3288>builder().putAll(original).put(profile.method_14463(), profile).build();
    }
}
