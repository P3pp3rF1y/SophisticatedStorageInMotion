package net.p3pp3rf1y.sophisticatedstorageinmotion;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.ILinkedStorageEndpointAccessProvider;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointAccessProviders;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedcore.network.ItemStackReference;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class LinkedStorageEndpointAccessProviderTest {
	private ILinkedStorageEndpointAccessProvider provider;
	private ServerPlayer player;
	private StorageContainerMenuBase<?> menu;
	private ItemStackReference reference;
	private LinkedStorageEndpointData endpoint;
	private ItemStack carrier;
	private ItemStack boat;

	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@BeforeEach
	void prepareProviderAndMenu() throws ReflectiveOperationException {
		FMLCommonSetupEvent event = mock(FMLCommonSetupEvent.class);
		Method setup = SophisticatedStorageInMotion.class.getDeclaredMethod("setup", FMLCommonSetupEvent.class);
		setup.setAccessible(true);
		setup.invoke(null, event);
		ArgumentCaptor<Runnable> work = ArgumentCaptor.forClass(Runnable.class);
		verify(event, times(2)).enqueueWork(work.capture());
		try (MockedStatic<LinkedStorageEndpointAccessProviders> providers = mockStatic(LinkedStorageEndpointAccessProviders.class)) {
			work.getAllValues().getLast().run();
			ArgumentCaptor<ILinkedStorageEndpointAccessProvider> registered = ArgumentCaptor.forClass(ILinkedStorageEndpointAccessProvider.class);
			providers.verify(() -> LinkedStorageEndpointAccessProviders.register(registered.capture()));
			provider = registered.getValue();
		}
		player = mock(ServerPlayer.class);
		menu = mock(StorageContainerMenuBase.class);
		player.containerMenu = menu;
		when(menu.stillValid(player)).thenReturn(true);
		when(menu.getTotalSlotsNumber()).thenReturn(1);
		endpoint = new LinkedStorageEndpointData(UUID.randomUUID(), UUID.randomUUID());
		carrier = mock(ItemStack.class);
		when(carrier.get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT)).thenReturn(endpoint);
		boat = mock(ItemStack.class);
		when(boat.getItem()).thenReturn(mock(MovingStorageItem.class));
		when(menu.getSlot(0)).thenReturn(new Slot(new SimpleContainer(boat), 0, 0, 0));
		reference = new ItemStackReference(menu.containerId, 0, "", "");
	}

	@Test
	void resolveEndpointStackReturnsWrappedEndpointFromAccessibleMenuItem() {
		try (MockedStatic<MovingStorageItem> moving = mockStatic(MovingStorageItem.class)) {
			moving.when(() -> MovingStorageItem.getStorageItem(boat)).thenReturn(carrier);

			ItemStack resolved = provider.resolveEndpointStack(player, reference);

			assertEquals(endpoint, resolved.get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT));
			assertSame(carrier, resolved);
		}
	}

	@Test
	void resolveEndpointStackReturnsWrappedEndpointFromCursor() {
		when(menu.getCarried()).thenReturn(boat);
		reference = new ItemStackReference(menu.containerId, -1, "", "");
		try (MockedStatic<MovingStorageItem> moving = mockStatic(MovingStorageItem.class)) {
			moving.when(() -> MovingStorageItem.getStorageItem(boat)).thenReturn(carrier);

			ItemStack resolved = provider.resolveEndpointStack(player, reference);

			assertEquals(endpoint, resolved.get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT));
		}
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
		when(menu.getSlot(0)).thenReturn(new Slot(new SimpleContainer(ItemStack.EMPTY), 0, 0, 0));

		ItemStack resolved = provider.resolveEndpointStack(player, reference);

		assertTrue(resolved.isEmpty());
		verify(player, never()).level();
	}
}
