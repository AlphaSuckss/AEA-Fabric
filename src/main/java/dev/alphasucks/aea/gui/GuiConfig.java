package dev.alphasucks.aea.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.data.TextComponent;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_2561;
import net.minecraft.class_2960;

/**
 * Decoded {@code aea:gui} power configuration plus its compiled {@link GuiModel}. Immutable: in singleplayer
 * the same instance can be read by the client and the server thread.
 */
public final class GuiConfig {
    public static final MapCodec<GuiConfig> CODEC = RecordCodecBuilder.<GuiConfig>mapCodec(i -> i.group(
        IdCodecs.ID.optionalFieldOf("id").forGetter(GuiConfig::id),
        TextComponent.CODEC.optionalFieldOf("title").forGetter(GuiConfig::title),
        Codec.BOOL.optionalFieldOf("show_inventory", false).forGetter(GuiConfig::showInventory),
        Codec.INT.optionalFieldOf("inventory_x", 0).forGetter(GuiConfig::inventoryX),
        Codec.INT.optionalFieldOf("inventory_y", 0).forGetter(GuiConfig::inventoryY),
        Codec.BOOL.optionalFieldOf("show_vanilla_background", true).forGetter(GuiConfig::showVanillaBackground),
        Codec.intRange(1, 4096).optionalFieldOf("width").forGetter(GuiConfig::width),
        Codec.intRange(1, 4096).optionalFieldOf("height").forGetter(GuiConfig::height),
        Codec.BOOL.optionalFieldOf("memory", false).forGetter(GuiConfig::memory),
        Codec.BOOL.optionalFieldOf("pause_game", false).forGetter(GuiConfig::pauseGame),
        GuiElement.CODEC.listOf().optionalFieldOf("elements", List.of()).forGetter(GuiConfig::elements)
    ).apply(i, GuiConfig::new)).flatXmap(GuiConfig::validate, DataResult::success);

    private final Optional<class_2960> id;
    private final Optional<class_2561> title;
    private final boolean showInventory;
    private final int inventoryX;
    private final int inventoryY;
    private final boolean showVanillaBackground;
    private final Optional<Integer> width;
    private final Optional<Integer> height;
    private final boolean memory;
    private final boolean pauseGame;
    private final List<GuiElement> elements;
    private final GuiModel model;
    private final String error;

    public GuiConfig(Optional<class_2960> id, Optional<class_2561> title, boolean showInventory, int inventoryX, int inventoryY,
                     boolean showVanillaBackground, Optional<Integer> width, Optional<Integer> height, boolean memory, boolean pauseGame,
                     List<GuiElement> elements) {
        this.id = id;
        this.title = title;
        this.showInventory = showInventory;
        this.inventoryX = inventoryX;
        this.inventoryY = inventoryY;
        this.showVanillaBackground = showVanillaBackground;
        this.width = width;
        this.height = height;
        this.memory = memory;
        this.pauseGame = pauseGame;
        this.elements = List.copyOf(elements);
        GuiModel built = null;
        String err = null;
        try {
            built = new GuiModel(this.elements, showInventory);
        } catch (IllegalArgumentException e) {
            err = e.getMessage();
        }
        this.model = built;
        this.error = err;
    }

    private static DataResult<GuiConfig> validate(GuiConfig cfg) {
        if (cfg.error != null) {
            return DataResult.error(() -> "Invalid aea:gui: " + cfg.error);
        }
        if (cfg.pauseGame && cfg.model.hasInteractive()) {
            AEA.LOGGER.warn("[AEA] An aea:gui has \"pause_game\": true but contains slots, buttons, sliders or the player inventory; "
                + "it will NOT pause the game (a paused singleplayer server would stop handling their clicks).");
        }
        return DataResult.success(cfg);
    }

    public Optional<class_2960> id() {
        return id;
    }

    public Optional<class_2561> title() {
        return title;
    }

    public boolean showInventory() {
        return showInventory;
    }

    public int inventoryX() {
        return inventoryX;
    }

    public int inventoryY() {
        return inventoryY;
    }

    public boolean showVanillaBackground() {
        return showVanillaBackground;
    }

    public Optional<Integer> width() {
        return width;
    }

    public Optional<Integer> height() {
        return height;
    }

    public boolean memory() {
        return memory;
    }

    public boolean pauseGame() {
        return pauseGame;
    }

    /** True if the game may actually pause while this GUI is open. */
    public boolean pausesGame() {
        return pauseGame && !model.hasInteractive();
    }

    public List<GuiElement> elements() {
        return elements;
    }

    public GuiModel model() {
        return model;
    }

    /** The GUI id: the {@code id} field, or the power's id. */
    public class_2960 guiId(class_2960 powerId) {
        return id.orElse(powerId);
    }
}
