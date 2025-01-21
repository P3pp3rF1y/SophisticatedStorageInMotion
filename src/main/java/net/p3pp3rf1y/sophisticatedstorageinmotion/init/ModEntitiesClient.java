package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.StorageBoatRenderer;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.StorageMinecartRenderer;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingLimitedBarrelScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingLimitedBarrelSettingsScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.MovingStorageSettingsScreen;

public class ModEntitiesClient {
	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ModEntitiesClient::registerEntityRenderers);
		modBus.addListener(ModEntitiesClient::onMenuScreenRegister);
	}

	private static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.STORAGE_MINECART.get(), StorageMinecartRenderer::new);
		event.registerEntityRenderer(ModEntities.STORAGE_BOAT.get(), StorageBoatRenderer::new);
	}

	private static void onMenuScreenRegister(RegisterMenuScreensEvent event) {
		event.register(ModEntities.MOVING_STORAGE_CONTAINER_TYPE.get(), MovingStorageScreen::constructScreen);
		event.register(ModEntities.MOVING_STORAGE_SETTINGS_CONTAINER_TYPE.get(), MovingStorageSettingsScreen::constructScreen);
		event.register(ModEntities.MOVING_LIMITED_BARREL_CONTAINER_TYPE.get(), MovingLimitedBarrelScreen::new);
		event.register(ModEntities.MOVING_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE.get(), MovingLimitedBarrelSettingsScreen::new);
	}
}
