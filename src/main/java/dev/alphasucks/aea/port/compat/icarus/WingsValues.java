package dev.alphasucks.aea.port.compat.icarus;

import dev.cammiescorner.icarus.api.IcarusPlayerValues;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class WingsValues {
   private WingsValues() {
   }

   public static IcarusPlayerValues of(WingsPower.Config cfg, IcarusPlayerValues fallback) {
      return (IcarusPlayerValues)Proxy.newProxyInstance(
         IcarusPlayerValues.class.getClassLoader(), new Class[]{IcarusPlayerValues.class}, new WingsValues.Handler(cfg, fallback)
      );
   }

   private record Handler(WingsPower.Config cfg, IcarusPlayerValues fallback) implements InvocationHandler {
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         switch (method.getName()) {
            case "wingsSpeed":
               if (this.cfg.wingsSpeed().isPresent()) {
                  return (float)this.cfg.wingsSpeed().getAsDouble();
               }
               break;
            case "maxSlowedMultiplier":
               if (this.cfg.maxSlowedMultiplier().isPresent()) {
                  return (float)this.cfg.maxSlowedMultiplier().getAsDouble();
               }
               break;
            case "armorSlows":
               if (this.cfg.armorSlows().isPresent()) {
                  return this.cfg.armorSlows().get();
               }
               break;
            case "exhaustionAmount":
               if (this.cfg.exhaustionAmount().isPresent()) {
                  return (float)this.cfg.exhaustionAmount().getAsDouble();
               }
               break;
            case "maxHeightAboveWorld":
               if (this.cfg.maxHeightAboveWorld().isPresent()) {
                  return this.cfg.maxHeightAboveWorld().getAsInt();
               }
               break;
            case "maxHeightEnabled":
               if (this.cfg.maxHeightAboveWorld().isPresent()) {
                  return true;
               }
         }

         try {
            return method.invoke(this.fallback, args);
         } catch (InvocationTargetException e) {
            throw e.getCause();
         }
      }
   }
}
