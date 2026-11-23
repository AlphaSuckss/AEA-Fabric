package dev.alphasucks.aea.gui;

import dev.alphasucks.aea.bridge.BridgedPower;
import dev.alphasucks.aea.gui.action.GuiActions;
import dev.alphasucks.aea.gui.condition.GuiConditions;
import dev.alphasucks.aea.port.power.PowerTypeRegistry;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_2378;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_7923;
import net.minecraft.server.MinecraftServer;

/** Registration and server entry points of the {@code aea:gui} feature. */
public final class AeaGui {
    public static final class_2960 POWER_TYPE_ID = new class_2960("aea", "gui");
    public static final ExtendedScreenHandlerType<GuiScreenHandler> MENU = new ExtendedScreenHandlerType<>(GuiScreenHandler::createClient);

    private static boolean initialised;

    private AeaGui() {
    }

    /** Must run before {@code AeaRegistrar.onAeaInitialized()} (the port registries are flushed into Apoli there). */
    public static void init() {
        if (initialised) {
            return;
        }
        initialised = true;
        PowerTypeRegistry.register(POWER_TYPE_ID, new GuiPowerType());
        GuiActions.register();
        GuiConditions.register();
        class_2378.method_10230(class_7923.field_41187, POWER_TYPE_ID, MENU);
        GuiMemory.bootstrap();

        ServerTickEvents.END_SERVER_TICK.register(AeaGui::tickServer);
        // close before the player's data is saved, so items are returned/remembered and not dropped or lost
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            class_3222 player = handler.field_14140;
            if (player != null && player.field_7512 instanceof GuiScreenHandler) {
                player.method_14247();
            }
        });
        ServerPlayNetworking.registerGlobalReceiver(GuiNetwork.BUTTON, (server, player, handler, buf, sender) -> {
            int syncId = buf.method_10816();
            int element = buf.method_10816();
            boolean pressed = buf.readBoolean();
            server.execute(() -> {
                GuiSession session = sessionFor(player, syncId);
                if (session == null || element < 0 || element >= session.h().model.elementCount()) {
                    return;
                }
                if (pressed) {
                    session.pressButton(element);
                } else {
                    session.releaseButton(element);
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(GuiNetwork.SLIDER, (server, player, handler, buf, sender) -> {
            int syncId = buf.method_10816();
            int element = buf.method_10816();
            double value = buf.readDouble();
            boolean released = buf.readBoolean();
            server.execute(() -> {
                GuiSession session = sessionFor(player, syncId);
                if (session == null || element < 0 || element >= session.h().model.elementCount()) {
                    return;
                }
                session.moveSlider(element, value, released);
            });
        });
    }

    private static GuiSession sessionFor(class_3222 player, int syncId) {
        if (player.field_7512 instanceof GuiScreenHandler h && h.field_7763 == syncId && !h.isClosed()) {
            return h.session();
        }
        return null;
    }

    private static void tickServer(MinecraftServer server) {
        List<class_3222> players = List.copyOf(server.method_3760().method_14571());
        for (class_3222 player : players) {
            if (player.field_7512 instanceof GuiScreenHandler h && h.session() != null) {
                h.session().tick();
            }
        }
    }

    /**
     * Opens the GUI of a power the player holds (server only). Fails if the power is missing, not an
     * {@code aea:gui} or inactive.
     */
    public static boolean open(class_3222 player, class_2960 powerId) {
        BridgedPower<GuiConfig> power = GuiPowers.find(player, powerId);
        if (power == null || player.method_37908().method_8608()) {
            return false;
        }
        boolean active;
        try {
            active = power.isActive();
        } catch (RuntimeException e) {
            active = false;
        }
        if (!active || !player.method_5805()) {
            return false;
        }
        if (player.field_7512 instanceof GuiScreenHandler current && current.session() != null && current.session().closing()) {
            // an action running while this GUI closes: open after the close has finished
            player.field_13995.execute(() -> open(player, powerId));
            return true;
        }
        GuiConfig config = power.config();
        player.method_17355(new Factory(powerId, config.guiId(powerId), config));
        return true;
    }

    private static final class Factory implements ExtendedScreenHandlerFactory {
        private final class_2960 powerId;
        private final class_2960 guiId;
        private final GuiConfig config;
        private GuiScreenHandler created;

        Factory(class_2960 powerId, class_2960 guiId, GuiConfig config) {
            this.powerId = powerId;
            this.guiId = guiId;
            this.config = config;
        }

        @Override
        public class_2561 method_5476() {
            return config.title().orElse(class_2561.method_43473());
        }

        @Override
        public class_1703 createMenu(int syncId, class_1661 inventory, class_1657 player) {
            created = GuiScreenHandler.createServer(syncId, inventory, (class_3222) player, powerId, guiId, config);
            return created;
        }

        @Override
        public void writeScreenOpeningData(class_3222 player, class_2540 buf) {
            GuiNetwork.writeOpenData(created, buf);
        }
    }
}
