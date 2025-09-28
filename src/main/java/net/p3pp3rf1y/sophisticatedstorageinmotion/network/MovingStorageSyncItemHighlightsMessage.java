
package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.MovingStorageHighlightHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record MovingStorageSyncItemHighlightsMessage(List<Integer> stackEntityIds, List<Integer> itemEntityIds) {
	public static void encode(MovingStorageSyncItemHighlightsMessage msg, FriendlyByteBuf packetBuffer) {
		packetBuffer.writeCollection(msg.stackEntityIds(), FriendlyByteBuf::writeInt);
		packetBuffer.writeCollection(msg.itemEntityIds(), FriendlyByteBuf::writeInt);
	}

	public static MovingStorageSyncItemHighlightsMessage decode(FriendlyByteBuf packetBuffer) {
		return new MovingStorageSyncItemHighlightsMessage(
				packetBuffer.readCollection(ArrayList::new, FriendlyByteBuf::readInt),
				packetBuffer.readCollection(ArrayList::new, FriendlyByteBuf::readInt)
		);
	}

	static void onMessage(MovingStorageSyncItemHighlightsMessage msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> handleMessage(msg));
		context.setPacketHandled(true);
	}


	public static void handleMessage(MovingStorageSyncItemHighlightsMessage payload) {
		MovingStorageHighlightHandler.INSTANCE.setHighlightedEntities(payload.stackEntityIds, payload.itemEntityIds);
	}
}
