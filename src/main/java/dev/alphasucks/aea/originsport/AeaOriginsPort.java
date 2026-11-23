package dev.alphasucks.aea.originsport;

import dev.alphasucks.aea.originsport.action.OriginActions;
import dev.alphasucks.aea.originsport.condition.OriginConditions;
import dev.alphasucks.aea.originsport.network.OriginsPortNetwork;
import dev.alphasucks.aea.originsport.origin.OriginExpressions;
import dev.alphasucks.aea.originsport.origin.OriginTags;
import dev.alphasucks.aea.originsport.power.ActionOnSwapPower;
import dev.alphasucks.aea.originsport.storage.StoredDataAttachment;
import dev.alphasucks.aea.originsport.swap.SwapDataAttachment;
import dev.alphasucks.aea.originsport.swap.SwapLayers;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.class_3264;

/**
 * Overgrown Origins 1.49.0 features that apace's Origins 1.10.2 lacks, running on top of it:
 * origin swapping (swappable layers, swap pools, swap screen/key, {@code origins:action_on_swap}),
 * origin storage (stored origins/values, {@code /origin storage}), origin tags, impact conditions
 * and the origin transfer actions. Type ids keep the {@code origins:} namespace.
 */
public final class AeaOriginsPort {
    private static boolean initialised;
    private static boolean clientInitialised;

    private AeaOriginsPort() {
    }

    /** Call from AEAContent.init(), before AeaRegistrar.onAeaInitialized() (registrations are bridged there). */
    public static void init() {
        if (initialised) {
            return;
        }
        initialised = true;
        OriginConditions.register();
        OriginActions.register();
        PowerTypeRegistry.register(ActionOnSwapPower.ID, new ActionOnSwapPower());
        OriginExpressions.register();

        SwapDataAttachment.init();
        StoredDataAttachment.init();

        ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new SwapLayers());
        ResourceManagerHelper.get(class_3264.field_14190).registerReloadListener(new OriginTags());

        OriginsPortNetwork.registerServer();
        OriginsPortEvents.register();
    }

    /** Call from AEAContent.initClient(). */
    public static void initClient() {
        if (clientInitialised) {
            return;
        }
        clientInitialised = true;
        dev.alphasucks.aea.originsport.client.OriginsPortClient.init();
    }
}
