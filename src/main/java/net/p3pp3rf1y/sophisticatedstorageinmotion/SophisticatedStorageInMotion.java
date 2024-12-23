package net.p3pp3rf1y.sophisticatedstorageinmotion;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.ClientEventHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SophisticatedStorageInMotion.MOD_ID)
public class SophisticatedStorageInMotion {
	public static final String MOD_ID = "sophisticatedstorageinmotion";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@SuppressWarnings("java:S1118") //needs to be public for mod to work
	public SophisticatedStorageInMotion(IEventBus modBus, Dist dist, ModContainer container) {
		ModItems.registerHandlers(modBus);
		ModEntities.registerHandlers(modBus);
		ModDataComponents.register(modBus);
		if (dist == Dist.CLIENT) {
			ClientEventHandler.registerHandlers(modBus);
			ModEntitiesClient.registerHandlers(modBus); //TODO move this to client event handler
		}
		modBus.addListener(ModPayloads::registerPayloads);
	}

	public static ResourceLocation getRL(String regName) {
		return ResourceLocation.parse(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}
}
