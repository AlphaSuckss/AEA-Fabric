package dev.alphasucks.aea.gui;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.IdCodecs;
import dev.alphasucks.aea.port.data.Nbt;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1304;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_7923;

/** Small codecs shared by the {@code aea:gui} power and its actions/conditions. */
public final class GuiCodecs {
    private GuiCodecs() {
    }

    private record StackSpec(class_2960 item, int count, Optional<Nbt> nbt) {
    }

    private static final MapCodec<StackSpec> STACK_OBJECT = AliasingMapCodec.wrap(
        RecordCodecBuilder.mapCodec(i -> i.group(
            IdCodecs.ID.fieldOf("item").forGetter(StackSpec::item),
            Codec.intRange(1, 127).optionalFieldOf("count", 1).forGetter(StackSpec::count),
            Nbt.CODEC.optionalFieldOf("nbt").forGetter(StackSpec::nbt)
        ).apply(i, StackSpec::new)),
        Map.of("id", "item", "amount", "count", "tag", "nbt")
    );

    /**
     * An item stack: either {@code "minecraft:bone"} or {@code {"item": "minecraft:bone", "count": 2, "nbt": "{...}"}}
     * ({@code id}/{@code amount}/{@code tag} are accepted as aliases). The decoded stack is a template: always copy it.
     */
    public static final Codec<class_1799> ITEM_STACK = Codec.either(IdCodecs.ID, STACK_OBJECT.codec()).comapFlatMap(
        either -> either.map(id -> build(id, 1, Optional.empty()), spec -> build(spec.item(), spec.count(), spec.nbt())),
        stack -> {
            class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
            if (stack.method_7947() == 1 && !stack.method_7985()) {
                return Either.left(id);
            }
            return Either.right(new StackSpec(id, stack.method_7947(), stack.method_7985() ? Optional.of(new Nbt(stack.method_7969())) : Optional.empty()));
        }
    );

    private static DataResult<class_1799> build(class_2960 id, int count, Optional<Nbt> nbt) {
        Optional<class_1792> item = class_7923.field_41178.method_17966(id);
        if (item.isEmpty()) {
            return DataResult.error(() -> "Unknown item '" + id + "'");
        }
        class_1799 stack = new class_1799(item.get(), count);
        nbt.ifPresent(n -> stack.method_7980(n.tag().method_10553()));
        return DataResult.success(stack);
    }

    /** {@code "mainhand"}, {@code "offhand"}, {@code "head"}, {@code "chest"}, {@code "legs"}, {@code "feet"}. */
    public static final Codec<class_1304> EQUIPMENT_SLOT = Codec.STRING.comapFlatMap(name -> {
        try {
            return DataResult.success(class_1304.method_5924(name));
        } catch (IllegalArgumentException e) {
            return DataResult.error(() -> "Unknown equipment slot '" + name + "' (expected mainhand, offhand, head, chest, legs or feet)");
        }
    }, class_1304::method_5923);

    /** A single string or a list of strings. */
    public static final Codec<List<String>> STRING_OR_LIST = Codec.either(Codec.STRING, Codec.STRING.listOf())
        .xmap(either -> either.map(List::of, l -> l), l -> l.size() == 1 ? Either.left(l.get(0)) : Either.right(l));

    /**
     * Parses one {@code visible} entry of a slot grid ({@code "row,col"}, 1-based, ranges allowed on
     * either side: {@code "2,1-3"}, {@code "1-3,5"}) into the cells it marks.
     */
    public static DataResult<int[]> parseCellRange(String raw, int rows, int cols) {
        String[] parts = raw.replace(" ", "").split(",");
        if (parts.length != 2) {
            return DataResult.error(() -> "Invalid slot_grid cell '" + raw + "': expected \"row,col\" (ranges like \"1-3,2\" are allowed)");
        }
        int[] r = range(parts[0]);
        int[] c = range(parts[1]);
        if (r == null || c == null) {
            return DataResult.error(() -> "Invalid slot_grid cell '" + raw + "': rows and columns must be numbers or ranges like 1-3");
        }
        if (r[0] < 1 || r[1] > rows || c[0] < 1 || c[1] > cols) {
            return DataResult.error(() -> "slot_grid cell '" + raw + "' is outside the " + rows + "x" + cols + " grid (rows and columns start at 1)");
        }
        return DataResult.success(new int[]{r[0], r[1], c[0], c[1]});
    }

    private static int[] range(String part) {
        try {
            int dash = part.indexOf('-', 1);
            if (dash < 0) {
                int v = Integer.parseInt(part);
                return new int[]{v, v};
            }
            int a = Integer.parseInt(part.substring(0, dash));
            int b = Integer.parseInt(part.substring(dash + 1));
            return new int[]{Math.min(a, b), Math.max(a, b)};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
