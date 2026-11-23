package dev.alphasucks.aea.macro;

import com.google.common.collect.ImmutableList;
import java.util.Deque;
import java.util.List;
import net.minecraft.class_2158;
import net.minecraft.class_2168;
import net.minecraft.class_2991;
import org.jetbrains.annotations.Nullable;

/** A {@code $} line of a function: text parts with {@code $(name)} variables between them (1.20.2 format). */
public final class MacroLine implements class_2158.class_2161 {
    private final List<String> parts;
    private final List<String> variables;
    /** Per variable: its index in the owning function's variable list (set by {@link MacroFunction}). */
    int[] slots;

    private MacroLine(List<String> parts, List<String> variables) {
        this.parts = parts;
        this.variables = variables;
    }

    public List<String> variables() {
        return variables;
    }

    /** Parses the text after the {@code $}. Errors are thrown like vanilla's function parser does. */
    static MacroLine parse(String macro, int line) {
        ImmutableList.Builder<String> parts = ImmutableList.builder();
        ImmutableList.Builder<String> vars = ImmutableList.builder();
        int length = macro.length();
        int start = 0;
        int at = macro.indexOf('$');
        while (at != -1) {
            if (at != length - 1 && macro.charAt(at + 1) == '(') {
                parts.add(macro.substring(start, at));
                int end = macro.indexOf(')', at + 1);
                if (end == -1) {
                    throw new IllegalArgumentException("Unterminated macro variable in macro '" + macro + "' on line " + line);
                }
                String name = macro.substring(at + 2, end);
                if (!validName(name)) {
                    throw new IllegalArgumentException("Invalid macro variable name '" + name + "' on line " + line);
                }
                vars.add(name);
                start = end + 1;
                at = macro.indexOf('$', start);
            } else {
                at = macro.indexOf('$', at + 1);
            }
        }
        if (start == 0) {
            throw new IllegalArgumentException("Macro without variables on line " + line);
        }
        if (start != length) {
            parts.add(macro.substring(start));
        }
        return new MacroLine(parts.build(), vars.build());
    }

    private static boolean validName(String name) {
        if (name.isEmpty()) {
            return false;
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                return false;
            }
        }
        return true;
    }

    /** The command text with the given values (indexed like the owning function's variables). */
    String build(List<String> functionValues) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < variables.size(); i++) {
            sb.append(parts.get(i)).append(functionValues.get(slots[i]));
        }
        if (parts.size() > variables.size()) {
            sb.append(parts.get(parts.size() - 1));
        }
        return sb.toString();
    }

    @Override
    public void method_9198(class_2991 manager, class_2168 source, Deque<class_2991.class_2992> stack, int maxChainLength, int depth,
                            @Nullable class_2991.class_6346 tracer) {
        // never reached: MacroFunction hides its template, only instantiated copies run
        throw new IllegalStateException("Tried to execute an uninstantiated macro");
    }

    @Override
    public String toString() {
        return "$" + variables + " " + parts;
    }
}
