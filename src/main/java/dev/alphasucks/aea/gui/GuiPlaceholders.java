package dev.alphasucks.aea.gui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.function.IntToDoubleFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_2585;
import net.minecraft.class_2588;
import net.minecraft.class_5250;
import net.minecraft.class_7417;
import org.jetbrains.annotations.Nullable;

/**
 * {@code {value}}, {@code {slider:<id>}} and {@code {player}} in GUI texts and {@code aea:gui_command}.
 * Unknown placeholders are left as they are.
 */
public final class GuiPlaceholders {
    private static final Pattern TOKEN = Pattern.compile("\\{(value|player|slider:[^{}\\s]+)}");
    private static final ThreadLocal<DecimalFormat> FORMAT = ThreadLocal.withInitial(() -> {
        DecimalFormat f = new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.ROOT));
        f.setGroupingUsed(false);
        return f;
    });

    private GuiPlaceholders() {
    }

    public static String format(double value) {
        if (Double.isNaN(value)) {
            return "0";
        }
        return FORMAT.get().format(value);
    }

    public static boolean has(String raw) {
        return raw != null && raw.indexOf('{') >= 0 && TOKEN.matcher(raw).find();
    }

    /** Resolves placeholders against a menu. {@code ownSlider}: element index of the slider whose {@code {value}} it is, or -1. */
    public static Function<String, String> resolver(@Nullable GuiScreenHandler h, @Nullable class_1657 player, int ownSlider, IntToDoubleFunction sliderValue) {
        return raw -> apply(raw, token -> {
            if (token.equals("player")) {
                return player == null ? null : player.method_5477().getString();
            }
            if (h == null || h.model == null) {
                return null;
            }
            if (token.equals("value")) {
                return ownSlider >= 0 && ownSlider < h.sliders.length ? format(sliderValue.applyAsDouble(ownSlider)) : null;
            }
            int e = h.model.sliderElement(token.substring("slider:".length()));
            return e < 0 ? null : format(sliderValue.applyAsDouble(e));
        });
    }

    public static String apply(String raw, Function<String, String> lookup) {
        if (raw == null || raw.indexOf('{') < 0) {
            return raw;
        }
        Matcher m = TOKEN.matcher(raw);
        StringBuilder out = null;
        int last = 0;
        while (m.find()) {
            String value = lookup.apply(m.group(1));
            if (value == null) {
                continue;
            }
            if (out == null) {
                out = new StringBuilder(raw.length() + 16);
            }
            out.append(raw, last, m.start()).append(value);
            last = m.end();
        }
        if (out == null) {
            return raw;
        }
        out.append(raw, last, raw.length());
        return out.toString();
    }

    /** Whether a text component (or any part of it) contains a placeholder. */
    public static boolean has(class_2561 text) {
        return has(text.getString()) || text.method_10851() instanceof class_2588;
    }

    /** Rebuilds a text component with placeholders resolved in its literal parts and translation arguments. */
    public static class_2561 apply(class_2561 text, Function<String, String> resolve) {
        class_7417 content = text.method_10851();
        class_5250 out;
        if (content instanceof class_2585 literal) {
            out = class_2561.method_43470(resolve.apply(literal.comp_737()));
        } else if (content instanceof class_2588 tr) {
            Object[] args = tr.method_11023();
            Object[] mapped = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object a = args[i];
                mapped[i] = a instanceof class_2561 t ? apply(t, resolve) : a instanceof String s ? resolve.apply(s) : a;
            }
            out = tr.method_48323() == null
                ? class_2561.method_43469(tr.method_11022(), mapped)
                : class_2561.method_48322(tr.method_11022(), tr.method_48323(), mapped);
        } else {
            out = class_5250.method_43477(content);
        }
        out.method_10862(text.method_10866());
        List<class_2561> siblings = text.method_10855();
        for (class_2561 sibling : siblings) {
            out.method_10852(apply(sibling, resolve));
        }
        return out;
    }
}
