package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;

import java.util.UUID;

public record RequestMovingStorageInventoryContentsPayload(UUID storageUuid) implements CustomPacketPayload {
	public static final Type<RequestMovingStorageInventoryContentsPayload> TYPE = new Type<>(SophisticatedStorageInMotion.getIdentifier("request_moving_storage_inventory_contents"));
	public static final StreamCodec<ByteBuf, RequestMovingStorageInventoryContentsPayload> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC,
			RequestMovingStorageInventoryContentsPayload::storageUuid,
			RequestMovingStorageInventoryContentsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(RequestMovingStorageInventoryContentsPayload payload, IPayloadContext context) {
		if (context.player() instanceof ServerPlayer serverPlayer) {
			PacketDistributor.sendToPlayer(serverPlayer, new MovingStorageContentsPayload(payload.storageUuid, MovingStorageData.get().getContents(payload.storageUuid)));
		}
	}
}
