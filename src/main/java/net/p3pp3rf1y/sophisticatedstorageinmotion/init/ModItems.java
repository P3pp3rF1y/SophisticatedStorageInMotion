package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.*;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageMinecartItem;

import java.util.function.Supplier;

public class ModItems {
	private ModItems() {
	}

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, SophisticatedStorageInMotion.MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB.location(), SophisticatedStorageInMotion.MOD_ID);

	public static final DeferredHolder<Item, StorageMinecartItem> STORAGE_MINECART = ITEMS.register("storage_minecart", StorageMinecartItem::new);

	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, SophisticatedStorageInMotion.MOD_ID);

	private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, SophisticatedStorageInMotion.MOD_ID);
	public static final Supplier<RecipeSerializer<?>> MOVING_STORAGE_FROM_STORAGE_SERIALIZER = RECIPE_SERIALIZERS.register("moving_storage_from_storage", MovingStorageFromStorageRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> UNCRAFT_MOVING_STORAGE_SERIALIZER = RECIPE_SERIALIZERS.register("uncraft_moving_storage", () -> new SimpleCraftingRecipeSerializer<>(UncraftMovingStorageRecipe::new));
	public static final Supplier<RecipeSerializer<?>> MOVING_STORAGE_TIER_UPGRADE_SHAPED_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("moving_storage_tier_upgrade_shaped_recipe", MovingStorageTierUpgradeShapedRecipe.Serializer::new);
	public static final Supplier<RecipeSerializer<?>> MOVING_STORAGE_TIER_UPGRADE_SHAPELESS_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("moving_storage_tier_upgrade_shapeless_recipe", MovingStorageTierUpgradeShapelessRecipe.Serializer::new);
	private static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, SophisticatedStorageInMotion.MOD_ID);
	public static final Supplier<IngredientType<MovingStorageIngredient>> MOVING_STORAGE_INGREDIENT_TYPE = INGREDIENT_TYPES.register("moving_storage", () -> new IngredientType<>(MovingStorageIngredient.CODEC));

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
		ATTACHMENT_TYPES.register(modBus);
		RECIPE_SERIALIZERS.register(modBus);
		INGREDIENT_TYPES.register(modBus);
	}

	public static void registerDispenseBehavior() {
		DispenserBlock.registerBehavior(STORAGE_MINECART.get(), StorageMinecartItem.DISPENSE_ITEM_BEHAVIOR);
	}
}
