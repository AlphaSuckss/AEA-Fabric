package dev.alphasucks.aea.mindcontrol.client;

import dev.alphasucks.aea.mindcontrol.Control;
import net.minecraft.class_1297;
import net.minecraft.class_304;
import net.minecraft.class_310;

/** What the client input mixins ask; all cheap when there is no session. */
public final class MindControlKeys {
    private MindControlKeys() {}

    /** Null for a key the controller has taken from this (controlled) player. */
    public static Object filter(Object binding, boolean press) {
        return binding instanceof class_304 key && MindControlClient.victim() ? MindControlClient.filter(key, press) : binding;
    }

    public static boolean controlling() {
        return MindControlClient.controlling();
    }

    public static boolean mayTurn() {
        return !MindControlClient.victimOf(Control.LOOK);
    }

    /** True when the wheel event is taken: by the controller for the victim's hotbar, or away from a victim's own. */
    public static boolean scroll(double y) {
        if (y == 0 || class_310.method_1551().field_1755 != null) return false;
        if (MindControlClient.controlling()) {
            MindControlClient.scroll += (int) Math.signum(y);
            return true;
        }
        if (MindControlClient.victimOf(Control.HOTBAR)) {
            MindControlClient.struggle++;
            return true;
        }
        return false;
    }

    /** The camera follows your mouse straight away instead of waiting for the victim to turn on the server. */
    public static boolean overridesCamera(class_1297 focused) {
        return MindControlClient.overridesCamera(focused);
    }

    public static float cameraYaw(float tickDelta) {
        return class_310.method_1551().field_1724.method_5705(tickDelta);
    }

    public static float cameraPitch(float tickDelta) {
        return class_310.method_1551().field_1724.method_5695(tickDelta);
    }
}
