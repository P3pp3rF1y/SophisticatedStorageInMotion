package net.p3pp3rf1y.sophisticatedstorageinmotion;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.CommonEventHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.data.DataGenerators;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntitiesClient;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.StorageInMotionPacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SophisticatedStorageInMotion.MOD_ID)
public class SophisticatedStorageInMotion {
	public static final String MOD_ID = "sophisticatedstorageinmotion";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@SuppressWarnings("java:S1118") //needs to be public for mod to work
	public SophisticatedStorageInMotion() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModItems.registerHandlers(modBus);
		ModEntities.registerHandlers(modBus);
		CommonEventHandler.registerHandlers();
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ModEntitiesClient.registerHandlers(modBus); //TODO move this to client event handler
		}
		modBus.addListener(DataGenerators::gatherData);
		modBus.addListener(SophisticatedStorageInMotion::setup);
	}

	public static ResourceLocation getRL(String regName) {
		return new ResourceLocation(getRegistryName(regName));
	}

	public static String getRegistryName(String regName) {
		return MOD_ID + ":" + regName;
	}

	private static void setup(FMLCommonSetupEvent event) {
		StorageInMotionPacketHandler.INSTANCE.init();
		event.enqueueWork(ModItems::registerDispenseBehavior);
	}
}
