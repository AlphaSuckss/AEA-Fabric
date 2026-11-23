package dev.alphasucks.aea.mindcontrol.client;

import dev.alphasucks.aea.mindcontrol.MindControlNetwork;
import net.minecraft.class_1297;

/**
 * What this client knows about the local player's session, for conditions checked on the client. Holds plain values
 * only, so the server can load it too.
 */
public final class MindControlClientView {
    private MindControlClientView() {}

    static volatile int role = MindControlNetwork.ROLE_NONE;
    static volatile int localId = -1, otherId = -1;

    public static boolean isControlled(class_1297 e) {
        return role == MindControlNetwork.ROLE_VICTIM && e.method_5628() == localId
            || role == MindControlNetwork.ROLE_CONTROLLER && e.method_5628() == otherId;
    }

    public static boolean isControlling(class_1297 e) {
        return role == MindControlNetwork.ROLE_CONTROLLER && e.method_5628() == localId
            || role == MindControlNetwork.ROLE_VICTIM && e.method_5628() == otherId;
    }

    public static boolean controls(class_1297 actor, class_1297 target) {
        return isControlling(actor) && isControlled(target);
    }
}
