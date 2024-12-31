package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

public class CommonEventHandler {
	private CommonEventHandler() {
	}

	public static void registerHandlers() {
		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		eventBus.addListener(CommonEventHandler::onMovingStorageUncrafted);
	}

	private static void onMovingStorageUncrafted(PlayerEvent.ItemCraftedEvent event) {
		ItemStack result = event.getCrafting();

		if (event.getEntity().level().isClientSide() || !(result.getItem() instanceof StorageBlockItem) || !isUncraftedFromSingleMovingStorage(event.getInventory())) {
			return;
		}

		NBTHelper.getUniqueId(result, StorageWrapper.UUID_TAG).ifPresent(storageId -> {
			MovingStorageData storageData = MovingStorageData.get(storageId);
			CompoundTag contents = storageData.getContents();
			contents.put(StorageWrapper.RENDER_INFO_TAG, NBTHelper.getCompound(result, StorageWrapper.RENDER_INFO_TAG).orElse(new CompoundTag()));
			CompoundTag fullContents = new CompoundTag();
			fullContents.put(StorageBlockEntity.STORAGE_WRAPPER_TAG, contents);

			ItemContentsStorage.get().setStorageContents(storageId, fullContents);

			storageData.removeStorageContents();
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
}
