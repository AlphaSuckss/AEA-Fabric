package dev.alphasucks.aea.hitbox.client;

import dev.alphasucks.aea.hitbox.HitboxNetwork;
import dev.alphasucks.aea.hitbox.HitboxPart;
import dev.alphasucks.aea.hitbox.HitboxWorld;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.class_1309;
import net.minecraft.class_310;
import net.minecraft.class_638;

/**
 * Client copies of {@code aea:hitbox_part} boxes. The server says which boxes an entity has; the client builds them
 * with the same network ids, moves them every tick, and lets the crosshair hit them.
 */
@Environment(EnvType.CLIENT)
public final class ClientHitboxParts {
    private ClientHitboxParts() {
    }

    private static final Int2ObjectOpenHashMap<HitboxNetwork.Part[]> SPECS = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectOpenHashMap<HitboxPart[]> LIVE = new Int2ObjectOpenHashMap<>();
    private static class_638 world;

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(HitboxNetwork.PARTS, (client, handler, buf, sender) -> {
            HitboxNetwork.Parts payload = HitboxNetwork.Parts.read(buf);
            client.execute(() -> apply(payload));
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    /** The boxes of an entity, for the hit box overlay (F3 + B). */
    public static HitboxPart[] partsOf(int owner) {
        HitboxPart[] parts = LIVE.get(owner);
        return parts == null ? HitboxPart.NONE : parts;
    }

    private static void apply(HitboxNetwork.Parts payload) {
        syncWorld();
        discard(payload.owner());
        if (payload.parts().length == 0) {
            SPECS.remove(payload.owner());
        } else {
            SPECS.put(payload.owner(), payload.parts());
        }
    }

    /** Everything belongs to one client world; a new world starts empty and the server sends the boxes again. */
    private static void syncWorld() {
        class_638 current = class_310.method_1551().field_1687;
        if (current == world) {
            return;
        }
        world = current;
        SPECS.clear();
        LIVE.clear();
    }

    private static void discard(int owner) {
        HitboxPart[] parts = LIVE.remove(owner);
        if (parts == null) {
            return;
        }
        for (HitboxPart part : parts) {
            HitboxWorld.remove(part.method_37908(), part);
        }
    }

    private static void tick() {
        syncWorld();
        if (world == null || SPECS.isEmpty()) {
            return;
        }
        it.unimi.dsi.fastutil.ints.IntArrayList gone = null;
        for (Int2ObjectMap.Entry<HitboxNetwork.Part[]> entry : SPECS.int2ObjectEntrySet()) {
            int owner = entry.getIntKey();
            HitboxPart[] parts = partsOf(owner);
            if (!(world.method_8469(owner) instanceof class_1309 living) || living.method_31481()) {
                // Out of sight: forget it. The server sends the boxes again when the entity comes back.
                if (parts.length > 0) {
                    discard(owner);
                }
                gone = gone == null ? new it.unimi.dsi.fastutil.ints.IntArrayList() : gone;
                gone.add(owner);
                continue;
            }
            if (parts.length == 0 || parts[0].owner != living) {
                if (parts.length > 0) {
                    discard(owner);
                }
                parts = build(living, entry.getValue());
                LIVE.put(owner, parts);
            }
            for (HitboxPart part : parts) {
                part.reposition();
            }
        }
        if (gone != null) {
            for (int i = 0; i < gone.size(); i++) {
                SPECS.remove(gone.getInt(i));
            }
        }
    }

    private static HitboxPart[] build(class_1309 owner, HitboxNetwork.Part[] specs) {
        HitboxPart[] parts = new HitboxPart[specs.length];
        for (int i = 0; i < specs.length; i++) {
            HitboxNetwork.Part spec = specs[i];
            HitboxPart part = new HitboxPart(owner, spec.width(), spec.height(), spec.forward(), spec.up(), spec.right(), spec.scaleWithEntity());
            part.method_5838(spec.id());
            HitboxWorld.add(owner.method_37908(), part);
            parts[i] = part;
        }
        return parts;
    }
}
