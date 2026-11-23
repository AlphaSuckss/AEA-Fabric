package dev.alphasucks.aea;

import net.fabricmc.api.ClientModInitializer;

public class AEAClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AEAContent.initClient();
    }
}
