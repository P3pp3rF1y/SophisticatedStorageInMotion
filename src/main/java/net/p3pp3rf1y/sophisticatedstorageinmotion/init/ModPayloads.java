package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageContentsPayload;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.OpenMovingStorageInventoryPayload;

public class ModPayloads {
	private ModPayloads() {
	}

	public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(SophisticatedStorageInMotion.MOD_ID).versioned("1.0");
		registrar.playToServer(OpenMovingStorageInventoryPayload.TYPE, OpenMovingStorageInventoryPayload.STREAM_CODEC, OpenMovingStorageInventoryPayload::handlePayload);
		registrar.playToClient(MovingStorageContentsPayload.TYPE, MovingStorageContentsPayload.STREAM_CODEC, MovingStorageContentsPayload::handlePayload);
	}
}
