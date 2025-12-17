package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltipBase;
import net.p3pp3rf1y.sophisticatedcore.inventory.ContainerContents;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;

import java.util.UUID;

public record MovingStorageContentsPayload(UUID storageUuid,
										   ContainerContents contents) implements CustomPacketPayload {
	public static final Type<MovingStorageContentsPayload> TYPE = new Type<>(SophisticatedStorageInMotion.getIdentifier("storage_contents"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MovingStorageContentsPayload> STREAM_CODEC = StreamCodec.composite(
			UUIDUtil.STREAM_CODEC,
			MovingStorageContentsPayload::storageUuid,
			ContainerContents.STREAM_CODEC,
			MovingStorageContentsPayload::contents,
			MovingStorageContentsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(MovingStorageContentsPayload payload, IPayloadContext context) {
		MovingStorageData.get().setContentsClient(payload.storageUuid, payload.contents);
		ClientStorageContentsTooltipBase.refreshContents();
	}
}
