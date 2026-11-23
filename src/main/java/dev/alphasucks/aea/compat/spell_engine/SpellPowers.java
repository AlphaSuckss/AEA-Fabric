package dev.alphasucks.aea.compat.spell_engine;

import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Active configs of a power type on either side. On the server this is {@link PowerLookup}; on the client, Spell Engine
 * asks about the local player (hotbar, cast bar, cooldowns), and the power types here are resolved by the server and
 * synced ({@code resolvesForClient}), so their conditions aren't run on the client.
 */
final class SpellPowers {
    private SpellPowers() {}

    static <C> void forEach(@Nullable class_1297 entity, class_2960 type, PowerTypeGate gate, Class<C> cls, Consumer<C> action) {
        if (entity == null) return;
        if (!entity.method_37908().field_9236) {
            if (gate.inUse()) PowerLookup.forEach(entity, type, cls, action);
            return;
        }
        PowerContainer container = PowerContainer.of(entity);
        if (container == null || container.isEmpty()) return;
        List<class_2960> powers = container.powersOfType(type);
        if (powers.isEmpty() || !Client.isLocal(entity)) return;
        for (int i = 0; i < powers.size(); i++) {
            class_2960 id = powers.get(i);
            if (container.isSuppressed(id) || !Client.isActive(id)) continue;
            Power power = ApoliPowers.get(id);
            if (power != null && cls.isInstance(power.config())) action.accept(cls.cast(power.config()));
        }
    }

    static boolean holds(@Nullable class_1297 entity, class_2960 type) {
        if (entity == null) return false;
        PowerContainer container = PowerContainer.of(entity);
        return container != null && !container.isEmpty() && !container.powersOfType(type).isEmpty();
    }

    /** Client-only lookups, in their own class so the server never loads them. */
    private static final class Client {
        static boolean isLocal(class_1297 entity) {
            return net.minecraft.class_310.method_1551().field_1724 == entity;
        }

        static boolean isActive(class_2960 id) {
            return dev.alphasucks.aea.port.client.ClientResolvedPowers.isActive(id);
        }
    }
}
