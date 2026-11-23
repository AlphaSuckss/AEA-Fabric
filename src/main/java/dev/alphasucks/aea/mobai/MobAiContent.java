package dev.alphasucks.aea.mobai;

import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

/** Registers aea:mob_pathing and aea:mob_ai (bridged to Apoli by AeaRegistrar with the other port types). */
public final class MobAiContent {
    private static boolean initialised;

    private MobAiContent() {
    }

    public static void init() {
        if (initialised) {
            return;
        }
        initialised = true;
        PowerTypeRegistry.register(MobPathingPower.ID, new MobPathingPower());
        PowerTypeRegistry.register(MobAiPower.ID, new MobAiPower());
        // goal undo on revoke runs outside the goal-selector iteration that may have triggered it
        ServerTickEvents.END_SERVER_TICK.register(server -> MobAiPower.runDeferred());
    }
}
