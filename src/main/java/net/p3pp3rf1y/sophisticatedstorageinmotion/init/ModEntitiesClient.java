package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
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
	}

	private static void onMenuScreenRegister(RegisterEvent event) {
		if (!event.getRegistryKey().equals(ForgeRegistries.Keys.MENU_TYPES)) {
			return;
		}

		MenuScreens.register(ModEntities.MOVING_STORAGE_CONTAINER_TYPE.get(), MovingStorageScreen::constructScreen);
		MenuScreens.register(ModEntities.MOVING_STORAGE_SETTINGS_CONTAINER_TYPE.get(), MovingStorageSettingsScreen::constructScreen);
		MenuScreens.register(ModEntities.MOVING_LIMITED_BARREL_CONTAINER_TYPE.get(), MovingLimitedBarrelScreen::new);
		MenuScreens.register(ModEntities.MOVING_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE.get(), MovingLimitedBarrelSettingsScreen::new);
	}
}
