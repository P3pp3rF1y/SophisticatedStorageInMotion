package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.PaintbrushMovingStorageOverlay;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;

public class  ClientEventHandler{

	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ClientEventHandler::registerClientExtensions);
		modBus.addListener(ClientEventHandler::registerOverlay);
	}

	private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerItem(StorageMinecartItemRenderer.getItemRenderProperties(), ModItems.STORAGE_MINECART.get());
	}

	private static void registerOverlay(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "paintbrush_moving_storage_info"), PaintbrushMovingStorageOverlay.HUD_PAINTBRUSH_INFO);
	}
}
