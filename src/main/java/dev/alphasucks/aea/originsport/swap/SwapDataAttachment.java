package dev.alphasucks.aea.originsport.swap;

import dev.alphasucks.aea.originsport.OriginsPortIds;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.class_1657;
import org.jetbrains.annotations.Nullable;

/** Persistent (relog + respawn) swap state, stored as a Fabric data attachment {@code aea:origin_swaps} on the player. */
public final class SwapDataAttachment {
    public static final AttachmentType<SwapData> TYPE = AttachmentRegistry.<SwapData>builder()
        .initializer(SwapData::new)
        .persistent(SwapData.CODEC)
        .copyOnDeath()
        .buildAndRegister(OriginsPortIds.aea("origin_swaps"));

    private SwapDataAttachment() {
    }

    public static void init() {
    }

    @Nullable
    public static SwapData get(class_1657 player) {
        return ((AttachmentTarget) player).getAttached(TYPE);
    }

    public static SwapData getOrCreate(class_1657 player) {
        return ((AttachmentTarget) player).getAttachedOrCreate(TYPE);
    }
}
