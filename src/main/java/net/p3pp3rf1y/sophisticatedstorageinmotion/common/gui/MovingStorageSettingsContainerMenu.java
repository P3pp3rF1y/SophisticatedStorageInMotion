package net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageSettingsPayload;
import org.jspecify.annotations.Nullable;

public class MovingStorageSettingsContainerMenu extends SettingsContainerMenu<IStorageWrapper> {
	private final int entityId;

	private ContainerContents.@Nullable SettingsData lastSettingsData = null;

	protected MovingStorageSettingsContainerMenu(int windowId, Player player, int entityId) {
		this(ModEntities.MOVING_STORAGE_SETTINGS_CONTAINER_TYPE.get(), windowId, player, entityId);
	}

	protected MovingStorageSettingsContainerMenu(MenuType<?> menuType, int windowId, Player player, int entityId) {
		super(menuType, windowId, player, getWrapper(player.level(), entityId));
		this.entityId = entityId;
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
			storageWrapper.getContentsUuid().ifPresent(uuid -> {
				MovingStorageData storage = MovingStorageData.get();
				if (storage.removeUpdatedStorageSettingsFlag(uuid)) {
					storageWrapper.getSettingsHandler().reloadFrom(storage.getContents(uuid).settings());
				}
			});
		}
	}

	public static MovingStorageSettingsContainerMenu fromBuffer(int windowId, Inventory playerInventory, FriendlyByteBuf buffer) {
		return new MovingStorageSettingsContainerMenu(windowId, playerInventory.player, buffer.readInt());
	}

	public int getEntityId() {
		return entityId;
	}

	private void sendStorageSettingsToClient() {
		if (player.level().isClientSide()) {
			return;
		}

		if (lastSettingsData == null || !lastSettingsData.equals(storageWrapper.getSettingsHandler().getSettingsData())) {
			lastSettingsData = storageWrapper.getSettingsHandler().getSettingsData().copy();

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
