package dev.alphasucks.aea.macro;

import net.minecraft.class_2561;

/** A macro function could not be instantiated (missing or bad arguments, or a line that doesn't parse). */
public final class MacroException extends Exception {
    private final class_2561 text;

    public MacroException(class_2561 text) {
        super(text.getString(), null, false, false);
        this.text = text;
    }

    public class_2561 text() {
        return text;
    }
}
