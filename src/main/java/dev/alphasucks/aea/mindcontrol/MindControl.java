package dev.alphasucks.aea.mindcontrol;

import com.mojang.brigadier.ParseResults;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.action.BiEntityAction;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.codec.SingleOrList;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.class_1268;
import net.minecraft.class_1282;
import net.minecraft.class_1297;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1311;
import net.minecraft.class_1324;
import net.minecraft.class_1545;
import net.minecraft.class_1548;
import net.minecraft.class_1571;
import net.minecraft.class_1603;
import net.minecraft.class_1674;
import net.minecraft.class_1675;
import net.minecraft.class_1677;
import net.minecraft.class_1923;
import net.minecraft.class_1934;
import net.minecraft.class_1937;
import net.minecraft.class_2168;
import net.minecraft.class_2378;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2487;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3230;
import net.minecraft.class_3966;
import net.minecraft.class_5134;
import net.minecraft.class_5321;
import net.minecraft.class_7471;
import net.minecraft.class_7923;
import net.minecraft.class_7924;
import net.minecraft.class_8103;
import net.minecraft.server.MinecraftServer;

/**
 * {@code aea:mind_control}: a player takes over another player or a mob. Ported from AEA for NeoForge.
 *
 * <p>The controller becomes a spectator whose camera is locked to the victim, which is how it works at any distance
 * and in any dimension: the server moves the controller along with the victim and loads what is around it. A
 * {@link ControlBody} stands where the controller was and passes the damage it takes on to them. Where they were and
 * their game mode are written into their player data first, so a crash or disconnect puts them back on their next login.
 *
 * <p>A mob victim is driven here on the server, with its AI paused ({@code MobMindControlMixin}). A player victim moves
 * on their own client, which presses the keys the server relays to it, so the server sees ordinary movement.
 */
public final class MindControl {
    private MindControl() {}

    public static final class_1299<ControlBody> BODY = class_1299.class_1300.<ControlBody>method_5903(ControlBody::new, class_1311.field_17715)
        .method_17687(0.6F, 1.8F)
        .method_27299(10)
        .method_5901()
        .method_5904()
        .method_5905("mind_control_body");

    private static final class_3230<class_1923> BODY_TICKET = class_3230.method_14291("aea_mind_control", Comparator.comparingLong(class_1923::method_8324));

    private static MinecraftServer server;

    // ---- configuration ----

    private static final Codec<Integer> CONTROLS = SingleOrList.of(Control.CODEC).xmap(l -> {
        int mask = 0;
        for (Control c : l) mask |= c.bit();
        return mask;
    }, mask -> {
        List<Control> l = new ArrayList<>();
        for (Control c : Control.values()) if (c.in(mask)) l.add(c);
        return l;
    });

    public record Cfg(int controls, Optional<Integer> duration, Optional<BiEntityCondition> releaseCondition, Optional<Integer> struggle,
                      boolean killCredit, boolean closeScreens, boolean bodyDamage, float shareDamage,
                      Optional<BiEntityAction> onStart, Optional<BiEntityAction> onRelease) {}

    public static final MapCodec<Cfg> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        CONTROLS.optionalFieldOf("controls", Control.ALL).forGetter(Cfg::controls),
        Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("duration").forGetter(Cfg::duration),
        LoggedOptionalField.strict("release_condition", BiEntityCondition.CODEC).forGetter(Cfg::releaseCondition),
        Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("struggle").forGetter(Cfg::struggle),
        Codec.BOOL.optionalFieldOf("kill_credit", false).forGetter(Cfg::killCredit),
        Codec.BOOL.optionalFieldOf("close_screens", false).forGetter(Cfg::closeScreens),
        Codec.BOOL.optionalFieldOf("body_damage", true).forGetter(Cfg::bodyDamage),
        Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("share_damage", 0.0F).forGetter(Cfg::shareDamage),
        LoggedOptionalField.of("on_start", BiEntityAction.CODEC).forGetter(Cfg::onStart),
        LoggedOptionalField.of("on_release", BiEntityAction.CODEC).forGetter(Cfg::onRelease)
    ).apply(i, Cfg::new));

    // ---- sessions ----

    private record Return(class_5321<class_1937> dimension, double x, double y, double z, float yRot, float xRot, class_1934 gameMode) {
        class_2487 save() {
            class_2487 t = new class_2487();
            t.method_10582("dimension", dimension.method_29177().toString());
            t.method_10549("x", x);
            t.method_10549("y", y);
            t.method_10549("z", z);
            t.method_10548("yRot", yRot);
            t.method_10548("xRot", xRot);
            t.method_10582("gameMode", gameMode.method_8381());
            return t;
        }

        static Return load(class_2487 t) {
            class_2960 dim = class_2960.method_12829(t.method_10558("dimension"));
            return new Return(dim == null ? class_1937.field_25179 : class_5321.method_29179(class_7924.field_41223, dim),
                t.method_10574("x"), t.method_10574("y"), t.method_10574("z"), t.method_10583("yRot"), t.method_10583("xRot"),
                class_1934.method_8378(t.method_10558("gameMode"), class_1934.field_9215));
        }
    }

    static final class Session {
        final UUID controllerId, victimId;
        final boolean victimIsPlayer;
        final Cfg cfg;
        final Return back;
        class_3222 controller;
        class_1309 victim;
        ControlBody body;
        class_3218 bodyLevel;
        class_1923 bodyChunk;
        ControlInput input = ControlInput.NONE;
        int ticks, struggle;
        boolean ended;

        Session(class_3222 controller, class_1309 victim, Cfg cfg, Return back) {
            this.controller = controller;
            this.victim = victim;
            this.controllerId = controller.method_5667();
            this.victimId = victim.method_5667();
            this.victimIsPlayer = victim instanceof class_3222;
            this.cfg = cfg;
            this.back = back;
        }

        String victimName() {
            return victim.method_5476().getString();
        }
    }

    private static final Map<UUID, Session> BY_CONTROLLER = new HashMap<>();
    private static final Map<UUID, Session> BY_VICTIM = new HashMap<>();

    public static boolean isControlled(class_1297 entity) {
        return !BY_VICTIM.isEmpty() && BY_VICTIM.containsKey(entity.method_5667());
    }

    public static boolean isControlling(class_1297 entity) {
        return !BY_CONTROLLER.isEmpty() && BY_CONTROLLER.containsKey(entity.method_5667());
    }

    public static boolean controls(class_1297 controller, class_1297 victim) {
        Session s = BY_CONTROLLER.isEmpty() ? null : BY_CONTROLLER.get(controller.method_5667());
        return s != null && s.victimId.equals(victim.method_5667());
    }

    /** The player controlling this entity, if its session gives them the kill credit. */
    public static class_3222 creditedController(class_1297 entity) {
        if (entity == null || BY_VICTIM.isEmpty()) return null;
        Session s = BY_VICTIM.get(entity.method_5667());
        return s != null && s.cfg.killCredit() ? s.controller : null;
    }

    static boolean bodyInUse(ControlBody body) {
        Optional<UUID> owner = body.owner();
        if (owner.isEmpty()) return false;
        Session s = BY_CONTROLLER.get(owner.get());
        return s != null && s.body == body;
    }

    // ---- start and end ----

    public static void start(Cfg cfg, class_1297 actor, class_1297 target) {
        if (!(actor instanceof class_3222 controller) || !(target instanceof class_1309 victim) || actor == target) return;
        if (victim instanceof ControlBody || !victim.method_5805() || !controller.method_5805()) return;
        if (isControlling(controller) || isControlled(controller) || isControlled(victim) || isControlling(victim)) return;

        Return back = new Return(controller.method_37908().method_27983(), controller.method_23317(), controller.method_23318(), controller.method_23321(),
            controller.method_36454(), controller.method_36455(), controller.field_13974.method_14257());
        ((MindControlReturn) controller).aea$setMindControlReturn(back.save());

        Session s = new Session(controller, victim, cfg, back);
        BY_CONTROLLER.put(s.controllerId, s);
        BY_VICTIM.put(s.victimId, s);

        class_3218 level = controller.method_51469();
        s.body = ControlBody.of(controller);
        s.bodyLevel = level;
        s.bodyChunk = new class_1923(controller.method_24515());
        level.method_14178().method_17297(BODY_TICKET, s.bodyChunk, 2, s.bodyChunk);
        level.method_8649(s.body);

        controller.method_5848();
        controller.method_7346();
        controller.method_7336(class_1934.field_9219);
        controller.method_14224(victim);
        if (victim instanceof class_1308 mob) {
            ((MindControlled) mob).aea$setMindControlled(true);
            mob.method_5942().method_6340();
            mob.method_5980(null);
        }
        sendStates(s);
        cfg.onStart().ifPresent(a -> a.run(BiEntityCtx.of(controller, victim, victim.method_37908())));
    }

    /** Ends the session the entity is in, from either side. */
    public static void release(class_1297 entity) {
        Session s = BY_CONTROLLER.get(entity.method_5667());
        if (s == null) s = BY_VICTIM.get(entity.method_5667());
        if (s != null) end(s, true);
    }

    private static void end(Session s, boolean restoreGameMode) {
        if (s.ended) return;
        s.ended = true;
        BY_CONTROLLER.remove(s.controllerId);
        BY_VICTIM.remove(s.victimId);

        if (s.body != null) s.body.method_31472();
        if (s.bodyLevel != null) s.bodyLevel.method_14178().method_17300(BODY_TICKET, s.bodyChunk, 2, s.bodyChunk);

        if (s.victim instanceof class_1308 mob) {
            ((MindControlled) mob).aea$setMindControlled(false);
            mob.field_6212 = mob.field_6227 = mob.field_6250 = 0;
            mob.method_6100(false);
            mob.method_5660(false);
        }
        class_3222 controller = online(s.controllerId);
        if (controller != null) {
            restore(controller, s.back, restoreGameMode);
            MindControlNetwork.sendState(controller, MindControlNetwork.State.NONE);
        }
        if (s.victimIsPlayer) {
            class_3222 victim = online(s.victimId);
            if (victim != null) MindControlNetwork.sendState(victim, MindControlNetwork.State.NONE);
        }
        if (controller != null && s.victim != null && s.victim.method_5805()) {
            class_1309 victim = s.victim;
            s.cfg.onRelease().ifPresent(a -> a.run(BiEntityCtx.of(controller, victim, victim.method_37908())));
        }
    }

    /** Puts the controller back where their body stood. */
    private static void restore(class_3222 player, Return back, boolean restoreGameMode) {
        ((MindControlReturn) player).aea$setMindControlReturn(null);
        player.method_14224(player);
        class_3218 level = player.field_13995.method_3847(back.dimension());
        if (level == null) level = player.field_13995.method_30002();
        player.method_14251(level, back.x(), back.y(), back.z(), back.yRot(), back.xRot());
        player.field_6017 = 0;
        if (restoreGameMode) player.method_7336(back.gameMode());
    }

    private static class_3222 online(UUID id) {
        return server == null ? null : server.method_3760().method_14602(id);
    }

    private static void sendStates(Session s) {
        int needed = s.cfg.struggle().orElse(0);
        MindControlNetwork.sendState(s.controller, new MindControlNetwork.State(MindControlNetwork.ROLE_CONTROLLER, s.victim.method_5628(), s.victimName(),
            s.cfg.controls(), needed, s.struggle));
        if (s.victim instanceof class_3222 victim) {
            MindControlNetwork.sendState(victim, new MindControlNetwork.State(MindControlNetwork.ROLE_VICTIM, s.controller.method_5628(),
                s.controller.method_5476().getString(), s.cfg.controls(), needed, s.struggle));
        }
    }

    // ---- each tick ----

    private static void tick(MinecraftServer server) {
        if (BY_CONTROLLER.isEmpty()) return;
        for (Session s : new ArrayList<>(BY_CONTROLLER.values())) {
            if (!s.ended) tick(server, s);
        }
    }

    private static void tick(MinecraftServer server, Session s) {
        class_3222 controller = server.method_3760().method_14602(s.controllerId);
        if (controller == null) {
            end(s, true);
            return;
        }
        s.controller = controller;
        if (controller.field_13974.method_14257() != class_1934.field_9219) {
            // Someone changed their game mode: the session is over, and the new mode stays.
            end(s, false);
            return;
        }

        class_1309 victim = resolveVictim(server, s);
        if (victim == null) {
            if (!s.victimIsPlayer) end(s, true);
            // A player victim who is dead or between dimensions: wait for them.
            return;
        }
        if (victim != s.victim) {
            s.victim = victim;
            sendStates(s);
        }
        if (controller.method_14242() != victim && victim.method_5805()) {
            controller.method_14224(victim);
            sendStates(s);
        }

        s.ticks++;
        if (s.cfg.duration().isPresent() && s.ticks >= s.cfg.duration().get()) {
            end(s, true);
            return;
        }
        if (s.cfg.releaseCondition().isPresent() && s.cfg.releaseCondition().get().test(BiEntityCtx.of(controller, victim, victim.method_37908()))) {
            end(s, true);
            return;
        }
        if (s.cfg.closeScreens() && victim instanceof class_3222 p && p.field_7512 != p.field_7498) p.method_7346();
        if (s.body != null) {
            if (s.body.method_31481()) {
                end(s, true);
                return;
            }
            s.body.method_6033(Math.min(controller.method_6032(), s.body.method_6063()));
        }
        if (victim instanceof class_1308 mob) drive(mob, s.input, s.cfg.controls());
    }

    private static class_1309 resolveVictim(MinecraftServer server, Session s) {
        class_1309 v = s.victim;
        if (v != null && !v.method_31481() && v.method_5805()) return v;
        if (s.victimIsPlayer) {
            class_3222 p = server.method_3760().method_14602(s.victimId);
            return p != null && p.method_5805() ? p : null;
        }
        if (v != null && v.method_35049() != class_1297.class_5529.field_27002) return null;
        for (class_3218 level : server.method_3738()) {
            if (level.method_14190(s.victimId) instanceof class_1309 found && found.method_5805()) {
                if (found instanceof class_1308 mob) ((MindControlled) mob).aea$setMindControlled(true);
                return found;
            }
        }
        return null;
    }

    // ---- input ----

    private static void receiveInput(class_3222 sender, ControlInput raw) {
        Session s = BY_CONTROLLER.get(sender.method_5667());
        if (s == null) return;
        ControlInput input = raw.masked(s.cfg.controls());
        s.input = input;
        if (s.victim instanceof class_3222 victim) {
            MindControlNetwork.sendRelay(victim, input);
        } else if (s.victim instanceof class_1308 mob && mob.method_5805()) {
            for (int i = 0; i < input.attacks(); i++) mobAttack(mob, s);
            for (int i = 0; i < input.uses(); i++) mobUse(mob);
        }
    }

    private static void receiveStruggle(class_3222 sender, int presses) {
        Session s = BY_VICTIM.get(sender.method_5667());
        if (s == null || s.cfg.struggle().isEmpty() || presses <= 0) return;
        s.struggle += Math.min(presses, 10);
        if (s.struggle >= s.cfg.struggle().get()) end(s, true);
        else sendStates(s);
    }

    /** Moves a mob from the controller's held keys. Runs before the mob ticks, so its own movement uses it. */
    private static void drive(class_1308 mob, ControlInput in, int controls) {
        if (Control.LOOK.in(controls)) {
            mob.method_36456(in.yRot());
            mob.method_5847(in.yRot());
            mob.method_36457(in.xRot());
        }
        float forward = (in.has(ControlInput.FORWARD) ? 1 : 0) - (in.has(ControlInput.BACK) ? 1 : 0);
        float strafe = (in.has(ControlInput.LEFT) ? 1 : 0) - (in.has(ControlInput.RIGHT) ? 1 : 0);
        float vertical = (in.has(ControlInput.JUMP) ? 1 : 0) - (in.has(ControlInput.SNEAK) ? 1 : 0);
        if (mob instanceof class_1571) {
            // Ghasts steer by pushing themselves, as their own move control does.
            class_243 dir = mob.method_5720().method_1021(forward)
                .method_1019(class_243.method_1030(0, mob.method_36454() - 90).method_1021(strafe)).method_1031(0, vertical, 0);
            if (dir.method_1027() > 1.0E-4) mob.method_18799(mob.method_18798().method_1019(dir.method_1029().method_1021(0.1)));
            return;
        }
        boolean swims = mob.method_5740() || mob.method_5799() || mob.method_5771();
        class_1324 flying = mob.method_5996(class_5134.field_23720);
        double speed = mob.method_5740() && flying != null ? flying.method_6194() : mob.method_26825(class_5134.field_23719);
        if (in.has(ControlInput.SPRINT)) speed *= 1.3;
        // Like a mob's own move control: the speed goes in both the input and the mob's speed, as its AI would set them.
        mob.method_6125((float) speed);
        mob.field_6250 = (float) (forward * speed);
        mob.field_6212 = (float) (strafe * speed);
        mob.field_6227 = swims ? (float) (vertical * speed) : 0;
        mob.method_6100(!swims && in.has(ControlInput.JUMP));
        mob.method_5660(!swims && in.has(ControlInput.SNEAK));
        mob.method_5728(in.has(ControlInput.SPRINT) && forward > 0);
        if (mob instanceof class_1545 && in.has(ControlInput.JUMP)) mob.method_18799(mob.method_18798().method_1031(0, 0.08, 0));
    }

    private static class_3966 lookedAt(class_1308 mob, double reach) {
        class_243 eye = mob.method_33571();
        class_243 look = mob.method_5720();
        class_243 end = eye.method_1019(look.method_1021(reach));
        class_238 box = mob.method_5829().method_18804(look.method_1021(reach)).method_1014(1.0);
        return class_1675.method_37226(mob.method_37908(), mob, eye, end, box, e -> e.method_5863() && !e.method_7325() && e != mob, 0.3F);
    }

    private static void mobAttack(class_1308 mob, Session s) {
        mob.method_6104(class_1268.field_5808);
        class_3966 hit = lookedAt(mob, 2.5 + mob.method_17681());
        if (hit != null && hit.method_17782() != s.controller && !(hit.method_17782() instanceof ControlBody body && body == s.body)) {
            mob.method_6121(hit.method_17782());
        }
    }

    private static void mobUse(class_1308 mob) {
        class_1937 level = mob.method_37908();
        class_243 look = mob.method_5720();
        if (mob instanceof class_1548 creeper) {
            creeper.method_7004();
        } else if (mob instanceof class_1571 ghast) {
            class_1674 ball = new class_1674(level, ghast, look.field_1352, look.field_1351, look.field_1350, ghast.method_7049());
            ball.method_5814(ghast.method_23317() + look.field_1352 * 4, ghast.method_23323(0.5) + 0.5, ghast.method_23321() + look.field_1350 * 4);
            level.method_8649(ball);
        } else if (mob instanceof class_1545 blaze) {
            class_243 dir = look.method_1029();
            class_1677 ball = new class_1677(level, blaze, dir.field_1352, dir.field_1351, dir.field_1350);
            ball.method_5814(ball.method_23317(), blaze.method_23323(0.5) + 0.5, ball.method_23321());
            level.method_8649(ball);
        } else if (mob instanceof class_1603 ranged) {
            class_3966 hit = lookedAt(mob, 64);
            if (hit != null && hit.method_17782() instanceof class_1309 target) ranged.method_7105(target, 1.0F);
        }
    }

    // ---- damage, chat, commands ----

    /** Damage to a body goes to its controller. A blow that could kill them ends the session first, so they die at home. */
    static boolean hurtBody(ControlBody body, class_1282 source, float amount) {
        Optional<UUID> owner = body.owner();
        Session s = owner.isEmpty() ? null : BY_CONTROLLER.get(owner.get());
        if (s == null || s.body != body) {
            body.method_31472();
            return false;
        }
        // With body_damage off the body can't be hurt, except by what nothing can resist (the void, /kill).
        if (!s.cfg.bodyDamage() && !source.method_48789(class_8103.field_42242)) return false;
        return hurtController(s, source, amount);
    }

    /** Hurts a controlling player, who is a spectator. A blow that could kill them ends the session first, so they die at home. */
    private static boolean hurtController(Session s, class_1282 source, float amount) {
        class_3222 controller = online(s.controllerId);
        if (controller == null || amount <= 0 || source.method_5529() == controller) return false;
        if (amount >= controller.method_6032() + controller.method_6067()) {
            end(s, true);
            return controller.method_5643(source, amount);
        }
        boolean invulnerable = controller.method_31549().field_7480;
        controller.method_31549().field_7480 = false;
        try {
            return controller.method_5643(source, amount);
        } finally {
            controller.method_31549().field_7480 = invulnerable;
        }
    }

    /** share_damage: part of what the controlled target takes (before its armour) also hits the controller. */
    public static void onDamageTaken(class_1309 entity, class_1282 source, float amount) {
        if (BY_VICTIM.isEmpty()) return;
        Session s = BY_VICTIM.get(entity.method_5667());
        if (s == null || s.ended || s.cfg.shareDamage() <= 0) return;
        hurtController(s, source, amount * s.cfg.shareDamage());
    }

    private static boolean onDamage(class_1309 victim, class_1282 source, float amount) {
        if (BY_VICTIM.isEmpty()) return true;
        Session s = BY_VICTIM.get(victim.method_5667());
        if (s == null || !s.cfg.killCredit()) return true;
        class_1297 attacker = source.method_5529();
        if (attacker == null || attacker == victim) victim.method_29505(s.controller);
        return true;
    }

    private static boolean onChat(class_7471 message, class_3222 sender) {
        if (BY_CONTROLLER.isEmpty()) return true;
        Session asController = BY_CONTROLLER.get(sender.method_5667());
        if (asController != null && Control.CHAT.in(asController.cfg.controls())) {
            class_1309 victim = asController.victim;
            if (victim != null) {
                sender.field_13995.method_3760().method_43514(
                    class_2561.method_43469("chat.type.text", victim.method_5476(), message.method_46291()), false);
            }
            return false;
        }
        Session asVictim = BY_VICTIM.get(sender.method_5667());
        return asVictim == null || !Control.CHAT.in(asVictim.cfg.controls());
    }

    /** True while a controller's command runs as their victim, so the victim's own block doesn't stop it. */
    private static boolean redirecting;

    /** From {@code CommandManagerMindControlMixin}: true when the command is taken (sent on as the victim, or blocked). */
    public static boolean onCommand(ParseResults<class_2168> parse) {
        if (BY_CONTROLLER.isEmpty() || redirecting) return false;
        class_2168 source = parse.getContext().getSource();
        if (!(source.method_9228() instanceof class_3222 player) || source.method_9228() != source.method_44023()) return false;
        Session asController = BY_CONTROLLER.get(player.method_5667());
        if (asController != null && Control.COMMANDS.in(asController.cfg.controls())) {
            class_1309 victim = asController.victim;
            String command = parse.getReader().getString();
            if (victim != null) {
                MinecraftServer srv = player.field_13995;
                srv.execute(() -> {
                    redirecting = true;
                    try {
                        srv.method_3734().method_44252(victim.method_5671(), command);
                    } finally {
                        redirecting = false;
                    }
                });
            }
            return true;
        }
        Session asVictim = BY_VICTIM.get(player.method_5667());
        return asVictim != null && Control.COMMANDS.in(asVictim.cfg.controls());
    }

    // ---- lifecycle ----

    private static void onLogin(class_3222 player) {
        class_2487 tag = ((MindControlReturn) player).aea$mindControlReturn();
        if (tag != null && !tag.method_33133() && !isControlling(player)) restore(player, Return.load(tag), true);
        Session asVictim = BY_VICTIM.get(player.method_5667());
        if (asVictim != null) {
            asVictim.victim = player;
            sendStates(asVictim);
        }
    }

    private static void onLogout(class_3222 player) {
        if (BY_CONTROLLER.isEmpty()) return;
        Session s = BY_CONTROLLER.get(player.method_5667());
        if (s != null) {
            // Saved as a spectator where the victim is; their next login puts them back.
            s.ended = true;
            BY_CONTROLLER.remove(s.controllerId);
            BY_VICTIM.remove(s.victimId);
            if (s.body != null) s.body.method_31472();
            if (s.bodyLevel != null) s.bodyLevel.method_14178().method_17300(BODY_TICKET, s.bodyChunk, 2, s.bodyChunk);
            if (s.victim instanceof class_1308 mob) ((MindControlled) mob).aea$setMindControlled(false);
            class_3222 victim = s.victimIsPlayer ? online(s.victimId) : null;
            if (victim != null) MindControlNetwork.sendState(victim, MindControlNetwork.State.NONE);
            return;
        }
        Session v = BY_VICTIM.get(player.method_5667());
        if (v != null) end(v, true);
    }

    private static void onRespawn(class_3222 player) {
        if (BY_VICTIM.isEmpty()) return;
        Session s = BY_VICTIM.get(player.method_5667());
        if (s != null) {
            s.victim = player;
            sendStates(s);
        }
    }

    /** A controller killed some other way than through their body ends the session before dying. */
    private static boolean onDeath(class_1309 entity) {
        if (BY_CONTROLLER.isEmpty()) return true;
        Session s = BY_CONTROLLER.get(entity.method_5667());
        if (s != null) end(s, true);
        return true;
    }

    private static void onStopping() {
        for (Session s : new ArrayList<>(BY_CONTROLLER.values())) end(s, true);
    }

    public static void register() {
        class_2378.method_10230(class_7923.field_41177, new class_2960("aea", "mind_control_body"), BODY);
        FabricDefaultAttributeRegistry.register(BODY, class_1309.method_26827());
        MindControlTypes.register();

        ServerLifecycleEvents.SERVER_STARTING.register(s -> server = s);
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> onStopping());
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> {
            BY_CONTROLLER.clear();
            BY_VICTIM.clear();
            server = null;
        });
        ServerTickEvents.START_SERVER_TICK.register(MindControl::tick);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(MindControl::onDamage);
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> onDeath(entity));
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> onChat(message, sender));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, srv) -> srv.execute(() -> onLogin(handler.field_14140)));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, srv) -> onLogout(handler.field_14140));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> onRespawn(newPlayer));

        ServerPlayNetworking.registerGlobalReceiver(MindControlNetwork.INPUT, (srv, player, handler, buf, sender) -> {
            ControlInput input = ControlInput.read(buf);
            srv.execute(() -> receiveInput(player, input));
        });
        ServerPlayNetworking.registerGlobalReceiver(MindControlNetwork.STRUGGLE, (srv, player, handler, buf, sender) -> {
            int presses = buf.method_10816();
            srv.execute(() -> receiveStruggle(player, presses));
        });
    }

    // ---- test hooks ----

    /** Feeds input as if the controller's client had sent it. */
    public static void testInput(class_3222 controller, ControlInput input) {
        receiveInput(controller, input);
    }

    public static Set<UUID> controllers() {
        return BY_CONTROLLER.keySet();
    }
}
