package dev.alphasucks.aea.client;

import dev.alphasucks.aea.bridge.AeaContext;
import dev.alphasucks.aea.port.AeaPortClient;
import net.minecraft.class_310;

/** Client-side registration for AEA. */
public final class AeaClientContent {
    private AeaClientContent() {
    }

    public static void init() {
        AeaContext.setClientWorldSupplier(() -> class_310.method_1551().field_1687);
        AeaPortClient.init();
    }
}
