package dev.alphasucks.aea.mindcontrol.client;

import dev.alphasucks.aea.mindcontrol.ControlBody;
import java.util.UUID;
import net.minecraft.class_1068;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_5602;
import net.minecraft.class_591;
import net.minecraft.class_634;
import net.minecraft.class_640;
import net.minecraft.class_8136;
import net.minecraft.class_897;
import net.minecraft.class_922;
import net.minecraft.class_970;
import net.minecraft.class_989;
import net.minecraft.class_5617.class_5618;

/** Draws a mind controller's body as them: their skin (wide or slim), armour and held items. */
public final class BodyRenderer extends class_897<ControlBody> {
    private final Model wide, slim;

    public BodyRenderer(class_5618 ctx) {
        super(ctx);
        this.wide = new Model(ctx, false);
        this.slim = new Model(ctx, true);
        this.field_4673 = 0.5F;
    }

    private static UUID id(ControlBody body) {
        return body.owner().orElse(body.method_5667());
    }

    private static class_640 info(ControlBody body) {
        class_634 connection = class_310.method_1551().method_1562();
        return connection == null ? null : connection.method_2871(id(body));
    }

    private static boolean isSlim(ControlBody body) {
        class_640 info = info(body);
        return "slim".equals(info != null ? info.method_2977() : class_1068.method_4647(id(body)));
    }

    static class_2960 skin(ControlBody body) {
        class_640 info = info(body);
        return info != null ? info.method_2968() : class_1068.method_4648(id(body));
    }

    @Override
    public void method_3936(ControlBody body, float yaw, float partialTick, class_4587 pose, class_4597 buffers, int light) {
        (isSlim(body) ? slim : wide).method_3936(body, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public class_2960 method_3931(ControlBody body) {
        return skin(body);
    }

    private static final class Model extends class_922<ControlBody, class_591<ControlBody>> {
        Model(class_5618 ctx, boolean slim) {
            super(ctx, new class_591<>(ctx.method_32167(slim ? class_5602.field_27581 : class_5602.field_27577), slim), 0.5F);
            method_4046(new class_970<>(this,
                new class_8136<>(ctx.method_32167(slim ? class_5602.field_27582 : class_5602.field_27579)),
                new class_8136<>(ctx.method_32167(slim ? class_5602.field_27583 : class_5602.field_27580)),
                ctx.method_48481()));
            method_4046(new class_989<>(this, ctx.method_43338()));
        }

        @Override
        public class_2960 method_3931(ControlBody body) {
            return skin(body);
        }

        @Override
        protected boolean method_4055(ControlBody body) {
            return body.method_16914();
        }
    }
}
