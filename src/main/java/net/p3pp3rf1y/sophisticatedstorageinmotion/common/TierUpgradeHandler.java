package net.p3pp3rf1y.sophisticatedstorageinmotion.common;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedstorage.entity.StorageHolderBase;
import net.p3pp3rf1y.sophisticatedstorage.entity.StorageHolderTierUpgradeHandler;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageTierUpgradeItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

import java.util.HashMap;
import java.util.Map;

public class TierUpgradeHandler {
	private TierUpgradeHandler() {
	}

	private static final Map<StorageTierUpgradeItem.TierUpgrade, Map<Item, IEntityTierUpgradeDefinition>> ENTITY_TIER_UPGRADE_DEFINITIONS = new HashMap<>();

	public static void onTierUpgradeInteract(PlayerInteractEvent.EntityInteract event) {
		Player player = event.getEntity();
		ItemStack itemInHand = player.getItemInHand(event.getHand());
		if (!(itemInHand.getItem() instanceof StorageTierUpgradeItem tierUpgradeItem)) {
			return;
		}

		Map<Item, IEntityTierUpgradeDefinition> tierDefinitions = ENTITY_TIER_UPGRADE_DEFINITIONS.get(tierUpgradeItem.getTier());
		if (tierDefinitions == null) {
			SophisticatedStorageInMotion.LOGGER.warn("No tier upgrade definitions found for {}", tierUpgradeItem.getTier());
			return;
		}

		if (event.getTarget() instanceof IMovingStorageEntity movingStorage && !movingStorage.getStorageHolder().isOpen() && !movingStorage.getStorageHolder().isPacked()) {
			upgradeEntity(event, event.getTarget(), player, itemInHand, tierDefinitions, movingStorage.getStorageItem().getItem(), movingStorage.getStorageItem());
		} else if (event.getTarget() instanceof MinecartChest minecartChest) {
			upgradeEntity(event, minecartChest, player, itemInHand, tierDefinitions, Items.CHEST, ItemStack.EMPTY);
		}
	}

	private static void upgradeEntity(PlayerInteractEvent.EntityInteract event, Entity entity, Player player, ItemStack tierUpgrade, Map<Item, IEntityTierUpgradeDefinition> tierDefinitions, Item tierDefinitionItem, ItemStack storageItem) {
		IEntityTierUpgradeDefinition upgradeDefinition = tierDefinitions.get(tierDefinitionItem);
		if (upgradeDefinition == null) {
			SophisticatedStorageInMotion.LOGGER.warn("No tier upgrade definition found for {}", () -> ForgeRegistries.ITEMS.getKey(tierDefinitionItem));
			return;
		}

		if (!player.level().isClientSide()) {
			upgradeDefinition.upgradeEntity(entity, storageItem);

			if (!player.isCreative()) {
				tierUpgrade.shrink(1);
			}
		}

		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
	}

	interface IEntityTierUpgradeDefinition {
		void upgradeEntity(Entity entity, ItemStack storageItem);
	}

	private static class VanillaMinecartChestTierUpgradeDefinition implements IEntityTierUpgradeDefinition {
		private final BlockItem upgradedItem;

		private VanillaMinecartChestTierUpgradeDefinition(BlockItem upgradedItem) {
			this.upgradedItem = upgradedItem;
		}

		public void upgradeEntity(Entity entity, ItemStack storageItem) {
			if (!(entity instanceof MinecartChest minecartChest)) {
				return;
			}

			StorageMinecart storageMinecart = new StorageMinecart(minecartChest.level(), minecartChest.getX(), minecartChest.getY(), minecartChest.getZ());
			storageMinecart.getStorageHolder().setStorageItem(WoodStorageBlockItem.setWoodType(new ItemStack(upgradedItem), WoodType.OAK));
			InventoryHandler inventoryHandler = storageMinecart.getStorageHolder().getStorageWrapper().getInventoryHandler();

			for (int slot = 0; slot < minecartChest.getContainerSize(); slot++) {
				inventoryHandler.setStackInSlot(slot, minecartChest.getItem(slot));
				minecartChest.setItem(slot, ItemStack.EMPTY);
			}

			minecartChest.discard();
			minecartChest.level().addFreshEntity(storageMinecart);
		}
	}

	private static class EntityTierUpgradeDefinition implements IEntityTierUpgradeDefinition {
		private final StorageHolderTierUpgradeHandler.StorageHolderUpgradeDefinition storageHolderDefinition;

		private EntityTierUpgradeDefinition(StorageHolderTierUpgradeHandler.StorageHolderUpgradeDefinition storageHolderDefinition) {
			this.storageHolderDefinition = storageHolderDefinition;
		}

		@Override
		public void upgradeEntity(Entity entity, ItemStack storageItem) {
			if (!(entity instanceof IMovingStorageEntity movingStorage)) {
				return;
			}
			StorageHolderBase storageHolder = movingStorage.getStorageHolder();

			storageHolderDefinition.upgradeStorageHolder(storageHolder, storageItem);
		}
	}

	private static class EntityTierUpgradeMap {
		private final ImmutableMap.Builder<Item, IEntityTierUpgradeDefinition> wrappedBuilder = ImmutableMap.builder();

		public static EntityTierUpgradeMap builder() {
			return new EntityTierUpgradeMap();
		}

		public EntityTierUpgradeMap put(Item item, IEntityTierUpgradeDefinition definition) {
			wrappedBuilder.put(item, definition);
			return this;
		}

		public EntityTierUpgradeMap putAllWrapped(Map<Item, StorageHolderTierUpgradeHandler.StorageHolderUpgradeDefinition> storageHolderDefinitions) {
			storageHolderDefinitions.forEach((item, definition) -> wrappedBuilder.put(item, new EntityTierUpgradeDefinition(definition)));
			return this;
		}

		public Map<Item, IEntityTierUpgradeDefinition> build() {
			return wrappedBuilder.build();
		}
	}

	static {
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC, Map.of(
				Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.CHEST_ITEM.get())
		));
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_COPPER, EntityTierUpgradeMap.builder()
				.put(Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.COPPER_CHEST_ITEM.get()))
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_COPPER))
				.build());
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_IRON, EntityTierUpgradeMap.builder()
				.put(Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.IRON_CHEST_ITEM.get()))
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_IRON))
				.build()
		);
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_GOLD, EntityTierUpgradeMap.builder()
				.put(Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.GOLD_CHEST_ITEM.get()))
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_GOLD))
				.build()
		);
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_DIAMOND, EntityTierUpgradeMap.builder()
				.put(Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.DIAMOND_CHEST_ITEM.get()))
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_DIAMOND))
				.build()
		);
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_NETHERITE, EntityTierUpgradeMap.builder()
				.put(Items.CHEST, new VanillaMinecartChestTierUpgradeDefinition(ModBlocks.NETHERITE_CHEST_ITEM.get()))
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.BASIC_TO_NETHERITE))
				.build()
		);

		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_IRON, EntityTierUpgradeMap.builder()
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_IRON)).build());
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_GOLD, EntityTierUpgradeMap.builder()
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_GOLD)).build());
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_DIAMOND, EntityTierUpgradeMap.builder()
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_DIAMOND)).build());
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_NETHERITE, EntityTierUpgradeMap.builder()
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.COPPER_TO_NETHERITE)).build());

		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.IRON_TO_GOLD, EntityTierUpgradeMap.builder()
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.IRON_TO_GOLD)).build());
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.IRON_TO_DIAMOND, EntityTierUpgradeMap.builder()
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.IRON_TO_DIAMOND)).build());
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.IRON_TO_NETHERITE, EntityTierUpgradeMap.builder()
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.IRON_TO_NETHERITE)).build());

		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.GOLD_TO_DIAMOND, EntityTierUpgradeMap.builder()
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.GOLD_TO_DIAMOND)).build());
		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.GOLD_TO_NETHERITE, EntityTierUpgradeMap.builder()
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.GOLD_TO_NETHERITE)).build());

		ENTITY_TIER_UPGRADE_DEFINITIONS.put(StorageTierUpgradeItem.TierUpgrade.DIAMOND_TO_NETHERITE, EntityTierUpgradeMap.builder()
				.putAllWrapped(StorageHolderTierUpgradeHandler.STORAGE_HOLDER_TIER_UPGRADE_DEFINITIONS.get(StorageTierUpgradeItem.TierUpgrade.DIAMOND_TO_NETHERITE)).build());

	}
}
