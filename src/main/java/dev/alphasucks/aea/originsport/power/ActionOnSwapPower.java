package dev.alphasucks.aea.originsport.power;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.originsport.OriginsPortIds;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** {@code origins:action_on_swap}: runs an entity action when the holder swaps origins on a layer. */
public final class ActionOnSwapPower extends PowerType<ActionOnSwapPower.Config> {
    public static final class_2960 ID = OriginsPortIds.origins("action_on_swap");

    @Override
    public MapCodec<Config> configCodec() {
        return RecordCodecBuilder.mapCodec(i -> i.group(
            LoggedOptionalField.of("entity_action", EntityAction.CODEC).forGetter(Config::entityAction),
            class_2960.field_25139.optionalFieldOf("layer").forGetter(Config::layer),
            class_2960.field_25139.optionalFieldOf("from_origin").forGetter(Config::fromOrigin),
            class_2960.field_25139.optionalFieldOf("to_origin").forGetter(Config::toOrigin)
        ).apply(i, Config::new));
    }

    public static void fire(class_3222 player, class_2960 layerId, @Nullable class_2960 from, class_2960 to) {
        EntityCtx ctx = new EntityCtx(player, player.method_37908());
        PowerLookup.forEach(player, ID, Config.class, cfg -> {
            if (cfg.layer().isPresent() && !cfg.layer().get().equals(layerId)) {
                return;
            }
            if (cfg.fromOrigin().isPresent() && !cfg.fromOrigin().get().equals(from)) {
                return;
            }
            if (cfg.toOrigin().isPresent() && !cfg.toOrigin().get().equals(to)) {
                return;
            }
            cfg.entityAction().ifPresent(action -> action.run(ctx));
        });
    }

    public record Config(Optional<EntityAction> entityAction, Optional<class_2960> layer, Optional<class_2960> fromOrigin, Optional<class_2960> toOrigin) {
    }
}
