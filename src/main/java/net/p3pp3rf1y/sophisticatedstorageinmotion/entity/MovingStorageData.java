package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageSavedData;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

import java.util.*;

public class MovingStorageData extends SavedData implements IStorageSavedData {
	private static final String SAVED_DATA_NAME = SophisticatedStorageInMotion.MOD_ID;
	private static final String STORAGE_CONTENTS_TAG = "storageContents";
	private static final MovingStorageData clientStorageCopy = new MovingStorageData();

	private final Map<UUID, CompoundTag> movingStorageContents = new HashMap<>();
	private final Set<UUID> updatedStorageSettingsFlags = new HashSet<>();

	private MovingStorageData() {
	}

	public static MovingStorageData get() {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null) {
				ServerLevel overworld = server.getLevel(Level.OVERWORLD);
				//noinspection ConstantConditions - by this time overworld is loaded
				DimensionDataStorage storage = overworld.getDataStorage();
				return storage.computeIfAbsent(new Factory<>(MovingStorageData::new, MovingStorageData::load), SAVED_DATA_NAME);
			}
		}
		return clientStorageCopy;
	}

	public static MovingStorageData load(CompoundTag nbt, HolderLookup.Provider registries) {
		MovingStorageData storageData = new MovingStorageData();
		storageData.readStorageContents(nbt);
		return storageData;
	}

	private void readStorageContents(CompoundTag nbt) {
		movingStorageContents.clear();
		ListTag list = nbt.getList(STORAGE_CONTENTS_TAG, Tag.TAG_COMPOUND);
		for (Tag storageNbt : list) {
			CompoundTag uuidContentsPair = (CompoundTag) storageNbt;
			UUID uuid = NbtUtils.loadUUID(Objects.requireNonNull(uuidContentsPair.get("uuid")));
			CompoundTag contents = uuidContentsPair.getCompound("contents");
			movingStorageContents.put(uuid, contents);
		}
	}

	@Override
	public CompoundTag save(CompoundTag compound, HolderLookup.Provider registries) {
		CompoundTag ret = new CompoundTag();
		writeStorageContents(ret);
		return ret;
	}

	private void writeStorageContents(CompoundTag ret) {
		ListTag list = new ListTag();
		for (Map.Entry<UUID, CompoundTag> entry : movingStorageContents.entrySet()) {
			CompoundTag uuidContentsPair = new CompoundTag();
			uuidContentsPair.putUUID("uuid", entry.getKey());
			uuidContentsPair.put("contents", entry.getValue());
			list.add(uuidContentsPair);
		}
		ret.put(STORAGE_CONTENTS_TAG, list);
		setDirty();
	}

	public CompoundTag getContents(UUID storageId) {
		return movingStorageContents.computeIfAbsent(storageId, k -> new CompoundTag());
	}

	public static void moveToItemStorage(ItemStack storageItem, UUID storageId) {
		MovingStorageData storageData = get();
		CompoundTag contents = storageData.getContents(storageId);
		contents.put(StorageWrapper.RENDER_INFO_TAG, storageItem.getOrDefault(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.EMPTY).copyTag());
		CompoundTag fullContents = new CompoundTag();
		fullContents.put(StorageBlockEntity.STORAGE_WRAPPER_TAG, contents);

		ItemContentsStorage.get().setStorageContents(storageId, fullContents);

		storageData.removeStorageContents(storageId);
	}

	public void removeStorageContents(UUID storageId) {
		movingStorageContents.remove(storageId);
		setDirty();
	}

	public void setContentsClient(UUID storageId, CompoundTag contents) {
		for (String key : contents.getAllKeys()) {
			//noinspection ConstantConditions - the key is one of the tag keys so there's no reason it wouldn't exist here
			getContents(storageId).put(key, contents.get(key));

			if (key.equals(MovingStorageWrapper.SETTINGS_TAG)) {
				updatedStorageSettingsFlags.add(storageId);
			}
		}
		setDirty();
	}

	public void setContents(UUID storageId, CompoundTag contents) {
		movingStorageContents.put(storageId, contents);
		setDirty();
	}

	@Override
	public void markChanged() {
		setDirty();
	}

	public boolean removeUpdatedStorageSettingsFlag(UUID storageId) {
		return updatedStorageSettingsFlags.remove(storageId);
	}
}
