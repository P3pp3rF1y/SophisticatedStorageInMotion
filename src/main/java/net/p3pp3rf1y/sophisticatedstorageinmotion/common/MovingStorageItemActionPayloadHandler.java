package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.common.IItemActionPayloadHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.ISlotTracker;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageSyncItemHighlightsMessage;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.StorageInMotionPacketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MovingStorageItemActionPayloadHandler implements IItemActionPayloadHandler<List<Integer>> {
	private MovingStorageItemActionPayloadHandler() {}

	public static MovingStorageItemActionPayloadHandler INSTANCE = new MovingStorageItemActionPayloadHandler();
	public static final ResourceLocation ID = SophisticatedStorageInMotion.getRL("moving_storage_item_action");

	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public HighlightResult computeHighlight(ServerPlayer player, ItemStackKey stackKey, List<Integer> clientData) {
		List<Integer> entitiesWithStack = new java.util.ArrayList<>();
		List<Integer> entitiesWithItem = new java.util.ArrayList<>();
		clientData.forEach(entityId -> {
			Entity entity = player.level().getEntity(entityId);
			if (entity instanceof IMovingStorageEntity movingStorageEntity) {
				ISlotTracker slotTracker = movingStorageEntity.getStorageHolder().getStorageWrapper().getInventoryHandler().getSlotTracker();
				if (slotTracker.getPartialStacks().contains(stackKey) || slotTracker.getFullStacks().contains(stackKey)) {
					entitiesWithStack.add(entityId);
				} else if (slotTracker.getItems().contains(stackKey.getStack().getItem())) {
					entitiesWithItem.add(entityId);
				}
			}
		});

		StorageInMotionPacketHandler.INSTANCE.sendToClient(player, new MovingStorageSyncItemHighlightsMessage(entitiesWithStack, entitiesWithItem));

		return new IItemActionPayloadHandler.HighlightResult(entitiesWithStack.size(), entitiesWithItem.size());
	}

	@Override
	public Map<Vec3, InventoryHandler> getTargetInventories(Player player, List<Integer> clientData) {
		return clientData.stream()
				.map(entityId -> player.level().getEntity(entityId))
				.filter(IMovingStorageEntity.class::isInstance)
				.collect(Collectors.toMap(e -> e.getBoundingBox().getCenter(),e -> ((IMovingStorageEntity)e).getStorageHolder().getStorageWrapper().getInventoryHandler()));
	}

	@Override
	public List<Integer> decode(FriendlyByteBuf packetBuffer) {
		return packetBuffer.readCollection(ArrayList::new, FriendlyByteBuf::readInt);
	}

	@Override
	public void encode(FriendlyByteBuf packetBuffer, List<Integer> value) {
		packetBuffer.writeCollection(value, FriendlyByteBuf::writeInt);
	}
}
