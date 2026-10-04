package net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ClientLinkedStorageContents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageGroupsSavedData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageSettingsPayload;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageSettingsPayload;
import org.jspecify.annotations.Nullable;

public class MovingStorageSettingsContainerMenu extends SettingsContainerMenu<IStorageWrapper> {
	private final int entityId;
	@Nullable
	private final LinkedStorageEndpointData openedEndpoint;
	private final IStorageWrapper openedWrapper;

	private ContainerContents.@Nullable SettingsData lastSettingsData = null;

	protected MovingStorageSettingsContainerMenu(int windowId, Player player, int entityId) {
		this(ModEntities.MOVING_STORAGE_SETTINGS_CONTAINER_TYPE.get(), windowId, player, entityId);
	}

	protected MovingStorageSettingsContainerMenu(MenuType<?> menuType, int windowId, Player player, int entityId) {
		super(menuType, windowId, player, getWrapper(player.level(), entityId));
		this.entityId = entityId;
		openedEndpoint = getEndpoint();
		openedWrapper = storageWrapper;
	}

	@Nullable
	private LinkedStorageEndpointData getEndpoint() {
		Entity entity = player.level().getEntity(entityId);
		return entity instanceof IMovingStorageEntity movingStorage ? movingStorage.getStorageItem().get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT) : null;
	}

	@Override
	public boolean stillValid(Player player) {
		Entity entity = player.level().getEntity(entityId);
		return entity instanceof IMovingStorageEntity movingStorage && entity.isAlive() && player.isWithinEntityInteractionRange(entity, 4.0F)
				&& !movingStorage.getStorageHolder().isPacked() && java.util.Objects.equals(openedEndpoint, getEndpoint())
				&& openedWrapper == movingStorage.getStorageHolder().getStorageWrapper()
				&& (openedEndpoint == null || openedWrapper != NoopStorageWrapper.INSTANCE
						&& (player.level().isClientSide() || player.level() instanceof ServerLevel serverLevel && LinkedStorageGroupsSavedData.get(serverLevel)
								.manager().isEndpointMember(openedEndpoint.groupId(), openedEndpoint.endpointId())));
	}

	private static IStorageWrapper getWrapper(Level level, int entityId) {
		if (!(level.getEntity(entityId) instanceof IMovingStorageEntity movingStorageEntity)) {
			return NoopStorageWrapper.INSTANCE;
		}

		return movingStorageEntity.getStorageHolder().getStorageWrapper();
	}

	@Override
	public void detectSettingsChangeAndReload() {
		if (player.level().isClientSide()) {
			if (openedEndpoint != null) {
				boolean snapshotChanged = ClientLinkedStorageContents.removeUpdatedGroup(openedEndpoint.groupId());
				boolean settingsChanged = ClientLinkedStorageContents.removeUpdatedSettings(openedEndpoint.groupId());
				if (snapshotChanged || settingsChanged) {
					ClientLinkedStorageContents.getContents(openedEndpoint.groupId()).ifPresent(contents -> {
						Entity entity = player.level().getEntity(entityId);
						if (snapshotChanged && entity instanceof IMovingStorageEntity movingStorage)
							movingStorage.getStorageHolder().refreshClientLinkedStorage();
						storageWrapper.getSettingsHandler().reloadFrom(contents.getContents(openedEndpoint.groupId()).settings());
					});
				}
				return;
			}
			storageWrapper.getContentsUuid().ifPresent(uuid -> {
				MovingStorageData storage = MovingStorageData.get();
				if (storage.removeUpdatedStorageSettingsFlag(uuid)) {
					storageWrapper.getSettingsHandler().reloadFrom(storage.getContents(uuid).settings());
				}
			});
		}
	}

	public static MovingStorageSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MovingStorageSettingsContainerMenu(windowId, playerInventory.player,
				MovingStorageContainerMenu.readMenuData(buffer, playerInventory.player));
	}

	public int getEntityId() {
		return entityId;
	}

	@Override
	public ItemStack getStorageSettingsTabIcon() {
		if (openedEndpoint != null && player.level().getEntity(entityId) instanceof IMovingStorageEntity movingStorage) {
			return movingStorage.getStorageItem();
		}
		return super.getStorageSettingsTabIcon();
	}

	private void sendStorageSettingsToClient() {
		if (player.level().isClientSide()) {
			return;
		}

		if (lastSettingsData == null || !lastSettingsData.equals(storageWrapper.getSettingsHandler().getSettingsData())) {
			lastSettingsData = storageWrapper.getSettingsHandler().getSettingsData().copy();

			if (openedEndpoint != null) {
				if (player instanceof ServerPlayer serverPlayer && stillValid(player)) {
					PacketDistributor.sendToPlayer(serverPlayer, new LinkedStorageSettingsPayload(openedEndpoint.groupId(), lastSettingsData));
				}
				return;
			}
			storageWrapper.getContentsUuid().ifPresent(uuid -> {
				ContainerContents.SettingsData settingsData = storageWrapper.getSettingsHandler().getSettingsData();
				if (player instanceof ServerPlayer serverPlayer) {
					PacketDistributor.sendToPlayer(serverPlayer, new MovingStorageSettingsPayload(uuid, settingsData));
				}
			});
		}
	}

	@Override
	public void broadcastChanges() {
		super.broadcastChanges();

		sendStorageSettingsToClient();
	}
}
