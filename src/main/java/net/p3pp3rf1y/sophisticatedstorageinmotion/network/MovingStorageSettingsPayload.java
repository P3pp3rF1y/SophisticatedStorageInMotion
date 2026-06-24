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

public record MovingStorageSettingsPayload(UUID storageUuid, ContainerContents.SettingsData settingsData) implements CustomPacketPayload {
	public static final Type<MovingStorageSettingsPayload> TYPE = new Type<>(SophisticatedStorageInMotion.getIdentifier("storage_settings"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MovingStorageSettingsPayload> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC,
			MovingStorageSettingsPayload::storageUuid, ContainerContents.SettingsData.STREAM_CODEC, MovingStorageSettingsPayload::settingsData,
			MovingStorageSettingsPayload::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handlePayload(MovingStorageSettingsPayload payload, IPayloadContext context) {
		MovingStorageData movingStorageData = MovingStorageData.get();
		ContainerContents contents = movingStorageData.getContents(payload.storageUuid);
		movingStorageData.setContentsClient(payload.storageUuid,
				new ContainerContents(contents.inventory(), contents.partitioner(), contents.upgrades(), payload.settingsData));
		ClientStorageContentsTooltipBase.refreshContents();
	}
}
