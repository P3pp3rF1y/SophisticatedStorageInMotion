package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelMaterial;
import net.p3pp3rf1y.sophisticatedstorage.block.DecorationTableBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ITintableBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static net.p3pp3rf1y.sophisticatedstorage.block.DecorationTableBlockEntity.STORAGE_DECORATOR;

public abstract class MovingStorageItem extends ItemBase {
	public MovingStorageItem(Properties properties) {
		super(properties);
	}

	public static void setStorageItem(ItemStack movingStorageItem, ItemStack storageItem) {
		movingStorageItem.set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(storageItem));
	}

	public static Optional<Item> getStorageItemType(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : Optional.of(storageItemContents.getItem());
	}

	public static Optional<WoodType> getStorageItemWoodType(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : WoodStorageBlockItem.getWoodType(storageItemContents);
	}

	public static Optional<Integer> getStorageItemMainColor(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : StorageBlockItem.getMainColorFromComponentHolder(storageItemContents);
	}

	public static Optional<Integer> getStorageItemAccentColor(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : StorageBlockItem.getAccentColorFromComponentHolder(storageItemContents);
	}

	public static boolean isStorageItemFlatTopBarrel(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents != null && BarrelBlockItem.isFlatTop(storageItemContents);
	}

	public static ItemStack getStorageItem(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.STORAGE_ITEM, SimpleItemContent.EMPTY).copy();
	}

	public abstract ItemStack getUncraftRemainingItem();

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Config.COMMON.enabledItems.isItemEnabled(this)) {
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL_ITEM.get()), WoodType.SPRUCE)));
			ItemStack limitedIStack = new ItemStack(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get());
			if (limitedIStack.getItem() instanceof ITintableBlockItem tintableBlockItem) {
				tintableBlockItem.setMainColor(limitedIStack, DyeColor.YELLOW.getTextureDiffuseColor());
				tintableBlockItem.setAccentColor(limitedIStack, DyeColor.LIME.getTextureDiffuseColor());
			}
			itemConsumer.accept(createWithStorage(this, limitedIStack));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_COPPER_BARREL_2.get()), WoodType.BIRCH)));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_IRON_BARREL_3.get()), WoodType.ACACIA)));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_DIAMOND_BARREL_4.get()), WoodType.CRIMSON)));
			itemConsumer.accept(createWithStorage(this, WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.NETHERITE_CHEST_ITEM.get()), WoodType.BAMBOO)));
			itemConsumer.accept(createWithStorage(this, new ItemStack(ModBlocks.IRON_SHULKER_BOX_ITEM.get())));
		}
	}

	public static ItemStack createWithStorage(Item movingStorageItem, ItemStack storageStack) {
		ItemStack movingStorage = new ItemStack(movingStorageItem);
		MovingStorageItem.setStorageItem(movingStorage, storageStack);
		return movingStorage;
	}

	@Override
	public Component getName(ItemStack stack) {
		SimpleItemContent storageItemContent = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContent != null ? Component.translatable(getDescriptionId(), storageItemContent.copy().getHoverName()) : super.getName(stack);
	}

	static {
		DecorationTableBlockEntity.registerItemDecorator(stack -> stack.getItem() instanceof MovingStorageItem, new DecorationTableBlockEntity.IItemDecorator() {
			@Override
			public boolean supportsMaterials(ItemStack input) {
				ItemStack storageItem = getStorageItem(input);
				return STORAGE_DECORATOR.supportsMaterials(storageItem);
			}

			@Override
			public boolean supportsTints(ItemStack input) {
				ItemStack storageItem = getStorageItem(input);
				return STORAGE_DECORATOR.supportsTints(storageItem);
			}

			@Override
			public boolean supportsTopInnerTrim(ItemStack input) {
				ItemStack storageItem = getStorageItem(input);
				return STORAGE_DECORATOR.supportsTopInnerTrim(storageItem);
			}

			@Override
			public ItemStack decorateWithMaterials(ItemStack input, Map<BarrelMaterial, ResourceLocation> materialsToApply) {
				ItemStack storageItem = getStorageItem(input);
				ItemStack storageResult = STORAGE_DECORATOR.decorateWithMaterials(storageItem, materialsToApply);
				if (storageResult.isEmpty()) {
					return ItemStack.EMPTY;
				}
				ItemStack result = input.copy();
				setStorageItem(result, storageResult);
				return result;
			}

			@Override
			public DecorationTableBlockEntity.TintDecorationResult decorateWithTints(ItemStack input, int mainColorToSet, int accentColorToSet) {
				ItemStack storageItem = getStorageItem(input);
				DecorationTableBlockEntity.TintDecorationResult tintResult = STORAGE_DECORATOR.decorateWithTints(storageItem, mainColorToSet, accentColorToSet);
				if (tintResult.result().isEmpty()) {
					return DecorationTableBlockEntity.TintDecorationResult.EMPTY;
				}
				ItemStack result = input.copy();
				setStorageItem(result, tintResult.result());
				return new DecorationTableBlockEntity.TintDecorationResult(result, tintResult.requiredDyeParts());
			}
		});
	}
}
