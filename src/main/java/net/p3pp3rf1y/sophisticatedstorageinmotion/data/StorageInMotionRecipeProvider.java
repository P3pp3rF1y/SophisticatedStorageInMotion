package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapeBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapelessBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageIngredient;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class StorageInMotionRecipeProvider extends RecipeProvider {
	public StorageInMotionRecipeProvider(DataGenerator generator) {
		super(generator.getPackOutput());
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		SpecialRecipeBuilder.special(ModItems.UNCRAFT_MOVING_STORAGE_SERIALIZER.get()).save(consumer, SophisticatedStorageInMotion.getRegistryName("uncraft_moving_storage"));

		ShapelessBasedRecipeBuilder.shapeless(ModItems.STORAGE_MINECART.get(), ModItems.MOVING_STORAGE_FROM_STORAGE_SERIALIZER.get())
				.requires(Items.MINECART)
				.requires(ModBlocks.ALL_STORAGE_TAG)
				.unlockedBy("has_sophisticated_storage", has(ModBlocks.ALL_STORAGE_TAG))
				.save(consumer);

		addStorageBoatFromStorageRecipes(consumer);

		ShapelessBasedRecipeBuilder.shapeless(MovingStorageItem.createWithStorage(new ItemStack(ModItems.STORAGE_MINECART.get()), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.CHEST_ITEM.get()), WoodType.OAK)))
				.requires(Items.CHEST_MINECART)
				.requires(Items.REDSTONE_TORCH)
				.unlockedBy("has_chest_minecart", has(Items.CHEST_MINECART))
				.save(consumer, SophisticatedStorageInMotion.getRL("chest_minecart_to_storage_minecart"));

		addVanillaChestBoatConversionRecipes(consumer);

		addTierUpgradeRecipes(consumer, ModItems.STORAGE_MINECART);
		addTierUpgradeRecipes(consumer, ModItems.STORAGE_BOAT);
	}

	private void addVanillaChestBoatConversionRecipes(Consumer<FinishedRecipe> consumer) {
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.OAK, Items.OAK_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.SPRUCE, Items.SPRUCE_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.BIRCH, Items.BIRCH_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.JUNGLE, Items.JUNGLE_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.ACACIA, Items.ACACIA_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.DARK_OAK, Items.DARK_OAK_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.MANGROVE, Items.MANGROVE_CHEST_BOAT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.BAMBOO, Items.BAMBOO_CHEST_RAFT);
		addVanillaChestBoatConversionRecipe(consumer, Boat.Type.CHERRY, Items.CHERRY_CHEST_BOAT);
	}

	private static void addVanillaChestBoatConversionRecipe(Consumer<FinishedRecipe> consumer, Boat.Type boatType, Item vanillaChestBoat) {
		ShapelessBasedRecipeBuilder.shapeless(MovingStorageItem.createWithStorage(StorageBoatItem.setBoatType(new ItemStack(ModItems.STORAGE_BOAT.get()), boatType), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.CHEST_ITEM.get()), WoodType.OAK)))
				.requires(vanillaChestBoat)
				.requires(Items.REDSTONE_TORCH)
				.unlockedBy("has_" + BuiltInRegistries.ITEM.getKey(vanillaChestBoat).getPath(), has(vanillaChestBoat))
				.save(consumer, SophisticatedStorageInMotion.getRL(boatType.getName() + "_storage_" + (boatType == Boat.Type.BAMBOO ? "raft" : "boat") + "_from_vanilla"));
	}

	private void addStorageBoatFromStorageRecipes(Consumer<FinishedRecipe> consumer) {
		StorageBoatItem.SUPPORTED_BOAT_TYPES.forEach((boatType, baseBoat) -> {
			ShapelessBasedRecipeBuilder.shapeless(StorageBoatItem.setBoatType(new ItemStack(ModItems.STORAGE_BOAT.get()), boatType), ModItems.MOVING_STORAGE_FROM_STORAGE_SERIALIZER.get())
					.requires(baseBoat.get())
					.requires(ModBlocks.ALL_STORAGE_TAG)
					.unlockedBy("has_sophisticated_storage", has(ModBlocks.ALL_STORAGE_TAG))
					.save(consumer, SophisticatedStorageInMotion.getRL(boatType.getName() + "_storage_" + (boatType == Boat.Type.BAMBOO ? "raft" : "boat")));
		});
	}

	private static void addTierUpgradeRecipes(Consumer<FinishedRecipe> consumer, RegistryObject<Item> movingStorageItem) {
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.CHEST_ITEM.get(), ModBlocks.COPPER_CHEST_ITEM.get(), Tags.Items.INGOTS_COPPER);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.BARREL_ITEM.get(), ModBlocks.COPPER_BARREL_ITEM.get(), Tags.Items.INGOTS_COPPER);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.SHULKER_BOX_ITEM.get(), ModBlocks.COPPER_SHULKER_BOX_ITEM.get(), Tags.Items.INGOTS_COPPER);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_1_ITEM.get(), ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), Tags.Items.INGOTS_COPPER);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_2_ITEM.get(), ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get(), Tags.Items.INGOTS_COPPER);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_3_ITEM.get(), ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get(), Tags.Items.INGOTS_COPPER);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_4_ITEM.get(), ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get(), Tags.Items.INGOTS_COPPER);

		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.COPPER_CHEST_ITEM.get(), ModBlocks.IRON_CHEST_ITEM.get(), Tags.Items.INGOTS_IRON);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.COPPER_BARREL_ITEM.get(), ModBlocks.IRON_BARREL_ITEM.get(), Tags.Items.INGOTS_IRON);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.COPPER_SHULKER_BOX_ITEM.get(), ModBlocks.IRON_SHULKER_BOX_ITEM.get(), Tags.Items.INGOTS_IRON);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_1_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), Tags.Items.INGOTS_IRON);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), Tags.Items.INGOTS_IRON);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_3_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), Tags.Items.INGOTS_IRON);
		addCheaperMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_COPPER_BARREL_4_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), Tags.Items.INGOTS_IRON);

		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.CHEST_ITEM.get(), ModBlocks.IRON_CHEST_ITEM.get(), Tags.Items.INGOTS_IRON);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.BARREL_ITEM.get(), ModBlocks.IRON_BARREL_ITEM.get(), Tags.Items.INGOTS_IRON);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.SHULKER_BOX_ITEM.get(), ModBlocks.IRON_SHULKER_BOX_ITEM.get(), Tags.Items.INGOTS_IRON);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_1_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), Tags.Items.INGOTS_IRON);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_2_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), Tags.Items.INGOTS_IRON);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_3_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), Tags.Items.INGOTS_IRON);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_BARREL_4_ITEM.get(), ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), Tags.Items.INGOTS_IRON);

		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.IRON_CHEST_ITEM.get(), ModBlocks.GOLD_CHEST_ITEM.get(), Tags.Items.INGOTS_GOLD);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.IRON_BARREL_ITEM.get(), ModBlocks.GOLD_BARREL_ITEM.get(), Tags.Items.INGOTS_GOLD);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.IRON_SHULKER_BOX_ITEM.get(), ModBlocks.GOLD_SHULKER_BOX_ITEM.get(), Tags.Items.INGOTS_GOLD);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_1_ITEM.get(), ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get(), Tags.Items.INGOTS_GOLD);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get(), ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get(), Tags.Items.INGOTS_GOLD);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_3_ITEM.get(), ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get(), Tags.Items.INGOTS_GOLD);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_IRON_BARREL_4_ITEM.get(), ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get(), Tags.Items.INGOTS_GOLD);

		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.GOLD_CHEST_ITEM.get(), ModBlocks.DIAMOND_CHEST_ITEM.get(), Tags.Items.GEMS_DIAMOND);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.GOLD_BARREL_ITEM.get(), ModBlocks.DIAMOND_BARREL_ITEM.get(), Tags.Items.GEMS_DIAMOND);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.GOLD_SHULKER_BOX_ITEM.get(), ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get(), Tags.Items.GEMS_DIAMOND);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get(), ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get(), Tags.Items.GEMS_DIAMOND);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_2_ITEM.get(), ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get(), Tags.Items.GEMS_DIAMOND);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_3_ITEM.get(), ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get(), Tags.Items.GEMS_DIAMOND);
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_GOLD_BARREL_4_ITEM.get(), ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get(), Tags.Items.GEMS_DIAMOND);

		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.DIAMOND_CHEST_ITEM.get(), ModBlocks.NETHERITE_CHEST_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.DIAMOND_BARREL_ITEM.get(), ModBlocks.NETHERITE_BARREL_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.DIAMOND_SHULKER_BOX_ITEM.get(), ModBlocks.NETHERITE_SHULKER_BOX_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM.get(), ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_2_ITEM.get(), ModBlocks.LIMITED_NETHERITE_BARREL_2_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_3_ITEM.get(), ModBlocks.LIMITED_NETHERITE_BARREL_3_ITEM.get());
		addMovingStorageDiamondToNetheriteTierUpgradeRecipe(consumer, movingStorageItem, ModBlocks.LIMITED_DIAMOND_BARREL_4_ITEM.get(), ModBlocks.LIMITED_NETHERITE_BARREL_4_ITEM.get());
	}

	private static void addCheaperMovingStorageTierUpgradeRecipe(Consumer<FinishedRecipe> consumer, RegistryObject<? extends Item> movingStorageItem, Item storageItem, Item upgradedStorageItem, TagKey<Item> material) {
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, storageItem, upgradedStorageItem, material, builder -> builder.pattern(" M ").pattern("MSM").pattern(" M "));
	}

	private static void addMovingStorageTierUpgradeRecipe(Consumer<FinishedRecipe> consumer, RegistryObject<? extends Item> movingStorageItem, Item storageItem, Item upgradedStorageItem, TagKey<Item> material) {
		addMovingStorageTierUpgradeRecipe(consumer, movingStorageItem, storageItem, upgradedStorageItem, material, builder -> builder.pattern("MMM").pattern("MSM").pattern("MMM"));
	}

	private static void addMovingStorageTierUpgradeRecipe(Consumer<FinishedRecipe> consumer, RegistryObject<? extends Item> movingStorageItem, Item storageItem, Item upgradedStorageItem, TagKey<Item> material, UnaryOperator<ShapeBasedRecipeBuilder> patternInit) {
		String storageItemPath = ForgeRegistries.ITEMS.getKey(storageItem).getPath();
		patternInit.apply(ShapeBasedRecipeBuilder.shaped(MovingStorageItem.createWithStorage(new ItemStack(movingStorageItem.get()), new ItemStack(upgradedStorageItem)), ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPED_RECIPE_SERIALIZER.get()))
				.define('S', MovingStorageIngredient.of(movingStorageItem.getHolder().orElseThrow(), storageItem))
				.define('M', material)
				.unlockedBy("has_" + storageItemPath, has(storageItem))
				.save(consumer, SophisticatedStorageInMotion.getRL(movingStorageItem.getKey().location().getPath() + "_with_" + storageItemPath + "_to_" + BuiltInRegistries.ITEM.getKey(upgradedStorageItem).getPath()));
	}

	private static Holder<Item> getHolder(RegistryObject<Item> item) {
		return item.getHolder().orElseThrow();
	}

	private static void addMovingStorageDiamondToNetheriteTierUpgradeRecipe(Consumer<FinishedRecipe> consumer, RegistryObject<? extends Item> movingStorageItem, Item storageItem, Item upgradedStorageItem) {
		String storageItemPath = ForgeRegistries.ITEMS.getKey(storageItem).getPath();
		ShapelessBasedRecipeBuilder.shapeless(MovingStorageItem.createWithStorage(new ItemStack(movingStorageItem.get()), new ItemStack(upgradedStorageItem)), ModItems.MOVING_STORAGE_TIER_UPGRADE_SHAPELESS_RECIPE_SERIALIZER.get())
				.requires(MovingStorageIngredient.of(movingStorageItem.getHolder().orElseThrow(), storageItem))
				.requires(Tags.Items.INGOTS_NETHERITE)
				.unlockedBy("has_" + storageItemPath, has(storageItem))
				.save(consumer, SophisticatedStorageInMotion.getRL(movingStorageItem.getKey().location().getPath() + "_with_" + storageItemPath + "_to_" + BuiltInRegistries.ITEM.getKey(upgradedStorageItem).getPath()));
	}
}
