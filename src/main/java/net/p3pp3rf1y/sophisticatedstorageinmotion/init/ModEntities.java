package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

import java.util.function.Supplier;

public class ModEntities {
	private ModEntities() {
	}

	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, SophisticatedStorageInMotion.MOD_ID);

	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, SophisticatedStorageInMotion.MOD_ID);

	public static final Supplier<EntityType<StorageMinecart>> STORAGE_MINECART = ENTITY_TYPES.register("storage_minecart", () -> EntityType.Builder.of((EntityType.EntityFactory<StorageMinecart>) StorageMinecart::new, MobCategory.MISC).sized(0.98F, 0.7F).clientTrackingRange(8).passengerAttachments(0.1875F).build(SophisticatedStorageInMotion.MOD_ID + ":storage_minecart"));

	public static final Supplier<MenuType<MovingStorageContainerMenu<?>>> MOVING_STORAGE_CONTAINER_TYPE = MENU_TYPES.register("moving_storage",
			() -> IMenuTypeExtension.create(MovingStorageContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MovingStorageSettingsContainerMenu>> MOVING_STORAGE_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("moving_storage_settings",
			() -> IMenuTypeExtension.create(MovingStorageSettingsContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MovingLimitedBarrelContainerMenu<?>>> MOVING_LIMITED_BARREL_CONTAINER_TYPE = MENU_TYPES.register("moving_limited_barrel",
			() -> IMenuTypeExtension.create(MovingLimitedBarrelContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MovingLimitedBarrelSettingsContainerMenu>> MOVING_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("moving_limited_barrel_settings",
			() -> IMenuTypeExtension.create(MovingLimitedBarrelSettingsContainerMenu::fromBuffer));

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, STORAGE_MINECART.get(), (entity, direction) -> entity.getStorageHolder().getStorageWrapper().getInventoryForInputOutput());
	}

	public static void registerHandlers(IEventBus modBus) {
		ENTITY_TYPES.register(modBus);
		MENU_TYPES.register(modBus);

		modBus.addListener(ModEntities::registerCapabilities);
	}
}
