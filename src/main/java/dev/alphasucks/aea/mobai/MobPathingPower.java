package dev.alphasucks.aea.mobai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.data.Expression;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1308;
import net.minecraft.class_1408;
import net.minecraft.class_2960;
import net.minecraft.class_7;
import org.jetbrains.annotations.Nullable;

/**
 * {@code aea:mob_pathing}: changes how the holder (any mob) plans routes. See {@code AEA_PORTED_TYPES.md}.
 */
public final class MobPathingPower extends PowerType<MobPathingPower.Config> {
    public static final class_2960 ID = new class_2960("aea", "mob_pathing");
    private static final PowerTypeGate GATE = new PowerTypeGate(ID);

    /** Vanilla node type names, in enum order (class_7). */
    private static final String[] NODE_NAMES = {
        "blocked", "open", "walkable", "walkable_door", "trapdoor", "powder_snow", "danger_powder_snow", "fence", "lava",
        "water", "water_border", "rail", "unpassable_rail", "danger_fire", "damage_fire", "danger_other", "damage_other",
        "door_open", "door_wood_closed", "door_iron_closed", "breach", "leaves", "sticky_honey", "cocoa", "damage_cautious"
    };

    private static final Codec<class_7> NODE_TYPE = Codec.STRING.comapFlatMap(MobPathingPower::nodeType, MobPathingPower::nodeName);

    private static DataResult<class_7> nodeType(String raw) {
        String name = raw.toLowerCase(Locale.ROOT);
        if (name.startsWith("minecraft:")) {
            name = name.substring("minecraft:".length());
        }
        class_7[] values = class_7.values();
        for (int i = 0; i < NODE_NAMES.length && i < values.length; i++) {
            if (NODE_NAMES[i].equals(name)) {
                return DataResult.success(values[i]);
            }
        }
        String finalName = name;
        return DataResult.error(() -> "Unknown path node type '" + finalName + "'. Known: " + String.join(", ", NODE_NAMES));
    }

    private static String nodeName(class_7 type) {
        int i = type.ordinal();
        return i < NODE_NAMES.length ? NODE_NAMES[i] : type.name().toLowerCase(Locale.ROOT);
    }

    public record Avoid(BlockCondition blockCondition, float penalty) {
        public static final Codec<Avoid> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockCondition.CODEC.fieldOf("block_condition").forGetter(Avoid::blockCondition),
            Codec.FLOAT.optionalFieldOf("penalty", 8.0F).forGetter(Avoid::penalty)
        ).apply(i, Avoid::new));
    }

    public record Config(
        Optional<Expression> jumpHeight,
        Optional<Expression> dropHeight,
        boolean airbornePathing,
        double airborneReach,
        Optional<BlockCondition> walkable,
        Optional<BlockCondition> blocked,
        List<Avoid> avoid,
        Map<class_7, Float> penalties,
        int repathInterval
    ) {
        @Nullable
        public Float penaltyFor(class_7 type) {
            return penalties.isEmpty() ? null : penalties.get(type);
        }

        public boolean hasBlockRules() {
            return walkable.isPresent() || blocked.isPresent() || !avoid.isEmpty();
        }
    }

    @Override
    public MapCodec<Config> configCodec() {
        return RecordCodecBuilder.mapCodec(i -> i.group(
            Expression.DOUBLE_OR_EXPR.optionalFieldOf("jump_height").forGetter(Config::jumpHeight),
            Expression.DOUBLE_OR_EXPR.optionalFieldOf("drop_height").forGetter(Config::dropHeight),
            Codec.BOOL.optionalFieldOf("airborne_pathing", false).forGetter(Config::airbornePathing),
            Codec.DOUBLE.optionalFieldOf("airborne_reach", 4.0).forGetter(Config::airborneReach),
            LoggedOptionalField.strict("walkable", BlockCondition.CODEC).forGetter(Config::walkable),
            LoggedOptionalField.strict("blocked", BlockCondition.CODEC).forGetter(Config::blocked),
            MobAiPower.strictOr("avoid", Avoid.CODEC.listOf(), List.<Avoid>of()).forGetter(Config::avoid),
            MobAiPower.strictOr("penalties", Codec.unboundedMap(NODE_TYPE, Codec.FLOAT)
                .xmap(m -> m.isEmpty() ? Map.<class_7, Float>of() : (Map<class_7, Float>) new EnumMap<>(m), m -> m), Map.<class_7, Float>of())
                .forGetter(Config::penalties),
            Codec.INT.optionalFieldOf("repath_interval", 0).forGetter(Config::repathInterval)
        ).apply(i, Config::new));
    }

    @Override
    public void tick(class_2960 powerId, Config cfg, PowerContainer holder) {
        if (cfg.repathInterval() <= 0 || !(holder.rawOwner() instanceof class_1308 mob) || mob.method_37908().field_9236) {
            return;
        }
        if (mob.field_6012 % cfg.repathInterval() == 0) {
            class_1408 nav = mob.method_5942();
            if (!nav.method_6357() && nav instanceof Repathable repathable) {
                repathable.aea$repath();
            }
        }
    }

    /** The mob's active pathing config (first active power wins), cached for the current tick. Server side only. */
    @Nullable
    public static Config config(@Nullable class_1308 mob) {
        if (mob == null || !GATE.inUse() || mob.method_37908().field_9236) {
            return null;
        }
        MobAiState state = ((MobAiState.Holder) mob).aea$mobAiState();
        long stamp = stamp(mob);
        if (state.pathStamp != stamp) {
            state.pathStamp = stamp;
            Config cfg = null;
            try {
                cfg = PowerLookup.firstActive(mob, ID, Config.class);
            } catch (RuntimeException ignored) {
            }
            state.path = cfg;
        }
        return state.path;
    }

    static long stamp(class_1308 mob) {
        return mob.method_37908().method_8510() * 1024L + (ApoliPowers.generation() & 1023);
    }
}
