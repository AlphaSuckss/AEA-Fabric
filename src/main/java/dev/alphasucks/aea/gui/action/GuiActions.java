package dev.alphasucks.aea.gui.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.gui.AeaGui;
import dev.alphasucks.aea.gui.GuiPlaceholders;
import dev.alphasucks.aea.gui.GuiPowers;
import dev.alphasucks.aea.gui.GuiScreenHandler;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.action.ItemAction;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.gui.GuiSlot;
import io.github.apace100.apoli.Apoli;
import java.util.Optional;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_2168;
import net.minecraft.class_2165;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/** Entity actions of the {@code aea:gui} feature (all run on the server with the player as the entity). */
public final class GuiActions {
    private GuiActions() {
    }

    public static void register() {
        ActionTypes.ENTITY.register(new class_2960("aea", "open_gui"), new OpenGui());
        ActionTypes.ENTITY.register(new class_2960("aea", "close_gui"), new CloseGui());
        ActionTypes.ENTITY.register(new class_2960("aea", "gui_command"), new GuiCommand());
        ActionTypes.ENTITY.register(new class_2960("aea", "gui_slot_action"), new GuiSlotItemAction());
        ActionTypes.ENTITY.register(new class_2960("aea", "modify_gui_slot"), new ModifyGuiSlotAction());
        ActionTypes.ENTITY.register(new class_2960("aea", "gui_craft"), new GuiCraftAction());
    }

    /** The open GUI of a server player, or null. */
    @Nullable
    static GuiScreenHandler serverGui(@Nullable class_1297 entity) {
        GuiScreenHandler h = GuiPowers.open(entity);
        return h != null && !h.isClient() && h.model != null ? h : null;
    }

    // ------------------------------------------------------------------ aea:open_gui

    public record OpenCfg(class_2960 power) {
    }

    public static final class OpenGui implements ActionType<EntityCtx, OpenCfg> {
        @Override
        public MapCodec<OpenCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(IdCodecs.ID.fieldOf("power").forGetter(OpenCfg::power)).apply(i, OpenCfg::new));
        }

        @Override
        public void run(OpenCfg cfg, EntityCtx ctx) {
            if (ctx.entity() instanceof class_3222 player) {
                AeaGui.open(player, cfg.power());
            }
        }
    }

    // ------------------------------------------------------------------ aea:close_gui

    public record CloseCfg(Optional<class_2960> gui) {
    }

    public static final class CloseGui implements ActionType<EntityCtx, CloseCfg> {
        @Override
        public MapCodec<CloseCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(IdCodecs.ID.optionalFieldOf("gui").forGetter(CloseCfg::gui)).apply(i, CloseCfg::new));
        }

        @Override
        public void run(CloseCfg cfg, EntityCtx ctx) {
            GuiScreenHandler h = serverGui(ctx.entity());
            if (h != null && ctx.entity() instanceof class_3222 player && (cfg.gui().isEmpty() || cfg.gui().get().equals(h.guiId))) {
                if (h.session() != null && h.session().closing()) {
                    return;
                }
                player.method_7346();
            }
        }
    }

    // ------------------------------------------------------------------ aea:gui_command

    public record CommandCfg(String command) {
    }

    /** {@code apoli:execute_command} (same source, permission level and output setting as Apoli 2.9.2) with GUI placeholders. */
    public static final class GuiCommand implements ActionType<EntityCtx, CommandCfg> {
        @Override
        public MapCodec<CommandCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(Codec.STRING.fieldOf("command").forGetter(CommandCfg::command)).apply(i, CommandCfg::new));
        }

        @Override
        public void run(CommandCfg cfg, EntityCtx ctx) {
            class_1297 entity = ctx.entity();
            if (entity == null) {
                return;
            }
            MinecraftServer server = entity.method_37908().method_8503();
            if (server == null) {
                return;
            }
            GuiScreenHandler h = serverGui(entity);
            int own = h == null ? -1 : contextSlider(h);
            String command = GuiPlaceholders.resolver(h, entity instanceof class_1657 p ? p : null, own, e -> h.sliders[e]).apply(cfg.command());
            int level = Apoli.config != null && Apoli.config.executeCommand != null ? Apoli.config.executeCommand.permissionLevel : 2;
            boolean showOutput = Apoli.config != null && Apoli.config.executeCommand != null && Apoli.config.executeCommand.showOutput;
            boolean validOutput = !(entity instanceof class_3222 player) || player.field_13987 != null;
            class_2168 source = new class_2168(
                showOutput && validOutput ? (class_2165) entity : class_2165.field_17395,
                entity.method_19538(),
                entity.method_5802(),
                entity.method_37908() instanceof class_3218 world ? world : null,
                level,
                entity.method_5477().getString(),
                entity.method_5476(),
                server,
                entity
            );
            server.method_3734().method_44252(source, command);
        }
    }

    static int contextSlider(GuiScreenHandler h) {
        return h.contextSlider();
    }

    // ------------------------------------------------------------------ aea:gui_slot_action

    public record SlotActionCfg(String slot, ItemAction itemAction) {
    }

    public static final class GuiSlotItemAction implements ActionType<EntityCtx, SlotActionCfg> {
        @Override
        public MapCodec<SlotActionCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("slot").forGetter(SlotActionCfg::slot),
                ItemAction.CODEC.fieldOf("item_action").forGetter(SlotActionCfg::itemAction)
            ).apply(i, SlotActionCfg::new));
        }

        @Override
        public void run(SlotActionCfg cfg, EntityCtx ctx) {
            GuiScreenHandler h = serverGui(ctx.entity());
            if (h == null) {
                return;
            }
            class_1657 player = (class_1657) ctx.entity();
            for (GuiSlot slot : h.resolveSlots(cfg.slot())) {
                class_1799 stack = slot.method_7677();
                cfg.itemAction().run(new ItemCtx(stack, player.method_37908(), player));
                if (stack.method_7960() && slot.method_7677() == stack) {
                    slot.method_7673(class_1799.field_8037);
                } else {
                    slot.method_7668();
                }
            }
        }
    }
}
