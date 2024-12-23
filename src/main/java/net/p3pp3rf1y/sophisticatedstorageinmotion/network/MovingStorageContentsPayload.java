package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.data.MovingStorageData;

import java.util.UUID;

public record MovingStorageContentsPayload(UUID storageUuid, CompoundTag contents) implements CustomPacketPayload {
	public static final Type<MovingStorageContentsPayload> TYPE = new Type<>(SophisticatedStorageInMotion.getRL("storage_contents"));
	public static final StreamCodec<ByteBuf, MovingStorageContentsPayload> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC,
			MovingStorageContentsPayload::storageUuid,
			ByteBufCodecs.COMPOUND_TAG,
			MovingStorageContentsPayload::contents,
			MovingStorageContentsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(MovingStorageContentsPayload payload, IPayloadContext context) {
		MovingStorageData.get(payload.storageUuid).setContents(payload.storageUuid, payload.contents);
	}
}
