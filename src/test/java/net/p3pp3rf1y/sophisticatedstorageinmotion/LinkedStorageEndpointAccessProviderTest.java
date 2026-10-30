package net.p3pp3rf1y.sophisticatedstorageinmotion;

import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.RegistryObject;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ILinkedStorageEndpointAccessProvider;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointAccessProviders;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageStackData;
import net.p3pp3rf1y.sophisticatedcore.network.ItemStackReference;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class LinkedStorageEndpointAccessProviderTest {
	private static StorageBoatItem storageBoat;
	private ILinkedStorageEndpointAccessProvider provider;
	private ServerPlayer player;
	private StorageContainerMenuBase<?> menu;
	private ItemStackReference reference;
	private LinkedStorageEndpointData endpoint;
	private ItemStack carrier;

	@BeforeAll
	static void bootstrap() throws ReflectiveOperationException {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
		RegistryObject<?>[] undecoratedStorageItems = {ModBlocks.BARREL_ITEM, ModBlocks.COPPER_BARREL_ITEM, ModBlocks.IRON_BARREL_ITEM,
				ModBlocks.GOLD_BARREL_ITEM, ModBlocks.DIAMOND_BARREL_ITEM, ModBlocks.NETHERITE_BARREL_ITEM, ModBlocks.LIMITED_BARREL_1_ITEM,
				ModBlocks.LIMITED_COPPER_BARREL_1_ITEM, ModBlocks.LIMITED_IRON_BARREL_1_ITEM, ModBlocks.LIMITED_GOLD_BARREL_1_ITEM,
				ModBlocks.LIMITED_DIAMOND_BARREL_1_ITEM, ModBlocks.LIMITED_NETHERITE_BARREL_1_ITEM};
		Item[] registeredItems = {Items.BARREL, Items.CHEST, Items.TRAPPED_CHEST, Items.ENDER_CHEST, Items.SHULKER_BOX, Items.IRON_BLOCK, Items.GOLD_BLOCK,
				Items.DIAMOND_BLOCK, Items.NETHERITE_BLOCK, Items.COPPER_BLOCK, Items.OAK_PLANKS, Items.SPRUCE_PLANKS};
		Field value = RegistryObject.class.getDeclaredField("value");
		value.setAccessible(true);
		for (int i = 0; i < undecoratedStorageItems.length; i++) {
			value.set(undecoratedStorageItems[i], registeredItems[i]);
		}
		GameData.unfreezeData();
		Method unfreeze = ForgeRegistries.ITEMS.getClass().getDeclaredMethod("unfreeze");
		unfreeze.setAccessible(true);
		unfreeze.invoke(ForgeRegistries.ITEMS);
		ResourceLocation boatId = new ResourceLocation("sophisticatedstorageinmotion", "security_test_storage_boat");
		storageBoat = new StorageBoatItem();
		ForgeRegistries.ITEMS.register(boatId, storageBoat);
		if (!BuiltInRegistries.ITEM.containsKey(boatId)) {
			Registry.register(BuiltInRegistries.ITEM, boatId, storageBoat);
		}
	}

	@BeforeEach
	void prepareProviderAndMenu() throws ReflectiveOperationException {
		FMLCommonSetupEvent event = mock(FMLCommonSetupEvent.class);
		Method setupWork = SophisticatedStorageInMotion.class.getDeclaredMethod("enqueueSetupWork", FMLCommonSetupEvent.class);
		setupWork.setAccessible(true);
		setupWork.invoke(null, event);
		ArgumentCaptor<Runnable> work = ArgumentCaptor.forClass(Runnable.class);
		verify(event, times(2)).enqueueWork(work.capture());
		try (MockedStatic<LinkedStorageEndpointAccessProviders> providers = mockStatic(LinkedStorageEndpointAccessProviders.class)) {
			work.getAllValues().get(1).run();
			ArgumentCaptor<ILinkedStorageEndpointAccessProvider> registered = ArgumentCaptor.forClass(ILinkedStorageEndpointAccessProvider.class);
			providers.verify(() -> LinkedStorageEndpointAccessProviders.register(registered.capture()));
			provider = registered.getValue();
		}
		player = mock(ServerPlayer.class);
		try (MockedStatic<ObfuscationReflectionHelper> ignored = mockStatic(ObfuscationReflectionHelper.class)) {
			menu = mock(StorageContainerMenuBase.class);
		}
		player.containerMenu = menu;
		when(menu.stillValid(player)).thenReturn(true);
		when(menu.getTotalSlotsNumber()).thenReturn(1);
		endpoint = new LinkedStorageEndpointData(UUID.randomUUID(), UUID.randomUUID());
		carrier = new ItemStack(Items.BARREL);
		LinkedStorageStackData.setEndpoint(carrier, endpoint);
		ItemStack boat = MovingStorageItem.createWithStorage(new ItemStack(storageBoat), carrier);
		when(menu.getSlot(0)).thenReturn(new Slot(new SimpleContainer(boat), 0, 0, 0));
		reference = new ItemStackReference(menu.containerId, 0, "", "");
	}

	@Test
	void resolveEndpointStackReturnsWrappedEndpointFromAccessibleMenuItem() {
		ItemStack resolved = provider.resolveEndpointStack(player, reference);

		assertEquals(endpoint, LinkedStorageStackData.getEndpoint(resolved));
		assertTrue(ItemStack.isSameItemSameTags(carrier, resolved));
	}

	@Test
	void resolveEndpointStackReturnsWrappedEndpointFromCursor() {
		ItemStack boat = menu.getSlot(0).getItem();
		when(menu.getCarried()).thenReturn(boat);
		reference = new ItemStackReference(menu.containerId, -1, "", "");

		ItemStack resolved = provider.resolveEndpointStack(player, reference);

		assertEquals(endpoint, LinkedStorageStackData.getEndpoint(resolved));
	}

	@Test
	void resolveEndpointStackRejectsInvalidReferenceWithoutSearchingNearbyEntities() {
		reference = new ItemStackReference(menu.containerId, Integer.MAX_VALUE, "", "");

		ItemStack resolved = provider.resolveEndpointStack(player, reference);

		assertTrue(resolved.isEmpty());
		verify(player, never()).level();
		verify(player, never()).getBoundingBox();
	}

	@Test
	void resolveEndpointStackRejectsUnrelatedMenuItemWithoutSearchingNearbyEntities() {
		when(menu.getSlot(0)).thenReturn(new Slot(new SimpleContainer(new ItemStack(Items.STONE)), 0, 0, 0));

		ItemStack resolved = provider.resolveEndpointStack(player, reference);

		assertTrue(resolved.isEmpty());
		verify(player, never()).level();
	}
}
