package dev.alphasucks.aea.mindcontrol;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

/** What a mind control session takes over. Each is a bit in a mask sent to the clients. */
public enum Control {
    MOVE("move"), LOOK("look"), JUMP("jump"), SNEAK("sneak"), SPRINT("sprint"), ATTACK("attack"), USE("use"),
    HOTBAR("hotbar"), DROP("drop"), SWAP_HANDS("swap_hands"), CHAT("chat"), COMMANDS("commands");

    public static final Codec<Control> CODEC = Codec.STRING.comapFlatMap(Control::byName, Control::serializedName);
    public static final int ALL = (1 << values().length) - 1;

    private final String name;

    Control(String name) {
        this.name = name;
    }

    private static DataResult<Control> byName(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        for (Control c : values()) {
            if (c.name.equals(n)) {
                return DataResult.success(c);
            }
        }
        return DataResult.error(() -> "Unknown mind control part \"" + name + "\" (move, look, jump, sneak, sprint, attack, use, hotbar, drop, swap_hands, chat, commands)");
    }

    public String serializedName() {
        return name;
    }

    public int bit() {
        return 1 << ordinal();
    }

    public boolean in(int mask) {
        return (mask & bit()) != 0;
    }
}
