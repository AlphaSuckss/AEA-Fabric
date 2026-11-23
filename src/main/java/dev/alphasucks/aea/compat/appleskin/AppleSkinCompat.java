package dev.alphasucks.aea.compat.appleskin;

import dev.alphasucks.aea.AEA;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ModifyFoodPower;
import io.github.apace100.apoli.util.modifier.Modifier;
import io.github.apace100.apoli.util.modifier.ModifierUtil;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import squeek.appleskin.api.AppleSkinApi;
import squeek.appleskin.api.event.FoodValuesEvent;
import squeek.appleskin.api.food.FoodValues;

import java.util.ArrayList;
import java.util.List;

/**
 * AppleSkin compatibility, loaded through AppleSkin's own {@code appleskin} entrypoint, so it only runs when AppleSkin
 * is installed; nothing else in AEA refers to this package.
 *
 * <p>Apoli's {@code apoli:modify_food} changes hunger and saturation when food is eaten, not in the item itself, so
 * AppleSkin's tooltip and hunger bar preview show the unchanged values. AppleSkin asks for a food's values through
 * {@link FoodValuesEvent}; this answers with the same calculation Apoli uses when the food is eaten.
 */
public final class AppleSkinCompat implements AppleSkinApi {
    private static boolean failed;

    @Override
    public void registerEvents() {
        FoodValuesEvent.EVENT.register(AppleSkinCompat::onFoodValues);
        AEA.LOGGER.info("[AEA] AppleSkin found: it shows apoli:modify_food values.");
    }

    private static void onFoodValues(FoodValuesEvent event) {
        if (failed || event.player == null || event.modifiedFoodValues == null || event.itemStack == null || event.itemStack.method_7960()) return;
        try {
            FoodValues modified = modify(event.player, event.itemStack, event.modifiedFoodValues);
            if (modified != null) event.modifiedFoodValues = modified;
        } catch (RuntimeException | LinkageError e) {
            // A display hook must never break AppleSkin's tooltip; stop trying after the first failure.
            failed = true;
            AEA.LOGGER.warn("[AEA] AppleSkin compat turned off: {}", e.toString());
        }
    }

    /** Apoli's own rule: every active modify_food power whose item_condition passes adds its modifiers. */
    private static FoodValues modify(class_1657 player, class_1799 stack, FoodValues values) {
        List<ModifyFoodPower> powers = PowerHolderComponent.getPowers(player, ModifyFoodPower.class);
        if (powers.isEmpty()) return null;
        List<Modifier> hunger = null, saturation = null;
        for (int i = 0; i < powers.size(); i++) {
            ModifyFoodPower p = powers.get(i);
            if (!p.doesApply(stack)) continue;
            if (!p.getFoodModifiers().isEmpty()) {
                if (hunger == null) hunger = new ArrayList<>();
                hunger.addAll(p.getFoodModifiers());
            }
            if (!p.getSaturationModifiers().isEmpty()) {
                if (saturation == null) saturation = new ArrayList<>();
                saturation.addAll(p.getSaturationModifiers());
            }
        }
        if (hunger == null && saturation == null) return null;
        int h = hunger == null ? values.hunger : (int) ModifierUtil.applyModifiers(player, hunger, values.hunger);
        float s = saturation == null ? values.saturationModifier
            : (float) ModifierUtil.applyModifiers(player, saturation, values.saturationModifier);
        return new FoodValues(Math.max(0, h), s);
    }
}
