package net.p3pp3rf1y.sophisticatedstorageinmotion.network;

import net.minecraftforge.network.NetworkDirection;
import net.p3pp3rf1y.sophisticatedcore.network.PacketHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

public class StorageInMotionPacketHandler extends PacketHandler {
	public static final StorageInMotionPacketHandler INSTANCE = new StorageInMotionPacketHandler(SophisticatedStorageInMotion.MOD_ID,
			SophisticatedStorageInMotion.getNetworkProtocolVersion());

	private StorageInMotionPacketHandler(String modId, String protocol) {
		super(modId, protocol);
	}

	@Override
	public void registerMessages() {
		registerMessage(OpenMovingStorageInventoryMessage.class, OpenMovingStorageInventoryMessage::encode, OpenMovingStorageInventoryMessage::decode,
				OpenMovingStorageInventoryMessage::onMessage, NetworkDirection.PLAY_TO_SERVER);
		registerMessage(MovingStorageContentsMessage.class, MovingStorageContentsMessage::encode, MovingStorageContentsMessage::decode,
				MovingStorageContentsMessage::onMessage, NetworkDirection.PLAY_TO_CLIENT);
		registerMessage(RequestMovingStorageInventoryContentsMessage.class, RequestMovingStorageInventoryContentsMessage::encode,
				RequestMovingStorageInventoryContentsMessage::decode, RequestMovingStorageInventoryContentsMessage::onMessage, NetworkDirection.PLAY_TO_SERVER);
		registerMessage(MovingStorageOpennessMessage.class, MovingStorageOpennessMessage::encode, MovingStorageOpennessMessage::decode,
				MovingStorageOpennessMessage::onMessage, NetworkDirection.PLAY_TO_CLIENT);
	}
}
