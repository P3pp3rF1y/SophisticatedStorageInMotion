package net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ClientLinkedStorageContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageGroupManager;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageGroupsSavedData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageSettingsPayload;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageLinkedStorageHostWrapper;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageContentsPayload;

import javax.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class MovingStorageContainerMenu<T extends Entity & IMovingStorageEntity> extends StorageContainerMenuBase<IStorageWrapper> implements ISyncedContainer {
	protected final WeakReference<T> storageEntity;
	private final ItemStackIdentity openedIdentity;
	private final IStorageWrapper openedWrapper;

	@Nullable
	private CompoundTag lastSettingsNbt = null;

	public MovingStorageContainerMenu(int containerId, Player player, int entityId) {
		this(ModEntities.MOVING_STORAGE_CONTAINER_TYPE.get(), containerId, player, entityId);
	}

	public MovingStorageContainerMenu(MenuType<?> menuType, int containerId, Player player, int entityId) {
		super(menuType, containerId, player, getWrapper(player.level(), entityId), NoopStorageWrapper.INSTANCE, -1, false,
				instantiateExtraSlots(player.level(), entityId));
		if (!(player.level().getEntity(entityId) instanceof IMovingStorageEntity movingStorageEntity)) {
			throw new IllegalArgumentException("Incorrect entity with id " + entityId + " expected to find IMovingStorageEntity");
		}
		storageEntity = new WeakReference<>((T) movingStorageEntity);
		openedIdentity = ItemStackIdentity.of(movingStorageEntity);
		openedWrapper = storageWrapper;
		movingStorageEntity.getStorageHolder().startOpen(player, storageEntity.get());
	}

	private static List<Slot> instantiateExtraSlots(Level level, int entityId) {
		if (!(level.getEntity(entityId) instanceof IMovingStorageEntity movingStorage)) {
			return Collections.emptyList();
		}

		return movingStorage.instantiateExtraSlots();
	}

	public Optional<T> getStorageEntity() {
		return Optional.ofNullable(storageEntity.get());
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		getStorageEntity().ifPresent(storageEntity -> storageEntity.getStorageHolder().stopOpen(player, storageEntity));
	}

	private static IStorageWrapper getWrapper(Level level, int entityId) {
		if (!(level.getEntity(entityId) instanceof IMovingStorageEntity movingStorage)) {
			return NoopStorageWrapper.INSTANCE;
		}

		return movingStorage.getStorageHolder().getStorageWrapper();
	}

	public static MovingStorageContainerMenu<?> fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MovingStorageContainerMenu<>(windowId, playerInventory.player, readMenuData(buffer, playerInventory.player));
	}

	public static void writeMenuData(FriendlyByteBuf buffer, Player player, int entityId) {
		buffer.writeInt(entityId);
		if (!(player.level() instanceof ServerLevel serverLevel) || !(player.level().getEntity(entityId) instanceof IMovingStorageEntity movingStorage)) {
			buffer.writeBoolean(false);
			return;
		}
		LinkedStorageEndpointData endpoint = movingStorage.getStorageItem().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT);
		if (endpoint == null || !LinkedStorageGroupsSavedData.get(serverLevel).manager().isEndpointMember(endpoint.groupId(), endpoint.endpointId())) {
			buffer.writeBoolean(false);
			return;
		}
		LinkedStorageGroupManager manager = LinkedStorageGroupsSavedData.get(serverLevel).manager();
		Optional<StorageLinkedStorageHostWrapper> host = manager.resolveVirtualHost(endpoint.groupId())
				.filter(StorageLinkedStorageHostWrapper.class::isInstance).map(StorageLinkedStorageHostWrapper.class::cast);
		if (host.isEmpty()) {
			buffer.writeBoolean(false);
			return;
		}
		buffer.writeBoolean(true);
		buffer.writeUUID(endpoint.groupId());
		buffer.writeVarLong(manager.getRevision(endpoint.groupId()));
		ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.encode(buffer, host.get().getDisplayName());
		FriendlyByteBuf.writeNbt(buffer, manager.resolveContents(endpoint.groupId()).orElseThrow().getContents());
		buffer.writeVarInt(host.get().getInventoryHandler().getSlots());
		buffer.writeVarInt(host.get().getUpgradeHandler().getSlots());
		buffer.writeVarInt(host.get().getColumnsTaken());
		FriendlyByteBuf.writeNbt(buffer, host.get().getVirtualCarrierSnapshot().orElseThrow());
	}

	public static int readMenuData(FriendlyByteBuf buffer, Player player) {
		int entityId = buffer.readInt();
		if (!buffer.readBoolean()) {
			return entityId;
		}
		UUID groupId = buffer.readUUID();
		long revision = buffer.readVarLong();
		Component groupName = ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC.decode(buffer);
		CompoundTag contents = Objects.requireNonNull(buffer.readNbt());
		int inventorySlots = buffer.readVarInt();
		int upgradeSlots = buffer.readVarInt();
		int columnsTaken = buffer.readVarInt();
		CompoundTag virtualCarrier = Objects.requireNonNull(buffer.readNbt());
		ClientLinkedStorageContents.updateContents(groupId, revision, contents, groupName, inventorySlots, upgradeSlots, columnsTaken);
		StorageLinkedStorageHostWrapper.applyClientSnapshotProfile(virtualCarrier, groupName, inventorySlots, upgradeSlots);
		if (player.level().getEntity(entityId) instanceof IMovingStorageEntity movingStorage
				&& movingStorage.getStorageItem().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT) instanceof LinkedStorageEndpointData endpoint
				&& groupId.equals(endpoint.groupId())) {
			ClientLinkedStorageContents.getContents(groupId).ifPresent(linkedContents -> movingStorage.getStorageHolder()
					.bindClientLinkedStorage(StorageLinkedStorageHostWrapper.create(linkedContents, virtualCarrier)));
			// This snapshot was applied before the menu bound its slots; it is not a subsequent contents change.
			ClientLinkedStorageContents.removeUpdatedGroup(groupId);
		}
		return entityId;
	}

	private record ItemStackIdentity(LinkedStorageEndpointData endpoint, Object item) {
		private static ItemStackIdentity of(IMovingStorageEntity entity) {
			return new ItemStackIdentity(entity.getStorageItem().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT), entity.getStorageItem().getItem());
		}
	}

	@Override
	public Optional<BlockPos> getBlockPosition() {
		return Optional.empty();
	}

	@Override
	public Optional<Entity> getEntity() {
		return getStorageEntity().map(e -> e);
	}

	@Override
	protected StorageContainerMenuBase<IStorageWrapper>.StorageUpgradeSlot instantiateUpgradeSlot(UpgradeHandler upgradeHandler, int slotIndex) {
		return new StorageUpgradeSlot(upgradeHandler, slotIndex) {
			@Override
			protected void onUpgradeChanged() {
				if (player.level().isClientSide()) {
					return;
				}
				storageWrapper.getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class).itemsChanged();
			}
		};
	}

	@Override
	public void openSettings() {
		if (isClientSide()) {
			sendToServer(data -> data.putString(ACTION_TAG, "openSettings"));
			return;
		}
		getStorageEntity().ifPresent(entity -> player.openMenu(
				new SophisticatedMenuProvider((w, p, pl) -> instantiateSettingsContainerMenu(w, pl, entity.getId()),
						Component.translatable(StorageTranslationHelper.INSTANCE.translGui("settings.title")), false),
				buffer -> writeMenuData(buffer, player, entity.getId())));
	}

	protected MovingStorageSettingsContainerMenu instantiateSettingsContainerMenu(int windowId, Player player, int entityId) {
		return new MovingStorageSettingsContainerMenu(windowId, player, entityId);
	}

	@Override
	protected boolean storageItemHasChanged() {
		return false; // the stack is only used for internal tracking in moving entities so it can't be swapped away by a player
	}

	@Override
	public boolean detectSettingsChangeAndReload() {
		if (openedIdentity.endpoint() != null && player.level().isClientSide) {
			boolean snapshotChanged = ClientLinkedStorageContents.removeUpdatedGroup(openedIdentity.endpoint().groupId());
			boolean settingsChanged = ClientLinkedStorageContents.removeUpdatedSettings(openedIdentity.endpoint().groupId());
			return (snapshotChanged || settingsChanged) && ClientLinkedStorageContents.getContents(openedIdentity.endpoint().groupId()).map(contents -> {
				if (snapshotChanged) {
					getStorageEntity().ifPresent(entity -> entity.getStorageHolder().refreshClientLinkedStorage());
				}
				storageWrapper.getSettingsHandler().reloadFrom(contents.getContents().getCompound("settings"));
				if (snapshotChanged) {
					refreshUpgradeControls();
				}
				return true;
			}).orElse(false);
		}
		if (player.level().isClientSide) {
			return storageWrapper.getContentsUuid().map(uuid -> {
				MovingStorageData storage = MovingStorageData.get(uuid);
				if (storage.removeUpdatedStorageSettingsFlag(uuid)) {
					storageWrapper.getSettingsHandler().reloadFrom(storage.getContents().getCompound(MovingStorageWrapper.SETTINGS_TAG));
					return true;
				}
				return false;
			}).orElse(false);
		}
		return false;
	}

	@Override
	public boolean stillValid(Player player) {
		return getStorageEntity().map(se -> se.isAlive() && player.canInteractWithEntity(se, 4.0F) && !se.getStorageHolder().isPacked()
				&& openedIdentity.equals(ItemStackIdentity.of(se)) && openedWrapper == se.getStorageHolder().getStorageWrapper()
				&& (openedIdentity.endpoint() == null || openedWrapper != NoopStorageWrapper.INSTANCE
						&& (player.level().isClientSide || player.level() instanceof ServerLevel serverLevel && LinkedStorageGroupsSavedData.get(serverLevel)
								.manager().isEndpointMember(openedIdentity.endpoint().groupId(), openedIdentity.endpoint().endpointId()))))
				.orElse(false);
	}

	@Override
	protected void sendStorageSettingsToClient() {
		if (player.level().isClientSide) {
			return;
		}
		if (lastSettingsNbt == null || !lastSettingsNbt.equals(storageWrapper.getSettingsHandler().getNbt())) {
			lastSettingsNbt = storageWrapper.getSettingsHandler().getNbt().copy();
			if (openedIdentity.endpoint() != null) {
				if (player instanceof ServerPlayer serverPlayer && stillValid(player)) {
					PacketDistributor.sendToPlayer(serverPlayer, new LinkedStorageSettingsPayload(openedIdentity.endpoint().groupId(), lastSettingsNbt));
				}
				return;
			}

			storageWrapper.getContentsUuid().ifPresent(uuid -> {
				CompoundTag settingsContents = new CompoundTag();
				CompoundTag settingsNbt = storageWrapper.getSettingsHandler().getNbt();
				if (!settingsNbt.isEmpty()) {
					settingsContents.put(MovingStorageWrapper.SETTINGS_TAG, settingsNbt);
					if (player instanceof ServerPlayer serverPlayer) {
						PacketDistributor.sendToPlayer(serverPlayer, new MovingStorageContentsPayload(uuid, settingsContents));
					}
				}
			});
		}
	}

	public float getSlotFillPercentage(int slot) {
		IMovingStorageEntity entity = storageEntity.get();
		if (entity == null) {
			return 0;
		}
		List<Float> slotFillRatios = entity.getStorageHolder().getStorageWrapper().getRenderInfo().getItemDisplayRenderInfo().getSlotFillRatios();
		return slot > -1 && slot < slotFillRatios.size() ? slotFillRatios.get(slot) : 0;
	}
}
