package dev.alphasucks.aea.port.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.alphasucks.aea.port.compat.ModCompat;
import dev.alphasucks.aea.port.compat.accessory.command.AccessoryCommand;
import dev.alphasucks.aea.port.power.builtin.ActionOnSendingMessagePower;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.class_1297;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2556;
import net.minecraft.class_2561;

/**
 * AEA: registers the ported Overgrown commands. Only roots that Apoli 2.9.2, Origins 1.10.2 and Sync
 * do not own are registered: Overgrown's {@code /power}, {@code /resource} and {@code /disguise} aliases are
 * dropped (their {@code /apoli:power}, {@code /apoli:resource} and {@code /apoli:disguise} roots stay), and
 * {@code /apoli:mount} is skipped because AEA does not port the mount-offset system.
 */
public final class AeaPortCommands {
    private static boolean registered;

    private AeaPortCommands() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerAll(dispatcher));
    }

    public static void registerAll(CommandDispatcher<class_2168> dispatcher) {
        ApoliPowerCommand.register(dispatcher);
        ApoliResourceCommand.register(dispatcher);
        ApoliSkillTreeCommand.register(dispatcher);
        ApoliDisguiseCommand.register(dispatcher);
        ApoliCloneCommand.register(dispatcher);
        ApoliKeyCommand.register(dispatcher);
        ApoliDevModeCommand.register(dispatcher);
        ApoliTickCommand.register(dispatcher);
        ApoliScaleCommand.register(dispatcher);
        if (ModCompat.anyAccessory()) {
            AccessoryCommand.register(dispatcher);
        }
        registerMessage(dispatcher);
    }

    /** Overgrown's inline {@code /apoli:message <text>}: dry-runs the executor's action_on_sending_message powers. */
    private static void registerMessage(CommandDispatcher<class_2168> dispatcher) {
        dispatcher.register(
            class_2170.method_9247("apoli:message")
                .requires(source -> source.method_9259(0))
                .then(class_2170.method_9244("text", StringArgumentType.greedyString()).executes(ctx -> {
                    class_1297 holder = ctx.getSource().method_9229();
                    String text = StringArgumentType.getString(ctx, "text");
                    ActionOnSendingMessagePower.Report report = ActionOnSendingMessagePower.diagnose(holder, text, class_2556.field_11737.method_29177());
                    for (String line : report.lines()) {
                        ctx.getSource().method_9226(() -> class_2561.method_43470(line), false);
                    }
                    return report.matched() ? 1 : 0;
                }))
        );
    }
}
