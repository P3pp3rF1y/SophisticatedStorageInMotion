package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.common.IHighlightRequestPayloadHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.ISlotTracker;
import net.p3pp3rf1y.sophisticatedcore.inventory.ItemStackKey;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageSyncItemHighlightsPayload;

import java.util.List;

public class MovingStorageHighlightRequestPayloadHandler implements IHighlightRequestPayloadHandler<List<Integer>> {
	private MovingStorageHighlightRequestPayloadHandler() {}

	public static MovingStorageHighlightRequestPayloadHandler INSTANCE = new MovingStorageHighlightRequestPayloadHandler();

	public static final ResourceLocation ID = SophisticatedStorageInMotion.getRL("moving_storage_highlight_request");
	@Override
	public ResourceLocation id() {
		return ID;
	}

	@Override
	public StreamCodec<ByteBuf, List<Integer>> requestCodec() {
		return ByteBufCodecs.INT.apply(ByteBufCodecs.list());
	}

	@Override
	public HighlightResult compute(ServerPlayer player, ItemStackKey stackKey, List<Integer> clientData) {
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

		if (!entitiesWithStack.isEmpty() || !entitiesWithItem.isEmpty()) {
			PacketDistributor.sendToPlayer(player, new MovingStorageSyncItemHighlightsPayload(entitiesWithStack, entitiesWithItem));
		}

		return new IHighlightRequestPayloadHandler.HighlightResult(entitiesWithStack.size(), entitiesWithItem.size());
	}
}
