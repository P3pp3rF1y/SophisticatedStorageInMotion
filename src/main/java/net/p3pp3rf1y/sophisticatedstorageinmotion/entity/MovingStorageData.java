package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageSavedData;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderData;
import net.p3pp3rf1y.sophisticatedcore.util.CodecHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ValueIOHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

import java.util.*;

//TODO after 1.22 remove support for legacy UUID deserialization via strings
public class MovingStorageData extends SavedData implements IStorageSavedData {
	private static final SavedDataType<MovingStorageData> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "moving_storage_data"), MovingStorageData::new,
			RecordCodecBuilder.create(builder -> builder.group(Codec.unboundedMap(CodecHelper.STRING_ENCODED_UUID, ContainerContents.CODEC)
					.fieldOf("storageContents").forGetter((MovingStorageData data) -> data.movingStorageContents)).apply(builder, MovingStorageData::new)));

	private static final MovingStorageData clientStorageCopy = new MovingStorageData();

	private final Map<UUID, ContainerContents> movingStorageContents = new HashMap<>();
	private final Set<UUID> updatedStorageSettingsFlags = new HashSet<>();

	private MovingStorageData(Map<UUID, ContainerContents> movingStorageContents) {
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
				SavedDataStorage storage = overworld.getDataStorage();
				return storage.computeIfAbsent(TYPE);
			}
		}
		return clientStorageCopy;
	}

	public ContainerContents getContents(UUID storageId) {
		return movingStorageContents.computeIfAbsent(storageId, k -> new ContainerContents());
	}

	public static void moveToItemStorage(HolderLookup.Provider registries, ItemStack storageItem, UUID storageId) {
		MovingStorageData storageData = get();
		ContainerContents contents = storageData.getContents(storageId);
		RenderData renderData = storageItem.getOrDefault(ModCoreDataComponents.RENDER_DATA, RenderData.EMPTY).copy();
		CompoundTag additionalBeData = ValueIOHelper.collectOutputToTag(registries, out -> {
			out.child(StorageBlockEntity.STORAGE_WRAPPER).store(StorageWrapper.RENDER_DATA, RenderData.CODEC, renderData);
		});
		ItemContentsStorage itemContentsStorage = ItemContentsStorage.get();
		itemContentsStorage.setContents(storageId, contents);
		itemContentsStorage.setAdditionalBeData(storageId, additionalBeData);
		storageData.removeStorageContents(storageId);
	}

	public void removeStorageContents(UUID storageId) {
		movingStorageContents.remove(storageId);
		setDirty();
	}

	public void setContentsClient(UUID storageId, ContainerContents contents) {
		if (!movingStorageContents.containsKey(storageId)) {
			movingStorageContents.put(storageId, contents);
			updatedStorageSettingsFlags.add(storageId);
		} else {
			ContainerContents currentContents = movingStorageContents.get(storageId);
			ContainerContents.SettingsData previousSettings = currentContents.settings().copy();
			currentContents.reloadFrom(contents);
			if (!currentContents.settings().equals(previousSettings)) {
				updatedStorageSettingsFlags.add(storageId);
			}
		}
	}

	public void setContents(UUID storageId, ContainerContents contents) {
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
