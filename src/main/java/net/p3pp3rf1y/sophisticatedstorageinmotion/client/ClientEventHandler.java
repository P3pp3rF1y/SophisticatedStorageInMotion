package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.PaintbrushMovingStorageOverlay;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

public class  ClientEventHandler{

	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ClientEventHandler::registerClientExtensions);
		modBus.addListener(ClientEventHandler::registerOverlay);
		modBus.addListener(ClientEventHandler::registerTooltipComponent);

		IEventBus eventBus = NeoForge.EVENT_BUS;
		eventBus.addListener(ClientMovingStorageContentsTooltip::onWorldLoad);

		net.p3pp3rf1y.sophisticatedstorage.client.ClientEventHandler.addSortScreenMatcher(screen -> screen instanceof MovingStorageScreen);
	}

	private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerItem(StorageMinecartItemRenderer.getItemRenderProperties(), ModItems.STORAGE_MINECART.get());
		event.registerItem(StorageBoatItemRenderer.getItemRenderProperties(), ModItems.STORAGE_BOAT.get());
	}

	private static void registerOverlay(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "paintbrush_moving_storage_info"), PaintbrushMovingStorageOverlay.HUD_PAINTBRUSH_INFO);
	}

	private static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(MovingStorageItem.MovingStorageContentsTooltip.class, ClientMovingStorageContentsTooltip::new);
	}
}
