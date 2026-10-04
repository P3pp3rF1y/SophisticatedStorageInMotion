package net.p3pp3rf1y.sophisticatedstorageinmotion;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointAccessProviders;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageStackData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.ClientEventHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.CommonEventHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.data.DataGenerators;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModCompat;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntitiesClient;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.StorageInMotionPacketHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(SophisticatedStorageInMotion.MOD_ID)
public class SophisticatedStorageInMotion {
	public static final String MOD_ID = "sophisticatedstorageinmotion";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static String networkProtocolVersion;

	@SuppressWarnings("java:S1118") // needs to be public for mod to work
	public SophisticatedStorageInMotion() {
		networkProtocolVersion = ModLoadingContext.get().getActiveContainer().getModInfo().getVersion().toString();
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModItems.registerHandlers(modBus);
		ModEntities.registerHandlers(modBus);
		CommonEventHandler.registerHandlers();
		ModCompat.initCompats();
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ClientEventHandler.registerHandlers(modBus);
			ModEntitiesClient.registerHandlers(modBus);
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

	public static String getNetworkProtocolVersion() {
		return networkProtocolVersion;
	}

	private static void setup(FMLCommonSetupEvent event) {
		StorageInMotionPacketHandler.INSTANCE.init();
		event.enqueueWork(ModItems::registerDispenseBehavior);
		event.enqueueWork(() -> LinkedStorageEndpointAccessProviders.register((player, manager, groupId) -> {
			for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
				LinkedStorageEndpointData endpoint = LinkedStorageStackData.getEndpoint(MovingStorageItem.getStorageItem(player.getInventory().getItem(slot)));
				if (endpoint != null && endpoint.groupId().equals(groupId) && manager.isEndpointMember(groupId, endpoint.endpointId())) {
					return true;
				}
			}
			return player.level().getEntities(player, player.getBoundingBox().inflate(8), entity -> entity instanceof IMovingStorageEntity).stream()
					.anyMatch(entity -> {
						LinkedStorageEndpointData endpoint = LinkedStorageStackData.getEndpoint(((IMovingStorageEntity) entity).getStorageItem());
						return endpoint != null && endpoint.groupId().equals(groupId) && manager.isEndpointMember(groupId, endpoint.endpointId());
					});
		}));
		ModCompat.compatsSetup();
	}
}
