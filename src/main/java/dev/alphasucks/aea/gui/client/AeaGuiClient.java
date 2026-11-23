package dev.alphasucks.aea.gui.client;

import dev.alphasucks.aea.gui.AeaGui;
import dev.alphasucks.aea.gui.GuiNetwork;
import dev.alphasucks.aea.gui.GuiScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

/** Client registration of the {@code aea:gui} screen and its state packet. */
public final class AeaGuiClient {
    private static boolean initialised;

    private AeaGuiClient() {
    }

    @SuppressWarnings("deprecation")
    public static void init() {
        if (initialised) {
            return;
        }
        initialised = true;
        ScreenRegistry.<GuiScreenHandler, GuiScreen>register(AeaGui.MENU, GuiScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(GuiNetwork.STATE, (client, handler, buf, sender) -> {
            int syncId = buf.method_10816();
            GuiNetwork.State state = GuiNetwork.State.read(buf);
            client.execute(() -> {
                if (client.field_1724 != null && client.field_1724.field_7512 instanceof GuiScreenHandler menu
                    && menu.field_7763 == syncId && !menu.broken && menu.isClient()) {
                    state.applyTo(menu);
                }
            });
        });
    }
}
