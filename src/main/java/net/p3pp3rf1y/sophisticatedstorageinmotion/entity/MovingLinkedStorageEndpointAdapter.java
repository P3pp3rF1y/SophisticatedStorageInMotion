package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ILinkedStorageBlockEndpoint;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ILinkedStorageEndpointAdapter;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageGroupManager;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageGroupsSavedData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageHostDescriptor;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockBase;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageLinkedStorageHostWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.Optional;
import java.util.UUID;

public final class MovingLinkedStorageEndpointAdapter implements ILinkedStorageEndpointAdapter<ILinkedStorageBlockEndpoint> {
	public static final MovingLinkedStorageEndpointAdapter INSTANCE = new MovingLinkedStorageEndpointAdapter();

	private MovingLinkedStorageEndpointAdapter() {
	}

	@Override
	public ResourceLocation factoryId() {
		return StorageLinkedStorageHostWrapper.FACTORY_ID;
	}

	@Override
	public Compatibility getCompatibility(ServerLevel level, ILinkedStorageBlockEndpoint endpoint, LinkedStorageHostDescriptor hostDescriptor) {
		EntityStorageHolder<?> holder = requireHolder(endpoint);
		if (!holder.isLinkedStorageLinkCandidate() || !StorageLinkedStorageHostWrapper.getCompatibilityKey(holder.getInstalledStorageItem())
				.equals(StorageLinkedStorageHostWrapper.getCompatibilityKey(hostDescriptor.virtualCarrier()))) {
			return Compatibility.INCOMPATIBLE;
		}
		IStorageWrapper wrapper = holder.getStorageWrapper();
		return InventoryHelper.isEmpty(wrapper.getInventoryHandler()) && InventoryHelper.isEmpty(wrapper.getUpgradeHandler())
				? Compatibility.COMPATIBLE
				: Compatibility.HAS_CONTENTS;
	}

	@Override
	public LinkedStorageHostDescriptor createHostDescriptor(ServerLevel level, ILinkedStorageBlockEndpoint endpoint) {
		EntityStorageHolder<?> holder = requireHolder(endpoint);
		return new LinkedStorageHostDescriptor(factoryId(),
				StorageLinkedStorageHostWrapper.createVirtualCarrier(holder.getInstalledStorageItem(), holder.getStorageWrapper()));
	}

	@Override
	public CompoundTag copyCanonicalContents(ServerLevel level, ILinkedStorageBlockEndpoint endpoint) {
		EntityStorageHolder<?> holder = requireHolder(endpoint);
		IStorageWrapper wrapper = holder.getStorageWrapper();
		UUID uuid = holder.getInstalledStorageItem().get(ModCoreDataComponents.STORAGE_UUID);
		CompoundTag contents = uuid == null ? new CompoundTag() : MovingStorageData.get(uuid).getContents().copy();
		contents.putInt(StorageWrapper.NUMBER_OF_INVENTORY_SLOTS_TAG, wrapper.getInventoryHandler().getSlots());
		contents.putInt(StorageWrapper.NUMBER_OF_UPGRADE_SLOTS_TAG, wrapper.getUpgradeHandler().getSlots());
		contents.putString(StorageWrapper.SORT_BY_TAG, wrapper.getSortBy().getSerializedName());
		return contents;
	}

	@Override
	public void bindEndpoint(ServerLevel level, ILinkedStorageBlockEndpoint endpoint, LinkedStorageEndpointData endpointData) {
		EntityStorageHolder<?> holder = requireHolder(endpoint);
		ItemStack item = holder.getInstalledStorageItem().copy();
		UUID previousUuid = item.get(ModCoreDataComponents.STORAGE_UUID);
		item.remove(ModCoreDataComponents.STORAGE_UUID);
		item.set(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT, endpointData);
		LinkedStorageGroupManager manager = LinkedStorageGroupsSavedData.get(level).manager();
		if (manager.isPrimaryEndpoint(endpointData.groupId(), endpointData.endpointId())) {
			item.set(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT, true);
		} else {
			item.remove(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT);
		}
		holder.setStorageItem(item);
		if (previousUuid != null) {
			MovingStorageData.get(previousUuid).removeStorageContents();
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

		Integer storedInventorySlots = storage.get(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS);
		Integer storedUpgradeSlots = storage.get(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS);
		int inventorySlots = storedInventorySlots == null ? block.getNumberOfInventorySlots() : storedInventorySlots;
		int upgradeSlots = storedUpgradeSlots == null ? block.getNumberOfUpgradeSlots() : storedUpgradeSlots;
		StorageLinkedStorageHostWrapper canonicalHost = host.get();
		canonicalHost.changeSize(inventorySlots - canonicalHost.getInventoryHandler().getSlots(), upgradeSlots - canonicalHost.getUpgradeHandler().getSlots());
		canonicalHost.getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class).itemsChanged();
		CompoundTag carrier = StorageLinkedStorageHostWrapper.withDisplayName(descriptor.get().virtualCarrier(), storage.getHoverName());
		carrier.putInt("inventorySlots", inventorySlots);
		carrier.putInt("upgradeSlots", upgradeSlots);
		carrier.putInt("baseStackSizeMultiplier", block.getBaseStackSizeMultiplier());
		CompoundTag renderInfo = canonicalHost.getRenderInfo().getNbt().copy();
		carrier.put(StorageWrapper.RENDER_INFO_TAG, renderInfo.copy());
		canonicalHost.persistCanonicalContents();
		if (!manager.updatePrimaryHostDescriptor(endpoint.groupId(), endpoint.endpointId(),
				new LinkedStorageHostDescriptor(StorageLinkedStorageHostWrapper.FACTORY_ID, carrier))) {
			return false;
		}
		storage.set(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT, true);
		storage.set(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.of(renderInfo));
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
