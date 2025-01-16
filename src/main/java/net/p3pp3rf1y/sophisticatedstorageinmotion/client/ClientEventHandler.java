package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.PaintbrushMovingStorageOverlay;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

public class ClientEventHandler {
	private ClientEventHandler() {
	}

	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ClientEventHandler::registerOverlay);
		modBus.addListener(ClientEventHandler::registerTooltipComponent);

		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		eventBus.addListener(ClientMovingStorageContentsTooltip::onWorldLoad);

		net.p3pp3rf1y.sophisticatedstorage.client.ClientEventHandler.addSortScreenMatcher(screen -> screen instanceof MovingStorageScreen);
	}

	private static void registerOverlay(RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "paintbrush_moving_storage_info", PaintbrushMovingStorageOverlay.HUD_PAINTBRUSH_INFO);
	}

	private static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(MovingStorageItem.MovingStorageContentsTooltip.class, ClientMovingStorageContentsTooltip::new);
	}
}
