package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.p3pp3rf1y.sophisticatedstorage.util.StreamCodecs;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageSettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageBoat;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageItemSyncHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

import java.util.Optional;
import java.util.function.Supplier;

public class ModEntities {
	private ModEntities() {
	}

	private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, SophisticatedStorageInMotion.MOD_ID);
	private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, SophisticatedStorageInMotion.MOD_ID);
	public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(SophisticatedStorageInMotion.MOD_ID);
	public static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, SophisticatedStorageInMotion.MOD_ID);

	public static final Supplier<EntityType<StorageMinecart>> STORAGE_MINECART = ENTITY_TYPES.registerEntityType("storage_minecart", StorageMinecart::new, MobCategory.MISC, builder -> builder.sized(0.98F, 0.7F).clientTrackingRange(8).passengerAttachments(0.1875F));
	public static final Supplier<EntityType<StorageBoat>> STORAGE_BOAT = ENTITY_TYPES.registerEntityType("storage_boat", StorageBoat::new, MobCategory.MISC, builder -> builder.sized(1.375F, 0.5625F).eyeHeight(0.5625F).clientTrackingRange(10));

	public static final Supplier<AttachmentType<ItemStack>> STORAGE_ITEM_ATTACHMENT = ATTACHMENT_TYPES.register(
			"storage_item", () -> AttachmentType.builder(() -> ItemStack.EMPTY).serialize(ItemStack.OPTIONAL_CODEC.fieldOf("storage_item")).sync(new StorageItemSyncHandler()).build()
	);

	public static final Supplier<MenuType<MovingStorageContainerMenu<?>>> MOVING_STORAGE_CONTAINER_TYPE = MENU_TYPES.register("moving_storage",
			() -> IMenuTypeExtension.create(MovingStorageContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MovingStorageSettingsContainerMenu>> MOVING_STORAGE_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("moving_storage_settings",
			() -> IMenuTypeExtension.create(MovingStorageSettingsContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MovingLimitedBarrelContainerMenu<?>>> MOVING_LIMITED_BARREL_CONTAINER_TYPE = MENU_TYPES.register("moving_limited_barrel",
			() -> IMenuTypeExtension.create(MovingLimitedBarrelContainerMenu::fromBuffer));

	public static final Supplier<MenuType<MovingLimitedBarrelSettingsContainerMenu>> MOVING_LIMITED_BARREL_SETTINGS_CONTAINER_TYPE = MENU_TYPES.register("moving_limited_barrel_settings",
			() -> IMenuTypeExtension.create(MovingLimitedBarrelSettingsContainerMenu::fromBuffer));

	public static final Supplier<EntityDataSerializer<WoodType>> WOOD_TYPE_SERIALIZER = ENTITY_DATA_SERIALIZERS.register("wood_type", () -> EntityDataSerializer.forValueType(StreamCodecs.WOOD_TYPE_STREAM_CODEC));

	private static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, STORAGE_MINECART.get(), (entity, direction) -> entity.getStorageHolder().getStorageWrapper().getInventoryForInputOutput());
		event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, STORAGE_BOAT.get(), (entity, direction) -> entity.getStorageHolder().getStorageWrapper().getInventoryForInputOutput());
	}

	public static void registerHandlers(IEventBus modBus) {
		ENTITY_TYPES.register(modBus);
		MENU_TYPES.register(modBus);
		ENTITY_DATA_SERIALIZERS.register(modBus);
		ATTACHMENT_TYPES.register(modBus);

		modBus.addListener(ModEntities::registerCapabilities);
	}
}
