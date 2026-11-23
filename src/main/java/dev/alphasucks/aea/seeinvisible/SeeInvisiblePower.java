package dev.alphasucks.aea.seeinvisible;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.extra.mixin.EntityFlagsAccessor;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import dev.alphasucks.aea.port.client.ClientResolvedPowers;
import java.util.List;
import java.util.Optional;
import net.minecraft.class_1291;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_7923;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

/**
 * {@code aea:see_invisible}, as in AEA for NeoForge: the holder sees invisible
 * entities, drawn at the configured opacity. Only what the holder sees changes: {@code isInvisible()}, and so
 * {@code apoli:invisible}, is untouched.
 * <ul>
 * <li>{@code opacity}: 0 solid, 1 not drawn. Default 0.5.</li>
 * <li>{@code bientity_condition}: actor is the holder, target the invisible entity.</li>
 * <li>{@code source}: one or a list of {@code "any"} (default), {@code "effect"} (the Invisibility effect or the
 * {@code Invisible} data tag), a power ID, a {@code #}tag (powers' own {@code tags} field), or the object forms in
 * {@link Source}.</li>
 * </ul>
 */
public final class SeeInvisiblePower extends PowerType<SeeInvisiblePower.Cfg> {
    public static final class_2960 ID = new class_2960("aea", "see_invisible");
    public static final PowerTypeGate GATE = new PowerTypeGate(ID);

    /** Shared entity flag the game sets for the Invisibility effect and the {@code Invisible} data tag. */
    private static final int FLAG_INVISIBLE = 5;

    /**
     * Why an entity is invisible. Written as a string ({@code "any"}, {@code "effect"}, a power ID or a
     * {@code #}power tag) or as an object: {@code {"type": "effect", "effect": ...}}, {@code {"type": "power",
     * "power": ...}} or {@code {"type": "power_tag", "tag": ...}}. Tags are the powers' own {@code tags} field.
     */
    public record Source(Kind kind, @Nullable class_2960 id, @Nullable String tag, @Nullable class_1291 effect) {
        public enum Kind { ANY, EFFECT, POWER, TAG }

        static Source any() {
            return new Source(Kind.ANY, null, null, null);
        }

        static Source effect(class_1291 effect) {
            return new Source(Kind.EFFECT, null, null, effect);
        }

        static Source power(class_2960 id) {
            return new Source(Kind.POWER, id, null, null);
        }

        static Source tag(String tag) {
            return new Source(Kind.TAG, null, tag, null);
        }

        private static final Codec<Source> AS_STRING = Codec.STRING.comapFlatMap(Source::read, Source::write);

        private record Obj(String type, Optional<class_2960> effect, Optional<class_2960> power, Optional<String> tag) {
        }

        private static final Codec<Source> AS_OBJECT = RecordCodecBuilder.<Obj>create(i -> i.group(
            Codec.STRING.fieldOf("type").forGetter(Obj::type),
            class_2960.field_25139.optionalFieldOf("effect").forGetter(Obj::effect),
            class_2960.field_25139.optionalFieldOf("power").forGetter(Obj::power),
            Codec.STRING.optionalFieldOf("tag").forGetter(Obj::tag)
        ).apply(i, Obj::new)).comapFlatMap(Source::fromObj, Source::toObj);

        /** A string, or failing that an object. */
        public static final Codec<Source> CODEC = new Codec<>() {
            @Override
            public <T> DataResult<com.mojang.datafixers.util.Pair<Source, T>> decode(com.mojang.serialization.DynamicOps<T> ops, T input) {
                return ops.getStringValue(input).result().isPresent() ? AS_STRING.decode(ops, input) : AS_OBJECT.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(Source input, com.mojang.serialization.DynamicOps<T> ops, T prefix) {
                return AS_STRING.encode(input, ops, prefix);
            }
        };

        private static DataResult<Source> read(String raw) {
            if (raw.equals("any")) {
                return DataResult.success(any());
            }
            if (raw.equals("effect")) {
                return DataResult.success(effect(class_1294.field_5905));
            }
            if (raw.startsWith("#")) {
                return raw.length() > 1 ? DataResult.success(tag(raw.substring(1)))
                    : DataResult.error(() -> "Empty power tag in see_invisible source");
            }
            class_2960 id = class_2960.method_12829(raw);
            if (id == null) {
                return DataResult.error(() -> "Not a source: \"" + raw + "\". Use \"any\", \"effect\", a power ID or a #power tag.");
            }
            return DataResult.success(power(id));
        }

        private static DataResult<Source> fromObj(Obj o) {
            return switch (o.type()) {
                case "any" -> DataResult.success(any());
                case "effect" -> {
                    if (o.effect().isEmpty()) {
                        yield DataResult.success(effect(class_1294.field_5905));
                    }
                    class_1291 found = class_7923.field_41174.method_10223(o.effect().get());
                    yield found != null && class_7923.field_41174.method_10250(o.effect().get()) ? DataResult.success(effect(found))
                        : DataResult.error(() -> "Unknown effect in see_invisible source: " + o.effect().get());
                }
                case "power" -> o.power().map(p -> DataResult.success(power(p)))
                    .orElseGet(() -> DataResult.error(() -> "see_invisible source of type \"power\" needs \"power\""));
                case "power_tag", "tag" -> o.tag().map(t -> DataResult.success(tag(t.startsWith("#") ? t.substring(1) : t)))
                    .orElseGet(() -> DataResult.error(() -> "see_invisible source of type \"power_tag\" needs \"tag\""));
                default -> DataResult.error(() -> "Unknown see_invisible source type: " + o.type());
            };
        }

        private Obj toObj() {
            return switch (kind) {
                case ANY -> new Obj("any", Optional.empty(), Optional.empty(), Optional.empty());
                case EFFECT -> new Obj("effect", Optional.ofNullable(class_7923.field_41174.method_10221(effect)), Optional.empty(), Optional.empty());
                case POWER -> new Obj("power", Optional.empty(), Optional.of(id), Optional.empty());
                case TAG -> new Obj("power_tag", Optional.empty(), Optional.empty(), Optional.of(tag));
            };
        }

        private String write() {
            return switch (kind) {
                case ANY -> "any";
                case EFFECT -> effect == class_1294.field_5905 ? "effect" : String.valueOf(class_7923.field_41174.method_10221(effect));
                case POWER -> id.toString();
                case TAG -> "#" + tag;
            };
        }

        boolean matches(class_1297 target) {
            return switch (kind) {
                case ANY -> true;
                case EFFECT -> effect == class_1294.field_5905
                    ? ((EntityFlagsAccessor) target).aea$sharedFlag(FLAG_INVISIBLE)
                    : target instanceof class_1309 living && living.method_6059(effect);
                case POWER -> hasActive(target, id);
                case TAG -> hasActiveTagged(target, tag);
            };
        }
    }

    /** One source or a list; a bad entry is reported as itself, not as "not a list". */
    private static final Codec<List<Source>> SOURCES = new Codec<>() {
        @Override
        public <T> DataResult<com.mojang.datafixers.util.Pair<List<Source>, T>> decode(com.mojang.serialization.DynamicOps<T> ops, T input) {
            if (ops.getStream(input).result().isPresent()) {
                return Source.CODEC.listOf().decode(ops, input);
            }
            return Source.CODEC.decode(ops, input).map(pair -> pair.mapFirst(List::of));
        }

        @Override
        public <T> DataResult<T> encode(List<Source> input, com.mojang.serialization.DynamicOps<T> ops, T prefix) {
            return Source.CODEC.listOf().encode(input, ops, prefix);
        }
    };

    private static final List<Source> ANY = List.of(Source.any());

    public record Cfg(float opacity, Optional<BiEntityCondition> bientityCondition, List<Source> sources) {
        boolean reveals(class_1297 observer, class_1297 target) {
            for (int i = 0; i < sources.size(); i++) {
                if (sources.get(i).matches(target)) {
                    return bientityCondition.isEmpty() || bientityCondition.get().test(new BiEntityCtx(observer, target, target.method_37908()));
                }
            }
            return false;
        }
    }

    private static final MapCodec<Cfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        LoggedOptionalField.of("opacity", Codec.floatRange(0.0F, 1.0F), 0.5F).forGetter(Cfg::opacity),
        LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(Cfg::bientityCondition),
        // strict: a source with a typo must not quietly turn into "any"
        LoggedOptionalField.strict("source", SOURCES).forGetter(c -> Optional.of(c.sources()))
    ).apply(i, (opacity, bientity, sources) -> new Cfg(opacity, bientity, sources.filter(l -> !l.isEmpty()).orElse(ANY))));

    /** The power's own {@code condition} is checked on the server and the result synced; the client reads it. */
    @Override
    public boolean resolvesForClient() {
        return true;
    }

    @Override
    public MapCodec<Cfg> configCodec() {
        return CODEC;
    }

    /** Guards against a condition that asks about the same entity's visibility again. */
    private static final ThreadLocal<Boolean> CHECKING = ThreadLocal.withInitial(() -> false);

    /**
     * How transparent {@code target} looks to {@code observer}: from 0 (solid) to 1 (not drawn), or NaN when none of
     * the observer's powers reveal it. With several matching powers the most visible one wins.
     */
    public static float revealOpacity(@Nullable class_1297 observer, @Nullable class_1297 target) {
        if (observer == null || target == null || !GATE.inUse() || CHECKING.get()) {
            return Float.NaN;
        }
        PowerContainer container = PowerContainer.of(observer);
        if (container == null || container.isEmpty()) {
            return Float.NaN;
        }
        List<class_2960> powers = container.powersOfType(ID);
        if (powers.isEmpty()) {
            return Float.NaN;
        }
        boolean client = observer.method_37908().field_9236;
        CHECKING.set(true);
        try {
            float best = Float.NaN;
            EntityCtx ctx = null;
            for (int i = 0; i < powers.size(); i++) {
                class_2960 id = powers.get(i);
                if (container.isSuppressed(id)) {
                    continue;
                }
                Power power = ApoliPowers.get(id);
                if (power == null || !(power.config() instanceof Cfg cfg)) {
                    continue;
                }
                if (!Float.isNaN(best) && cfg.opacity() >= best) {
                    continue;
                }
                if (client) {
                    if (!ClientResolvedPowers.isActive(id)) {
                        continue;
                    }
                } else if (power.condition().isPresent()) {
                    if (ctx == null) {
                        ctx = EntityCtx.of(observer, observer.method_37908());
                    }
                    if (!testSafely(power, ctx)) {
                        continue;
                    }
                }
                if (cfg.reveals(observer, target)) {
                    best = cfg.opacity();
                }
            }
            return best;
        } finally {
            CHECKING.set(false);
        }
    }

    private static boolean hasActiveTagged(class_1297 entity, String tag) {
        PowerContainer container = PowerContainer.of(entity);
        if (container == null || container.isEmpty()) {
            return false;
        }
        for (class_2960 powerId : container.allPowers()) {
            Power power = ApoliPowers.get(powerId);
            if (power != null && power.hasTag(tag) && !container.isSuppressed(powerId)
                && (power.condition().isEmpty() || testSafely(power, EntityCtx.of(entity, entity.method_37908())))) {
                return true;
            }
        }
        return false;
    }

    /** The entity holds this power, it is not suppressed and its condition passes. */
    private static boolean hasActive(class_1297 entity, class_2960 powerId) {
        PowerContainer container = PowerContainer.of(entity);
        if (container == null || container.isEmpty() || !container.hasPower(powerId) || container.isSuppressed(powerId)) {
            return false;
        }
        Power power = ApoliPowers.get(powerId);
        if (power == null) {
            // a power of one of Apoli 2.9.2's own types: ask Apoli whether it is active
            return apoliActive(entity, powerId);
        }
        return power.condition().isEmpty() || testSafely(power, EntityCtx.of(entity, entity.method_37908()));
    }

    private static boolean testSafely(Power power, EntityCtx ctx) {
        try {
            return power.condition().get().test(ctx);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static boolean apoliActive(class_1297 entity, class_2960 powerId) {
        try {
            if (!io.github.apace100.apoli.power.PowerTypeRegistry.contains(powerId)) {
                return false;
            }
            io.github.apace100.apoli.power.PowerType<?> type = io.github.apace100.apoli.power.PowerTypeRegistry.get(powerId);
            return io.github.apace100.apoli.component.PowerHolderComponent.KEY.maybeGet(entity)
                .map(holder -> holder.getPower(type))
                .map(io.github.apace100.apoli.power.Power::isActive)
                .orElse(false);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
