package dev.alphasucks.aea.originsport.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.originsport.OriginsPortIds;
import dev.alphasucks.aea.originsport.apace.ApaceOrigins;
import dev.alphasucks.aea.originsport.network.OriginsPortNetwork;
import dev.alphasucks.aea.originsport.origin.OriginSelection;
import dev.alphasucks.aea.originsport.origin.OriginTransfers;
import dev.alphasucks.aea.originsport.storage.OriginStorage;
import dev.alphasucks.aea.originsport.swap.SwapLayers;
import dev.alphasucks.aea.originsport.swap.SwapManager;
import dev.alphasucks.aea.port.action.ActionType;
import dev.alphasucks.aea.port.action.ActionTypes;
import dev.alphasucks.aea.port.action.EntityAction;
import dev.alphasucks.aea.port.alias.AliasingMapCodec;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import net.minecraft.class_1657;
import net.minecraft.class_2960;
import net.minecraft.class_3222;
import net.minecraft.class_3542;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Overgrown Origins' entity and bi-entity actions, ported onto apace's origins + AEA's swap/storage systems. */
public final class OriginActions {
    private OriginActions() {
    }

    public static void register() {
        ActionTypes.ENTITY.register(OriginsPortIds.origins("store_origin"), new StoreOrigin());
        ActionTypes.BI_ENTITY.register(OriginsPortIds.origins("store_origin"), new StoreTargetOrigin());
        ActionTypes.ENTITY.register(OriginsPortIds.origins("apply_stored_origin"), new ApplyStoredOrigin());
        ActionTypes.ENTITY.register(OriginsPortIds.origins("store_value"), new StoreValue());
        ActionTypes.BI_ENTITY.register(OriginsPortIds.origins("copy_origin"), new CopyOrigin());
        ActionTypes.BI_ENTITY.register(OriginsPortIds.origins("transfer_origin"), new TransferOrigin());
        ActionTypes.ENTITY.register(OriginsPortIds.origins("grant_origin"), new GrantOrigin());
        ActionTypes.ENTITY.register(OriginsPortIds.origins("revoke_origin"), new RevokeOrigin());
        ActionTypes.ENTITY.register(OriginsPortIds.origins("force_swap"), new ForceSwap());
        ActionTypes.ENTITY.register(OriginsPortIds.origins("open_swap_menu"), new OpenSwapMenu());
    }

    // ------------------------------------------------------------------ storage

    public record StoreCfg(String key, class_2960 layer, Optional<class_2960> origin, boolean clear) {
    }

    static final MapCodec<StoreCfg> STORE_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Codec.STRING.fieldOf("key").forGetter(StoreCfg::key),
        class_2960.field_25139.optionalFieldOf("layer", OriginStorage.DEFAULT_LAYER).forGetter(StoreCfg::layer),
        class_2960.field_25139.optionalFieldOf("origin").forGetter(StoreCfg::origin),
        Codec.BOOL.optionalFieldOf("clear", false).forGetter(StoreCfg::clear)
    ).apply(i, StoreCfg::new));

    static void store(StoreCfg cfg, class_1657 holder, class_1657 source) {
        if (cfg.clear()) {
            OriginStorage.clear(holder, cfg.key());
        } else if (cfg.origin().isPresent()) {
            OriginStorage.storeOrigin(holder, cfg.key(), cfg.layer(), cfg.origin().get());
        } else {
            OriginStorage.storeOrigin(holder, cfg.key(), source, cfg.layer());
        }
    }

    /** {@code origins:store_origin} (entity): stores the entity's own origin on a layer (or a given origin) under a key. */
    public static final class StoreOrigin implements ActionType<EntityCtx, StoreCfg> {
        @Override
        public MapCodec<StoreCfg> codec() {
            return STORE_CODEC;
        }

        @Override
        public void run(StoreCfg cfg, EntityCtx ctx) {
            if (ctx.entity() instanceof class_1657 player && !ctx.level().method_8608()) {
                store(cfg, player, player);
            }
        }
    }

    /** {@code origins:store_origin} (bi-entity): the actor stores the target's origin. */
    public static final class StoreTargetOrigin implements ActionType<BiEntityCtx, StoreCfg> {
        @Override
        public MapCodec<StoreCfg> codec() {
            return STORE_CODEC;
        }

        @Override
        public void run(StoreCfg cfg, BiEntityCtx ctx) {
            if (!ctx.level().method_8608() && ctx.actor() instanceof class_1657 holder && ctx.target() instanceof class_1657 source) {
                store(cfg, holder, source);
            }
        }
    }

    public record ApplyStoredCfg(String key, Optional<class_2960> layer, boolean clear) {
    }

    /** {@code origins:apply_stored_origin}: gives the entity the origin stored under a key. */
    public static final class ApplyStoredOrigin implements ActionType<EntityCtx, ApplyStoredCfg> {
        @Override
        public MapCodec<ApplyStoredCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("key").forGetter(ApplyStoredCfg::key),
                class_2960.field_25139.optionalFieldOf("layer").forGetter(ApplyStoredCfg::layer),
                Codec.BOOL.optionalFieldOf("clear", false).forGetter(ApplyStoredCfg::clear)
            ).apply(i, ApplyStoredCfg::new));
        }

        @Override
        public void run(ApplyStoredCfg cfg, EntityCtx ctx) {
            if (ctx.entity() instanceof class_3222 player) {
                OriginStorage.applyStoredOrigin(player, cfg.key(), cfg.layer().orElse(null), cfg.clear());
            }
        }
    }

    public record StoreValueCfg(String key, String value, boolean clear) {
    }

    /** {@code origins:store_value}: stores a string (with {@code [key]} placeholders resolved) under a key. */
    public static final class StoreValue implements ActionType<EntityCtx, StoreValueCfg> {
        @Override
        public MapCodec<StoreValueCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.STRING.fieldOf("key").forGetter(StoreValueCfg::key),
                Codec.STRING.optionalFieldOf("value", "").forGetter(StoreValueCfg::value),
                Codec.BOOL.optionalFieldOf("clear", false).forGetter(StoreValueCfg::clear)
            ).apply(i, StoreValueCfg::new));
        }

        @Override
        public void run(StoreValueCfg cfg, EntityCtx ctx) {
            if (ctx.entity() instanceof class_1657 player && !ctx.level().method_8608()) {
                if (cfg.clear()) {
                    OriginStorage.clear(player, cfg.key());
                } else {
                    String resolved = OriginStorage.resolve(player, cfg.value());
                    if (resolved != null) {
                        OriginStorage.storeValue(player, cfg.key(), resolved);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ transfers

    public record CopyCfg(class_2960 fromLayer, class_2960 toLayer, Optional<class_2960> origin, Optional<OriginSelection> selection) {
    }

    /** {@code origins:copy_origin}: the actor gets a copy of the target's origin. */
    public static final class CopyOrigin implements ActionType<BiEntityCtx, CopyCfg> {
        @Override
        public MapCodec<CopyCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                class_2960.field_25139.optionalFieldOf("from_layer", OriginsPortIds.origins("origin")).forGetter(CopyCfg::fromLayer),
                class_2960.field_25139.optionalFieldOf("to_layer", OriginsPortIds.origins("copy")).forGetter(CopyCfg::toLayer),
                class_2960.field_25139.optionalFieldOf("origin").forGetter(CopyCfg::origin),
                OriginSelection.CODEC.optionalFieldOf("selection").forGetter(CopyCfg::selection)
            ).apply(i, CopyCfg::new));
        }

        @Override
        public void run(CopyCfg cfg, BiEntityCtx ctx) {
            if (ctx.actor() instanceof class_3222 actor && ctx.target() instanceof class_3222 target) {
                OriginTransfers.transferOrigin(target, actor, cfg.fromLayer(), cfg.toLayer(), true, cfg.selection().orElse(null), cfg.origin().orElse(null), false);
            }
        }
    }

    public enum TransferMode implements class_3542 {
        STEAL("steal"),
        GIVE("give");

        public static final Codec<TransferMode> CODEC = class_3542.method_28140(TransferMode::values);
        private final String name;

        TransferMode(String name) {
            this.name = name;
        }

        @Override
        public String method_15434() {
            return name;
        }
    }

    public record TransferCfg(TransferMode mode, boolean copy, class_2960 fromLayer, class_2960 toLayer, Optional<EntityAction> actorAction,
                              Optional<EntityAction> targetAction, Optional<class_2960> origin, Optional<OriginSelection> selection, boolean random) {
    }

    /** {@code origins:transfer_origin}: steals the target's origin for the actor (or gives the actor's to the target). */
    public static final class TransferOrigin implements ActionType<BiEntityCtx, TransferCfg> {
        @Override
        public MapCodec<TransferCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                TransferMode.CODEC.optionalFieldOf("mode", TransferMode.STEAL).forGetter(TransferCfg::mode),
                Codec.BOOL.optionalFieldOf("copy", false).forGetter(TransferCfg::copy),
                class_2960.field_25139.optionalFieldOf("from_layer", OriginsPortIds.origins("origin")).forGetter(TransferCfg::fromLayer),
                class_2960.field_25139.optionalFieldOf("to_layer", OriginsPortIds.origins("origin")).forGetter(TransferCfg::toLayer),
                LoggedOptionalField.of("actor_action", EntityAction.CODEC).forGetter(TransferCfg::actorAction),
                LoggedOptionalField.of("target_action", EntityAction.CODEC).forGetter(TransferCfg::targetAction),
                class_2960.field_25139.optionalFieldOf("origin").forGetter(TransferCfg::origin),
                OriginSelection.CODEC.optionalFieldOf("selection").forGetter(TransferCfg::selection),
                Codec.BOOL.optionalFieldOf("random", false).forGetter(TransferCfg::random)
            ).apply(i, TransferCfg::new));
        }

        @Override
        public void run(TransferCfg cfg, BiEntityCtx ctx) {
            boolean moved = true;
            if (ctx.actor() instanceof class_3222 actor && ctx.target() instanceof class_3222 target) {
                class_3222 donor = cfg.mode() == TransferMode.STEAL ? target : actor;
                class_3222 recipient = cfg.mode() == TransferMode.STEAL ? actor : target;
                moved = OriginTransfers.transferOrigin(donor, recipient, cfg.fromLayer(), cfg.toLayer(), cfg.copy(), cfg.selection().orElse(null),
                    cfg.origin().orElse(null), cfg.random());
            }
            // Overgrown runs the side actions whether or not anything moved; kept
            cfg.actorAction().ifPresent(a -> a.run(ctx.asActor()));
            cfg.targetAction().ifPresent(a -> a.run(ctx.asTarget()));
        }
    }

    // ------------------------------------------------------------------ grant / revoke

    public record GrantCfg(class_2960 origin, class_2960 layer, boolean toPool) {
    }

    /** {@code origins:grant_origin}: sets a main origin, or adds the origin to a swap pool. */
    public static final class GrantOrigin implements ActionType<EntityCtx, GrantCfg> {
        @Override
        public MapCodec<GrantCfg> codec() {
            return AliasingMapCodec.wrap(RecordCodecBuilder.mapCodec(i -> i.group(
                class_2960.field_25139.fieldOf("origin").forGetter(GrantCfg::origin),
                class_2960.field_25139.optionalFieldOf("layer", OriginStorage.DEFAULT_LAYER).forGetter(GrantCfg::layer),
                Codec.BOOL.optionalFieldOf("to_pool", false).forGetter(GrantCfg::toPool)
            ).apply(i, GrantCfg::new)), Map.of("swappable", "to_pool"));
        }

        @Override
        public void run(GrantCfg cfg, EntityCtx ctx) {
            if (!(ctx.entity() instanceof class_3222 player) || !ApaceOrigins.isLive(cfg.origin())) {
                return;
            }
            if (SwapLayers.server().isSwapLayer(cfg.layer())) {
                SwapManager.grantToPool(player, cfg.layer(), cfg.origin());
            } else if (ApaceOrigins.layer(cfg.layer()) == null) {
                return;
            } else if (cfg.toPool()) {
                class_2960 poolLayer = SwapManager.grantTargetFor(cfg.layer());
                if (poolLayer != null) {
                    SwapManager.grantToPool(player, poolLayer, cfg.origin());
                }
            } else {
                ApaceOrigins.choose(player, cfg.layer(), cfg.origin());
            }
        }
    }

    public record RevokeCfg(Optional<class_2960> origin, class_2960 layer) {
    }

    /** {@code origins:revoke_origin}: removes an origin from a layer (main or pool), or everything on the layer. */
    public static final class RevokeOrigin implements ActionType<EntityCtx, RevokeCfg> {
        @Override
        public MapCodec<RevokeCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                class_2960.field_25139.optionalFieldOf("origin").forGetter(RevokeCfg::origin),
                class_2960.field_25139.optionalFieldOf("layer", OriginStorage.DEFAULT_LAYER).forGetter(RevokeCfg::layer)
            ).apply(i, RevokeCfg::new));
        }

        @Override
        public void run(RevokeCfg cfg, EntityCtx ctx) {
            if (!(ctx.entity() instanceof class_3222 player) || !OriginTransfers.isKnownLayer(cfg.layer())) {
                return;
            }
            boolean swapLayer = SwapLayers.server().isSwapLayer(cfg.layer());
            if (cfg.origin().isPresent()) {
                class_2960 originId = cfg.origin().get();
                if (!SwapManager.revokeFromAnyPool(player, cfg.layer(), originId)
                    && !swapLayer && originId.equals(ApaceOrigins.mainOrigin(player, cfg.layer()))) {
                    OriginTransfers.removeOrigin(player, cfg.layer());
                }
            } else {
                OriginTransfers.removeOrigin(player, cfg.layer());
                for (class_2960 granted : List.copyOf(SwapManager.grantedPoolOf(player, cfg.layer()))) {
                    SwapManager.revokeFromAnyPool(player, cfg.layer(), granted);
                }
            }
        }
    }

    // ------------------------------------------------------------------ swapping

    public enum SwapMode implements class_3542 {
        LINEAR("linear"),
        RANDOM("random"),
        MAIN("main");

        public static final Codec<SwapMode> CODEC = class_3542.method_28140(SwapMode::values);
        private final String name;

        SwapMode(String name) {
            this.name = name;
        }

        @Override
        public String method_15434() {
            return name;
        }
    }

    public record ForceSwapCfg(SwapMode mode, Optional<class_2960> layer, Optional<class_2960> origin, boolean includeMain) {
    }

    /** {@code origins:force_swap}: swaps to a given origin, the next one, a random one, or back to the main one. */
    public static final class ForceSwap implements ActionType<EntityCtx, ForceSwapCfg> {
        @Override
        public MapCodec<ForceSwapCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                SwapMode.CODEC.optionalFieldOf("mode", SwapMode.LINEAR).forGetter(ForceSwapCfg::mode),
                class_2960.field_25139.optionalFieldOf("layer").forGetter(ForceSwapCfg::layer),
                class_2960.field_25139.optionalFieldOf("origin").forGetter(ForceSwapCfg::origin),
                Codec.BOOL.optionalFieldOf("include_main", true).forGetter(ForceSwapCfg::includeMain)
            ).apply(i, ForceSwapCfg::new));
        }

        @Override
        public void run(ForceSwapCfg cfg, EntityCtx ctx) {
            if (!(ctx.entity() instanceof class_3222 player)) {
                return;
            }
            class_2960 target = SwapManager.resolveTarget(cfg.layer().orElse(null));
            if (target == null) {
                return;
            }
            if (cfg.origin().isPresent()) {
                SwapManager.applySwap(player, target, cfg.origin().get());
                return;
            }
            switch (cfg.mode()) {
                case LINEAR -> SwapManager.cycle(player, target, false);
                case RANDOM -> SwapManager.random(player, target, cfg.includeMain());
                case MAIN -> SwapManager.resetToMain(player, target);
            }
        }
    }

    public record OpenSwapMenuCfg(Optional<class_2960> layer) {
    }

    /** {@code origins:open_swap_menu}: opens the swap screen for a layer. */
    public static final class OpenSwapMenu implements ActionType<EntityCtx, OpenSwapMenuCfg> {
        @Override
        public MapCodec<OpenSwapMenuCfg> codec() {
            return RecordCodecBuilder.mapCodec(i -> i.group(
                class_2960.field_25139.optionalFieldOf("layer").forGetter(OpenSwapMenuCfg::layer)
            ).apply(i, OpenSwapMenuCfg::new));
        }

        @Override
        public void run(OpenSwapMenuCfg cfg, EntityCtx ctx) {
            if (ctx.entity() instanceof class_3222 player) {
                class_2960 target = SwapManager.resolveTarget(cfg.layer().orElse(null));
                if (target != null) {
                    OriginsPortNetwork.openSwapScreen(player, target);
                }
            }
        }
    }
}
