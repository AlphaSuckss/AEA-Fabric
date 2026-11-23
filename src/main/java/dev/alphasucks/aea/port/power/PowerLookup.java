package dev.alphasucks.aea.port.power;

import dev.alphasucks.aea.port.attribution.PowerCause;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.class_1297;
import net.minecraft.class_2960;
import org.jetbrains.annotations.Nullable;

public final class PowerLookup {
   private PowerLookup() {
   }

   public static boolean holdsType(@Nullable class_1297 entity, class_2960 canonicalId) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      return container != null && !container.isEmpty() && !container.powersOfType(canonicalId).isEmpty();
   }

   public static boolean hasActive(@Nullable class_1297 entity, class_2960 canonicalId) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(canonicalId);
         if (powers.isEmpty()) {
            return false;
         }

         EntityCtx ctx = null;
         int i = 0;

         while (true) {
            if (i >= powers.size()) {
               return false;
            }

            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null) {
                  if (!power.condition().isPresent()) {
                     break;
                  }

                  if (ctx == null) {
                     ctx = EntityCtx.of(entity, entity.method_37908());
                  }

                  if (((EntityCondition)power.condition().get()).test(ctx)) {
                     break;
                  }
               }
            }

            i++;
         }

         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public static <C> C firstActive(@Nullable class_1297 entity, class_2960 canonicalId, Class<C> configClass) {
      if (entity == null) {
         return null;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(canonicalId);
         if (powers.isEmpty()) {
            return null;
         }

         EntityCtx ctx = null;
         int i = 0;

         Object cfg;
         while (true) {
            if (i >= powers.size()) {
               return null;
            }

            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null) {
                  cfg = power.config();
                  if (configClass.isInstance(cfg)) {
                     if (!power.condition().isPresent()) {
                        break;
                     }

                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (((EntityCondition)power.condition().get()).test(ctx)) {
                        break;
                     }
                  }
               }
            }

            i++;
         }

         return (C)cfg;
      } else {
         return null;
      }
   }

   public static <C> void forEachEntry(@Nullable class_1297 entity, class_2960 canonicalId, Class<C> configClass, BiConsumer<class_2960, C> consumer) {
      if (entity != null) {
         PowerContainer container = PowerContainer.of(entity);
         if (container != null && !container.isEmpty()) {
            List<class_2960> powers = container.powersOfType(canonicalId);
            if (!powers.isEmpty()) {
               EntityCtx ctx = null;

               for (int i = 0; i < powers.size(); i++) {
                  class_2960 powerId = (class_2960)powers.get(i);
                  if (!container.isSuppressed(powerId)) {
                     Power power = ApoliPowers.get(powerId);
                     if (power != null) {
                        Object cfg = power.config();
                        if (configClass.isInstance(cfg)) {
                           if (power.condition().isPresent()) {
                              if (ctx == null) {
                                 ctx = EntityCtx.of(entity, entity.method_37908());
                              }

                              if (!((EntityCondition)power.condition().get()).test(ctx)) {
                                 continue;
                              }
                           }

                           boolean attributed = PowerCause.push(entity, powerId);

                           try {
                              consumer.accept(powerId, (C)cfg);
                           } finally {
                              if (attributed) {
                                 PowerCause.pop();
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static <C> void forEach(@Nullable class_1297 entity, class_2960 canonicalId, Class<C> configClass, Consumer<C> consumer) {
      if (entity != null) {
         PowerContainer container = PowerContainer.of(entity);
         if (container != null && !container.isEmpty()) {
            List<class_2960> powers = container.powersOfType(canonicalId);
            if (!powers.isEmpty()) {
               EntityCtx ctx = null;

               for (int i = 0; i < powers.size(); i++) {
                  class_2960 powerId = (class_2960)powers.get(i);
                  if (!container.isSuppressed(powerId)) {
                     Power power = ApoliPowers.get(powerId);
                     if (power != null) {
                        Object cfg = power.config();
                        if (configClass.isInstance(cfg)) {
                           if (power.condition().isPresent()) {
                              if (ctx == null) {
                                 ctx = EntityCtx.of(entity, entity.method_37908());
                              }

                              if (!((EntityCondition)power.condition().get()).test(ctx)) {
                                 continue;
                              }
                           }

                           boolean attributed = PowerCause.push(entity, powerId);

                           try {
                              consumer.accept((C)cfg);
                           } finally {
                              if (attributed) {
                                 PowerCause.pop();
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static <C> boolean anyActive(@Nullable class_1297 entity, class_2960 canonicalId, Class<C> configClass, Predicate<C> test) {
      return anyActive(entity, canonicalId, configClass, PowerLookup::testConfig, test);
   }

   private static <C> boolean testConfig(C config, Predicate<C> test) {
      return test.test(config);
   }

   public static <C, A> boolean anyActive(@Nullable class_1297 entity, class_2960 canonicalId, Class<C> configClass, BiPredicate<C, A> test, A argument) {
      if (entity == null) {
         return false;
      }

      PowerContainer container = PowerContainer.of(entity);
      if (container != null && !container.isEmpty()) {
         List<class_2960> powers = container.powersOfType(canonicalId);
         if (powers.isEmpty()) {
            return false;
         }

         EntityCtx ctx = null;
         int i = 0;

         while (true) {
            if (i >= powers.size()) {
               return false;
            }

            class_2960 powerId = (class_2960)powers.get(i);
            if (!container.isSuppressed(powerId)) {
               Power power = ApoliPowers.get(powerId);
               if (power != null) {
                  Object cfg = power.config();
                  if (configClass.isInstance(cfg) && test.test((C)cfg, argument)) {
                     if (!power.condition().isPresent()) {
                        break;
                     }

                     if (ctx == null) {
                        ctx = EntityCtx.of(entity, entity.method_37908());
                     }

                     if (((EntityCondition)power.condition().get()).test(ctx)) {
                        break;
                     }
                  }
               }
            }

            i++;
         }

         return true;
      } else {
         return false;
      }
   }

   public static <C> void collect(@Nullable class_1297 entity, class_2960 canonicalId, Class<C> configClass, List<C> out) {
      if (entity != null) {
         PowerContainer container = PowerContainer.of(entity);
         if (container != null && !container.isEmpty()) {
            List<class_2960> powers = container.powersOfType(canonicalId);
            if (!powers.isEmpty()) {
               EntityCtx ctx = null;

               for (int i = 0; i < powers.size(); i++) {
                  class_2960 powerId = (class_2960)powers.get(i);
                  if (!container.isSuppressed(powerId)) {
                     Power power = ApoliPowers.get(powerId);
                     if (power != null) {
                        Object cfg = power.config();
                        if (configClass.isInstance(cfg)) {
                           if (power.condition().isPresent()) {
                              if (ctx == null) {
                                 ctx = EntityCtx.of(entity, entity.method_37908());
                              }

                              if (!((EntityCondition)power.condition().get()).test(ctx)) {
                                 continue;
                              }
                           }

                           out.add((C)cfg);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static <C> List<C> active(@Nullable class_1297 entity, class_2960 canonicalId, Class<C> configClass) {
      List<C>[] result = new List[]{List.of()};
      forEach(entity, canonicalId, configClass, c -> {
         if (result[0] == List.of()) {
            result[0] = new ArrayList();
         }

         result[0].add(c);
      });
      return result[0];
   }
}
