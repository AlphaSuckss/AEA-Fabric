package dev.alphasucks.aea.port.power.builtin;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.alphasucks.aea.port.codec.LoggedOptionalField;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.data.ItemStackData;
import dev.alphasucks.aea.port.power.ApoliIds;
import dev.alphasucks.aea.port.power.PowerLookup;
import dev.alphasucks.aea.port.power.PowerType;
import dev.alphasucks.aea.port.power.PowerTypeGate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.class_1657;
import net.minecraft.class_173;
import net.minecraft.class_1799;
import net.minecraft.class_181;
import net.minecraft.class_1914;
import net.minecraft.class_1916;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3988;
import net.minecraft.class_52;
import net.minecraft.class_8567;
import net.minecraft.class_8567.class_8568;
import org.jetbrains.annotations.Nullable;

public final class ExtraTradesPower extends PowerType<ExtraTradesPower.Config> {
   private static final String SAVE_KEY = "apoli:extra_trades";
   private static final PowerTypeGate IN_USE = new PowerTypeGate(ApoliIds.EXTRA_TRADES);

   @Override
   public MapCodec<ExtraTradesPower.Config> configCodec() {
      return RecordCodecBuilder.mapCodec(
         i -> i.group(
               LoggedOptionalField.strict("bientity_condition", BiEntityCondition.CODEC).forGetter(ExtraTradesPower.Config::bientityCondition),
               ExtraTradesPower.Trade.CODEC.listOf().fieldOf("trades").forGetter(ExtraTradesPower.Config::trades)
            )
            .apply(i, ExtraTradesPower.Config::new)
      );
   }

   public static List<class_1914> offersFor(class_3988 merchant, class_1657 player, Map<class_2960, class_1916> rolled) {
      if (IN_USE.inUse() && merchant.method_37908() instanceof class_3218 level && PowerLookup.holdsType(player, ApoliIds.EXTRA_TRADES)) {
         BiEntityCtx ctx = BiEntityCtx.of(player, merchant, level);
         List<class_1914> shown = new ArrayList();
         PowerLookup.forEachEntry(player, ApoliIds.EXTRA_TRADES, ExtraTradesPower.Config.class, (powerId, cfg) -> {
            if (!cfg.bientityCondition().isPresent() || ((BiEntityCondition)cfg.bientityCondition().get()).test(ctx)) {
               shown.addAll((Collection)rolled.computeIfAbsent(powerId, id -> roll(cfg, level, merchant)));
            }
         });
         return shown;
      } else {
         return List.of();
      }
   }

   private static class_1916 roll(ExtraTradesPower.Config cfg, class_3218 level, class_3988 merchant) {
      class_1916 offers = new class_1916();

      for (ExtraTradesPower.Trade trade : cfg.trades()) {
         class_1914 offer = trade.roll(level, merchant);
         if (offer != null) {
            offers.add(offer);
         }
      }

      return offers;
   }

   public static void save(class_3988 merchant, @org.jetbrains.annotations.Nullable Map<class_2960, class_1916> rolled, class_2487 tag) {
      if (rolled != null && !rolled.isEmpty()) {
         class_2487 saved = new class_2487();
         rolled.forEach((powerId, offers) -> saved.method_10566(powerId.toString(), offers.method_8268()));
         tag.method_10566("apoli:extra_trades", saved);
      }
   }

   public static Map<class_2960, class_1916> load(class_3988 merchant, class_2487 tag) {
      Map<class_2960, class_1916> rolled = new LinkedHashMap();
      if (!tag.method_10573("apoli:extra_trades", 10)) {
         return rolled;
      }

      class_2487 saved = tag.method_10562("apoli:extra_trades");

      for (String key : saved.method_10541()) {
         class_2960 powerId = class_2960.method_12829(key);
         if (powerId != null && saved.method_10573(key, 10)) {
            rolled.put(powerId, new class_1916(saved.method_10562(key)));
         }
      }

      return rolled;
   }

   public record Config(Optional<BiEntityCondition> bientityCondition, List<ExtraTradesPower.Trade> trades) {
   }

   public record Trade(
      ExtraTradesPower.TradeItem buy, Optional<ExtraTradesPower.TradeItem> buyB, ExtraTradesPower.TradeItem sell, int maxUses, int xp, float priceMultiplier
   ) {
      static final Codec<ExtraTradesPower.Trade> CODEC = RecordCodecBuilder.create(
         i -> i.group(
               ExtraTradesPower.TradeItem.CODEC.fieldOf("buy").forGetter(ExtraTradesPower.Trade::buy),
               ExtraTradesPower.TradeItem.CODEC.optionalFieldOf("buy_b").forGetter(ExtraTradesPower.Trade::buyB),
               ExtraTradesPower.TradeItem.CODEC.fieldOf("sell").forGetter(ExtraTradesPower.Trade::sell),
               Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("max_uses", 12).forGetter(ExtraTradesPower.Trade::maxUses),
               Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("xp", 1).forGetter(ExtraTradesPower.Trade::xp),
               Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("price_multiplier", 0.05F).forGetter(ExtraTradesPower.Trade::priceMultiplier)
            )
            .apply(i, ExtraTradesPower.Trade::new)
      );

      @Nullable
      class_1914 roll(class_3218 level, class_3988 merchant) {
         class_1799 cost = this.buy.roll(level, merchant);
         class_1799 result = this.sell.roll(level, merchant);
         if (!cost.method_7960() && !result.method_7960()) {
            class_1799 second = (class_1799)this.buyB.map(item -> item.roll(level, merchant)).orElse(class_1799.field_8037);
            return new class_1914(cost, second, result, this.maxUses, this.xp, this.priceMultiplier);
         } else {
            return null;
         }
      }
   }

   public record TradeItem(Optional<ItemStackData> stack, Optional<class_2960> lootTable) {
      private static final Codec<class_2960> LOOT_TABLE = class_2960.field_25139.fieldOf("loot_table").codec();
      static final Codec<ExtraTradesPower.TradeItem> CODEC = new Codec<ExtraTradesPower.TradeItem>() {
         public <T> DataResult<Pair<ExtraTradesPower.TradeItem, T>> decode(DynamicOps<T> ops, T input) {
            boolean table = (Boolean)ops.getMap(input).result().map(map -> map.get("loot_table") != null).orElse(false);
            return table
               ? ExtraTradesPower.TradeItem.LOOT_TABLE
                  .decode(ops, input)
                  .map(p -> p.mapFirst(id -> new ExtraTradesPower.TradeItem(Optional.empty(), Optional.of(id))))
               : ItemStackData.CODEC.decode(ops, input).map(p -> p.mapFirst(data -> new ExtraTradesPower.TradeItem(Optional.of(data), Optional.empty())));
         }

         public <T> DataResult<T> encode(ExtraTradesPower.TradeItem item, DynamicOps<T> ops, T prefix) {
            return item.lootTable.isPresent()
               ? ExtraTradesPower.TradeItem.LOOT_TABLE.encode((class_2960)item.lootTable.get(), ops, prefix)
               : ItemStackData.CODEC.encode((ItemStackData)item.stack.orElseThrow(), ops, prefix);
         }
      };

      class_1799 roll(class_3218 level, class_3988 merchant) {
         if (this.stack.isPresent()) {
            return ((ItemStackData)this.stack.get()).stack().method_7972();
         }

         if (this.lootTable.isEmpty()) {
            return class_1799.field_8037;
         }

         class_52 table = level.method_8503().method_3857().getLootTable((class_2960)this.lootTable.get());
         class_8567 params = new class_8568(level)
            .method_51874(class_181.field_24424, merchant.method_19538())
            .method_51874(class_181.field_1226, merchant)
            .method_51875(class_173.field_16235);
         List<class_1799> rolled = table.method_51878(params);
         return rolled.isEmpty() ? class_1799.field_8037 : (class_1799)rolled.get(0);
      }
   }
}
