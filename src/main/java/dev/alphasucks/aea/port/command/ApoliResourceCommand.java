package dev.alphasucks.aea.port.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.alphasucks.aea.port.power.ApoliPowers;
import dev.alphasucks.aea.port.power.Power;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerContainerImpl;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.advancement.ApoliCriteria;
import dev.alphasucks.aea.port.power.builtin.ModifyResourceChangePower;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.CooldownPower;
import io.github.apace100.apoli.power.VariableIntPower;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_2168;
import net.minecraft.class_2170;
import net.minecraft.class_2172;
import net.minecraft.class_2186;
import net.minecraft.class_2214;
import net.minecraft.class_2218;
import net.minecraft.class_2232;
import net.minecraft.class_2233;
import net.minecraft.class_2561;
import net.minecraft.class_267;
import net.minecraft.class_269;
import net.minecraft.class_2960;
import net.minecraft.class_2218.class_2219;

public final class ApoliResourceCommand {
   public static final SuggestionProvider<class_2168> RESOURCE_POWERS = (ctx, builder) -> {
      List<class_2960> held = heldResourcePowers(ctx);
      return class_2172.method_9270(held.isEmpty() ? loadedResourcePowers() : held, builder);
   };

   private ApoliResourceCommand() {
   }

   public static void register(CommandDispatcher<class_2168> dispatcher) {
      LiteralArgumentBuilder<class_2168> root = (LiteralArgumentBuilder<class_2168>)class_2170.method_9247("apoli:resource")
         .requires(ApoliPermissions.require("apoli.command.resource", 2));
      root.then(
         class_2170.method_9247("get")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(
                     ((RequiredArgumentBuilder)class_2170.method_9244("power", class_2232.method_9441())
                           .suggests(RESOURCE_POWERS)
                           .executes(ApoliResourceCommand::get))
                        .then(class_2170.method_9244("position", IntegerArgumentType.integer(0)).executes(ApoliResourceCommand::get))
                  )
            )
      );
      root.then(
         class_2170.method_9247("list")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(class_2170.method_9244("power", class_2232.method_9441()).suggests(RESOURCE_POWERS).executes(ApoliResourceCommand::list))
            )
      );
      root.then(
         class_2170.method_9247("set")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(
                     class_2170.method_9244("power", class_2232.method_9441())
                        .suggests(RESOURCE_POWERS)
                        .then(
                           ((RequiredArgumentBuilder)class_2170.method_9244("value", IntegerArgumentType.integer()).executes(ApoliResourceCommand::set))
                              .then(class_2170.method_9244("position", IntegerArgumentType.integer(0)).executes(ApoliResourceCommand::set))
                        )
                  )
            )
      );
      root.then(
         class_2170.method_9247("change")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(
                     class_2170.method_9244("power", class_2232.method_9441())
                        .suggests(RESOURCE_POWERS)
                        .then(
                           ((RequiredArgumentBuilder)class_2170.method_9244("value", IntegerArgumentType.integer()).executes(ApoliResourceCommand::change))
                              .then(class_2170.method_9244("position", IntegerArgumentType.integer(0)).executes(ApoliResourceCommand::change))
                        )
                  )
            )
      );
      root.then(
         class_2170.method_9247("has")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(class_2170.method_9244("power", class_2232.method_9441()).suggests(RESOURCE_POWERS).executes(ApoliResourceCommand::has))
            )
      );
      root.then(
         class_2170.method_9247("operation")
            .then(
               class_2170.method_9244("targets", class_2186.method_9306())
                  .then(
                     ((RequiredArgumentBuilder)class_2170.method_9244("power", class_2232.method_9441()).suggests(RESOURCE_POWERS).then(operationTail()))
                        .then(class_2170.method_9244("position", IntegerArgumentType.integer(0)).then(operationTail()))
                  )
            )
      );
      // AEA: Overgrown also redirected "/resource" here; Apoli 2.9.2 owns that root, so only /apoli:resource is registered.
      dispatcher.register(root);
   }

   private static RequiredArgumentBuilder<class_2168, class_2219> operationTail() {
      return (RequiredArgumentBuilder<class_2168, class_2219>)class_2170.method_9244("operation", class_2218.method_9404())
         .then(
            class_2170.method_9244("source", class_2233.method_9447())
               .suggests(class_2233.field_9951)
               .then(class_2170.method_9244("objective", class_2214.method_9391()).executes(ApoliResourceCommand::operation))
         );
   }

   private static int operation(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      int position = position(ctx);
      class_2219 operation = class_2218.method_9409(ctx, "operation");
      class_269 scoreboard = ((class_2168)ctx.getSource()).method_9211().method_3845();
      class_267 source = scoreboard.method_1180(class_2233.method_9452(ctx, "source"), class_2214.method_9395(ctx, "objective"));
      int affected = 0;

      for (class_1297 target : class_2186.method_9317(ctx, "targets")) {
         if (target.method_5805()) {
            PowerContainer c = PowerContainer.of(target);
            if (c != null) {
               OptionalInt current = position < 0 ? PowerResources.read(c, power) : PowerResources.readAt(c, power, position);
               if (!current.isEmpty()) {
                  ApoliResourceCommand.ResourceValue value = new ApoliResourceCommand.ResourceValue(scoreboard, current.getAsInt());
                  operation.apply(value, source);
                  OptionalInt written = position < 0
                     ? writeAll(c, power, value.method_1126())
                     : writeAt(c, power, position, value.method_1126());
                  if (!written.isEmpty()) {
                     int before = current.getAsInt();
                     int after = written.getAsInt();
                     ((class_2168)ctx.getSource())
                        .method_9226(
                           () -> class_2561.method_43470(target.method_5477().getString() + " — " + power + " changed from " + before + " to " + after), true
                        );
                     affected++;
                  }
               }
            }
         }
      }

      if (affected == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No target holds the resource power " + power));
      }

      return affected;
   }

   private static int position(CommandContext<class_2168> ctx) {
      try {
         return IntegerArgumentType.getInteger(ctx, "position");
      } catch (IllegalArgumentException absent) {
         return -1;
      }
   }

   private static int list(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      int reported = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null) {
            int size = Math.max(1, PowerResources.size(c, power));
            StringBuilder line = new StringBuilder();
            boolean any = false;

            for (int slot = 0; slot < size; slot++) {
               OptionalInt val = PowerResources.readAt(c, power, slot);
               if (!val.isEmpty()) {
                  if (any) {
                     line.append(", ");
                  }

                  line.append(slot).append(": ").append(val.getAsInt());
                  any = true;
               }
            }

            if (any) {
               String body = line.toString();
               ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(e.method_5477().getString() + " — " + power + " [" + body + "]"), false);
               reported++;
            }
         }
      }

      if (reported == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No target has a resource value for " + power));
      }

      return reported;
   }

   private static int get(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      int position = position(ctx);
      OptionalInt first = OptionalInt.empty();
      int reported = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null) {
            OptionalInt val = position < 0 ? PowerResources.read(c, power) : PowerResources.readAt(c, power, position);
            if (!val.isEmpty()) {
               int v = val.getAsInt();
               String at = position < 0 ? "" : "[" + position + "]";
               ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(e.method_5477().getString() + " — " + power + at + ": " + v), false);
               if (first.isEmpty()) {
                  first = OptionalInt.of(v);
               }

               reported++;
            }
         }
      }

      if (reported == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No target has a resource value for " + power));
         return 0;
      } else {
         return first.orElse(0);
      }
   }

   private static int set(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      int value = IntegerArgumentType.getInteger(ctx, "value");
      int position = position(ctx);
      int affected = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null) {
            OptionalInt written = position < 0 ? writeAll(c, power, value) : writeAt(c, power, position, value);
            if (!written.isEmpty()) {
               int w = written.getAsInt();
               ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(e.method_5477().getString() + " — " + power + " set to " + w), true);
               affected++;
            }
         }
      }

      if (affected == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No target holds the resource power " + power));
      }

      return affected;
   }

   private static int change(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      int delta = IntegerArgumentType.getInteger(ctx, "value");
      int position = position(ctx);
      int affected = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null) {
            OptionalInt cur = position < 0 ? PowerResources.read(c, power) : PowerResources.readAt(c, power, position);
            if (!cur.isEmpty()) {
               OptionalInt written = position < 0
                  ? write(c, power, cur.getAsInt() + delta)
                  : writeAt(c, power, position, cur.getAsInt() + delta);
               if (!written.isEmpty()) {
                  int w = written.getAsInt();
                  ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(e.method_5477().getString() + " — " + power + " now " + w), true);
                  affected++;
               }
            }
         }
      }

      if (affected == 0) {
         ((class_2168)ctx.getSource()).method_9213(class_2561.method_43470("No target holds the resource power " + power));
      }

      return affected;
   }

   private static int has(CommandContext<class_2168> ctx) throws CommandSyntaxException {
      class_2960 power = class_2232.method_9443(ctx, "power");
      int count = 0;

      for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
         PowerContainer c = PowerContainer.of(e);
         if (c != null && PowerResources.read(c, power).isPresent()) {
            ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470(e.method_5477().getString() + " has resource " + power), false);
            count++;
         }
      }

      if (count == 0) {
         ((class_2168)ctx.getSource()).method_9226(() -> class_2561.method_43470("No target holds the resource power " + power), false);
      }

      return count;
   }

   public static OptionalInt writeAll(PowerContainer container, class_2960 power, int value) {
      int size = PowerResources.size(container, power);
      if (size <= 1) {
         return write(container, power, value);
      }

      OptionalInt last = OptionalInt.empty();

      for (int slot = 0; slot < size; slot++) {
         OptionalInt written = writeAt(container, power, slot, value);
         if (written.isPresent()) {
            last = written;
         }
      }

      return last;
   }

   private static List<class_2960> heldResourcePowers(CommandContext<class_2168> ctx) {
      List<class_2960> out = new ArrayList();

      try {
         for (class_1297 e : class_2186.method_9317(ctx, "targets")) {
            PowerContainer c = PowerContainer.of(e);
            if (c != null) {
               for (class_2960 id : c.allPowers()) {
                  Power power = ApoliPowers.get(id);
                  if (power != null && (PowerResources.isResource(id) || nativeResource(c, id) != null) && !out.contains(id)) {
                     out.add(id);
                  }
               }
            }
         }
      } catch (Exception var8) {
      }

      return out;
   }

   private static List<class_2960> loadedResourcePowers() {
      List<class_2960> out = new ArrayList();

      for (Entry<class_2960, Power> e : ApoliPowers.view().entrySet()) {
         if (PowerResources.isResource((class_2960)e.getKey()) || nativeResourceType((class_2960)e.getKey())) {
            out.add((class_2960)e.getKey());
         }
      }

      return out;
   }

   /** AEA: {@link PowerResources#write}, falling back to Apoli 2.9.2's own resource/cooldown powers. */
   public static OptionalInt write(PowerContainer container, class_2960 power, int value) {
      OptionalInt written = PowerResources.write(container, power, value);
      return written.isPresent() ? written : writeNative(container, power, value);
   }

   /** AEA: {@link PowerResources#writeAt}; Apoli 2.9.2 resources only have slot 0. */
   public static OptionalInt writeAt(PowerContainer container, class_2960 power, int slot, int value) {
      OptionalInt written = PowerResources.writeAt(container, power, slot, value);
      return written.isPresent() || slot != 0 ? written : writeNative(container, power, value);
   }

   /** AEA: the held Apoli 2.9.2 power instance if it is a resource ({@link VariableIntPower}) or a cooldown. */
   @Nullable
   private static io.github.apace100.apoli.power.Power nativeResource(PowerContainer container, class_2960 power) {
      if (!(container instanceof PowerContainerImpl impl) || !io.github.apace100.apoli.power.PowerTypeRegistry.contains(power)) {
         return null;
      }
      io.github.apace100.apoli.power.PowerType<?> type = io.github.apace100.apoli.power.PowerTypeRegistry.get(power);
      if (!impl.component().hasPower(type)) {
         return null;
      }
      io.github.apace100.apoli.power.Power instance = impl.component().getPower(type);
      return instance instanceof VariableIntPower || instance instanceof CooldownPower ? instance : null;
   }

   /** AEA: loaded Apoli 2.9.2 power types that are resources without needing a holder (apoli:resource / apoli:cooldown). */
   private static boolean nativeResourceType(class_2960 power) {
      if (!io.github.apace100.apoli.power.PowerTypeRegistry.contains(power)) {
         return false;
      }
      try {
         io.github.apace100.apoli.power.factory.PowerFactory.Instance factory = io.github.apace100.apoli.power.PowerTypeRegistry.get(power).getFactory();
         if (factory == null) {
            return false;
         }
         class_2960 id = factory.getFactory().getSerializerId();
         return "apoli".equals(id.method_12836()) && ("resource".equals(id.method_12832()) || "cooldown".equals(id.method_12832()));
      } catch (RuntimeException e) {
         return false;
      }
   }

   private static OptionalInt writeNative(PowerContainer container, class_2960 power, int value) {
      io.github.apace100.apoli.power.Power instance = nativeResource(container, power);
      if (instance == null || container.owner().method_37908().method_8608()) {
         return OptionalInt.empty();
      }
      int target = value;
      if (ModifyResourceChangePower.inUse()) {
         int before = container.getAuxIntOr(power, value);
         target = ModifyResourceChangePower.adjust(container, power, before, value);
      }
      int result;
      if (instance instanceof VariableIntPower variable) {
         result = variable.setValue(target);
      } else {
         CooldownPower cooldown = (CooldownPower)instance;
         cooldown.setCooldown(target);
         result = cooldown.getRemainingTicks();
      }
      PowerHolderComponent.syncPower(container.owner(), instance.getType());
      ApoliCriteria.resourceChanged(container.rawOwner(), power, result);
      return OptionalInt.of(result);
   }

   private static final class ResourceValue extends class_267 {
      private int value;

      private ResourceValue(class_269 scoreboard, int value) {
         super(scoreboard, null, "");
         this.value = value;
      }

      @Override
      public int method_1126() {
         return this.value;
      }

      @Override
      public void method_1128(int value) {
         this.value = value;
      }
   }
}
