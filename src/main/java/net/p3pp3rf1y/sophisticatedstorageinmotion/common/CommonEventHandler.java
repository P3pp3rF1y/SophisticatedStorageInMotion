package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.PaintbrushItem;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StackStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class CommonEventHandler {
	private CommonEventHandler() {
	}

	public static void registerHandlers() {
		IEventBus eventBus = NeoForge.EVENT_BUS;
		eventBus.addListener(CommonEventHandler::onMovingStorageUncrafted);
		eventBus.addListener(CommonEventHandler::onMovingStorageCraftedFromShulkerBox);
		eventBus.addListener(TierUpgradeHandler::onTierUpgradeInteract);
		eventBus.addListener(StorageToolHandler::onStorageToolInteract);
		eventBus.addListener(CommonEventHandler::onPacked);
		eventBus.addListener(CommonEventHandler::onPaintbrushInteract);
		eventBus.addListener(CommonEventHandler::onStorageUpgradeInteract);
	}

	private static void onStorageUpgradeInteract(PlayerInteractEvent.EntityInteract event) {
		Player player = event.getEntity();
		ItemStack itemInHand = player.getItemInHand(event.getHand());
		if (!(event.getTarget() instanceof IMovingStorageEntity movingStorage) || !(itemInHand.getItem() instanceof UpgradeItemBase<?>)) {
			return;
		}

		if (StorageBlockBase.tryAddSingleUpgrade(player, event.getHand(), itemInHand, movingStorage.getStorageHolder().getStorageWrapper())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}

	private static void onPaintbrushInteract(PlayerInteractEvent.EntityInteract event) {
		Player player = event.getEntity();
		ItemStack itemInHand = player.getItemInHand(event.getHand());
		if (!(event.getTarget() instanceof IMovingStorageEntity movingStorage) || itemInHand.getItem() != ModItems.PAINTBRUSH.get()) {
			return;
		}

		if (!(movingStorage.getStorageItem().getItem() instanceof BlockItem blockItem)) {
			return;
		}
		BlockState state = blockItem.getBlock().defaultBlockState();
		SoundEvent placeSound = state.getSoundType().getPlaceSound();
		if (PaintbrushItem.paint(player, itemInHand, movingStorage.getStorageHolder(), movingStorage.getStorageHolder().getStorageWrapper(),
				event.getTarget().position(), Direction.UP, placeSound)) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}

	private static void onPacked(PlayerInteractEvent.EntityInteract event) {
		Player player = event.getEntity();
		ItemStack itemInHand = player.getItemInHand(event.getHand());
		if (!(event.getTarget() instanceof IMovingStorageEntity movingStorage) || itemInHand.getItem() != ModItems.PACKING_TAPE.get() || Config.COMMON.dropPacked.get()) {
			return;
		}

		if (movingStorage.getStorageHolder().pack()) {
			if (!player.isCreative()) {
				itemInHand.setDamageValue(itemInHand.getDamageValue() + 1);
				if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage()) {
					player.setItemInHand(event.getHand(), ItemStack.EMPTY);
				}
			}
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}

	private static void onMovingStorageUncrafted(PlayerEvent.ItemCraftedEvent event) {
		ItemStack result = event.getCrafting();

		if (event.getEntity().level().isClientSide() || !(result.getItem() instanceof StorageBlockItem) || !isUncraftedFromSingleMovingStorage(event.getInventory())) {
			return;
		}

		@Nullable UUID storageId = result.get(ModCoreDataComponents.STORAGE_UUID);

		if (storageId == null) {
			return;
		}

		MovingStorageData storageData = MovingStorageData.get(storageId);
		CompoundTag contents = storageData.getContents();
		contents.put(StorageWrapper.RENDER_INFO_TAG, result.getOrDefault(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.EMPTY).copyTag());
		CompoundTag fullContents = new CompoundTag();
		fullContents.put(StorageBlockEntity.STORAGE_WRAPPER_TAG, contents);

		ItemContentsStorage.get().setStorageContents(storageId, fullContents);

		storageData.removeStorageContents();
	}

	private static boolean isUncraftedFromSingleMovingStorage(Container inventory) {
		boolean hasMovingStorage = false;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);

			if (!hasMovingStorage && stack.getItem() instanceof MovingStorageItem) {
				hasMovingStorage = true;
			} else if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private static void onMovingStorageCraftedFromShulkerBox(PlayerEvent.ItemCraftedEvent event) {
		Level level = event.getEntity().level();
		ItemStack result = event.getCrafting();

		if (level.isClientSide()) {
			return;
		}

		if (!isCraftedFromShulkerBox(event.getInventory())) {
			return;
		}

		ItemStack storageItem = MovingStorageItem.getStorageItem(result);
		if (storageItem.getItem() instanceof ShulkerBoxItem) {
					StackStorageWrapper shulkerStorageWrapper = StackStorageWrapper.fromStack(level.registryAccess(), storageItem);
				shulkerStorageWrapper.getContentsUuid().ifPresent(id -> {
					ItemContentsStorage itemContentsStorage = ItemContentsStorage.get();
					CompoundTag contentsNbt = itemContentsStorage.getOrCreateStorageContents(id).getCompound(StorageBlockEntity.STORAGE_WRAPPER_TAG);
					CompoundTag migratedContentsNbt = new CompoundTag();
					migratedContentsNbt.put(StorageWrapper.CONTENTS_TAG, contentsNbt.getCompound(StorageWrapper.CONTENTS_TAG));
					migratedContentsNbt.put(StorageWrapper.SETTINGS_TAG, contentsNbt.getCompound(StorageWrapper.SETTINGS_TAG));
					MovingStorageData.get(id).setContents(migratedContentsNbt);
					storageItem.set(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.of(contentsNbt.getCompound(StorageWrapper.RENDER_INFO_TAG)));
					MovingStorageItem.setStorageItem(result, storageItem);
					itemContentsStorage.removeStorageContents(id);
				});
				MovingStorageItem.setStorageItem(result, storageItem);
		}
	}

	private static boolean isCraftedFromShulkerBox(Container craftingGrid) {
		boolean foundShulker = false;
		for (int slot = 0; slot < craftingGrid.getContainerSize(); slot++) {
			if (craftingGrid.getItem(slot).getItem() instanceof ShulkerBoxItem) {
				foundShulker = true;
			}
		}
		return foundShulker;
	}
}
