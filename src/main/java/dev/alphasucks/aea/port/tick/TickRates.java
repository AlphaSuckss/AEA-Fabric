package dev.alphasucks.aea.port.tick;

import dev.alphasucks.aea.port.ApoliNetwork;
import dev.alphasucks.aea.port.network.payload.TickRateS2C;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_5321;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public final class TickRates {
   public static final int NORMAL = -1;
   public static final int CLEAR_ALL = -1;
   public static final int MAX_RATE = 10000;
   private static final Int2ObjectOpenHashMap<TickRates.Owned> ENTITIES = new Int2ObjectOpenHashMap();
   private static final Map<class_5321<class_1937>, Long2ObjectOpenHashMap<TickState>> CHUNKS = new HashMap();
   private static final Map<class_5321<class_1937>, TickState> DIMENSIONS = new HashMap();
   private static final Int2IntOpenHashMap SYNCED = new Int2IntOpenHashMap();
   private static long currentTick = -1L;
   private static int baseRate = 20;
   private static boolean anyChunkOrDimension;
   private static boolean serverOverridden;
   private static float savedRate = 20.0F;
   private static boolean savedFrozen;
   private static long serverExpiry = -1L;

   private TickRates() {
   }

   public static boolean idle() {
      return ENTITIES.isEmpty() && !anyChunkOrDimension;
   }

   public static void clear() {
      ENTITIES.clear();
      CHUNKS.clear();
      DIMENSIONS.clear();
      SYNCED.clear();
      currentTick = -1L;
      baseRate = 20;
      anyChunkOrDimension = false;
      serverOverridden = false;
      savedRate = 20.0F;
      savedFrozen = false;
      serverExpiry = -1L;
   }

   public static void serverTick(MinecraftServer server) {
      if (serverOverridden && serverExpiry != -1L && server.method_3780() >= serverExpiry) {
         releaseServer(server);
      }

      if (!idle()) {
         sync(server);
      }

      if (idle() && !SYNCED.isEmpty()) {
         SYNCED.clear();
         ApoliNetwork.broadcastTickRate(server, new TickRateS2C(-1, -1, baseRate));
      }
   }

   public static void requestServer(MinecraftServer server, float rate, boolean frozen, int duration) {
      if (!serverOverridden) {
         savedRate = TickRateVanilla.exactRate(server);
         savedFrozen = TickRateVanilla.frozen(server);
         serverOverridden = true;
      }

      serverExpiry = duration <= 0 ? -1L : server.method_3780() + duration;
      TickRateVanilla.setRate(server, rate);
      TickRateVanilla.setFrozen(server, frozen);
   }

   public static void releaseServer(MinecraftServer server) {
      if (serverOverridden) {
         serverOverridden = false;
         serverExpiry = -1L;
         TickRateVanilla.setRate(server, savedRate);
         TickRateVanilla.setFrozen(server, savedFrozen);
      }
   }

   public static int baseRate() {
      return baseRate;
   }

   private static void markScopes() {
      anyChunkOrDimension = !CHUNKS.isEmpty() || !DIMENSIONS.isEmpty();
   }

   private static void sync(MinecraftServer server) {
      long now = server.method_3780();
      if (now != currentTick) {
         currentTick = now;
         baseRate = TickRateVanilla.rate(server);
         advance(now);
      }
   }

   private static void advance(long now) {
      Iterator<TickRates.Owned> entities = ENTITIES.values().iterator();

      while (entities.hasNext()) {
         TickRates.Owned owned = (TickRates.Owned)entities.next();
         if (owned.state().expired(now)) {
            entities.remove();
            class_1297 entity = (class_1297)owned.owner().get();
            if (entity != null && !entity.method_31481()) {
               pushToTrackers(entity, -1);
            }
         } else {
            owned.state().countDown();
         }
      }

      Iterator<Entry<class_5321<class_1937>, Long2ObjectOpenHashMap<TickState>>> byLevel = CHUNKS.entrySet().iterator();

      while (byLevel.hasNext()) {
         Long2ObjectOpenHashMap<TickState> byChunk = (Long2ObjectOpenHashMap<TickState>)((Entry)byLevel.next()).getValue();
         Iterator<TickState> chunks = byChunk.values().iterator();

         while (chunks.hasNext()) {
            TickState state = (TickState)chunks.next();
            if (state.expired(now)) {
               chunks.remove();
            } else {
               state.countDown();
            }
         }

         if (byChunk.isEmpty()) {
            byLevel.remove();
         }
      }

      Iterator<TickState> dimensions = DIMENSIONS.values().iterator();

      while (dimensions.hasNext()) {
         TickState state = (TickState)dimensions.next();
         if (state.expired(now)) {
            dimensions.remove();
         } else {
            state.countDown();
         }
      }

      markScopes();
   }

   public static TickState stateFor(class_1297 entity, TickScope scope) {
      return switch (scope) {
         case ENTITY, SERVER -> entityState(entity);
         case CHUNK -> chunkState(entity.method_37908().method_27983(), entity.method_31476().method_8324());
         case DIMENSION -> dimensionState(entity.method_37908().method_27983());
      };
   }

   public static TickState entityState(class_1297 entity) {
      TickState existing = lookupEntity(entity);
      if (existing != null) {
         return existing;
      }

      TickState state = new TickState();
      ENTITIES.put(entity.method_5628(), new TickRates.Owned(new WeakReference(entity), state));
      return state;
   }

   @Nullable
   private static TickState lookupEntity(class_1297 entity) {
      if (ENTITIES.isEmpty()) {
         return null;
      } else {
         int id = entity.method_5628();
         TickRates.Owned owned = (TickRates.Owned)ENTITIES.get(id);
         if (owned == null) {
            return null;
         } else if (owned.owner().get() != entity) {
            ENTITIES.remove(id);
            return null;
         } else {
            return owned.state();
         }
      }
   }

   public static TickState chunkState(class_5321<class_1937> dimension, long chunkPos) {
      Long2ObjectOpenHashMap<TickState> byChunk = (Long2ObjectOpenHashMap<TickState>)CHUNKS.computeIfAbsent(dimension, key -> new Long2ObjectOpenHashMap());
      TickState state = (TickState)byChunk.get(chunkPos);
      if (state == null) {
         state = new TickState();
         byChunk.put(chunkPos, state);
      }

      anyChunkOrDimension = true;
      return state;
   }

   public static TickState dimensionState(class_5321<class_1937> dimension) {
      anyChunkOrDimension = true;
      return (TickState)DIMENSIONS.computeIfAbsent(dimension, key -> new TickState());
   }

   public static void exempt(class_1297 entity, long expiry) {
      TickState state = entityState(entity);
      if (state.rate() == -1 && !state.frozen()) {
         state.setExempt(true);
         state.keepUntil(expiry);
      }
   }

   public static void clearEntity(class_1297 entity) {
      ENTITIES.remove(entity.method_5628());
   }

   public static void clearChunk(class_5321<class_1937> dimension, long chunkPos) {
      Long2ObjectOpenHashMap<TickState> byChunk = (Long2ObjectOpenHashMap<TickState>)CHUNKS.get(dimension);
      if (byChunk != null) {
         byChunk.remove(chunkPos);
         if (byChunk.isEmpty()) {
            CHUNKS.remove(dimension);
         }

         markScopes();
      }
   }

   public static void clearDimension(class_5321<class_1937> dimension) {
      DIMENSIONS.remove(dimension);
      markScopes();
   }

   public static void prune(TickScope scope, class_1297 entity) {
      switch (scope) {
         case ENTITY:
         case SERVER:
            TickState statex = lookupEntity(entity);
            if (statex != null && statex.isDefault()) {
               clearEntity(entity);
            }
            break;
         case CHUNK:
            class_5321<class_1937> dimx = entity.method_37908().method_27983();
            long pos = entity.method_31476().method_8324();
            Long2ObjectOpenHashMap<TickState> byChunk = (Long2ObjectOpenHashMap<TickState>)CHUNKS.get(dimx);
            if (byChunk != null) {
               TickState statexx = (TickState)byChunk.get(pos);
               if (statexx != null && statexx.isDefault()) {
                  clearChunk(dimx, pos);
               }
            }
            break;
         case DIMENSION:
            class_5321<class_1937> dim = entity.method_37908().method_27983();
            TickState state = (TickState)DIMENSIONS.get(dim);
            if (state != null && state.isDefault()) {
               clearDimension(dim);
            }
      }
   }

   @Nullable
   private static TickState lookupChunk(class_5321<class_1937> dimension, long chunkPos) {
      if (CHUNKS.isEmpty()) {
         return null;
      }

      Long2ObjectOpenHashMap<TickState> byChunk = (Long2ObjectOpenHashMap<TickState>)CHUNKS.get(dimension);
      return byChunk == null ? null : (TickState)byChunk.get(chunkPos);
   }

   private static int resolveEntity(class_1297 entity) {
      int rate = entityRate(entity, baseRate, anyChunkOrDimension);
      pushToTrackers(entity, rate);
      return rate;
   }

   private static int entityRate(class_1297 entity, int base, boolean scopes) {
      TickState own = lookupEntity(entity);
      int rate;
      if (own != null && !own.isDefault()) {
         rate = own.effectiveRate(base);
      } else {
         class_1297 vehicle = entity.method_5854();
         if (vehicle != null) {
            rate = entityRate(vehicle, base, scopes);
         } else {
            rate = scopes ? chunkRate(entity.method_37908(), entity.method_31476().method_8324(), base) : -1;
         }
      }

      return entity.method_5782() ? withController(entity, rate, base) : rate;
   }

   private static int withController(class_1297 vehicle, int rate, int base) {
      class_1297 controller = vehicle.method_5642();
      if (controller == null) {
         return rate;
      } else {
         TickState own = lookupEntity(controller);
         if (own != null && !own.isDefault()) {
            int controlled = own.effectiveRate(base);
            return controlled < (rate == -1 ? base : rate) ? controlled : rate;
         } else {
            return rate;
         }
      }
   }

   private static int chunkRate(class_1937 level, long chunkPos, int base) {
      TickState own = lookupChunk(level.method_27983(), chunkPos);
      if (own != null && !own.isDefault()) {
         return own.effectiveRate(base);
      }

      if (DIMENSIONS.isEmpty()) {
         return -1;
      }

      TickState dimension = (TickState)DIMENSIONS.get(level.method_27983());
      return dimension != null && !dimension.isDefault() ? dimension.effectiveRate(base) : -1;
   }

   public static int resolveChunk(class_1937 level, long chunkPos) {
      return chunkRate(level, chunkPos, baseRate);
   }

   private static void pushToTrackers(class_1297 entity, int rate) {
      int id = entity.method_5628();
      int previous = SYNCED.get(id);
      if (previous != rate) {
         if (rate == -1) {
            if (previous == Integer.MIN_VALUE) {
               return;
            }

            SYNCED.remove(id);
         } else {
            SYNCED.put(id, rate);
         }

         ApoliNetwork.sendTickRateToTrackers(entity, new TickRateS2C(id, rate, baseRate));
      }
   }

   public static boolean shouldTickEntity(class_1297 entity) {
      if (idle()) {
         return true;
      }

      MinecraftServer server = entity.method_5682();
      if (server == null) {
         return true;
      }

      sync(server);
      int rate = resolveEntity(entity);
      if (rate == -1) {
         return true;
      }

      if (!gate(rate, baseRate, entity.method_37908().method_8510())) {
         return false;
      }

      if (rate > 0 && !(entity instanceof class_1309)) {
         entity.field_6007 = true;
      }

      return true;
   }

   public static boolean shouldTickChunk(class_1937 level, long chunkPos) {
      if (!anyChunkOrDimension) {
         return true;
      }

      MinecraftServer server = level.method_8503();
      if (server == null) {
         return true;
      }

      sync(server);
      int rate = resolveChunk(level, chunkPos);
      return rate == -1 || gate(rate, baseRate, level.method_8510());
   }

   public static boolean gate(int rate, int base, long tick) {
      if (rate >= base) {
         return true;
      } else {
         return rate <= 0 ? false : Math.floorDiv(tick * rate, base) != Math.floorDiv((tick - 1L) * rate, base);
      }
   }

   public static int effectiveRate(class_1297 entity, TickScope scope) {
      MinecraftServer server = entity.method_5682();
      int base = TickRateVanilla.rate(server);

      return switch (scope) {
         case ENTITY -> {
            int rate = entityRate(entity, base, true);
            yield rate == -1 ? base : rate;
         }
         case SERVER -> TickRateVanilla.frozen(server) ? 0 : base;
         case CHUNK -> effectiveChunkRate(entity.method_37908(), entity.method_31476().method_8324(), base);
         case DIMENSION -> effectiveDimensionRate(entity.method_37908(), base);
      };
   }

   public static int effectiveChunkRate(class_1937 level, long chunkPos, int base) {
      TickState own = lookupChunk(level.method_27983(), chunkPos);
      return own != null && !own.isDefault() ? own.effectiveRate(base) : effectiveDimensionRate(level, base);
   }

   public static int effectiveDimensionRate(class_1937 level, int base) {
      if (DIMENSIONS.isEmpty()) {
         return base;
      }

      TickState own = (TickState)DIMENSIONS.get(level.method_27983());
      return own == null ? base : own.effectiveRate(base);
   }

   @Nullable
   public static TickState peekEntity(class_1297 entity) {
      return lookupEntity(entity);
   }

   @Nullable
   public static TickState peekChunk(class_5321<class_1937> dimension, long chunkPos) {
      return lookupChunk(dimension, chunkPos);
   }

   static {
      SYNCED.defaultReturnValue(Integer.MIN_VALUE);
   }

   private record Owned(WeakReference<class_1297> owner, TickState state) {
   }
}
