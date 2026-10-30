package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.network.ItemStackReference;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.UUID;

public record RequestMovingStorageInventoryContentsPayload(UUID storageUuid, ItemStackReference itemReference) implements CustomPacketPayload {
	public static final Type<RequestMovingStorageInventoryContentsPayload> TYPE = new Type<>(
			SophisticatedStorageInMotion.getIdentifier("request_moving_storage_inventory_contents"));
	public static final StreamCodec<ByteBuf, RequestMovingStorageInventoryContentsPayload> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC,
			RequestMovingStorageInventoryContentsPayload::storageUuid, ItemStackReference.STREAM_CODEC,
			RequestMovingStorageInventoryContentsPayload::itemReference, RequestMovingStorageInventoryContentsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(RequestMovingStorageInventoryContentsPayload payload, IPayloadContext context) {
		if (context.player() instanceof ServerPlayer serverPlayer) {
			ItemStack stack = payload.itemReference.resolve(serverPlayer);
			if (!(stack.getItem() instanceof MovingStorageItem)) {
				return;
			}
			ItemStack inner = MovingStorageItem.getStorageItem(stack);
			if (!payload.storageUuid.equals(inner.get(ModCoreDataComponents.STORAGE_UUID)) || StorageBlockEntity.hasLinkedStorageEndpoint(inner)) {
				return;
			}
			MovingStorageData.get().findContents(payload.storageUuid)
					.ifPresent(contents -> PacketDistributor.sendToPlayer(serverPlayer, new MovingStorageContentsPayload(payload.storageUuid, contents)));
		}
	}
}
