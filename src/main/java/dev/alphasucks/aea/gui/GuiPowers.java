package dev.alphasucks.aea.gui;

import dev.alphasucks.aea.bridge.BridgedPower;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import io.github.apace100.apoli.component.PowerHolderComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

/** Lookups of {@code aea:gui} powers on an entity (via the entity's own power component, never the shared registry). */
public final class GuiPowers {
    private GuiPowers() {
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static BridgedPower<GuiConfig> find(@Nullable class_1297 entity, class_2960 powerId) {
        for (BridgedPower<GuiConfig> p : all(entity)) {
            if (p.powerId().equals(powerId)) {
                return p;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static List<BridgedPower<GuiConfig>> all(@Nullable class_1297 entity) {
        if (entity == null) {
            return List.of();
        }
        Optional<PowerHolderComponent> component;
        try {
            component = PowerHolderComponent.KEY.maybeGet(entity);
        } catch (RuntimeException e) {
            return List.of();   // components not attached yet (entity still being constructed)
        }
        if (component.isEmpty()) {
            return List.of();
        }
        List<BridgedPower<GuiConfig>> out = new ArrayList<>(2);
        for (io.github.apace100.apoli.power.Power p : component.get().getPowers()) {
            if (p instanceof BridgedPower<?> bp && AeaGui.POWER_TYPE_ID.equals(bp.typeId()) && bp.config() instanceof GuiConfig) {
                out.add((BridgedPower<GuiConfig>) bp);
            }
        }
        return out;
    }

    /** The config of a GUI power: the entity's own power first, the loaded power table as a fallback. */
    @Nullable
    public static GuiConfig config(@Nullable class_1297 entity, class_2960 powerId) {
        BridgedPower<GuiConfig> own = find(entity, powerId);
        if (own != null) {
            return own.config();
        }
        Power loaded = ApoliPowers.get(powerId);
        return loaded != null && loaded.config() instanceof GuiConfig cfg ? cfg : null;
    }

    /** A GUI power held by the entity whose GUI id is {@code guiId}. */
    @Nullable
    public static BridgedPower<GuiConfig> byGuiId(@Nullable class_1297 entity, class_2960 guiId) {
        for (BridgedPower<GuiConfig> p : all(entity)) {
            if (p.config().guiId(p.powerId()).equals(guiId)) {
                return p;
            }
        }
        return null;
    }

    /** Server thread: the GUI being built for a player (conditions evaluated on open already see it). */
    private static final ThreadLocal<GuiScreenHandler> OPENING = new ThreadLocal<>();

    static void beginOpening(GuiScreenHandler handler) {
        OPENING.set(handler);
    }

    static void endOpening() {
        OPENING.remove();
    }

    /** The AEA GUI the player has open on this side (or that is opening right now), or null. */
    @Nullable
    public static GuiScreenHandler open(@Nullable class_1297 entity) {
        GuiScreenHandler opening = OPENING.get();
        if (opening != null && opening.player == entity) {
            return opening;
        }
        return entity instanceof class_1657 player && player.field_7512 instanceof GuiScreenHandler h && !h.isClosed() ? h : null;
    }
}
