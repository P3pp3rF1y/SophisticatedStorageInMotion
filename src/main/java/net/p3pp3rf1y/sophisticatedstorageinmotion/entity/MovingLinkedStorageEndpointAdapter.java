package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.*;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageLinkedStorageHostWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.Optional;
import java.util.UUID;

public final class MovingLinkedStorageEndpointAdapter implements ILinkedStorageEndpointAdapter<ILinkedStorageBlockEndpoint> {
	public static final MovingLinkedStorageEndpointAdapter INSTANCE = new MovingLinkedStorageEndpointAdapter();

	private MovingLinkedStorageEndpointAdapter() {
	}

	@Override
	public Identifier factoryId() {
		return StorageLinkedStorageHostWrapper.FACTORY_ID;
	}

	@Override
	public Compatibility getCompatibility(ServerLevel level, ILinkedStorageBlockEndpoint endpoint, LinkedStorageHostDescriptor hostDescriptor) {
		EntityStorageHolder<?> holder = requireHolder(endpoint);
		if (!holder.isLinkedStorageLinkCandidate() || !StorageLinkedStorageHostWrapper.getCompatibilityKey(holder.getInstalledStorageItem())
				.equals(StorageLinkedStorageHostWrapper.getCompatibilityKey(hostDescriptor.virtualCarrier()))) {
			return Compatibility.INCOMPATIBLE;
		}
		return ResourceHandlerUtil.isEmpty(holder.getStorageWrapper().getInventoryHandler())
				&& ResourceHandlerUtil.isEmpty(holder.getStorageWrapper().getUpgradeHandler()) ? Compatibility.COMPATIBLE : Compatibility.HAS_CONTENTS;
	}

	@Override
	public LinkedStorageHostDescriptor createHostDescriptor(ServerLevel level, ILinkedStorageBlockEndpoint endpoint) {
		EntityStorageHolder<?> holder = requireHolder(endpoint);
		return new LinkedStorageHostDescriptor(factoryId(),
				StorageLinkedStorageHostWrapper.createVirtualCarrier(holder.getInstalledStorageItem(), holder.getStorageWrapper()));
	}

	@Override
	public ContainerContents copyCanonicalContents(ServerLevel level, ILinkedStorageBlockEndpoint endpoint) {
		ItemStack item = requireHolder(endpoint).getInstalledStorageItem();
		UUID uuid = item.get(ModCoreDataComponents.STORAGE_UUID);
		return uuid == null ? new ContainerContents() : MovingStorageData.get().getContents(uuid).copy();
	}

	@Override
	public void bindEndpoint(ServerLevel level, ILinkedStorageBlockEndpoint endpoint, LinkedStorageEndpointData endpointData) {
		EntityStorageHolder<?> holder = requireHolder(endpoint);
		ItemStack item = holder.getInstalledStorageItem().copy();
		UUID previousUuid = item.get(ModCoreDataComponents.STORAGE_UUID);
		item.remove(ModCoreDataComponents.STORAGE_UUID);
		item.set(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT, endpointData);
		if (LinkedStorageGroupsSavedData.get(level).manager().isPrimaryEndpoint(endpointData.groupId(), endpointData.endpointId())) {
			item.set(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT, true);
		} else {
			item.remove(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT);
		}
		holder.setStorageItem(item);
		if (previousUuid != null) {
			MovingStorageData.get().removeStorageContents(previousUuid);
		}
	}

	public static boolean completePrimaryTierUpgrade(ServerLevel level, ItemStack result, Container inputs) {
		if (!(result.getItem() instanceof MovingStorageItem)) {
			return false;
		}
		ItemStack storage = MovingStorageItem.getStorageItem(result);
		LinkedStorageEndpointData endpoint = storage.get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT);
		if (endpoint == null || !(storage.getItem() instanceof BlockItem blockItem) || !(blockItem.getBlock() instanceof StorageBlockBase block)
				|| !hasOriginalEndpoint(inputs, endpoint, storage)) {
			return false;
		}
		LinkedStorageGroupManager manager = LinkedStorageGroupsSavedData.get(level).manager();
		if (!manager.isPrimaryEndpoint(endpoint.groupId(), endpoint.endpointId())) {
			return false;
		}
		Optional<LinkedStorageHostDescriptor> descriptor = manager.getHostDescriptor(endpoint.groupId())
				.filter(host -> host.factoryId().equals(StorageLinkedStorageHostWrapper.FACTORY_ID));
		Optional<StorageLinkedStorageHostWrapper> host = manager.resolveVirtualHost(endpoint.groupId())
				.filter(StorageLinkedStorageHostWrapper.class::isInstance).map(StorageLinkedStorageHostWrapper.class::cast);
		if (descriptor.isEmpty() || host.isEmpty()) {
			return false;
		}
		int inventorySlots = storage.getOrDefault(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, block.getNumberOfInventorySlots());
		int upgradeSlots = storage.getOrDefault(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, block.getNumberOfUpgradeSlots());
		StorageLinkedStorageHostWrapper canonicalHost = host.get();
		canonicalHost.changeSize(inventorySlots - canonicalHost.getInventoryHandler().size(), upgradeSlots - canonicalHost.getUpgradeHandler().size());
		canonicalHost.getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class).itemsChanged();
		canonicalHost.persistCanonicalContents();
		if (!manager.updatePrimaryHostDescriptor(endpoint.groupId(), endpoint.endpointId(), new LinkedStorageHostDescriptor(
				StorageLinkedStorageHostWrapper.FACTORY_ID, StorageLinkedStorageHostWrapper.createVirtualCarrier(storage, canonicalHost)))) {
			return false;
		}
		storage.set(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT, true);
		storage.set(ModCoreDataComponents.RENDER_DATA, canonicalHost.getRenderDataHandler().getData().copy());
		MovingStorageItem.setStorageItem(result, storage);
		return true;
	}

	private static boolean hasOriginalEndpoint(Container inputs, LinkedStorageEndpointData endpoint, ItemStack resultStorage) {
		for (int slot = 0; slot < inputs.getContainerSize(); slot++) {
			ItemStack input = inputs.getItem(slot);
			if (!(input.getItem() instanceof MovingStorageItem)) {
				continue;
			}
			ItemStack originalStorage = MovingStorageItem.getStorageItem(input);
			if (endpoint.equals(originalStorage.get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT)) && originalStorage.getItem() != resultStorage.getItem()) {
				return true;
			}
		}
		return false;
	}

	private static EntityStorageHolder<?> requireHolder(ILinkedStorageBlockEndpoint endpoint) {
		if (endpoint instanceof EntityStorageHolder<?> holder) {
			return holder;
		}
		throw new IllegalArgumentException("Unsupported moving storage endpoint");
	}
}
