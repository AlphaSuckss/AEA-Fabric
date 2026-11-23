package dev.alphasucks.aea.macro.mixin;

import com.mojang.brigadier.CommandDispatcher;
import dev.alphasucks.aea.macro.MacroCommands;
import java.util.Collection;
import net.minecraft.class_2158;
import net.minecraft.class_2168;
import net.minecraft.class_3062;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(class_3062.class)
public abstract class FunctionCommandMacroMixin {
    /** Adds the argument branches next to vanilla's /function (merged by Brigadier, vanilla's node is kept). */
    @Inject(method = "method_13380", at = @At("TAIL"))
    private static void aea$registerMacroArguments(CommandDispatcher<class_2168> dispatcher, CallbackInfo ci) {
        MacroCommands.register(dispatcher);
    }

    /** {@code /function <macro>} without arguments: report "missing arguments" (only taken over when a macro is involved). */
    @Inject(method = "method_13381", at = @At("HEAD"), cancellable = true)
    private static void aea$macroWithoutArguments(class_2168 source, Collection<class_2158> functions, CallbackInfoReturnable<Integer> cir) {
        if (MacroCommands.anyMacro(functions)) {
            cir.setReturnValue(MacroCommands.run(source, functions, null));
        }
    }
}
