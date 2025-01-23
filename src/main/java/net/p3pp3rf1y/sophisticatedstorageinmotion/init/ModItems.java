package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.*;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageMinecartItem;

import java.util.function.Supplier;

public class ModItems {
	private ModItems() {
	}

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SophisticatedStorageInMotion.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB.location(), SophisticatedStorageInMotion.MOD_ID);

	public static final RegistryObject<Item> STORAGE_MINECART = ITEMS.register("storage_minecart", StorageMinecartItem::new);

	public static final RegistryObject<Item> STORAGE_BOAT = ITEMS.register("storage_boat", StorageBoatItem::new);

	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SophisticatedStorageInMotion.MOD_ID);
	public static final RegistryObject<RecipeSerializer<?>> MOVING_STORAGE_FROM_STORAGE_SERIALIZER = RECIPE_SERIALIZERS.register("moving_storage_from_storage", MovingStorageFromStorageRecipe.Serializer::new);
	public static final RegistryObject<RecipeSerializer<? extends CraftingRecipe>> UNCRAFT_MOVING_STORAGE_SERIALIZER = RECIPE_SERIALIZERS.register("uncraft_moving_storage", () -> new SimpleCraftingRecipeSerializer<>(UncraftMovingStorageRecipe::new));
	public static final Supplier<RecipeSerializer<?>> MOVING_STORAGE_TIER_UPGRADE_SHAPED_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("moving_storage_tier_upgrade_shaped_recipe", MovingStorageTierUpgradeShapedRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> MOVING_STORAGE_TIER_UPGRADE_SHAPELESS_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("moving_storage_tier_upgrade_shapeless_recipe", MovingStorageTierUpgradeShapelessRecipe.Serializer::new);

	public static Supplier<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () ->
			CreativeModeTab.builder().icon(() -> new ItemStack(STORAGE_MINECART.get()))
					.title(Component.translatable("itemGroup.sophisticatedstorageinmotion"))
					.displayItems((featureFlags, output) -> {
						ITEMS.getEntries().stream().filter(i -> i.get() instanceof ItemBase).forEach(i -> ((ItemBase) i.get()).addCreativeTabItems(output::accept));
					})
					.build());

	public static void registerHandlers(IEventBus modBus) {
		ITEMS.register(modBus);
		CREATIVE_MODE_TABS.register(modBus);
		RECIPE_SERIALIZERS.register(modBus);
		modBus.addListener(ModItems::registerRecipeIngredients);
	}

	private static void registerRecipeIngredients(RegisterEvent event) {
		if (event.getRegistryKey().equals(ForgeRegistries.Keys.RECIPE_SERIALIZERS)) {
			CraftingHelper.register(new ResourceLocation(SophisticatedStorageInMotion.MOD_ID, "moving_storage"), MovingStorageIngredient.Serializer.INSTANCE);
		}
	}

	public static void registerDispenseBehavior() {
		DispenserBlock.registerBehavior(STORAGE_MINECART.get(), StorageMinecartItem.DISPENSE_ITEM_BEHAVIOR);
		DispenserBlock.registerBehavior(STORAGE_BOAT.get(), StorageBoatItem.DISPENSE_ITEM_BEHAVIOR);
	}
}
