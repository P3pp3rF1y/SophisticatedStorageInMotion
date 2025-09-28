
package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.MovingStorageHighlightHandler;

import java.util.List;

public record MovingStorageSyncItemHighlightsPayload(List<Integer> stackEntityIds, List<Integer> itemEntityIds) implements CustomPacketPayload {
	public static final Type<MovingStorageSyncItemHighlightsPayload> TYPE = new Type<>(SophisticatedStorageInMotion.getRL("moving_storage_sync_item_highlights"));
	public static final StreamCodec<ByteBuf, MovingStorageSyncItemHighlightsPayload> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
			MovingStorageSyncItemHighlightsPayload::stackEntityIds,
			ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
			MovingStorageSyncItemHighlightsPayload::itemEntityIds,
			MovingStorageSyncItemHighlightsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(MovingStorageSyncItemHighlightsPayload payload, IPayloadContext context) {
		MovingStorageHighlightHandler.INSTANCE.setHighlightedEntities(payload.stackEntityIds, payload.itemEntityIds);
	}
}
