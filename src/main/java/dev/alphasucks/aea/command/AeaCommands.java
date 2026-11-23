package dev.alphasucks.aea.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.alphasucks.aea.AEA;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_2168;
import net.minecraft.class_2170;

/** {@code /aea validate}, and AEA's {@code /scale} when Pehkui isn't installed. */
public final class AeaCommands {
    private static boolean registered;
    private static boolean announced;

    private AeaCommands() {
    }

    public static void init() {
        if (registered) {
            return;
        }
        registered = true;
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<class_2168> dispatcher) {
        if (!FabricLoader.getInstance().isModLoaded("pehkui")) {
            PehkuiScaleCommand.register(dispatcher);
            if (!announced) {
                announced = true;
                AEA.LOGGER.info("[AEA] Pehkui is not installed: /scale is provided by AEA on top of its own scale system.");
            }
        }
        dispatcher.register(class_2170.method_9247("aea").requires(source -> source.method_9259(2))
            .then(class_2170.method_9247("validate")
                .executes(ctx -> guard(ctx.getSource(), false))
                .then(class_2170.method_9247("fields").executes(ctx -> guard(ctx.getSource(), true)))));
    }

    private static int guard(class_2168 source, boolean fields) {
        try {
            return PackValidator.run(source, fields);
        } catch (Throwable t) {
            AEA.LOGGER.error("[AEA] /aea validate crashed", t);
            source.method_9213(net.minecraft.class_2561.method_43470("[AEA] validate failed: " + t));
            return 0;
        }
    }
}
