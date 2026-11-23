package dev.alphasucks.aea.mindcontrol.client;

import dev.alphasucks.aea.mindcontrol.Control;
import dev.alphasucks.aea.mindcontrol.ControlInput;
import dev.alphasucks.aea.mindcontrol.MindControl;
import dev.alphasucks.aea.mindcontrol.MindControlNetwork;
import dev.alphasucks.aea.mindcontrol.mixin.client.KeyBindingMindControlAccessor;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_2540;
import net.minecraft.class_2561;
import net.minecraft.class_304;
import net.minecraft.class_310;
import net.minecraft.class_315;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_746;

/**
 * The client half of mind control. As the controller it reads the keys you press and the way you look, sends them each
 * tick, keeps them from moving your own (spectating) player, and points the camera with your mouse. As the victim it
 * ignores your keys for the controlled parts and presses the relayed ones instead, so the game moves you normally.
 */
public final class MindControlClient {
    private MindControlClient() {}

    private static int role = MindControlNetwork.ROLE_NONE;
    private static int other = -1, controls, struggleNeeded, struggleCount;
    private static String otherName = "";

    // Controller: mouse wheel steps since the last tick.
    static int scroll;
    // Victim: what the controller pressed, waiting to be applied this tick.
    private static ControlInput relay = ControlInput.NONE;
    private static int attacks, uses, drops, swaps, hotbar = -1, relayScroll;
    private static boolean dropAll;
    static int struggle;

    public static void init() {
        EntityRendererRegistry.register(MindControl.BODY, BodyRenderer::new);
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> renderHud(graphics));
        ClientTickEvents.START_CLIENT_TICK.register(client -> preTick());
        ClientTickEvents.END_CLIENT_TICK.register(client -> postTick());
        ClientPlayNetworking.registerGlobalReceiver(MindControlNetwork.STATE, (client, handler, buf, sender) -> {
            MindControlNetwork.State state = MindControlNetwork.State.read(buf);
            client.execute(() -> receiveState(state));
        });
        ClientPlayNetworking.registerGlobalReceiver(MindControlNetwork.RELAY, (client, handler, buf, sender) -> {
            ControlInput input = ControlInput.read(buf);
            client.execute(() -> receiveRelay(input));
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(() -> receiveState(MindControlNetwork.State.NONE)));
    }

    public static boolean controlling() {
        return role == MindControlNetwork.ROLE_CONTROLLER;
    }

    /** True for a controlled part of the local player, whose real keys are ignored. */
    public static boolean victimOf(Control control) {
        return role == MindControlNetwork.ROLE_VICTIM && control.in(controls);
    }

    static boolean overridesCamera(class_1297 focused) {
        if (role != MindControlNetwork.ROLE_CONTROLLER || !Control.LOOK.in(controls)) return false;
        return focused != null && focused.method_5628() == other && class_310.method_1551().field_1724 != null;
    }

    private static void receiveState(MindControlNetwork.State p) {
        int was = role;
        role = p.role();
        other = p.other();
        otherName = p.otherName();
        controls = p.controls();
        struggleNeeded = p.struggleNeeded();
        struggleCount = p.struggleCount();
        class_310 mc = class_310.method_1551();
        MindControlClientView.role = role;
        MindControlClientView.localId = mc.field_1724 == null ? -1 : mc.field_1724.method_5628();
        MindControlClientView.otherId = other;
        if (was == MindControlNetwork.ROLE_VICTIM && role != MindControlNetwork.ROLE_VICTIM) {
            // Back to your own keys, as they are physically held right now.
            class_304.method_1424();
            relay = ControlInput.NONE;
        }
        if (was == MindControlNetwork.ROLE_CONTROLLER && role != MindControlNetwork.ROLE_CONTROLLER && mc.field_1724 != null) {
            mc.method_1504(mc.field_1724);
        }
        if (role == MindControlNetwork.ROLE_VICTIM && was != MindControlNetwork.ROLE_VICTIM) {
            attacks = uses = drops = swaps = relayScroll = 0;
            hotbar = -1;
        }
    }

    private static void receiveRelay(ControlInput in) {
        if (role != MindControlNetwork.ROLE_VICTIM) return;
        relay = in;
        attacks += in.attacks();
        uses += in.uses();
        drops += in.drops();
        swaps += in.swaps();
        relayScroll += in.scroll();
        if (in.hotbar() >= 0) hotbar = in.hotbar();
        dropAll = in.has(ControlInput.DROP_ALL);
    }

    private static void preTick() {
        if (role == MindControlNetwork.ROLE_NONE) return;
        class_310 mc = class_310.method_1551();
        class_746 player = mc.field_1724;
        if (player == null || mc.field_1687 == null) return;
        if (role == MindControlNetwork.ROLE_CONTROLLER) captureAndSend(mc, player);
        else applyRelay(mc, player);
    }

    private static void captureAndSend(class_310 mc, class_746 player) {
        class_1297 victim = mc.field_1687.method_8469(other);
        if (victim != null && mc.method_1560() != victim) mc.method_1504(victim);
        class_315 o = mc.field_1690;
        int flags = 0, a = 0, u = 0, d = 0, s = 0, h = -1;
        if (mc.field_1755 == null) {
            if (o.field_1894.method_1434()) flags |= ControlInput.FORWARD;
            if (o.field_1881.method_1434()) flags |= ControlInput.BACK;
            if (o.field_1913.method_1434()) flags |= ControlInput.LEFT;
            if (o.field_1849.method_1434()) flags |= ControlInput.RIGHT;
            if (o.field_1903.method_1434()) flags |= ControlInput.JUMP;
            if (o.field_1832.method_1434()) flags |= ControlInput.SNEAK;
            if (o.field_1867.method_1434()) flags |= ControlInput.SPRINT;
            if (o.field_1886.method_1434()) flags |= ControlInput.ATTACK_DOWN;
            if (o.field_1904.method_1434()) flags |= ControlInput.USE_DOWN;
            if (class_437.method_25441()) flags |= ControlInput.DROP_ALL;
        }
        // Presses are taken here, before the game handles them, so your spectating self doesn't act on them.
        while (o.field_1886.method_1436()) a++;
        while (o.field_1904.method_1436()) u++;
        while (o.field_1869.method_1436()) d++;
        while (o.field_1831.method_1436()) s++;
        for (int i = 0; i < 9; i++) {
            while (o.field_1852[i].method_1436()) h = i;
        }
        int sc = scroll;
        scroll = 0;
        if (!ClientPlayNetworking.canSend(MindControlNetwork.INPUT)) return;
        class_2540 buf = PacketByteBufs.create();
        new ControlInput(flags, player.method_36454(), player.method_36455(), a, u, d, s, h, sc).write(buf);
        ClientPlayNetworking.send(MindControlNetwork.INPUT, buf);
    }

    private static void applyRelay(class_310 mc, class_746 player) {
        class_315 o = mc.field_1690;
        ControlInput in = relay;
        if (Control.MOVE.in(controls)) {
            down(o.field_1894, in.has(ControlInput.FORWARD));
            down(o.field_1881, in.has(ControlInput.BACK));
            down(o.field_1913, in.has(ControlInput.LEFT));
            down(o.field_1849, in.has(ControlInput.RIGHT));
        }
        if (Control.JUMP.in(controls)) down(o.field_1903, in.has(ControlInput.JUMP));
        if (Control.SNEAK.in(controls)) down(o.field_1832, in.has(ControlInput.SNEAK));
        if (Control.SPRINT.in(controls)) down(o.field_1867, in.has(ControlInput.SPRINT));
        if (Control.ATTACK.in(controls)) {
            down(o.field_1886, in.has(ControlInput.ATTACK_DOWN));
            click(o.field_1886, attacks);
        }
        if (Control.USE.in(controls)) {
            down(o.field_1904, in.has(ControlInput.USE_DOWN));
            click(o.field_1904, uses);
        }
        if (Control.SWAP_HANDS.in(controls)) click(o.field_1831, swaps);
        if (Control.DROP.in(controls)) {
            for (int i = 0; i < drops && !player.method_7325(); i++) {
                if (player.method_7290(dropAll)) player.method_6104(class_1268.field_5808);
            }
        }
        if (Control.HOTBAR.in(controls)) {
            if (hotbar >= 0) player.method_31548().field_7545 = hotbar;
            for (int i = 0; i < Math.abs(relayScroll); i++) player.method_31548().method_7373(Math.signum(relayScroll));
        }
        attacks = uses = drops = swaps = relayScroll = 0;
        hotbar = -1;
        if (struggle > 0) {
            if (ClientPlayNetworking.canSend(MindControlNetwork.STRUGGLE)) {
                class_2540 buf = PacketByteBufs.create();
                buf.method_10804(struggle);
                ClientPlayNetworking.send(MindControlNetwork.STRUGGLE, buf);
            }
            struggle = 0;
        }
    }

    /** Turns the victim after their tick, so the frames until the next one ease from the old direction to the new. */
    private static void postTick() {
        if (role != MindControlNetwork.ROLE_VICTIM || !Control.LOOK.in(controls)) return;
        class_746 player = class_310.method_1551().field_1724;
        if (player == null) return;
        player.method_36456(relay.yRot());
        player.method_36457(relay.xRot());
    }

    private static void down(class_304 key, boolean down) {
        ((KeyBindingMindControlAccessor) key).aea$setPressedRaw(down);
    }

    private static void click(class_304 key, int times) {
        if (times > 0) {
            KeyBindingMindControlAccessor k = (KeyBindingMindControlAccessor) key;
            k.aea$setTimesPressed(k.aea$timesPressed() + times);
        }
    }

    private static void renderHud(class_332 graphics) {
        if (role == MindControlNetwork.ROLE_NONE) return;
        class_310 mc = class_310.method_1551();
        if (mc.field_1690.field_1842) return;
        class_2561 text = role == MindControlNetwork.ROLE_CONTROLLER
            ? class_2561.method_43469("aea.mind_control.controlling", otherName)
            : class_2561.method_43469("aea.mind_control.controlled", otherName);
        int cx = graphics.method_51421() / 2;
        int y = 22;
        graphics.method_27534(mc.field_1772, text, cx, y, 0xD9A6FF);
        if (role == MindControlNetwork.ROLE_VICTIM && struggleNeeded > 0) {
            int w = 102, x = cx - w / 2, by = y + 12;
            graphics.method_25294(x, by, x + w, by + 5, 0xC0000000);
            int filled = Math.min(w - 2, (w - 2) * struggleCount / struggleNeeded);
            graphics.method_25294(x + 1, by + 1, x + 1 + filled, by + 4, 0xFFB45CFF);
            graphics.method_27534(mc.field_1772, class_2561.method_43471("aea.mind_control.struggle"), cx, by + 8, 0xBBBBBB);
        }
    }

    /** For a controlled player: null for a key the controller has, counting presses towards struggling free. */
    static class_304 filter(class_304 key, boolean press) {
        if (!controlled(class_310.method_1551().field_1690, key)) return key;
        if (press) struggle++;
        return null;
    }

    private static boolean controlled(class_315 o, class_304 k) {
        if (k == o.field_1894 || k == o.field_1881 || k == o.field_1913 || k == o.field_1849) return Control.MOVE.in(controls);
        if (k == o.field_1903) return Control.JUMP.in(controls);
        if (k == o.field_1832) return Control.SNEAK.in(controls);
        if (k == o.field_1867) return Control.SPRINT.in(controls);
        if (k == o.field_1886) return Control.ATTACK.in(controls);
        if (k == o.field_1904) return Control.USE.in(controls);
        if (k == o.field_1869) return Control.DROP.in(controls);
        if (k == o.field_1831) return Control.SWAP_HANDS.in(controls);
        if (k == o.field_1890) return Control.CHAT.in(controls);
        if (k == o.field_1845) return Control.COMMANDS.in(controls) || Control.CHAT.in(controls);
        for (class_304 slot : o.field_1852) if (k == slot) return Control.HOTBAR.in(controls);
        return false;
    }

    /** Whether the local player's own keys are being filtered at all. */
    static boolean victim() {
        return role == MindControlNetwork.ROLE_VICTIM;
    }
}
