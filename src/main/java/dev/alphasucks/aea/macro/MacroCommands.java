package dev.alphasucks.aea.macro;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.class_2158;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2179;
import net.minecraft.class_2203;
import net.minecraft.class_2284;
import net.minecraft.class_2487;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_2991;
import net.minecraft.class_3062;
import net.minecraft.class_3162;
import net.minecraft.class_3164;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

/**
 * {@code /function <name> <arguments>} and {@code /function <name> with (block|entity|storage) ... [<path>]}. They are
 * merged into vanilla's /function node (Brigadier merges nodes with the same name), so vanilla's own
 * {@code /function <name>} stays untouched.
 */
public final class MacroCommands {
    private static final DynamicCommandExceptionType NOT_COMPOUND = new DynamicCommandExceptionType(
        type -> class_2561.method_43469("commands.function.error.argument_not_compound", type));
    private static final SimpleCommandExceptionType MULTIPLE = new SimpleCommandExceptionType(class_2561.method_43471("commands.data.get.multiple"));

    private MacroCommands() {
    }

    public static void register(CommandDispatcher<class_2168> dispatcher) {
        LiteralArgumentBuilder<class_2168> with = class_2170.method_9247("with");
        for (class_3164.class_3167 type : class_3164.field_13792) {
            type.method_13925(with, builder -> builder
                .executes(ctx -> run(ctx.getSource(), class_2284.method_9769(ctx, "name"), type.method_13924(ctx).method_13881()))
                .then(class_2170.method_9244("path", class_2203.method_9360())
                    .executes(ctx -> run(ctx.getSource(), class_2284.method_9769(ctx, "name"),
                        single(class_2203.method_9358(ctx, "path"), type.method_13924(ctx))))));
        }
        dispatcher.register(class_2170.method_9247("function")
            .requires(source -> source.method_9259(2))
            .then(class_2170.method_9244("name", class_2284.method_9760())
                .suggests(class_3062.field_13662)
                .then(class_2170.method_9244("arguments", class_2179.method_9284())
                    .executes(ctx -> run(ctx.getSource(), class_2284.method_9769(ctx, "name"), class_2179.method_9285(ctx, "arguments"))))
                .then(with)));
    }

    private static class_2487 single(class_2203.class_2209 path, class_3162 object) throws CommandSyntaxException {
        List<class_2520> found = path.method_9366(object.method_13881());
        if (found.size() != 1) {
            throw MULTIPLE.create();
        }
        class_2520 element = found.get(0);
        if (element instanceof class_2487 compound) {
            return compound;
        }
        throw NOT_COMPOUND.create(element.method_23258().method_23259());
    }

    /** Vanilla's /function execution, with macro functions instantiated first (errors go to the command's source). */
    public static int run(class_2168 source, Collection<class_2158> functions, @Nullable class_2487 arguments) {
        class_2991 manager = source.method_9211().method_3740();
        class_2168 exec = source.method_9217().method_9230(2);
        int total = 0;
        boolean returned = false;
        boolean ran = false;
        for (class_2158 function : functions) {
            class_2158 target = function;
            if (function instanceof MacroFunction macro) {
                try {
                    target = macro.instantiate(arguments, manager.method_12900(), exec);
                } catch (MacroException e) {
                    source.method_9213(e.text());
                    continue;
                }
            }
            MutableObject<OptionalInt> result = new MutableObject<>(OptionalInt.empty());
            int value = manager.method_12904(target, exec.method_51411(v -> result.setValue(OptionalInt.of(v))));
            OptionalInt returnedValue = result.getValue();
            total += returnedValue.orElse(value);
            returned |= returnedValue.isPresent();
            ran = true;
        }
        if (ran) {
            int sum = total;
            if (functions.size() == 1) {
                class_2158 only = functions.iterator().next();
                String key = returned ? "commands.function.success.single.result" : "commands.function.success.single";
                source.method_9226(() -> class_2561.method_43469(key, sum, only.method_9194()), true);
            } else if (returned) {
                source.method_9226(() -> class_2561.method_43469("commands.function.success.multiple.result", functions.size()), true);
            } else {
                source.method_9226(() -> class_2561.method_43469("commands.function.success.multiple", sum, functions.size()), true);
            }
        }
        return total;
    }

    public static boolean anyMacro(Collection<class_2158> functions) {
        for (class_2158 f : functions) {
            if (f instanceof MacroFunction) {
                return true;
            }
        }
        return false;
    }
}
