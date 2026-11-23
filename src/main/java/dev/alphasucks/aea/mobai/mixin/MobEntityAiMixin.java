package dev.alphasucks.aea.mobai.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.alphasucks.aea.mobai.MobAiState;
import dev.alphasucks.aea.mobai.MobPathingPower;
import net.minecraft.class_1308;
import net.minecraft.class_7;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(class_1308.class)
public abstract class MobEntityAiMixin implements MobAiState.Holder {
    @Unique
    private MobAiState aea$state;

    @Override
    public MobAiState aea$mobAiState() {
        MobAiState state = this.aea$state;
        if (state == null) {
            state = new MobAiState();
            this.aea$state = state;
        }
        return state;
    }

    /** aea:mob_pathing "penalties": per node type cost (-1 = never enter). */
    @ModifyReturnValue(method = "method_5944", at = @At("RETURN"))
    private float aea$nodePenalty(float original, @Local(argsOnly = true) class_7 type) {
        MobPathingPower.Config cfg = MobPathingPower.config((class_1308) (Object) this);
        if (cfg != null) {
            Float penalty = cfg.penaltyFor(type);
            if (penalty != null) {
                return penalty;
            }
        }
        return original;
    }
}
