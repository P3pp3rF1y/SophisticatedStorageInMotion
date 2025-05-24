package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;

import java.util.function.Supplier;

public record MovingStorageOpennessMessage(int entityId, boolean shouldBeOpen) {
	public static void encode(MovingStorageOpennessMessage msg, FriendlyByteBuf packetBuffer) {
		packetBuffer.writeInt(msg.entityId);
		packetBuffer.writeBoolean(msg.shouldBeOpen);
	}

	public static MovingStorageOpennessMessage decode(FriendlyByteBuf packetBuffer) {
		return new MovingStorageOpennessMessage(
				packetBuffer.readInt(),
				packetBuffer.readBoolean()
		);
	}

	static void onMessage(MovingStorageOpennessMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> handleMessage(msg));
		context.setPacketHandled(true);
	}

	private static void handleMessage(MovingStorageOpennessMessage msg) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

		Entity entity = player.level().getEntity(msg.entityId());
		if (entity instanceof IMovingStorageEntity storageEntity) {
			storageEntity.getStorageHolder().setShouldBeOpen(msg.shouldBeOpen);
		}
	}
}
