package dev.alphasucks.aea.gamerule;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.AEA;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.ConditionType;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BiomeCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.condition.context.FluidCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.condition.context.StaticCtx;
import dev.alphasucks.aea.port.data.Comparison;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.minecraft.class_1937;
import net.minecraft.class_2960;

/**
 * {@code aea:gamerule}, as in AEA for NeoForge: tests a data pack gamerule ({@code <namespace>:<name>}) or a vanilla
 * one (its plain name). Without {@code comparison} it passes when the rule is true or not zero. Registered for every
 * condition context, static included.
 */
public final class GameRuleCondition<CTX> implements ConditionType<CTX, GameRuleCondition.Cfg> {
    public static final class_2960 ID = new class_2960("aea", "gamerule");
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    public record Cfg(String gamerule, Optional<Comparison> comparison, Optional<Double> compareTo) {
    }

    private static final MapCodec<Cfg> CODEC = RecordCodecBuilder.<Cfg>mapCodec(i -> i.group(
        Codec.STRING.fieldOf("gamerule").forGetter(Cfg::gamerule),
        LoggedOptionalField.of("comparison", Comparison.CODEC).forGetter(Cfg::comparison),
        LoggedOptionalField.of("compare_to", Codec.DOUBLE).forGetter(Cfg::compareTo)
    ).apply(i, Cfg::new)).flatXmap(GameRuleCondition::validate, GameRuleCondition::validate);

    private static DataResult<Cfg> validate(Cfg cfg) {
        return cfg.comparison().isPresent() != cfg.compareTo().isPresent()
            ? DataResult.error(() -> "aea:gamerule needs both \"comparison\" and \"compare_to\", or neither")
            : DataResult.success(cfg);
    }

    private final Function<CTX, class_1937> world;

    private GameRuleCondition(Function<CTX, class_1937> world) {
        this.world = world;
    }

    public static void register() {
        ConditionTypes.ENTITY.register(ID, new GameRuleCondition<EntityCtx>(EntityCtx::level));
        ConditionTypes.BI_ENTITY.register(ID, new GameRuleCondition<BiEntityCtx>(BiEntityCtx::level));
        ConditionTypes.BLOCK.register(ID, new GameRuleCondition<BlockCtx>(BlockCtx::level));
        ConditionTypes.ITEM.register(ID, new GameRuleCondition<ItemCtx>(ItemCtx::level));
        ConditionTypes.DAMAGE.register(ID, new GameRuleCondition<DamageCtx>(DamageCtx::level));
        ConditionTypes.FLUID.register(ID, new GameRuleCondition<FluidCtx>(FluidCtx::level));
        ConditionTypes.BIOME.register(ID, new GameRuleCondition<BiomeCtx>(BiomeCtx::level));
        // a static condition has no world of its own: the server's values, or the client's copy on the client
        ConditionTypes.STATIC.register(ID, new GameRuleCondition<StaticCtx>(ctx -> null));
    }

    @Override
    public MapCodec<Cfg> codec() {
        return CODEC;
    }

    @Override
    public boolean test(Cfg cfg, CTX ctx) {
        OptionalInt value = DataGameRules.read(cfg.gamerule(), world.apply(ctx));
        if (value.isEmpty()) {
            if (WARNED.add(cfg.gamerule())) {
                AEA.LOGGER.warn("[AEA] aea:gamerule: there is no gamerule called \"{}\" (its value counts as 0).", cfg.gamerule());
            }
            value = OptionalInt.of(0);
        }
        int number = value.getAsInt();
        return cfg.comparison().map(c -> c.compare(number, cfg.compareTo().get())).orElse(number != 0);
    }
}
