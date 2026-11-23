package dev.alphasucks.aea.bridge;

import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.Power;
import net.minecraft.class_1309;
import net.minecraft.class_2487;
import net.minecraft.class_2495;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * An Apoli 2.9.2 {@link Power} that runs one of Overgrown's flyweight power types.
 * <p>
 * Overgrown keeps per-power state (an int, an int array and an NBT compound) in its
 * own container; here it lives on the power instance and is saved/synced by Apoli.
 */
public class BridgedPower<C> extends Power implements io.github.apace100.apoli.power.HudRendered {
    private static final Set<class_1309> HOLDERS = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private final BridgePowerFactory<C> factory;
    private final C config;
    @Nullable
    private Integer auxInt;
    private int @Nullable [] auxInts;
    @Nullable
    private class_2487 auxNbt;

    public BridgedPower(io.github.apace100.apoli.power.PowerType<?> type, class_1309 entity, BridgePowerFactory<C> factory, C config) {
        super(type, entity);
        this.factory = factory;
        this.config = config;
        this.setTicking(factory.ticks());
    }

    public static Set<class_1309> holders() {
        synchronized (HOLDERS) {
            return Set.copyOf(HOLDERS);
        }
    }

    public C config() {
        return config;
    }

    public class_2960 typeId() {
        return factory.canonicalTypeId();
    }

    public class_2960 powerId() {
        return type.getIdentifier();
    }

    public BridgePowerFactory<C> bridgeFactory() {
        return factory;
    }

    public class_1309 entity() {
        return entity;
    }

    @Nullable
    private PowerContainerImpl container() {
        return PowerContainerImpl.of(entity);
    }

    /** Apoli condition check only (what Overgrown's lookups use). */
    public boolean conditionsMet() {
        return super.isActive();
    }

    @Override
    public boolean isActive() {
        if (config == null || !super.isActive()) {
            return false;
        }
        try {
            return factory.opoliType().isActive(powerId(), config, EntityCtx.of(entity, entity.method_37908()));
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Apoli calls this when the power is attached, including when it is loaded from disk. */
    @Override
    public void onAdded() {
        HOLDERS.add(entity);
    }

    /** Overgrown's onAdded means "granted" (not "loaded"), which is Apoli's onGained. */
    @Override
    public void onGained() {
        PowerContainerImpl c = container();
        if (c != null && config != null) {
            factory.opoliType().onAdded(powerId(), config, c, factory.lastSource(entity, type));
        }
    }

    /** Overgrown's onRemoved means "revoked", which is Apoli's onLost (onRemoved also fires on reloads). */
    @Override
    public void onLost() {
        PowerContainerImpl c = container();
        if (c != null && config != null) {
            factory.opoliType().onRemoved(powerId(), config, c, factory.lastSource(entity, type));
        }
    }

    @Override
    public void tick() {
        PowerContainerImpl c = container();
        if (c == null || config == null) {
            return;
        }
        if (conditionsMet()) {
            factory.opoliType().tick(powerId(), config, c);
        }
        factory.opoliType().tickStored(powerId(), config, c);
    }

    @Override
    public boolean shouldTickWhenInactive() {
        return true;
    }

    // ---- HUD: Overgrown's hud_render shown through Apoli 2.9.2's resource bar renderer ----

    @Nullable
    private dev.alphasucks.aea.port.data.HudRender.Entry hudEntry() {
        if (config == null) {
            return null;
        }
        dev.alphasucks.aea.port.data.HudRender hud;
        try {
            hud = factory.opoliType().hudRender(config);
        } catch (RuntimeException e) {
            return null;
        }
        if (hud == null || !hud.shouldRender()) {
            return null;
        }
        return hud.selectEntry(EntityCtx.of(entity, entity.method_37908()));
    }

    /** Fill in [0,1], or a negative value when Overgrown would not draw a bar. */
    private float computeFill(dev.alphasucks.aea.port.data.HudRender.Entry entry) {
        PowerContainerImpl c = container();
        if (c == null) {
            return -1f;
        }
        java.util.OptionalInt value = dev.alphasucks.aea.port.power.PowerResources.read(c, powerId());
        if (value.isEmpty()) {
            return -1f;
        }
        int current = value.getAsInt();
        if (factory.opoliType().isCooldown()) {
            if (current <= 0) {
                return -1f;
            }
            int bound = entry.max().isPresent()
                ? entry.max().get().evalIntWith(entity, c, current)
                : dev.alphasucks.aea.port.power.PowerResources.bound(c, powerId(), true).orElse(0);
            return bound <= 0 ? 1f : 1f - (float) current / bound;
        }
        int max;
        if (entry.max().isPresent()) {
            max = entry.max().get().evalIntWith(entity, c, current);
        } else {
            java.util.OptionalInt upper = dev.alphasucks.aea.port.power.PowerResources.bound(c, powerId(), true);
            if (upper.isEmpty() || upper.getAsInt() == Integer.MAX_VALUE) {
                return -1f;
            }
            max = upper.getAsInt();
        }
        java.util.OptionalInt lower = dev.alphasucks.aea.port.power.PowerResources.bound(c, powerId(), false);
        int min = lower.isPresent() && lower.getAsInt() != Integer.MIN_VALUE ? lower.getAsInt() : 0;
        if (max == min) {
            return current >= max ? 1f : 0f;
        }
        return Math.max(0f, Math.min(1f, (float) (current - min) / (max - min)));
    }

    @Override
    public io.github.apace100.apoli.util.HudRender getRenderSettings() {
        dev.alphasucks.aea.port.data.HudRender.Entry entry = hudEntry();
        if (entry == null) {
            return io.github.apace100.apoli.util.HudRender.DONT_RENDER;
        }
        return new io.github.apace100.apoli.util.HudRender(true, entry.barIndex(), entry.spriteLocation(), null, entry.inverted());
    }

    @Override
    public float getFill() {
        dev.alphasucks.aea.port.data.HudRender.Entry entry = hudEntry();
        return entry == null ? 0f : Math.max(0f, computeFill(entry));
    }

    @Override
    public boolean shouldRender() {
        dev.alphasucks.aea.port.data.HudRender.Entry entry = hudEntry();
        return entry != null && computeFill(entry) >= 0f;
    }

    // ---- per power state ------------------------------------------------------------

    public @Nullable Integer auxInt() {
        return auxInt;
    }

    public void setAuxInt(@Nullable Integer value) {
        this.auxInt = value;
    }

    public int @Nullable [] auxInts() {
        return auxInts;
    }

    public void setAuxInts(int @Nullable [] values) {
        this.auxInts = values;
    }

    public class_2487 auxNbt() {
        if (auxNbt == null) {
            auxNbt = new class_2487();
        }
        return auxNbt;
    }

    public void setAuxNbt(@Nullable class_2487 tag) {
        this.auxNbt = tag;
    }

    public void clearAux() {
        auxInt = null;
        auxInts = null;
        auxNbt = null;
    }

    public void sync() {
        if (!entity.method_37908().method_8608()) {
            PowerHolderComponent.syncPower(entity, type);
        }
    }

    @Override
    public class_2520 toTag() {
        class_2487 tag = new class_2487();
        if (auxInt != null) {
            tag.method_10569("Int", auxInt);
        }
        if (auxInts != null) {
            tag.method_10539("Ints", auxInts);
        }
        if (auxNbt != null && !auxNbt.method_33133()) {
            tag.method_10566("Data", auxNbt.method_10553());
        }
        return tag;
    }

    @Override
    public void fromTag(class_2520 tag) {
        if (!(tag instanceof class_2487 c)) {
            return;
        }
        auxInt = c.method_10545("Int") ? c.method_10550("Int") : null;
        auxInts = c.method_10545("Ints") ? c.method_10561("Ints") : null;
        auxNbt = c.method_10545("Data") ? c.method_10562("Data") : null;
    }
}
