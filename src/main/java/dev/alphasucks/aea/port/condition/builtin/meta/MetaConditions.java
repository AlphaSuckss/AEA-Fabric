package dev.alphasucks.aea.port.condition.builtin.meta;

import dev.alphasucks.aea.port.Apoli;
import dev.alphasucks.aea.port.alias.AliasingOptions;
import dev.alphasucks.aea.port.condition.BiEntityCondition;
import dev.alphasucks.aea.port.condition.BiomeCondition;
import dev.alphasucks.aea.port.condition.BlockCondition;
import dev.alphasucks.aea.port.condition.ConditionTypes;
import dev.alphasucks.aea.port.condition.DamageCondition;
import dev.alphasucks.aea.port.condition.EntityCondition;
import dev.alphasucks.aea.port.condition.FluidCondition;
import dev.alphasucks.aea.port.condition.ItemCondition;
import dev.alphasucks.aea.port.condition.StaticCondition;
import dev.alphasucks.aea.port.condition.TypedConditionRegistry;
import dev.alphasucks.aea.port.condition.context.BiEntityCtx;
import dev.alphasucks.aea.port.condition.context.BiomeCtx;
import dev.alphasucks.aea.port.condition.context.BlockCtx;
import dev.alphasucks.aea.port.condition.context.DamageCtx;
import dev.alphasucks.aea.port.condition.context.EntityCtx;
import dev.alphasucks.aea.port.condition.context.FluidCtx;
import dev.alphasucks.aea.port.condition.context.ItemCtx;
import dev.alphasucks.aea.port.condition.context.StaticCtx;
import net.minecraft.class_2960;

public final class MetaConditions {
   private static final class_2960 ALL_OF = Apoli.id("all_of");
   private static final class_2960 ANY_OF = Apoli.id("any_of");
   private static final class_2960 RANDOM_CHANCE = Apoli.id("random_chance");
   private static final class_2960 CONSTANT = Apoli.id("constant");
   private static final class_2960 NOTHING = Apoli.id("nothing");
   private static final class_2960 MOD_LOADED = Apoli.id("mod_loaded");
   private static final class_2960 MINECRAFT_VERSION = Apoli.id("minecraft_version");

   private MetaConditions() {
   }

   private static AliasingOptions allOfAliases() {
      return AliasingOptions.builder().addTypeAlias(Apoli.id("and")).build();
   }

   private static AliasingOptions anyOfAliases() {
      return AliasingOptions.builder().addTypeAlias(Apoli.id("or")).build();
   }

   private static AliasingOptions randomChanceAliases() {
      return AliasingOptions.builder().addTypeAlias(Apoli.id("chance")).build();
   }

   private static AliasingOptions modLoadedAliases() {
      return AliasingOptions.builder().addTypeAlias(Apoli.id("mod_installed")).renameField("mod_id", "mod").renameField("modid", "mod").build();
   }

   private static AliasingOptions minecraftVersionAliases() {
      return AliasingOptions.builder().addTypeAlias(Apoli.id("game_version")).addTypeAlias(Apoli.id("mc_version")).build();
   }

   private static <CTX> void registerUniversal(TypedConditionRegistry<CTX> reg) {
      reg.register(MOD_LOADED, new ModLoadedCondition<>(), modLoadedAliases());
      reg.register(MINECRAFT_VERSION, new MinecraftVersionCondition<>(), minecraftVersionAliases());
   }

   public static void registerEntity() {
      TypedConditionRegistry<EntityCtx> reg = ConditionTypes.ENTITY;
      reg.register(ALL_OF, new AllOfMeta<EntityCtx, EntityCondition>(EntityCondition.CODEC, EntityCondition::test), allOfAliases());
      reg.register(ANY_OF, new AnyOfMeta<EntityCtx, EntityCondition>(EntityCondition.CODEC, EntityCondition::test), anyOfAliases());
      reg.register(RANDOM_CHANCE, new RandomChanceMeta<>(), randomChanceAliases());
      reg.register(CONSTANT, new ConstantMeta<>());
      reg.register(NOTHING, new NothingMetaCondition<>());
      registerUniversal(reg);
   }

   public static void registerBiEntity() {
      TypedConditionRegistry<BiEntityCtx> reg = ConditionTypes.BI_ENTITY;
      reg.register(ALL_OF, new AllOfMeta<BiEntityCtx, BiEntityCondition>(BiEntityCondition.CODEC, BiEntityCondition::test), allOfAliases());
      reg.register(ANY_OF, new AnyOfMeta<BiEntityCtx, BiEntityCondition>(BiEntityCondition.CODEC, BiEntityCondition::test), anyOfAliases());
      reg.register(RANDOM_CHANCE, new RandomChanceMeta<>(), randomChanceAliases());
      reg.register(CONSTANT, new ConstantMeta<>());
      reg.register(NOTHING, new NothingMetaCondition<>());
      registerUniversal(reg);
   }

   public static void registerBlock() {
      TypedConditionRegistry<BlockCtx> reg = ConditionTypes.BLOCK;
      reg.register(ALL_OF, new AllOfMeta<BlockCtx, BlockCondition>(BlockCondition.CODEC, BlockCondition::test), allOfAliases());
      reg.register(ANY_OF, new AnyOfMeta<BlockCtx, BlockCondition>(BlockCondition.CODEC, BlockCondition::test), anyOfAliases());
      reg.register(RANDOM_CHANCE, new RandomChanceMeta<>(), randomChanceAliases());
      reg.register(CONSTANT, new ConstantMeta<>());
      reg.register(NOTHING, new NothingMetaCondition<>());
      reg.register(Apoli.id("offset"), new OffsetBlockMetaCondition());
      registerUniversal(reg);
   }

   public static void registerItem() {
      TypedConditionRegistry<ItemCtx> reg = ConditionTypes.ITEM;
      reg.register(ALL_OF, new AllOfMeta<ItemCtx, ItemCondition>(ItemCondition.CODEC, ItemCondition::test), allOfAliases());
      reg.register(ANY_OF, new AnyOfMeta<ItemCtx, ItemCondition>(ItemCondition.CODEC, ItemCondition::test), anyOfAliases());
      reg.register(RANDOM_CHANCE, new RandomChanceMeta<>(), randomChanceAliases());
      reg.register(CONSTANT, new ConstantMeta<>());
      reg.register(NOTHING, new NothingMetaCondition<>());
      registerUniversal(reg);
   }

   public static void registerDamage() {
      TypedConditionRegistry<DamageCtx> reg = ConditionTypes.DAMAGE;
      reg.register(ALL_OF, new AllOfMeta<DamageCtx, DamageCondition>(DamageCondition.CODEC, DamageCondition::test), allOfAliases());
      reg.register(ANY_OF, new AnyOfMeta<DamageCtx, DamageCondition>(DamageCondition.CODEC, DamageCondition::test), anyOfAliases());
      reg.register(RANDOM_CHANCE, new RandomChanceMeta<>(), randomChanceAliases());
      reg.register(CONSTANT, new ConstantMeta<>());
      reg.register(NOTHING, new NothingMetaCondition<>());
      registerUniversal(reg);
   }

   public static void registerFluid() {
      TypedConditionRegistry<FluidCtx> reg = ConditionTypes.FLUID;
      reg.register(ALL_OF, new AllOfMeta<FluidCtx, FluidCondition>(FluidCondition.CODEC, FluidCondition::test), allOfAliases());
      reg.register(ANY_OF, new AnyOfMeta<FluidCtx, FluidCondition>(FluidCondition.CODEC, FluidCondition::test), anyOfAliases());
      reg.register(RANDOM_CHANCE, new RandomChanceMeta<>(), randomChanceAliases());
      reg.register(CONSTANT, new ConstantMeta<>());
      reg.register(NOTHING, new NothingMetaCondition<>());
      registerUniversal(reg);
   }

   public static void registerBiome() {
      TypedConditionRegistry<BiomeCtx> reg = ConditionTypes.BIOME;
      reg.register(ALL_OF, new AllOfMeta<BiomeCtx, BiomeCondition>(BiomeCondition.CODEC, BiomeCondition::test), allOfAliases());
      reg.register(ANY_OF, new AnyOfMeta<BiomeCtx, BiomeCondition>(BiomeCondition.CODEC, BiomeCondition::test), anyOfAliases());
      reg.register(RANDOM_CHANCE, new RandomChanceMeta<>(), randomChanceAliases());
      reg.register(CONSTANT, new ConstantMeta<>());
      reg.register(NOTHING, new NothingMetaCondition<>());
      registerUniversal(reg);
   }

   public static void registerStatic() {
      TypedConditionRegistry<StaticCtx> reg = ConditionTypes.STATIC;
      reg.register(ALL_OF, new AllOfMeta<StaticCtx, StaticCondition>(StaticCondition.CODEC, (cond, ctx) -> cond.test(ctx)), allOfAliases());
      reg.register(ANY_OF, new AnyOfMeta<StaticCtx, StaticCondition>(StaticCondition.CODEC, (cond, ctx) -> cond.test(ctx)), anyOfAliases());
      reg.register(CONSTANT, new ConstantMeta<>());
      reg.register(NOTHING, new NothingMetaCondition<>());
      registerUniversal(reg);
   }
}
