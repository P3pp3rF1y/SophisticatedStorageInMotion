package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
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
	private static final SavedDataType<MovingStorageData> TYPE = new SavedDataType<>(SophisticatedStorageInMotion.MOD_ID, MovingStorageData::new,
			RecordCodecBuilder.create(builder -> builder.group(Codec.unboundedMap(Codec.STRING.xmap(UUID::fromString, UUID::toString), CompoundTag.CODEC)
					.fieldOf("storageContents").forGetter(data -> data.movingStorageContents)).apply(builder, MovingStorageData::new)));

	private static final MovingStorageData clientStorageCopy = new MovingStorageData();

	private final Map<UUID, CompoundTag> movingStorageContents = new HashMap<>();
	private final Set<UUID> updatedStorageSettingsFlags = new HashSet<>();

	private MovingStorageData(Map<UUID, CompoundTag> movingStorageContents) {
		this.movingStorageContents.putAll(movingStorageContents);
	}

	private MovingStorageData() {
	}

	public static MovingStorageData get() {
		if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null) {
				ServerLevel overworld = server.getLevel(Level.OVERWORLD);
				// noinspection ConstantConditions - by this time overworld is loaded
				DimensionDataStorage storage = overworld.getDataStorage();
				return storage.computeIfAbsent(TYPE);
			}
		}
		return clientStorageCopy;
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
		for (String key : contents.keySet()) {
			// noinspection ConstantConditions - the key is one of the tag keys so there's no reason it wouldn't exist here
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
