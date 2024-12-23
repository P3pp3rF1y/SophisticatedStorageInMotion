package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;

public class  ClientEventHandler{

	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ClientEventHandler::registerClientExtensions);
	}

	private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerItem(StorageMinecartItemRenderer.getItemRenderProperties(), ModItems.STORAGE_MINECART.get());
	}
}
