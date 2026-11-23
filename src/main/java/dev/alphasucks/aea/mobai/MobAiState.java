package dev.alphasucks.aea.mobai;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.class_1352;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

/**
 * Per-mob state, stored on the MobEntity by {@code MobEntityAiMixin} (so it dies with the mob — nothing static
 * holds mobs). Holds the per-tick power lookup caches and what aea:mob_ai has changed on this mob.
 */
public final class MobAiState {
    long pathStamp = Long.MIN_VALUE;
    @Nullable
    MobPathingPower.Config path;

    long aiStamp = Long.MIN_VALUE;
    List<MobAiPower.Config> ai = List.of();

    /** aea:mob_ai powers applied to this mob, by power id. */
    final Map<class_2960, MobAiPower.Applied> applied = new HashMap<>(2);
    /** every goal any aea:mob_ai power added to this mob (never stripped by "remove") */
    final Set<class_1352> aeaGoals = Collections.newSetFromMap(new IdentityHashMap<>());
    /** decided when the first aea:mob_ai power is applied, kept while any is applied */
    @Nullable
    Boolean brainMob;
    boolean injectedAttackDamage;

    public interface Holder {
        MobAiState aea$mobAiState();
    }
}
