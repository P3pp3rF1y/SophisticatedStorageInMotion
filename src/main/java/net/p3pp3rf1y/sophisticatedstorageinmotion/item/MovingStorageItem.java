package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.api.IStashStorageItem;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelMaterial;
import net.p3pp3rf1y.sophisticatedstorage.block.DecorationTableBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ITintableBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static net.p3pp3rf1y.sophisticatedstorage.block.DecorationTableBlockEntity.STORAGE_DECORATOR;

public abstract class MovingStorageItem extends ItemBase implements IStashStorageItem {
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

	public static ItemStack getStorageItem(DataComponentHolder stack) {
		return stack.getOrDefault(ModDataComponents.STORAGE_ITEM, SimpleItemContent.EMPTY).copy();
	}

	public abstract ItemStack getUncraftRemainingItem(ItemStack input);

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Config.COMMON.enabledItems.isItemEnabled(this)) {
			List<ItemStack> movingStorages = getBaseMovingStorageItems();
			movingStorages.forEach(movingStorage -> {
				itemConsumer.accept(createWithStorage(movingStorage.copy(), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL_ITEM.get()), WoodType.SPRUCE)));
				ItemStack limitedIStack = new ItemStack(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get());
				if (limitedIStack.getItem() instanceof ITintableBlockItem tintableBlockItem) {
					tintableBlockItem.setMainColor(limitedIStack, DyeColor.YELLOW.getTextureDiffuseColor());
					tintableBlockItem.setAccentColor(limitedIStack, DyeColor.LIME.getTextureDiffuseColor());
				}
				itemConsumer.accept(createWithStorage(movingStorage.copy(), limitedIStack));
				itemConsumer.accept(createWithStorage(movingStorage.copy(), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_COPPER_BARREL_2.get()), WoodType.BIRCH)));
				itemConsumer.accept(createWithStorage(movingStorage.copy(), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_IRON_BARREL_3.get()), WoodType.ACACIA)));
				itemConsumer.accept(createWithStorage(movingStorage.copy(), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_DIAMOND_BARREL_4.get()), WoodType.CRIMSON)));
				itemConsumer.accept(createWithStorage(movingStorage.copy(), WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.NETHERITE_CHEST_ITEM.get()), WoodType.BAMBOO)));
				itemConsumer.accept(createWithStorage(movingStorage.copy(), new ItemStack(ModBlocks.IRON_SHULKER_BOX_ITEM.get())));
			});
		}
	}

	public List<ItemStack> getBaseMovingStorageItems() {
		return List.of(new ItemStack(this));
	}

	public static ItemStack createWithStorage(ItemStack movingStorage, ItemStack storageStack) {
		MovingStorageItem.setStorageItem(movingStorage, storageStack);
		return movingStorage;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
		super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag);
		if (tooltipFlag.isAdvanced()) {
			HolderLookup.Provider registries = context.registries();
			if (registries != null && MovingStorageWrapper.hasContentsUuid(stack)) {
				getMovingStorageWrapper(stack).getContentsUuid().ifPresent(uuid -> {
					tooltipAdder.accept(Component.literal("UUID: " + uuid).withStyle(ChatFormatting.DARK_GRAY));

				});
			}
		}
		if (!Minecraft.getInstance().hasShiftDown() && MovingStorageWrapper.hasContentsUuid(stack)) {
			tooltipAdder.accept(Component.translatable(
					TranslationHelper.INSTANCE.translItemTooltip("storage") + ".press_for_contents",
					Component.translatable(TranslationHelper.INSTANCE.translItemTooltip("storage") + ".shift").withStyle(ChatFormatting.AQUA)
			).withStyle(ChatFormatting.GRAY));
		}
	}

	@Override
	public Component getName(ItemStack stack) {
		SimpleItemContent storageItemContent = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContent != null ? Component.translatable(getDescriptionId(), storageItemContent.copy().getHoverName()) : super.getName(stack);
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		if (FMLEnvironment.getDist().isClient()) {
			return Optional.ofNullable(MovingStorageItemClient.getTooltipImage(stack));
		}
		return Optional.empty();
	}

	@Override
	public Optional<TooltipComponent> getInventoryTooltip(ItemStack stack) {
		return Optional.of(new MovingStorageContentsTooltip(stack));
	}

	@Override
	public StashResult getItemStashable(HolderLookup.Provider registries, ItemStack storageStack, ItemStack stack) {
		if (getStorageItemType(storageStack).map(item -> item instanceof ShulkerBoxItem).orElse(false)) {
			MovingStorageWrapper wrapper = getMovingStorageWrapper(storageStack);

			try (Transaction tx = Transaction.openRoot()) {
				if (wrapper.getInventoryForUpgradeProcessing().insert(ItemResource.of(stack), stack.getCount(), tx) == 0) {
					return StashResult.NO_SPACE;
				}
			}
			if (wrapper.getInventoryHandler().getSlotTracker().getItems().contains(stack.getItem()) || wrapper.getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).matchesFilter(stack)) {
				return StashResult.MATCH_AND_SPACE;
			}

			return StashResult.SPACE;
		}

		return StashResult.NO_SPACE;
	}

	public static MovingStorageWrapper getMovingStorageWrapper(ItemStack movingStorageStack) {
		ItemStack storageItem = getStorageItem(movingStorageStack);
		MovingStorageWrapper wrapper = MovingStorageWrapper.fromStack(storageItem, () -> {
				},
				() -> movingStorageStack.set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(storageItem)), MovingStorageData::get,
				() -> movingStorageStack.getOrDefault(net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents.LOCKED, false),
				locked -> movingStorageStack.set(net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents.LOCKED, locked),
				upgrade -> true);
		return wrapper;
	}

	public int stash(ItemStack movingStorageStack, ItemResource resource, int amount, TransactionContext tx) {
		MovingStorageWrapper wrapper = getMovingStorageWrapper(movingStorageStack);
		if (wrapper.getContentsUuid().isEmpty()) {
			wrapper.setContentsUuid(UUID.randomUUID());
		}
		return wrapper.getInventoryForUpgradeProcessing().insert(resource, amount, tx);
	}

	@Override
	public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		if (hasCreativeScreenContainerOpen(player) || stack.getCount() > 1 || !slot.mayPickup(player) || slot.getItem().isEmpty() || action != ClickAction.PRIMARY || !isShulkerBoxMovingStorage(stack)) {
			return super.overrideStackedOnOther(stack, slot, action, player);
		}

		ItemStack stackToStash = slot.getItem();
		try (Transaction tx = Transaction.openRoot()) {
			int stashed = stash(stack, ItemResource.of(stackToStash), stackToStash.getCount(), tx);
			if (stashed > 0) {
				tx.commit();
				slot.safeTake(stashed, stashed, player);
				return true;
			}
		}

		return super.overrideStackedOnOther(stack, slot, action, player);
	}

	private boolean isShulkerBoxMovingStorage(ItemStack movingStorageStack) {
		return getStorageItemType(movingStorageStack).map(item -> item instanceof ShulkerBoxItem).orElse(false);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction action, Player player, SlotAccess carriedAccess) {
		if (hasCreativeScreenContainerOpen(player) || stack.getCount() > 1 || !slot.mayPlace(stack) || action != ClickAction.PRIMARY || !isShulkerBoxMovingStorage(stack)) {
			return super.overrideOtherStackedOnMe(stack, otherStack, slot, action, player, carriedAccess);
		}

		try (Transaction tx = Transaction.openRoot()) {
			int stashed = stash(stack, ItemResource.of(otherStack), otherStack.getCount(), tx);
			if (stashed > 0) {
				tx.commit();
				carriedAccess.set(otherStack.copyWithCount(otherStack.getCount() - stashed));
				slot.set(stack);
				return true;
			}
		}

		return super.overrideOtherStackedOnMe(stack, otherStack, slot, action, player, carriedAccess);
	}

	private boolean hasCreativeScreenContainerOpen(Player player) {
		return player.level().isClientSide() && player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu;
	}

	public record MovingStorageContentsTooltip(ItemStack movingStorage) implements TooltipComponent {
		public ItemStack getMovingStorage() {
			return movingStorage;
		}
	}

	static {
		DecorationTableBlockEntity.registerItemDecorator(item -> item instanceof MovingStorageItem, new DecorationTableBlockEntity.IItemDecorator() {
			@Override
			public boolean supportsMaterials(ItemResource input) {
				ItemStack storageItem = getStorageItem(input);
				return STORAGE_DECORATOR.supportsMaterials(ItemResource.of(storageItem));
			}

			@Override
			public boolean supportsTints(ItemResource input) {
				ItemStack storageItem = getStorageItem(input);
				return STORAGE_DECORATOR.supportsTints(ItemResource.of(storageItem));
			}

			@Override
			public boolean supportsTopInnerTrim(ItemResource input) {
				ItemStack storageItem = getStorageItem(input);
				return STORAGE_DECORATOR.supportsTopInnerTrim(ItemResource.of(storageItem));
			}

			@Override
			public ItemStack decorateWithMaterials(ItemStack input, Map<BarrelMaterial, Identifier> materialsToApply) {
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
