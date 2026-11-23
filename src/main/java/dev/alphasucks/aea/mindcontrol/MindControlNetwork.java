package dev.alphasucks.aea.mindcontrol;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

/** The four mind control channels, the same ids as AEA for NeoForge. */
public final class MindControlNetwork {
    private MindControlNetwork() {}

    public static final int ROLE_NONE = 0, ROLE_CONTROLLER = 1, ROLE_VICTIM = 2;

    /** Server → client: this player's part in a session. */
    public static final class_2960 STATE = new class_2960("aea", "mind_control_state");
    /** Controller → server: this tick's input. */
    public static final class_2960 INPUT = new class_2960("aea", "mind_control_input");
    /** Server → controlled player: the controller's input, limited to the session's controls. */
    public static final class_2960 RELAY = new class_2960("aea", "mind_control_relay");
    /** Controlled player → server: keys they pressed that were ignored, for struggling free. */
    public static final class_2960 STRUGGLE = new class_2960("aea", "mind_control_struggle");

    /** {@code other} is the entity id of the other side. */
    public record State(int role, int other, String otherName, int controls, int struggleNeeded, int struggleCount) {
        public static final State NONE = new State(ROLE_NONE, -1, "", 0, 0, 0);

        public void write(class_2540 buf) {
            buf.method_10804(role);
            buf.method_10804(other);
            String name = otherName.length() > 64 ? otherName.substring(0, 64) : otherName;
            buf.method_10788(name, 64);
            buf.method_10804(controls);
            buf.method_10804(struggleNeeded);
            buf.method_10804(struggleCount);
        }

        public static State read(class_2540 buf) {
            return new State(buf.method_10816(), buf.method_10816(), buf.method_10800(64), buf.method_10816(), buf.method_10816(), buf.method_10816());
        }
    }

    static void sendState(class_3222 player, State state) {
        if (player.field_13987 == null || !ServerPlayNetworking.canSend(player, STATE)) return;
        class_2540 buf = PacketByteBufs.create();
        state.write(buf);
        ServerPlayNetworking.send(player, STATE, buf);
    }

    static void sendRelay(class_3222 player, ControlInput input) {
        if (player.field_13987 == null || !ServerPlayNetworking.canSend(player, RELAY)) return;
        class_2540 buf = PacketByteBufs.create();
        input.write(buf);
        ServerPlayNetworking.send(player, RELAY, buf);
    }
}
