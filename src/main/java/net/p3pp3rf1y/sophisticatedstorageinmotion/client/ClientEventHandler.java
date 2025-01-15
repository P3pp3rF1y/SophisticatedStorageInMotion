package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.IEventBus;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.PaintbrushMovingStorageOverlay;

public class ClientEventHandler {
	private ClientEventHandler() {
	}

	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ClientEventHandler::registerOverlay);
	}

	private static void registerOverlay(RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "paintbrush_moving_storage_info", PaintbrushMovingStorageOverlay.HUD_PAINTBRUSH_INFO);
	}
}
