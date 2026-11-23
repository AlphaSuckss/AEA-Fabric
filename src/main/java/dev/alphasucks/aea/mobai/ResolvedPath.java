package dev.alphasucks.aea.mobai;

import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.List;
import net.minecraft.class_1308;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_3532;
import org.jetbrains.annotations.Nullable;

/**
 * A mob's pathing config resolved for one path computation (built when the node maker is initialised, dropped
 * when it is cleared). Block-condition results are cached per position for the length of the search.
 */
public final class ResolvedPath {
    private static final int RULE_BLOCKED = 0;
    private static final int RULE_WALKABLE = 1;
    private static final int RULE_AVOID = 2;
    public static final int MAX_JUMP = 8;
    public static final int MAX_DROP = 256;

    public final class_1308 mob;
    public final MobPathingPower.Config config;
    /** extra blocks the planner may step up in one go, or -1 for vanilla */
    public final int jump;
    /** blocks the planner may drop, or -1 for vanilla */
    public final int drop;
    private final Long2LongOpenHashMap cache = new Long2LongOpenHashMap();

    private ResolvedPath(class_1308 mob, MobPathingPower.Config config) {
        this.mob = mob;
        this.config = config;
        // clamped: every extra jump block is another recursion level per neighbour per node
        this.jump = config.jumpHeight().map(e -> Math.min(MAX_JUMP, Math.max(0, class_3532.method_15357(e.eval(mob))))).orElse(-1);
        this.drop = config.dropHeight().map(e -> Math.min(MAX_DROP, Math.max(0, class_3532.method_15357(e.eval(mob))))).orElse(-1);
    }

    @Nullable
    public static ResolvedPath of(@Nullable class_1308 mob) {
        MobPathingPower.Config cfg = MobPathingPower.config(mob);
        return cfg == null ? null : new ResolvedPath(mob, cfg);
    }

    public boolean hasBlockRules() {
        return config.hasBlockRules();
    }

    public boolean isBlocked(int x, int y, int z) {
        return config.blocked().isPresent() && test(RULE_BLOCKED, config.blocked().get(), x, y, z);
    }

    /**
     * For a node whose feet are at (x, y, z): does what it stands on match {@code walkable}? Checks the block below and
     * the feet block itself (carpets, snow layers, slabs are stood "in"). True when there is no walkable rule.
     */
    public boolean isWalkableFloor(int x, int y, int z) {
        if (config.walkable().isEmpty()) {
            return true;
        }
        BlockCondition walkable = config.walkable().get();
        return test(RULE_WALKABLE, walkable, x, y - 1, z) || test(RULE_WALKABLE, walkable, x, y, z);
    }

    /** Highest avoid penalty for a node standing at (x, y, z) (checks the floor and the feet block), or 0. Negative means never. */
    public float avoidPenalty(int x, int y, int z) {
        List<MobPathingPower.Avoid> avoid = config.avoid();
        float best = 0.0F;
        for (int i = 0; i < avoid.size() && i < 30; i++) {
            MobPathingPower.Avoid a = avoid.get(i);
            if (test(RULE_AVOID + i, a.blockCondition(), x, y - 1, z) || test(RULE_AVOID + i, a.blockCondition(), x, y, z)) {
                if (a.penalty() < 0.0F) {
                    return -1.0F;
                }
                best = Math.max(best, a.penalty());
            }
        }
        return best;
    }

    private boolean test(int rule, BlockCondition condition, int x, int y, int z) {
        long key = class_2338.method_10064(x, y, z);
        long bits = cache.get(key);
        long tested = 1L << (rule * 2);
        long result = 1L << (rule * 2 + 1);
        if ((bits & tested) != 0) {
            return (bits & result) != 0;
        }
        boolean value = evaluate(condition, x, y, z);
        cache.put(key, bits | tested | (value ? result : 0L));
        return value;
    }

    private boolean evaluate(BlockCondition condition, int x, int y, int z) {
        class_1937 level = mob.method_37908();
        class_2338 pos = new class_2338(x, y, z);
        if (!level.method_22340(pos)) {
            return false;
        }
        try {
            return condition.test(new BlockCtx(pos, level.method_8320(pos), level, mob, null));
        } catch (RuntimeException e) {
            return false;
        }
    }

    public interface Holder {
        @Nullable
        ResolvedPath aea$resolvedPath();
    }
}
