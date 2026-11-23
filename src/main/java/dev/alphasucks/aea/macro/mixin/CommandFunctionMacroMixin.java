package dev.alphasucks.aea.macro.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.alphasucks.aea.macro.MacroParse;
import java.util.List;
import net.minecraft.class_2158;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Function macros: vanilla's parser still parses every normal line; AEA only pre/post-processes the file. */
@Mixin(class_2158.class)
public abstract class CommandFunctionMacroMixin {
    @ModifyVariable(method = "method_9195", at = @At("HEAD"), argsOnly = true)
    private static List<String> aea$prepareMacroLines(List<String> lines) {
        return MacroParse.prepare(lines);
    }

    @ModifyReturnValue(method = "method_9195", at = @At("RETURN"))
    private static class_2158 aea$attachMacroLines(class_2158 parsed) {
        return MacroParse.finish(parsed);
    }
}
