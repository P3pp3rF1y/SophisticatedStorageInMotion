package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.EnderLinkerItem;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageService;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageStackData;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.entity.StorageHolderToolHandler;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.PackingTapeItem;
import net.p3pp3rf1y.sophisticatedstorage.item.PaintbrushItem;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingLinkedStorageEndpointAdapter;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.UUID;

public class CommonEventHandler {
	private CommonEventHandler() {
	}

	public static void registerHandlers() {
		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		eventBus.addListener(CommonEventHandler::onMovingStorageUncrafted);
		eventBus.addListener(CommonEventHandler::onMovingStorageCraftedFromShulkerBox);
		eventBus.addListener(CommonEventHandler::onMovingStorageTierUpgraded);
		eventBus.addListener(TierUpgradeHandler::onTierUpgradeInteract);
		eventBus.addListener(CommonEventHandler::onStorageToolInteract);
		eventBus.addListener(CommonEventHandler::onPacked);
		eventBus.addListener(CommonEventHandler::onPaintbrushInteract);
		eventBus.addListener(CommonEventHandler::onStorageUpgradeInteract);
		eventBus.addListener(CommonEventHandler::onEnderLinkerInteract);
	}

	private static void onEnderLinkerInteract(PlayerInteractEvent.EntityInteract event) {
		if (!(event.getTarget() instanceof IMovingStorageEntity movingStorage)
				|| !(event.getEntity().getItemInHand(event.getHand()).getItem() instanceof EnderLinkerItem)) {
			return;
		}
		EnderLinkerItem.tryLinkInteractionTarget(event.getEntity(), event.getEntity().getItemInHand(event.getHand()), movingStorage.getStorageHolder(),
				event.getTarget().blockPosition()).ifPresent(result -> {
					event.setCanceled(true);
					event.setCancellationResult(result == LinkedStorageService.LinkResult.SUCCESS ? InteractionResult.SUCCESS : InteractionResult.FAIL);
				});
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
		if (!(event.getTarget() instanceof IMovingStorageEntity movingStorage) || !(itemInHand.getItem() instanceof PackingTapeItem)) {
			return;
		}

		if (Config.COMMON.dropPacked.get()) {
			player.displayClientMessage(Component.translatable("gui.sophisticatedstorage.status.packing_tape_disabled"), true);
		} else if (movingStorage.getStorageHolder().isLinkedStorage()) {
			player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 1, 0.7F);
			player.displayClientMessage(StorageTranslationHelper.INSTANCE.translStatusMessage("packing_tape_linked_storage"), true);
		} else if (movingStorage.getStorageHolder().pack()) {
			if (!player.isCreative()) {
				itemInHand.setDamageValue(itemInHand.getDamageValue() + 1);
				if (itemInHand.getDamageValue() >= itemInHand.getMaxDamage()) {
					player.setItemInHand(event.getHand(), ItemStack.EMPTY);
				}
			}
		} else {
			return;
		}
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
	}

	private static void onMovingStorageUncrafted(PlayerEvent.ItemCraftedEvent event) {
		ItemStack result = event.getCrafting();

		if (event.getEntity().level().isClientSide() || !(result.getItem() instanceof StorageBlockItem)
				|| !isUncraftedFromSingleMovingStorage(event.getInventory())) {
			return;
		}

		NBTHelper.getUniqueId(result, StorageWrapper.UUID_TAG).ifPresent(storageId -> {
			MovingStorageData.moveToItemStorage(result, storageId);
		});
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

	private static void onMovingStorageTierUpgraded(PlayerEvent.ItemCraftedEvent event) {
		if (event.getEntity().level() instanceof ServerLevel serverLevel) {
			MovingLinkedStorageEndpointAdapter.completePrimaryTierUpgrade(serverLevel, event.getCrafting(), event.getInventory());
		}
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
		if (LinkedStorageStackData.getEndpoint(storageItem) != null) {
			return;
		}
		if (storageItem.getItem() instanceof ShulkerBoxItem) {
			UUID uuid = NBTHelper.getUniqueId(storageItem, "uuid").orElse(null);
			if (uuid != null) {
				ItemContentsStorage itemContentsStorage = ItemContentsStorage.get();
				CompoundTag contentsNbt = itemContentsStorage.getOrCreateStorageContents(uuid).getCompound(StorageBlockEntity.STORAGE_WRAPPER_TAG);
				CompoundTag migratedContentsNbt = new CompoundTag();
				migratedContentsNbt.put(StorageWrapper.CONTENTS_TAG, contentsNbt.getCompound(StorageWrapper.CONTENTS_TAG));
				migratedContentsNbt.put(StorageWrapper.SETTINGS_TAG, contentsNbt.getCompound(StorageWrapper.SETTINGS_TAG));
				MovingStorageData.get(uuid).setContents(migratedContentsNbt);
				storageItem.getOrCreateTag().put(StorageWrapper.RENDER_INFO_TAG, contentsNbt.getCompound(StorageWrapper.RENDER_INFO_TAG));
				MovingStorageItem.setStorageItem(storageItem, result);
				itemContentsStorage.removeStorageContents(uuid);
			}
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

	public static void onStorageToolInteract(PlayerInteractEvent.EntityInteract event) {
		Player player = event.getEntity();
		ItemStack itemInHand = player.getItemInHand(event.getHand());
		if (!(event.getTarget() instanceof IMovingStorageEntity movingStorageEntity) || itemInHand.getItem() != ModItems.STORAGE_TOOL.get()
				|| movingStorageEntity.getStorageHolder().isPacked()) {
			return;
		}

		InteractionResult result = StorageHolderToolHandler.tryStorageToolInteract(itemInHand, movingStorageEntity.getStorageHolder());

		if (result.consumesAction()) {
			event.setCanceled(true);
			event.setCancellationResult(result);
		}
	}
}
