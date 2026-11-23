package dev.alphasucks.aea.compat.carryonledger;

import com.github.quiltservertools.ledger.Ledger;
import com.github.quiltservertools.ledger.actionutils.ActionFactory;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2586;
import net.minecraft.class_2680;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Carry On -> Ledger logging (backport of Lunascape's "Carry On Ledger Logs", CC0, from 1.21.4 to 1.20.1).
 * Only ever loaded by the mixins in aea-carryon-ledger.mixins.json, which apply only when both Carry On and
 * Ledger are installed (Ledger itself only loads on dedicated servers).
 */
public final class CarryOnLedgerLogging {
    private static final Logger LOGGER = LoggerFactory.getLogger("AEA/CarryOnLedger");
    /** Ledger's Sources.PLAYER */
    private static final String SOURCE = "player";
    private static boolean failed;

    private CarryOnLedgerLogging() {
    }

    /** A block was picked up (removed from the world) by a player. */
    public static void pickedUp(class_1937 world, class_2338 pos, class_2680 state, class_3222 player, @Nullable class_2586 blockEntity) {
        if (failed || world.method_8608()) {
            return;
        }
        try {
            Ledger.getApi().logAction(ActionFactory.INSTANCE.blockBreakAction(world, pos.method_10062(), state, player, blockEntity, SOURCE));
        } catch (Throwable t) {
            disable(t);
        }
    }

    /** A carried block was put back into the world by a player (normal placement, or on death/logout). */
    public static void placed(class_1937 world, class_2338 pos, class_3222 player) {
        if (failed || world.method_8608()) {
            return;
        }
        try {
            class_2680 state = world.method_8320(pos);
            if (state.method_26215()) {
                return;
            }
            Ledger.getApi().logAction(ActionFactory.INSTANCE.blockPlaceAction(world, pos.method_10062(), state, player, world.method_8321(pos), SOURCE));
        } catch (Throwable t) {
            disable(t);
        }
    }

    /** An incompatible Ledger build must not break Carry On: log once and stop trying. */
    private static void disable(Throwable t) {
        failed = true;
        LOGGER.error("Could not log a Carry On action to Ledger; Carry On logging is disabled until restart", t);
    }
}
