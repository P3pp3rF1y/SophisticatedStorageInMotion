package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedstorage.block.LimitedBarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageLinkedStorageHostWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class LinkedMovingStorageIdentityTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void nestedCarrierSurvivesMovingItemCopyWithoutAllocatingOuterIdentity() {
		LinkedStorageEndpointData endpoint = new LinkedStorageEndpointData(UUID.randomUUID(), UUID.randomUUID());
		ItemStack carrier = new ItemStack(ModBlocks.BARREL_ITEM.get());
		carrier.set(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT, endpoint);
		carrier.set(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT, true);
		ItemStack boat = MovingStorageItem.createWithStorage(new ItemStack(ModItems.STORAGE_BOAT.get()), carrier);

		assertEquals(endpoint, MovingStorageItem.getStorageItem(boat).get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT));
		assertEquals(endpoint, MovingStorageItem.getStorageItem(boat.copy()).get(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT));
		assertFalse(boat.has(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT));
		assertFalse(boat.has(ModCoreDataComponents.STORAGE_UUID));
		assertFalse(MovingStorageItem.getStorageItem(boat).has(ModCoreDataComponents.STORAGE_UUID));
	}

	@Test
	void limitedBarrelLinkCompatibilityMatchesBaseSlotCountAcrossPhysicalForms() {
		LimitedBarrelBlockEntity placed = new LimitedBarrelBlockEntity(BlockPos.ZERO, ModBlocks.LIMITED_BARREL_2.get().defaultBlockState());
		String placedKey = StorageLinkedStorageHostWrapper.getCompatibilityKey(placed);
		ItemStack moving = MovingStorageItem.createWithStorage(new ItemStack(ModItems.STORAGE_BOAT.get()),
				new ItemStack(ModBlocks.LIMITED_COPPER_BARREL_2_ITEM.get()));

		assertEquals(placedKey, StorageLinkedStorageHostWrapper.getCompatibilityKey(MovingStorageItem.getStorageItem(moving)));
		assertEquals(placedKey, StorageLinkedStorageHostWrapper.getCompatibilityKey(new ItemStack(ModBlocks.LIMITED_IRON_BARREL_2_ITEM.get())));
		assertNotEquals(placedKey, StorageLinkedStorageHostWrapper.getCompatibilityKey(new ItemStack(ModBlocks.LIMITED_BARREL_1_ITEM.get())));
		assertNotEquals(placedKey, StorageLinkedStorageHostWrapper.getCompatibilityKey(new ItemStack(ModBlocks.BARREL_ITEM.get())));
	}
}
