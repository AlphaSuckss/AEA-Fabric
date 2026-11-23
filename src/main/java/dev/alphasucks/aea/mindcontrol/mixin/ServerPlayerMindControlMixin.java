package dev.alphasucks.aea.mindcontrol.mixin;

import dev.alphasucks.aea.mindcontrol.MindControlReturn;
import net.minecraft.class_2487;
import net.minecraft.class_3222;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps where a controller's body stood in their player data, so a crash or disconnect puts them back. */
@Mixin(class_3222.class)
public abstract class ServerPlayerMindControlMixin implements MindControlReturn {
    @Unique private static final String AEA$RETURN_TAG = "aea_mind_control_return";
    @Unique private class_2487 aea$mindControlReturn;

    @Override
    public class_2487 aea$mindControlReturn() {
        return aea$mindControlReturn;
    }

    @Override
    public void aea$setMindControlReturn(class_2487 tag) {
        aea$mindControlReturn = tag;
    }

    @Inject(method = "method_5652", at = @At("TAIL"))
    private void aea$writeReturn(class_2487 nbt, CallbackInfo ci) {
        if (aea$mindControlReturn != null) nbt.method_10566(AEA$RETURN_TAG, aea$mindControlReturn.method_10553());
    }

    @Inject(method = "method_5749", at = @At("TAIL"))
    private void aea$readReturn(class_2487 nbt, CallbackInfo ci) {
        class_2487 tag = nbt.method_10562(AEA$RETURN_TAG);
        aea$mindControlReturn = tag.method_33133() ? null : tag;
    }
}
