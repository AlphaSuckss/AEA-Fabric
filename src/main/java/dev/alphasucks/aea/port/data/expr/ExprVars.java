package dev.alphasucks.aea.port.data.expr;

import dev.alphasucks.aea.port.condition.builtin.entity.SimpleFlagConditions;
import dev.alphasucks.aea.port.data.AttackStrengthContext;
import dev.alphasucks.aea.port.data.EnchantmentLevels;
import dev.alphasucks.aea.port.data.ItemSlot;
import dev.alphasucks.aea.port.data.NbtPathValue;
import dev.alphasucks.aea.port.data.NbtSources;
import dev.alphasucks.aea.port.data.SlotStacks;
import dev.alphasucks.aea.port.power.PowerContainer;
import dev.alphasucks.aea.port.power.PowerResources;
import dev.alphasucks.aea.port.power.builtin.NightVisionPower;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import net.minecraft.class_1297;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_1944;
import net.minecraft.class_1959;
import net.minecraft.class_2338;
import net.minecraft.class_2520;
import net.minecraft.class_2960;
import net.minecraft.class_2203.class_2209;
import org.jetbrains.annotations.Nullable;

public final class ExprVars {
   private static final String ACTOR_PREFIX = "actor_";
   private static final String TARGET_PREFIX = "target_";
   private static final Map<String, ExprVars.ResolvedVar> VARS = new HashMap();
   private static final ThreadLocal<int[]> BOUND_DEPTH = ThreadLocal.withInitial(() -> new int[1]);
   private static final int NBT_SELF = 0;
   private static final int NBT_TARGET = 1;
   private static final int NBT_ACTOR = 2;
   private static final int NBT_SLOT = 3;
   private static final int NBT_BLOCK = 4;
   private static final int NBT_STORAGE = 5;

   private ExprVars() {
   }

   public static void load() {
   }

   public static void register(String name, ExprVar accessor) {
      VARS.put(name, new ExprVars.ResolvedVar(accessor, false));
   }

   public static void registerContext(String name) {
      int slot = ExprContext.slot(name);
      register(name, (e, c, l, v) -> ExprContext.get(slot));
   }

   @Nullable
   public static ExprVars.ResolvedVar resolve(String name) {
      ExprVars.ResolvedVar known = (ExprVars.ResolvedVar)VARS.get(name);
      if (known != null) {
         return known;
      } else {
         ExprVars.ResolvedVar prefixed = resolvePrefixed(name);
         if (prefixed != null) {
            return prefixed;
         } else if (name.indexOf(58) < 0) {
            return null;
         } else {
            class_2960 literal = class_2960.method_12829(name);
            ExprVars.ResolvedVar bound = resolveBound(name, "_max", true, literal);
            if (bound != null) {
               return bound;
            } else {
               bound = resolveBound(name, "_min", false, literal);
               if (bound != null) {
                  return bound;
               } else {
                  bound = resolveSize(name, literal);
                  if (bound != null) {
                     return bound;
                  } else {
                     return literal == null ? null : new ExprVars.ResolvedVar((e, c, l, v) -> readResource(c, literal), true);
                  }
               }
            }
         }
      }
   }

   public static ExprVars.ResolvedVar resolveIndexed(class_2960 id, ExprNode index) {
      return new ExprVars.ResolvedVar((e, c, l, v) -> {
         int slot = (int)Math.round(index.eval(e, c, l, v));
         return readResourceAt(c, id, slot);
      }, true);
   }

   @Nullable
   private static ExprVars.ResolvedVar resolvePrefixed(String name) {
      boolean target = name.startsWith("target_");
      if (!target && !name.startsWith("actor_")) {
         return null;
      }

      String base = name.substring(target ? "target_".length() : "actor_".length());
      if (base.indexOf(58) >= 0) {
         return null;
      }

      ExprVars.ResolvedVar delegate = (ExprVars.ResolvedVar)VARS.get(base);
      if (delegate == null) {
         return null;
      }

      ExprVar accessor = delegate.accessor();
      int slot = target ? 1 : 0;
      return new ExprVars.ResolvedVar((e, c, l, v) -> {
         class_1297 bound = ExprPeer.frame()[slot];
         return accessor.get(bound != null ? bound : e, c, l, v);
      }, delegate.needsContainer(), true);
   }

   @Nullable
   private static ExprVars.ResolvedVar resolveBound(String name, String suffix, boolean max, @Nullable class_2960 literal) {
      if (!name.endsWith(suffix)) {
         return null;
      }

      class_2960 base = class_2960.method_12829(name.substring(0, name.length() - suffix.length()));
      return base == null ? null : new ExprVars.ResolvedVar((e, c, l, v) -> readBound(c, base, literal, max), true);
   }

   @Nullable
   private static ExprVars.ResolvedVar resolveSize(String name, @Nullable class_2960 literal) {
      if (!name.endsWith("_size")) {
         return null;
      }

      class_2960 base = class_2960.method_12829(name.substring(0, name.length() - "_size".length()));
      return base == null ? null : new ExprVars.ResolvedVar((e, c, l, v) -> {
         int size = PowerResources.size(c, base);
         if (size > 0) {
            return size;
         } else {
            return literal == null ? 0.0 : readResource(c, literal);
         }
      }, true);
   }

   private static double readBound(@Nullable PowerContainer container, class_2960 base, @Nullable class_2960 literal, boolean max) {
      int[] depth = (int[])BOUND_DEPTH.get();
      if (depth[0] >= 8) {
         return 0.0;
      }

      depth[0]++;

      try {
         OptionalInt bound = PowerResources.bound(container, base, max);
         if (bound.isPresent()) {
            return bound.getAsInt();
         }
      } finally {
         depth[0]--;
      }

      return literal == null ? 0.0 : readResource(container, literal);
   }

   public static double readResource(@Nullable PowerContainer container, class_2960 id) {
      if (container == null) {
         return 0.0;
      }

      OptionalInt value = PowerResources.read(container, id);
      return value.isPresent() ? value.getAsInt() : container.getAuxIntOr(id, 0);
   }

   public static double readResourceAt(@Nullable PowerContainer container, class_2960 id, int slot) {
      if (container == null) {
         return 0.0;
      }

      OptionalInt value = PowerResources.readAt(container, id, slot);
      return value.isPresent() ? value.getAsInt() : 0.0;
   }

   private static void registerIdFunctions() {
      ExprParser.registerIdFunction("resource", (id, args, name, at) -> {
         if (args.isEmpty()) {
            return new ExprVars.ResolvedVar((e, c, l, v) -> readResource(c, id), true);
         }

         ExprNode index = (ExprNode)args.get(0);
         return new ExprVars.ResolvedVar((e, c, l, v) -> readResourceAt(c, id, (int)Math.round(index.eval(e, c, l, v))), true);
      });
      ExprParser.registerIdFunction("target_resource", (id, args, name, at) -> {
         ExprNode index = args.isEmpty() ? null : (ExprNode)args.get(0);
         return new ExprVars.ResolvedVar((e, c, l, v) -> {
            PowerContainer peer = peerContainer(e, c);
            return index == null ? readResource(peer, id) : readResourceAt(peer, id, (int)Math.round(index.eval(e, c, l, v)));
         }, true, true);
      });
      ExprParser.registerIdFunction("actor_resource", (id, args, name, at) -> {
         ExprNode index = args.isEmpty() ? null : (ExprNode)args.get(0);
         return new ExprVars.ResolvedVar((e, c, l, v) -> {
            PowerContainer peer = actorContainer(e, c);
            return index == null ? readResource(peer, id) : readResourceAt(peer, id, (int)Math.round(index.eval(e, c, l, v)));
         }, true, true);
      });
      ExprParser.registerIdFunction("actor_has_power", (id, args, name, at) -> new ExprVars.ResolvedVar((e, c, l, v) -> {
         PowerContainer peer = actorContainer(e, c);
         return peer != null && peer.hasPower(id) ? 1.0 : 0.0;
      }, true, true));
      ExprParser.registerIdFunction(
         "has_power", (id, args, name, at) -> new ExprVars.ResolvedVar((e, c, l, v) -> c != null && c.hasPower(id) ? 1.0 : 0.0, true)
      );
      ExprParser.registerIdFunction("target_has_power", (id, args, name, at) -> new ExprVars.ResolvedVar((e, c, l, v) -> {
         PowerContainer peer = peerContainer(e, c);
         return peer != null && peer.hasPower(id) ? 1.0 : 0.0;
      }, true, true));
      ExprParser.registerIdFunction(
         "has_resource", (id, args, name, at) -> new ExprVars.ResolvedVar((e, c, l, v) -> PowerResources.read(c, id).isPresent() ? 1.0 : 0.0, true)
      );
      ExprParser.registerIdFunction("resource_size", (id, args, name, at) -> new ExprVars.ResolvedVar((e, c, l, v) -> PowerResources.size(c, id), true));
      ExprParser.registerIdFunction("resource_contains", (id, args, name, at) -> {
         if (args.isEmpty()) {
            throw new ExprParseException("resource_contains(id, value) expects a value to look for", at);
         }

         ExprNode wanted = (ExprNode)args.get(0);
         return new ExprVars.ResolvedVar((e, c, l, v) -> PowerResources.indexOf(c, id, (int)Math.round(wanted.eval(e, c, l, v))) >= 0 ? 1.0 : 0.0, true);
      });
      ExprParser.registerIdFunction("resource_index_of", (id, args, name, at) -> {
         if (args.isEmpty()) {
            throw new ExprParseException("resource_index_of(id, value) expects a value to look for", at);
         }

         ExprNode wanted = (ExprNode)args.get(0);
         return new ExprVars.ResolvedVar((e, c, l, v) -> PowerResources.indexOf(c, id, (int)Math.round(wanted.eval(e, c, l, v))), true);
      });
   }

   private static void registerBracketFunctions() {
      ExprParser.registerBracketFunction("enchantment", ExprVars::enchantment);
      ExprParser.registerBracketFunction("target_enchantment", (args, name, at) -> onPeer(enchantment(args, name, at), 1));
      ExprParser.registerBracketFunction("actor_enchantment", (args, name, at) -> onPeer(enchantment(args, name, at), 0));
      ExprParser.registerBracketFunction("durability", ExprVars::durability);
      ExprParser.registerBracketFunction("target_durability", (args, name, at) -> onPeer(durability(args, name, at), 1));
      ExprParser.registerBracketFunction("actor_durability", (args, name, at) -> onPeer(durability(args, name, at), 0));
      ExprParser.registerBracketFunction("nbt", ExprVars::nbt);
      ExprParser.registerBracketFunction("data", ExprVars::nbt);
      ExprParser.registerBracketFunction("attribute", ExprAttributes::query);
      ExprParser.registerBracketFunction("target_attribute", (args, name, at) -> onPeer(ExprAttributes.query(args, name, at), 1));
      ExprParser.registerBracketFunction("actor_attribute", (args, name, at) -> onPeer(ExprAttributes.query(args, name, at), 0));
   }

   private static ExprVars.ResolvedVar onPeer(ExprVars.ResolvedVar delegate, int slot) {
      ExprVar accessor = delegate.accessor();
      return new ExprVars.ResolvedVar((e, c, l, v) -> {
         class_1297 bound = ExprPeer.frame()[slot];
         return accessor.get(bound != null ? bound : e, c, l, v);
      }, delegate.needsContainer(), true);
   }

   private static ExprVars.ResolvedVar enchantment(List<String> args, String name, int at) throws ExprParseException {
      if (!args.isEmpty() && !((String)args.get(0)).isEmpty()) {
         class_2960 id = class_2960.method_12829((String)args.get(0));
         if (id == null) {
            throw new ExprParseException("'" + (String)args.get(0) + "' is not a valid enchantment id", at);
         }

         int scope = 1;
         ItemSlot slot = null;
         boolean max = false;

         for (int i = 1; i < args.size(); i++) {
            String arg = (String)args.get(i);
            if (!arg.equals("sum")) {
               if (arg.equals("max")) {
                  max = true;
               } else {
                  int named = SlotStacks.scopeOf(arg);
                  if (named >= 0) {
                     scope = named;
                     slot = null;
                  } else {
                     ItemSlot parsed = (ItemSlot)ItemSlot.parse(arg).result().orElse(null);
                     if (parsed == null) {
                        throw new ExprParseException("'" + arg + "' is not an item slot or a calculation (sum, max)", at);
                     }

                     slot = parsed;
                     scope = 0;
                  }
               }
            }
         }

         EnchantmentLevels levels = EnchantmentLevels.of(id);
         int finalScope = scope;
         ItemSlot finalSlot = slot;
         boolean useMax = max;
         return new ExprVars.ResolvedVar((e, c, l, v) -> {
            class_1937 level = levelOf(e, l);
            if (level == null || e == null) {
               return 0.0;
            }

            if (finalScope == 0) {
               return levels.levelIn(level, SlotStacks.get(e, finalSlot));
            }

            if (e instanceof class_1309 living) {
               class_1304[] slots = SlotStacks.slotsOf(finalScope);
               int total = 0;
               int best = 0;

               for (int i = 0; i < slots.length; i++) {
                  int value = levels.levelIn(level, living.method_6118(slots[i]));
                  total += value;
                  if (value > best) {
                     best = value;
                  }
               }

               return useMax ? best : total;
            } else {
               return 0.0;
            }
         }, false);
      } else {
         throw new ExprParseException("enchantment[id, slot, calculation] expects an enchantment id", at);
      }
   }

   private static ExprVars.ResolvedVar durability(List<String> args, String name, int at) throws ExprParseException {
      if (!args.isEmpty() && !((String)args.get(0)).isEmpty()) {
         ItemSlot slot = (ItemSlot)ItemSlot.parse((String)args.get(0)).result().orElse(null);
         if (slot == null) {
            throw new ExprParseException("'" + (String)args.get(0) + "' is not an item slot", at);
         } else {
            return new ExprVars.ResolvedVar((e, c, l, v) -> {
               class_1799 stack = SlotStacks.get(e, slot);
               int max = stack.method_7936();
               return max > 0 ? max - stack.method_7919() : 0.0;
            }, false);
         }
      } else {
         throw new ExprParseException(name + "[slot] expects an item slot", at);
      }
   }

   private static ExprVars.ResolvedVar nbt(List<String> args, String name, int at) throws ExprParseException {
      if (!args.isEmpty() && !((String)args.get(0)).isEmpty()) {
         int source = 0;
         ItemSlot slot = null;
         class_2960 storage = null;
         ExprNode blockX = null;
         ExprNode blockY = null;
         ExprNode blockZ = null;
         boolean needsPeer = false;
         boolean needsContainer = false;
         int index = 0;
         if (args.size() > 1) {
            String head = (String)args.get(0);
            switch (head) {
               case "self":
               case "entity":
               case "holder":
                  index = 1;
                  break;
               case "target":
                  source = 1;
                  needsPeer = true;
                  index = 1;
                  break;
               case "actor":
                  source = 2;
                  needsPeer = true;
                  index = 1;
                  break;
               case "block":
                  if (args.size() < 5) {
                     throw new ExprParseException(name + "[block, x, y, z, path] expects three coordinates", at);
                  }

                  source = 4;
                  ExprParser.Nested x = ExprParser.compileNested((String)args.get(1), name, at);
                  ExprParser.Nested y = ExprParser.compileNested((String)args.get(2), name, at);
                  ExprParser.Nested z = ExprParser.compileNested((String)args.get(3), name, at);
                  blockX = x.node();
                  blockY = y.node();
                  blockZ = z.node();
                  needsContainer = x.needsContainer() || y.needsContainer() || z.needsContainer();
                  needsPeer = x.needsPeer() || y.needsPeer() || z.needsPeer();
                  index = 4;
                  break;
               case "storage":
                  if (args.size() < 3) {
                     throw new ExprParseException(name + "[storage, id, path] expects a storage id", at);
                  }

                  source = 5;
                  storage = class_2960.method_12829((String)args.get(1));
                  if (storage == null) {
                     throw new ExprParseException("'" + (String)args.get(1) + "' is not a valid storage id", at);
                  }

                  index = 2;
                  break;
               default:
                  ItemSlot parsed = (ItemSlot)ItemSlot.parse(head).result().orElse(null);
                  if (parsed != null) {
                     slot = parsed;
                     source = 3;
                     index = 1;
                  } else if (args.size() != 2 || NbtPathValue.aggregatorOf((String)args.get(1)) < 0) {
                     throw new ExprParseException("'" + head + "' is not an NBT source (self, target, actor, block, storage or an item slot)", at);
                  }
            }
         }

         if (index >= args.size()) {
            throw new ExprParseException(name + "[source, path] expects an NBT path", at);
         }

         if (args.size() - index > 2) {
            throw new ExprParseException(name + "[...] takes a source, a path and an aggregator, but got " + args.size() + " arguments", at);
         }

         String pathSource = (String)args.get(index);
         int aggregator = 0;
         if (index + 1 < args.size()) {
            aggregator = NbtPathValue.aggregatorOf((String)args.get(index + 1));
            if (aggregator < 0) {
               throw new ExprParseException("'" + (String)args.get(index + 1) + "' is not an NBT aggregator (first, sum, max, min, count)", at);
            }
         }

         class_2209 path = NbtPathValue.parse(pathSource);
         if (path == null) {
            throw new ExprParseException("'" + pathSource + "' is not a valid NBT path", at);
         }

         int finalSource = source;
         ItemSlot finalSlot = slot;
         class_2960 finalStorage = storage;
         ExprNode x = blockX;
         ExprNode y = blockY;
         ExprNode z = blockZ;
         int finalAggregator = aggregator;
         return new ExprVars.ResolvedVar(
            (entity, container, level, value) -> {
               class_1937 world = levelOf(entity, level);

               class_2520 root = switch (finalSource) {
                  case 1 -> NbtSources.ofEntity(peerOr(1, entity));
                  case 2 -> NbtSources.ofEntity(peerOr(0, entity));
                  case 3 -> NbtSources.ofStack(SlotStacks.get(entity, finalSlot), world);
                  case 4 -> NbtSources.ofBlock(
                     world,
                     class_2338.method_49637(
                        x.eval(entity, container, level, value), y.eval(entity, container, level, value), z.eval(entity, container, level, value)
                     )
                  );
                  case 5 -> NbtSources.ofStorage(world, finalStorage);
                  default -> NbtSources.ofEntity(entity);
               };
               return NbtPathValue.read(root, path, finalAggregator);
            },
            needsContainer,
            needsPeer
         );
      } else {
         throw new ExprParseException(name + "[source, path] expects an NBT path", at);
      }
   }

   @Nullable
   private static class_1297 peerOr(int slot, @Nullable class_1297 fallback) {
      class_1297 bound = ExprPeer.frame()[slot];
      return bound != null ? bound : fallback;
   }

   @Nullable
   private static PowerContainer actorContainer(@Nullable class_1297 primary, @Nullable PowerContainer own) {
      class_1297 actor = ExprPeer.actor();
      return actor != null && actor != primary ? PowerContainer.of(actor) : own;
   }

   @Nullable
   private static PowerContainer peerContainer(@Nullable class_1297 primary, @Nullable PowerContainer own) {
      class_1297 peer = ExprPeer.target();
      return peer != null && peer != primary ? PowerContainer.of(peer) : own;
   }

   private static class_1937 levelOf(@Nullable class_1297 entity, @Nullable class_1937 level) {
      if (level != null) {
         return level;
      } else {
         return entity != null ? entity.method_37908() : null;
      }
   }

   static {
      register("value", (e, c, l, v) -> v);
      registerContext("damage");
      registerContext("distance");
      registerContext("hit_x");
      registerContext("hit_y");
      registerContext("hit_z");
      registerContext("count");
      registerContext("index");
      registerContext("mouse_x");
      registerContext("mouse_y");
      registerContext("held_ticks");
      register("health", (e, c, l, v) -> e instanceof class_1309 le ? le.method_6032() : 0.0);
      register("max_health", (e, c, l, v) -> e instanceof class_1309 le ? le.method_6063() : 0.0);
      register("absorption", (e, c, l, v) -> e instanceof class_1309 le ? le.method_6067() : 0.0);
      register("armor", (e, c, l, v) -> e instanceof class_1309 le ? le.method_6096() : 0.0);
      register("air", (e, c, l, v) -> e != null ? e.method_5669() : 0.0);
      register("max_air", (e, c, l, v) -> e != null ? e.method_5748() : 0.0);
      register("fall_distance", (e, c, l, v) -> e != null ? e.field_6017 : 0.0);
      register("attack_charge", (e, c, l, v) -> AttackStrengthContext.charge(e));
      register("x", (e, c, l, v) -> e != null ? e.method_23317() : 0.0);
      register("y", (e, c, l, v) -> e != null ? e.method_23318() : 0.0);
      register("z", (e, c, l, v) -> e != null ? e.method_23321() : 0.0);
      register("yaw", (e, c, l, v) -> e != null ? e.method_36454() : 0.0);
      register("pitch", (e, c, l, v) -> e != null ? e.method_36455() : 0.0);
      register("velocity_x", (e, c, l, v) -> e != null ? e.method_18798().field_1352 : 0.0);
      register("velocity_y", (e, c, l, v) -> e != null ? e.method_18798().field_1351 : 0.0);
      register("velocity_z", (e, c, l, v) -> e != null ? e.method_18798().field_1350 : 0.0);
      register("food", (e, c, l, v) -> e instanceof class_1657 p ? p.method_7344().method_7586() : 0.0);
      register("saturation", (e, c, l, v) -> e instanceof class_1657 p ? p.method_7344().method_7589() : 0.0);
      register("xp_level", (e, c, l, v) -> e instanceof class_1657 p ? p.field_7520 : 0.0);
      register("xp_progress", (e, c, l, v) -> e instanceof class_1657 p ? p.field_7510 : 0.0);
      register("power_count", (e, c, l, v) -> c == null ? 0.0 : c.allPowers().size());
      registerIdFunctions();
      registerBracketFunctions();
      register("world_time", (e, c, l, v) -> {
         class_1937 level = levelOf(e, l);
         return level != null ? level.method_8510() : 0.0;
      });
      register("day_time", (e, c, l, v) -> {
         class_1937 level = levelOf(e, l);
         return level != null ? level.method_8532() : 0.0;
      });
      register("difficulty", (e, c, l, v) -> {
         class_1937 level = levelOf(e, l);
         return level != null ? level.method_8407().method_5461() : 0.0;
      });
      register("moon_phase", (e, c, l, v) -> {
         class_1937 level = levelOf(e, l);
         return level != null ? level.method_30273() : 0.0;
      });
      register("biome_temperature", (e, c, l, v) -> e != null ? ((class_1959)e.method_37908().method_23753(e.method_24515()).comp_349()).method_8712() : 0.0);
      register("brightness", (e, c, l, v) -> e != null ? SimpleFlagConditions.syncedSky(e.method_37908()).method_22349(e.method_24515()) : 0.0);
      register("light", (e, c, l, v) -> e != null ? SimpleFlagConditions.syncedSky(e.method_37908()).method_22339(e.method_24515()) : 0.0);
      register("block_light", (e, c, l, v) -> e != null ? e.method_37908().method_8314(class_1944.field_9282, e.method_24515()) : 0.0);
      register("sky_light", (e, c, l, v) -> e != null ? e.method_37908().method_8314(class_1944.field_9284, e.method_24515()) : 0.0);
      register("night_vision", (e, c, l, v) -> e instanceof class_1309 le ? NightVisionPower.effectScale(le) : 0.0F);
   }

   public record ResolvedVar(ExprVar accessor, boolean needsContainer, boolean needsPeer) {
      public ResolvedVar(ExprVar accessor, boolean needsContainer) {
         this(accessor, needsContainer, false);
      }
   }
}
