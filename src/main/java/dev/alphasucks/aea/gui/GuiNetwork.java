package dev.alphasucks.aea.gui;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.class_2540;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import org.jetbrains.annotations.Nullable;

/**
 * Packets of the {@code aea:gui} menu. The server sends state (visibility, progress, slider values,
 * selector entity ids); the client only sends "button pressed/released" and "slider = value".
 */
public final class GuiNetwork {
    /** S2C: state delta of the open GUI. */
    public static final class_2960 STATE = new class_2960("aea", "gui_state");
    /** C2S: varint syncId, varint element, boolean pressed. */
    public static final class_2960 BUTTON = new class_2960("aea", "gui_button");
    /** C2S: varint syncId, varint element, double value, boolean released. */
    public static final class_2960 SLIDER = new class_2960("aea", "gui_slider");

    private static final int MAX_ENTITY_IDS = 16;

    private GuiNetwork() {
    }

    /** A (partial) snapshot of a GUI's server state. Null/empty parts are not sent. */
    public record State(@Nullable BitSet visible, List<Integer> progressIdx, List<Float> progressVal, List<Integer> sliderIdx,
                        List<Double> sliderVal, List<Integer> entityIdx, List<int[]> entityVal) {
        public static State full(GuiScreenHandler h) {
            Builder b = new Builder();
            b.visible = (BitSet) h.visible.clone();
            for (int e = 0; e < h.progress.length; e++) {
                if (h.model.element(e) instanceof GuiElement.Image img && img.progress().isPresent()) {
                    b.progress(e, h.progress[e]);
                }
                if (!Double.isNaN(h.sliders[e])) {
                    b.slider(e, h.sliders[e]);
                }
                if (h.entityIds[e].length > 0) {
                    b.entities(e, h.entityIds[e]);
                }
            }
            return b.build();
        }

        public boolean isEmpty() {
            return visible == null && progressIdx.isEmpty() && sliderIdx.isEmpty() && entityIdx.isEmpty();
        }

        public void write(class_2540 buf) {
            int mask = (visible != null ? 1 : 0) | (!progressIdx.isEmpty() ? 2 : 0) | (!sliderIdx.isEmpty() ? 4 : 0) | (!entityIdx.isEmpty() ? 8 : 0);
            buf.writeByte(mask);
            if (visible != null) {
                buf.method_33557(visible);
            }
            if ((mask & 2) != 0) {
                buf.method_10804(progressIdx.size());
                for (int i = 0; i < progressIdx.size(); i++) {
                    buf.method_10804(progressIdx.get(i));
                    buf.writeFloat(progressVal.get(i));
                }
            }
            if ((mask & 4) != 0) {
                buf.method_10804(sliderIdx.size());
                for (int i = 0; i < sliderIdx.size(); i++) {
                    buf.method_10804(sliderIdx.get(i));
                    buf.writeDouble(sliderVal.get(i));
                }
            }
            if ((mask & 8) != 0) {
                buf.method_10804(entityIdx.size());
                for (int i = 0; i < entityIdx.size(); i++) {
                    buf.method_10804(entityIdx.get(i));
                    int[] ids = entityVal.get(i);
                    buf.method_10804(ids.length);
                    for (int id : ids) {
                        buf.method_10804(id);
                    }
                }
            }
        }

        public static State read(class_2540 buf) {
            Builder b = new Builder();
            int mask = buf.readByte();
            if ((mask & 1) != 0) {
                b.visible = buf.method_33558();
            }
            if ((mask & 2) != 0) {
                int n = Math.min(buf.method_10816(), 4096);
                for (int i = 0; i < n; i++) {
                    b.progress(buf.method_10816(), buf.readFloat());
                }
            }
            if ((mask & 4) != 0) {
                int n = Math.min(buf.method_10816(), 4096);
                for (int i = 0; i < n; i++) {
                    b.slider(buf.method_10816(), buf.readDouble());
                }
            }
            if ((mask & 8) != 0) {
                int n = Math.min(buf.method_10816(), 4096);
                for (int i = 0; i < n; i++) {
                    int e = buf.method_10816();
                    int len = Math.min(buf.method_10816(), MAX_ENTITY_IDS);
                    int[] ids = new int[len];
                    for (int k = 0; k < len; k++) {
                        ids[k] = buf.method_10816();
                    }
                    b.entities(e, ids);
                }
            }
            return b.build();
        }

        /** Client: copies the received values into the menu (bounds-checked). */
        public void applyTo(GuiScreenHandler h) {
            int n = h.progress.length;
            if (visible != null) {
                h.visible.clear();
                h.visible.or(visible.get(0, n));
            }
            for (int i = 0; i < progressIdx.size(); i++) {
                int e = progressIdx.get(i);
                if (e >= 0 && e < n) {
                    h.progress[e] = Math.max(0f, Math.min(1f, progressVal.get(i)));
                }
            }
            for (int i = 0; i < sliderIdx.size(); i++) {
                int e = sliderIdx.get(i);
                if (e >= 0 && e < n && h.model.element(e) instanceof GuiElement.Slider s) {
                    h.sliders[e] = s.clamp(sliderVal.get(i));
                }
            }
            for (int i = 0; i < entityIdx.size(); i++) {
                int e = entityIdx.get(i);
                if (e >= 0 && e < n) {
                    h.entityIds[e] = entityVal.get(i);
                }
            }
        }
    }

    public static final class Builder {
        @Nullable
        BitSet visible;
        private final List<Integer> progressIdx = new ArrayList<>();
        private final List<Float> progressVal = new ArrayList<>();
        private final List<Integer> sliderIdx = new ArrayList<>();
        private final List<Double> sliderVal = new ArrayList<>();
        private final List<Integer> entityIdx = new ArrayList<>();
        private final List<int[]> entityVal = new ArrayList<>();

        public Builder visible(BitSet bits) {
            this.visible = (BitSet) bits.clone();
            return this;
        }

        public Builder progress(int element, float value) {
            progressIdx.add(element);
            progressVal.add(value);
            return this;
        }

        public Builder slider(int element, double value) {
            sliderIdx.add(element);
            sliderVal.add(value);
            return this;
        }

        public Builder entities(int element, int[] ids) {
            entityIdx.add(element);
            entityVal.add(ids.length > MAX_ENTITY_IDS ? java.util.Arrays.copyOf(ids, MAX_ENTITY_IDS) : ids);
            return this;
        }

        public State build() {
            return new State(visible, List.copyOf(progressIdx), List.copyOf(progressVal), List.copyOf(sliderIdx), List.copyOf(sliderVal),
                List.copyOf(entityIdx), List.copyOf(entityVal));
        }
    }

    /** Extra data of the open-screen packet: ids, slot count (sanity check) and the full initial state. */
    public static void writeOpenData(GuiScreenHandler h, class_2540 buf) {
        buf.method_10812(h.powerId);
        buf.method_10812(h.guiId);
        buf.method_10804(h.field_7761.size());
        State.full(h).write(buf);
    }

    public static void sendState(class_3222 player, int syncId, State state) {
        if (state.isEmpty() || player.field_13987 == null || !ServerPlayNetworking.canSend(player, STATE)) {
            return;
        }
        class_2540 buf = PacketByteBufs.create();
        buf.method_10804(syncId);
        state.write(buf);
        ServerPlayNetworking.send(player, STATE, buf);
    }
}
