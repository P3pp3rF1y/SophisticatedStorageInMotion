package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

public class ModEntities {
	private ModEntities() {
	}

	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, SophisticatedStorageInMotion.MOD_ID);

	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SophisticatedStorageInMotion.MOD_ID);

	public static final RegistryObject<EntityType<StorageMinecart>> STORAGE_MINECART = ENTITY_TYPES.register("storage_minecart", () -> EntityType.Builder.of((EntityType.EntityFactory<StorageMinecart>) StorageMinecart::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8).build(SophisticatedStorageInMotion.MOD_ID + ":storage_minecart"));

	public static final RegistryObject<MenuType<MovingStorageContainerMenu<?>>> MOVING_STORAGE_CONTAINER_TYPE = MENU_TYPES.register("moving_storage",
			() -> IForgeMenuType.create(MovingStorageContainerMenu::fromBuffer));

	public static final RegistryObject<MenuType<MovingStorageSettingsContainerMenu>> MOVING_STORAGE_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("moving_storage_settings",
			() -> IForgeMenuType.create(MovingStorageSettingsContainerMenu::fromBuffer));

	public static final RegistryObject<MenuType<MovingLimitedBarrelContainerMenu<?>>> MOVING_LIMITED_BARREL_CONTAINER_TYPE = MENU_TYPES.register("moving_limited_barrel",
			() -> IForgeMenuType.create(MovingLimitedBarrelContainerMenu::fromBuffer));

	public static final RegistryObject<MenuType<MovingLimitedBarrelSettingsContainerMenu>> MOVING_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("moving_limited_barrel_settings",
			() -> IForgeMenuType.create(MovingLimitedBarrelSettingsContainerMenu::fromBuffer));

	public static void registerHandlers(IEventBus modBus) {
		ENTITY_TYPES.register(modBus);
		MENU_TYPES.register(modBus);
	}
}
