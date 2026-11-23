package dev.alphasucks.aea.port.power;

import dev.alphasucks.aea.bridge.AeaSuppression;
import dev.alphasucks.aea.bridge.BridgedPower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.VariableIntPower;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

/**
 * AEA: a stateless view that implements Overgrown's {@link PowerContainer} on top of Apoli
 * 2.9.2's {@link PowerHolderComponent}. Per-power state lives on {@link BridgedPower}.
 */
public final class PowerContainerImpl implements PowerContainer {
    private final class_1309 owner;
    private final PowerHolderComponent component;

    private PowerContainerImpl(class_1309 owner, PowerHolderComponent component) {
        this.owner = owner;
        this.component = component;
    }

    @Nullable
    public static PowerContainerImpl of(@Nullable class_1297 entity) {
        if (!(entity instanceof class_1309 living)) {
            return null;
        }
        // AEA: hooks like eye height / dimensions run inside the Entity constructor, before Cardinal
        // Components has attached anything; KEY.getNullable NPEs there ("Couldn't place player in world").
        if (!(((Object) living) instanceof dev.onyxstudios.cca.api.v3.component.ComponentProvider provider)
            || provider.getComponentContainer() == null) {
            return null;
        }
        PowerHolderComponent component;
        try {
            component = PowerHolderComponent.KEY.getNullable(living);
        } catch (NullPointerException e) {
            return null;
        }
        return component == null ? null : new PowerContainerImpl(living, component);
    }

    public PowerHolderComponent component() {
        return component;
    }

    @Nullable
    private static io.github.apace100.apoli.power.PowerType<?> type(class_2960 id) {
        return io.github.apace100.apoli.power.PowerTypeRegistry.contains(id) ? io.github.apace100.apoli.power.PowerTypeRegistry.get(id) : null;
    }

    @Nullable
    private io.github.apace100.apoli.power.Power instance(class_2960 id) {
        io.github.apace100.apoli.power.PowerType<?> t = type(id);
        if (t == null || !component.hasPower(t)) {
            return null;
        }
        return component.getPower(t);
    }

    @Nullable
    public BridgedPower<?> bridged(class_2960 id) {
        return instance(id) instanceof BridgedPower<?> b ? b : null;
    }

    // ------------------------------------------------------------------ structure

    @Override
    public boolean addPower(class_2960 power, class_2960 source) {
        io.github.apace100.apoli.power.PowerType<?> t = type(power);
        if (t == null || owner.method_37908().method_8608()) {
            return false;
        }
        boolean added = component.addPower(t, source);
        if (added) {
            component.sync();
        }
        return added;
    }

    @Override
    public boolean removePower(class_2960 power, class_2960 source) {
        io.github.apace100.apoli.power.PowerType<?> t = type(power);
        if (t == null || !component.hasPower(t, source) || owner.method_37908().method_8608()) {
            return false;
        }
        component.removePower(t, source);
        component.sync();
        return true;
    }

    @Override
    public boolean removeAllFromSource(class_2960 source) {
        if (owner.method_37908().method_8608()) {
            return false;
        }
        int removed = component.removeAllPowersFromSource(source);
        if (removed > 0) {
            component.sync();
        }
        return removed > 0;
    }

    @Override
    public boolean removePowerCompletely(class_2960 power) {
        io.github.apace100.apoli.power.PowerType<?> t = type(power);
        if (t == null || !component.hasPower(t) || owner.method_37908().method_8608()) {
            return false;
        }
        for (class_2960 source : List.copyOf(component.getSources(t))) {
            component.removePower(t, source);
        }
        component.sync();
        return true;
    }

    @Override
    public void clear() {
        if (owner.method_37908().method_8608()) {
            return;
        }
        for (io.github.apace100.apoli.power.PowerType<?> t : List.copyOf(component.getPowerTypes(false))) {
            for (class_2960 source : List.copyOf(component.getSources(t))) {
                component.removePower(t, source);
            }
        }
        component.sync();
    }

    @Override
    public boolean hasPower(class_2960 power) {
        io.github.apace100.apoli.power.PowerType<?> t = type(power);
        return t != null && component.hasPower(t);
    }

    @Override
    public boolean isEmpty() {
        return component.getPowers().isEmpty();
    }

    @Override
    public List<class_2960> powersOfType(class_2960 canonicalTypeId) {
        List<class_2960> out = null;
        for (io.github.apace100.apoli.power.Power p : component.getPowers()) {
            if (p instanceof BridgedPower<?> b && b.typeId().equals(canonicalTypeId)) {
                if (out == null) {
                    out = new ArrayList<>(2);
                }
                out.add(b.powerId());
            }
        }
        return out == null ? List.of() : out;
    }

    @Override
    public Set<class_2960> sourcesOf(class_2960 power) {
        io.github.apace100.apoli.power.PowerType<?> t = type(power);
        return t == null || !component.hasPower(t) ? Set.of() : new LinkedHashSet<>(component.getSources(t));
    }

    @Override
    public Set<class_2960> allPowers() {
        Set<class_2960> out = new LinkedHashSet<>();
        for (io.github.apace100.apoli.power.PowerType<?> t : component.getPowerTypes(true)) {
            out.add(t.getIdentifier());
        }
        return out;
    }

    @Override
    public Set<class_2960> allSources() {
        Set<class_2960> out = new LinkedHashSet<>();
        for (io.github.apace100.apoli.power.PowerType<?> t : component.getPowerTypes(false)) {
            out.addAll(component.getSources(t));
        }
        return out;
    }

    // ---------------------------------------------------------------- suppression

    @Override
    public boolean suppressPower(class_2960 power, class_2960 source) {
        return AeaSuppression.suppress(owner, power, source);
    }

    @Override
    public boolean unsuppressPower(class_2960 power, class_2960 source) {
        return AeaSuppression.unsuppress(owner, power, source);
    }

    @Override
    public boolean isSuppressed(class_2960 power) {
        return AeaSuppression.isSuppressed(owner, power);
    }

    @Override
    public Set<class_2960> suppressedPowers() {
        return AeaSuppression.suppressed(owner);
    }

    @Override
    public Set<class_2960> suppressionSourcesOf(class_2960 power) {
        return AeaSuppression.sourcesOf(owner, power);
    }

    // ---------------------------------------------------------------------- owner

    @Override
    public class_1309 owner() {
        return owner;
    }

    @Override
    public class_1297 rawOwner() {
        return owner;
    }

    @Override
    public void markDirty() {
        if (!owner.method_37908().method_8608()) {
            component.sync();
        }
    }

    // ------------------------------------------------------------------------ aux

    @Override
    public OptionalInt getAuxInt(class_2960 powerId) {
        io.github.apace100.apoli.power.Power p = instance(powerId);
        if (p instanceof BridgedPower<?> b) {
            Integer v = b.auxInt();
            return v == null ? OptionalInt.empty() : OptionalInt.of(v);
        }
        if (p instanceof VariableIntPower v) {
            return OptionalInt.of(v.getValue());
        }
        if (p instanceof CooldownPower c) {
            return OptionalInt.of(c.getRemainingTicks());
        }
        return OptionalInt.empty();
    }

    /** Writes an int to the power: aux state for ported powers, the value for Apoli resources, the remaining ticks for cooldowns. */
    public void setAuxInt(class_2960 powerId, int value) {
        io.github.apace100.apoli.power.Power p = instance(powerId);
        if (p instanceof BridgedPower<?> b) {
            Integer prev = b.auxInt();
            b.setAuxInt(value);
            if (prev == null || prev != value) {
                b.sync();
            }
        } else if (p instanceof VariableIntPower v) {
            if (v.getValue() != value) {
                v.setValue(value);
                PowerHolderComponent.syncPower(owner, p.getType());
            }
        } else if (p instanceof CooldownPower c) {
            c.setCooldown(value);
            PowerHolderComponent.syncPower(owner, p.getType());
        }
    }

    @Override
    public int @Nullable [] getAuxInts(class_2960 powerId) {
        BridgedPower<?> b = bridged(powerId);
        return b == null ? null : b.auxInts();
    }

    public void setAuxInts(class_2960 powerId, int[] values) {
        BridgedPower<?> b = bridged(powerId);
        if (b != null) {
            int[] prev = b.auxInts();
            b.setAuxInts(values);
            if (prev == null || !java.util.Arrays.equals(prev, values)) {
                b.sync();
            }
        }
    }

    public int[] auxIntsAtLeast(class_2960 powerId, int length, int fill) {
        BridgedPower<?> b = bridged(powerId);
        int[] existing = b == null ? null : b.auxInts();
        if (existing != null && existing.length >= length) {
            return existing;
        }
        int[] grown = new int[length];
        if (fill != 0) {
            java.util.Arrays.fill(grown, fill);
        }
        if (existing != null) {
            System.arraycopy(existing, 0, grown, 0, existing.length);
        }
        if (b != null) {
            b.setAuxInts(grown);
            b.sync();
        }
        return grown;
    }

    public void trimAuxInts(class_2960 powerId, int maxLength) {
        BridgedPower<?> b = bridged(powerId);
        int[] existing = b == null ? null : b.auxInts();
        if (existing != null && existing.length > maxLength) {
            b.setAuxInts(maxLength <= 0 ? null : java.util.Arrays.copyOf(existing, maxLength));
            b.sync();
        }
    }

    public class_2487 getAuxNbt(class_2960 powerId) {
        BridgedPower<?> b = bridged(powerId);
        return b == null ? new class_2487() : b.auxNbt();
    }

    public void setAuxNbt(class_2960 powerId, class_2487 tag) {
        BridgedPower<?> b = bridged(powerId);
        if (b != null) {
            b.setAuxNbt(tag);
            b.sync();
        }
    }

    public void removeAux(class_2960 powerId) {
        BridgedPower<?> b = bridged(powerId);
        if (b != null) {
            b.clearAux();
            b.sync();
        }
    }

    public Set<class_2960> heldTypes() {
        Set<class_2960> out = new HashSet<>();
        for (io.github.apace100.apoli.power.Power p : component.getPowers()) {
            if (p instanceof BridgedPower<?> b) {
                out.add(b.typeId());
            }
        }
        return out;
    }
}
