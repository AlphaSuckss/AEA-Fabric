package dev.alphasucks.aea.originsport.storage;

import dev.alphasucks.aea.originsport.OriginsPortIds;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.class_1657;
import org.jetbrains.annotations.Nullable;

/** Persistent (relog + respawn) origin storage, a Fabric data attachment {@code aea:origin_stored_data} on the player. */
public final class StoredDataAttachment {
    // AEA: Overgrown's id is origins:stored_data; internal ids live in aea: here
    public static final AttachmentType<StoredData> TYPE = AttachmentRegistry.<StoredData>builder()
        .initializer(StoredData::new)
        .persistent(StoredData.CODEC)
        .copyOnDeath()
        .buildAndRegister(OriginsPortIds.aea("origin_stored_data"));

    private StoredDataAttachment() {
    }

    public static void init() {
    }

    @Nullable
    public static StoredData get(class_1657 player) {
        return ((AttachmentTarget) player).getAttached(TYPE);
    }

    public static StoredData getOrCreate(class_1657 player) {
        return ((AttachmentTarget) player).getAttachedOrCreate(TYPE);
    }
}
