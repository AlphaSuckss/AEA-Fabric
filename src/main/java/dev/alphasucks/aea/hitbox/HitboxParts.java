package dev.alphasucks.aea.hitbox;

import dev.alphasucks.aea.port.power.PowerLookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_1309;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;

/** The {@code aea:hitbox_part} boxes of one living entity, and the server hooks that keep them in step with its powers. */
public final class HitboxParts {
    /** Implemented by every living entity through a mixin. */
    public interface Holder {
        HitboxParts aea$hitboxParts();
    }

    /** Entities that have at least one box. Entries leave when the entity is removed or its last box goes. */
    private static final Set<class_1309> TRACKED = Collections.newSetFromMap(new WeakHashMap<>());

    private final ArrayList<class_2960> powers = new ArrayList<>(2);
    private final ArrayList<HitboxPart> parts = new ArrayList<>(2);
    private boolean dirty;

    public static HitboxParts of(class_1309 entity) {
        return ((Holder) entity).aea$hitboxParts();
    }

    /** Players who started tracking an entity this tick. They are told after the entity itself has been sent. */
    private static final ArrayList<class_1309> NEW_TRACKED = new ArrayList<>();
    private static final ArrayList<class_3222> NEW_TRACKERS = new ArrayList<>();

    public static void init() {
        // End of the server tick: after every entity (and so every power) has ticked.
        ServerTickEvents.END_SERVER_TICK.register(server -> endServerTick());
        EntityTrackingEvents.START_TRACKING.register((tracked, player) -> {
            if (tracked instanceof class_1309 living && !of(living).parts.isEmpty()) {
                NEW_TRACKED.add(living);
                NEW_TRACKERS.add(player);
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            TRACKED.clear();
            NEW_TRACKED.clear();
            NEW_TRACKERS.clear();
        });
    }

    /** Called from the power's tick: makes sure the box exists and moves it. */
    public void touch(class_1309 owner, class_2960 power, HitboxPartPower.Cfg cfg) {
        int index = powers.indexOf(power);
        HitboxPart part = index < 0 ? null : parts.get(index);
        if (part != null && (part.method_37908() != owner.method_37908() || !matches(part, cfg))) {
            removeAt(index);
            part = null;
        }
        if (part == null) {
            part = new HitboxPart(owner, cfg.width(), cfg.height(), cfg.forward(), cfg.up(), cfg.right(), cfg.scaleWithEntity());
            powers.add(power);
            parts.add(part);
            HitboxWorld.add(owner.method_37908(), part);
            dirty = true;
            TRACKED.add(owner);
        } else if (part.reposition()) {
            dirty = true;
        }
        part.seenAge = owner.field_6012;
    }

    public void drop(class_2960 power) {
        int index = powers.indexOf(power);
        if (index >= 0) {
            removeAt(index);
        }
    }

    private static boolean matches(HitboxPart part, HitboxPartPower.Cfg cfg) {
        return part.width == cfg.width() && part.height == cfg.height() && part.forward == cfg.forward()
            && part.up == cfg.up() && part.right == cfg.right() && part.scaleWithEntity == cfg.scaleWithEntity();
    }

    private void removeAt(int index) {
        HitboxPart part = parts.remove(index);
        powers.remove(index);
        HitboxWorld.remove(part.method_37908(), part);
        dirty = true;
    }

    private static void endServerTick() {
        if (!TRACKED.isEmpty()) {
            Iterator<class_1309> it = TRACKED.iterator();
            while (it.hasNext()) {
                class_1309 owner = it.next();
                HitboxParts state = of(owner);
                if (owner.method_31481()) {
                    while (!state.parts.isEmpty()) {
                        state.removeAt(state.parts.size() - 1);
                    }
                    state.dirty = false;
                    it.remove();
                    continue;
                }
                state.dropStale(owner);
                // Follow the owner even on ticks where its powers did not run.
                for (int i = 0; i < state.parts.size(); i++) {
                    if (state.parts.get(i).reposition()) {
                        state.dirty = true;
                    }
                }
                if (state.dirty) {
                    state.dirty = false;
                    for (class_3222 player : PlayerLookup.tracking(owner)) {
                        state.send(owner, player);
                    }
                    if (owner instanceof class_3222 self) {
                        state.send(owner, self);
                    }
                }
                if (state.parts.isEmpty()) {
                    it.remove();
                }
            }
        }
        if (!NEW_TRACKED.isEmpty()) {
            for (int i = 0; i < NEW_TRACKED.size(); i++) {
                class_1309 owner = NEW_TRACKED.get(i);
                HitboxParts state = of(owner);
                if (!owner.method_31481() && !state.parts.isEmpty()) {
                    state.send(owner, NEW_TRACKERS.get(i));
                }
            }
            NEW_TRACKED.clear();
            NEW_TRACKERS.clear();
        }
    }

    /**
     * Removes the boxes of powers that are gone or switched off. A box whose power has not ticked for two of the
     * owner's ticks is checked against the owner's active powers, so a box survives ticks that another mod skipped.
     */
    private void dropStale(class_1309 owner) {
        int age = owner.field_6012;
        ArrayList<class_2960> active = null;
        for (int i = parts.size() - 1; i >= 0; i--) {
            HitboxPart part = parts.get(i);
            if (age - part.seenAge < 2) {
                continue;
            }
            if (active == null) {
                ArrayList<class_2960> found = new ArrayList<>(2);
                try {
                    PowerLookup.forEachEntry(owner, HitboxPartPower.ID, HitboxPartPower.Cfg.class, (id, cfg) -> found.add(id));
                } catch (RuntimeException e) {
                    // a failing condition counts as "not active"
                }
                active = found;
            }
            if (active.contains(powers.get(i))) {
                part.seenAge = age;
            } else {
                removeAt(i);
            }
        }
    }

    private void send(class_1309 owner, class_3222 player) {
        // Not gated on canSend: right after login the client's channel list may not have arrived yet, and a client
        // that does not know the channel just ignores the packet.
        if (player.field_13987 == null) {
            return;
        }
        HitboxNetwork.Part[] out = new HitboxNetwork.Part[parts.size()];
        for (int i = 0; i < out.length; i++) {
            HitboxPart p = parts.get(i);
            out[i] = new HitboxNetwork.Part(p.method_5628(), p.width, p.height, p.forward, p.up, p.right, p.scaleWithEntity);
        }
        class_2540 buf = PacketByteBufs.create();
        new HitboxNetwork.Parts(owner.method_5628(), out).write(buf);
        ServerPlayNetworking.send(player, HitboxNetwork.PARTS, buf);
    }
}
