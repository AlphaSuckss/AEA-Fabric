package dev.alphasucks.aea.port.power;

import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.network.payload.SyncResolvedPowersS2C;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;

/**
 * Overgrown's server side of "resolved for client" power types ({@link PowerType#resolvesForClient()}): every tick
 * the server works out which of each player's powers of those types are active (held, not suppressed, condition
 * passes) and sends the list when it changes. The client only reads it ({@code ClientResolvedPowers}), so a
 * condition that only the server can check still works. This was missing from the Fabric port, so client-side types
 * such as {@code apoli:zoom}, {@code apoli:modify_fov}, {@code apoli:modify_camera} and
 * {@code apoli:modify_hearing_range} never turned on.
 */
public final class ResolvedPowers {
    private ResolvedPowers() {
    }

    private record Sent(class_2960[] powers, int[] targets) {
    }

    private static final Map<UUID, Sent> SENT = new HashMap<>();
    private static final List<class_2960> SCRATCH = new ArrayList<>();
    private static int[] scratchTargets = new int[8];
    private static class_2960[] types;
    private static PowerTypeGate[] gates = new PowerTypeGate[0];

    public static void tick(MinecraftServer server) {
        if (types == null) {
            build();
        }
        if (types.length == 0) {
            return;
        }
        boolean anyInUse = false;
        for (PowerTypeGate gate : gates) {
            if (gate.inUse()) {
                anyInUse = true;
                break;
            }
        }
        if (!anyInUse && SENT.isEmpty()) {
            return;
        }
        List<class_3222> players = server.method_3760().method_14571();
        for (int p = 0; p < players.size(); p++) {
            class_3222 player = players.get(p);
            UUID uuid = player.method_5667();
            Sent sent = SENT.get(uuid);
            if (anyInUse) {
                resolve(player, sent);
            } else {
                SCRATCH.clear();
            }
            if (matches(sent)) {
                continue;
            }
            int size = SCRATCH.size();
            if (size == 0) {
                SENT.remove(uuid);
                ApoliNetwork.sendResolvedPowers(player, new SyncResolvedPowersS2C(List.of(), new int[0]));
                continue;
            }
            class_2960[] powers = SCRATCH.toArray(new class_2960[0]);
            int[] targets = Arrays.copyOf(scratchTargets, size);
            SENT.put(uuid, new Sent(powers, targets));
            ApoliNetwork.sendResolvedPowers(player, new SyncResolvedPowersS2C(List.of(powers), targets));
        }
        SCRATCH.clear();
    }

    public static void forget(UUID player) {
        SENT.remove(player);
    }

    public static void clear() {
        SENT.clear();
        SCRATCH.clear();
        types = null;
    }

    private static void build() {
        List<class_2960> found = new ArrayList<>();
        for (Map.Entry<class_2960, PowerType<?>> entry : PowerTypeRegistry.view().entrySet()) {
            if (entry.getValue().resolvesForClient()) {
                found.add(entry.getKey());
            }
        }
        class_2960[] built = found.toArray(new class_2960[0]);
        PowerTypeGate[] builtGates = new PowerTypeGate[built.length];
        for (int i = 0; i < built.length; i++) {
            builtGates[i] = new PowerTypeGate(built[i]);
        }
        gates = builtGates;
        types = built;
    }

    private static void resolve(class_3222 player, Sent sent) {
        SCRATCH.clear();
        PowerContainer container = PowerContainer.of(player);
        if (container == null || container.isEmpty()) {
            return;
        }
        class_2960[] typeIds = types;
        EntityCtx ctx = null;
        for (int t = 0; t < typeIds.length; t++) {
            if (!gates[t].inUse()) {
                continue;
            }
            List<class_2960> powers = container.powersOfType(typeIds[t]);
            for (int i = 0, n = powers.size(); i < n; i++) {
                class_2960 powerId = powers.get(i);
                if (container.isSuppressed(powerId)) {
                    continue;
                }
                Power power = ApoliPowers.get(powerId);
                if (power == null) {
                    continue;
                }
                PowerType<?> type = PowerTypeRegistry.get(power.typeId());
                if (type == null) {
                    continue;
                }
                if (power.condition().isPresent()) {
                    if (ctx == null) {
                        ctx = EntityCtx.of(player, player.method_37908());
                    }
                    if (!power.condition().get().test(ctx)) {
                        continue;
                    }
                }
                int index = SCRATCH.size();
                if (index == scratchTargets.length) {
                    scratchTargets = Arrays.copyOf(scratchTargets, index * 2);
                }
                SCRATCH.add(powerId);
                scratchTargets[index] = invokeTarget(type, powerId, power.config(), player, previousTarget(sent, powerId));
            }
        }
    }

    private static int previousTarget(Sent sent, class_2960 powerId) {
        if (sent == null) {
            return -1;
        }
        class_2960[] powers = sent.powers();
        for (int i = 0; i < powers.length; i++) {
            if (powers[i].equals(powerId)) {
                return sent.targets()[i];
            }
        }
        return -1;
    }

    private static boolean matches(Sent sent) {
        int size = SCRATCH.size();
        if (sent == null) {
            return size == 0;
        }
        if (sent.powers().length != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!sent.powers()[i].equals(SCRATCH.get(i)) || sent.targets()[i] != scratchTargets[i]) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int invokeTarget(PowerType type, class_2960 powerId, Object cfg, class_3222 player, int current) {
        return type.clientTarget(powerId, cfg, player, current);
    }
}
