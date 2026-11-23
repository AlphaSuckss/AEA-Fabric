package dev.alphasucks.aea.macro;

import dev.alphasucks.aea.AEA;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.class_2158;

/**
 * Adds 1.20.2 function syntax to vanilla's function parser without replacing it: line continuations are joined and
 * {@code $} lines are turned into comments before vanilla parses the file, then put back as {@link MacroLine}s.
 * Line numbers in vanilla's error messages stay correct (joined lines become empty lines).
 */
public final class MacroParse {
    private record Pending(List<MacroLine> plan, List<String> variables) {
    }

    /** Functions are parsed on worker threads, one file per call. */
    private static final ThreadLocal<Pending> PENDING = new ThreadLocal<>();

    private MacroParse() {
    }

    public static List<String> prepare(List<String> lines) {
        PENDING.remove();
        String[] result = null;
        List<MacroLine> plan = new ArrayList<>();
        Set<String> variables = new LinkedHashSet<>();
        boolean macros = false;
        int size = lines.size();
        for (int i = 0; i < size; i++) {
            int first = i;
            int lineNumber = i + 1;
            String line = lines.get(i).trim();
            if (continues(line)) {
                if (result == null) {
                    result = lines.toArray(new String[0]);
                }
                StringBuilder joined = new StringBuilder(line);
                do {
                    if (++i == size) {
                        throw new IllegalArgumentException("Line continuation at end of file");
                    }
                    joined.deleteCharAt(joined.length() - 1);
                    joined.append(lines.get(i).trim());
                    result[i] = "";
                } while (continues(joined));
                line = joined.toString();
                result[first] = line;
            }
            if (line.isEmpty() || line.charAt(0) == '#') {
                continue;
            }
            if (line.charAt(0) == '$') {
                MacroLine macro = MacroLine.parse(line.substring(1), lineNumber);
                if (result == null) {
                    result = lines.toArray(new String[0]);
                }
                result[first] = "#";
                plan.add(macro);
                variables.addAll(macro.variables());
                macros = true;
            } else {
                plan.add(null); // vanilla parses this one
            }
        }
        if (macros) {
            PENDING.set(new Pending(plan, List.copyOf(variables)));
        }
        return result == null ? lines : Arrays.asList(result);
    }

    public static class_2158 finish(class_2158 parsed) {
        Pending pending = PENDING.get();
        if (pending == null) {
            return parsed;
        }
        PENDING.remove();
        class_2158.class_2161[] vanilla = parsed.method_9193();
        int expected = 0;
        for (MacroLine m : pending.plan()) {
            if (m == null) {
                expected++;
            }
        }
        if (vanilla.length != expected) {
            AEA.LOGGER.error("[AEA] Function {} has macro lines, but another mod changed how functions are parsed ({} commands instead of {}); its $ lines were dropped",
                parsed.method_9194(), vanilla.length, expected);
            return parsed;
        }
        class_2158.class_2161[] merged = new class_2158.class_2161[pending.plan().size()];
        int next = 0;
        for (int i = 0; i < merged.length; i++) {
            MacroLine m = pending.plan().get(i);
            merged[i] = m != null ? m : vanilla[next++];
        }
        return new MacroFunction(parsed.method_9194(), merged, pending.variables());
    }

    private static boolean continues(CharSequence line) {
        int n = line.length();
        return n > 0 && line.charAt(n - 1) == '\\';
    }
}
