package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;

public record MovingStorageOpennessPayload(int entityId, boolean shouldBeOpen) implements CustomPacketPayload {
	public static final Type<MovingStorageOpennessPayload> TYPE = new Type<>(SophisticatedStorageInMotion.getIdentifier("storage_openness"));
	public static final StreamCodec<ByteBuf, MovingStorageOpennessPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT,
			MovingStorageOpennessPayload::entityId,
			ByteBufCodecs.BOOL,
			MovingStorageOpennessPayload::shouldBeOpen,
			MovingStorageOpennessPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(MovingStorageOpennessPayload payload, IPayloadContext context) {
		Player player = context.player();
		Entity entity = player.level().getEntity(payload.entityId());
		if (entity instanceof IMovingStorageEntity storageEntity) {
			storageEntity.getStorageHolder().setShouldBeOpen(payload.shouldBeOpen);
		}
	}
}
