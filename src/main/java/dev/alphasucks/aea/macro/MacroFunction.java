package dev.alphasucks.aea.macro;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.alphasucks.aea.AEA;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.class_2158;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2481;
import net.minecraft.class_2487;
import net.minecraft.class_2489;
import net.minecraft.class_2494;
import net.minecraft.class_2503;
import net.minecraft.class_2516;
import net.minecraft.class_2520;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_2991;
import org.jetbrains.annotations.Nullable;

/**
 * A function with {@code $} lines (1.20.2 function macros). It only runs through {@code /function <name> <arguments>}
 * or {@code /function <name> with ...}, which build a plain function with the values filled in.
 */
public final class MacroFunction extends class_2158 {
    private static final int CACHE_SIZE = 8;
    private static final Set<class_2960> WARNED = ConcurrentHashMap.newKeySet();
    private static final DecimalFormat DECIMAL = new DecimalFormat("#");

    static {
        DECIMAL.setMaximumFractionDigits(15);
        DECIMAL.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
    }

    private final class_2158.class_2161[] template;
    private final List<String> variables;
    private final class_2158.class_2161[] uninstantiated;
    private final Map<List<String>, class_2158> cache = new LinkedHashMap<>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<List<String>, class_2158> eldest) {
            return size() > CACHE_SIZE;
        }
    };

    MacroFunction(class_2960 id, class_2158.class_2161[] template, List<String> variables) {
        super(id, template);
        this.template = template;
        this.variables = List.copyOf(variables);
        for (class_2158.class_2161 element : template) {
            if (element instanceof MacroLine line) {
                int[] slots = new int[line.variables().size()];
                for (int i = 0; i < slots.length; i++) {
                    slots[i] = this.variables.indexOf(line.variables().get(i));
                }
                line.slots = slots;
            }
        }
        this.uninstantiated = new class_2158.class_2161[]{new NotInstantiated(id)};
    }

    public List<String> variables() {
        return variables;
    }

    /**
     * Anything that runs this function without arguments (function tags, /schedule, advancement rewards, ...) gets a
     * single element that skips it with a warning instead of running half of it.
     */
    @Override
    public class_2158.class_2161[] method_9193() {
        return uninstantiated;
    }

    /** Builds (or reuses) the plain function for these arguments. */
    public class_2158 instantiate(@Nullable class_2487 arguments, CommandDispatcher<class_2168> dispatcher, class_2168 source) throws MacroException {
        if (arguments == null) {
            throw new MacroException(class_2561.method_43469("commands.function.error.missing_arguments", method_9194()));
        }
        List<String> values = new ArrayList<>(variables.size());
        for (String name : variables) {
            if (!arguments.method_10545(name)) {
                throw new MacroException(class_2561.method_43469("commands.function.error.missing_argument", method_9194(), name));
            }
            values.add(asString(arguments.method_10580(name)));
        }
        class_2158 cached = cache.get(values);
        if (cached != null) {
            return cached;
        }
        class_2158.class_2161[] elements = new class_2158.class_2161[template.length];
        for (int i = 0; i < template.length; i++) {
            if (template[i] instanceof MacroLine line) {
                String command = line.build(values);
                try {
                    ParseResults<class_2168> parsed = dispatcher.parse(command, source);
                    if (parsed.getReader().canRead()) {
                        throw class_2170.method_23917(parsed);
                    }
                    elements[i] = new class_2158.class_2160(parsed);
                } catch (CommandSyntaxException e) {
                    throw new MacroException(class_2561.method_43469("commands.function.error.parse", method_9194(), command, e.getMessage()));
                }
            } else {
                elements[i] = template[i];
            }
        }
        class_2960 id = method_9194();
        class_2158 built = new class_2158(new class_2960(id.method_12836(), id.method_12832() + "/" + values.hashCode()), elements);
        cache.put(values, built);
        return built;
    }

    /** Same number formatting as 1.20.2: no exponent, no trailing ".0", up to 15 decimals. */
    private static String asString(class_2520 nbt) {
        if (nbt instanceof class_2494 f) {
            return DECIMAL.format(f.method_10700());
        }
        if (nbt instanceof class_2489 d) {
            return DECIMAL.format(d.method_10697());
        }
        if (nbt instanceof class_2481 b) {
            return String.valueOf(b.method_10698());
        }
        if (nbt instanceof class_2516 s) {
            return String.valueOf(s.method_10696());
        }
        if (nbt instanceof class_2503 l) {
            return String.valueOf(l.method_10699());
        }
        return nbt.method_10714();
    }

    /** What a macro function runs as when no arguments were given. */
    private record NotInstantiated(class_2960 id) implements class_2158.class_2161 {
        @Override
        public void method_9198(class_2991 manager, class_2168 source, Deque<class_2991.class_2992> stack, int maxChainLength, int depth,
                                @Nullable class_2991.class_6346 tracer) {
            String message = "Macro function " + id + " needs arguments (/function " + id + " {...}); it was skipped";
            if (tracer != null) {
                tracer.method_36352(depth, message);
            }
            if (WARNED.add(id)) {
                AEA.LOGGER.warn("[AEA] {} (run from a function tag, /schedule or similar). Further skips are not logged.", message);
            }
        }
    }
}
