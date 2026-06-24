package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemHandler;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.StorageInMotionTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;

import java.util.Locale;

public class StorageBoat extends ChestBoat implements IMovingStorageEntity {
	static final EntityDataAccessor<ItemStack> DATA_STORAGE_ITEM = SynchedEntityData.defineId(StorageBoat.class, EntityDataSerializers.ITEM_STACK);
	static final EntityDataAccessor<WoodType> DATA_WOOD_TYPE = SynchedEntityData.defineId(StorageBoat.class, ModEntities.WOOD_TYPE_SERIALIZER.get());

	private final EntityStorageHolder<StorageBoat> entityStorageHolder;

	public StorageBoat(EntityType<StorageBoat> entityType, Level level) {
		super(entityType, level, () -> Items.ACACIA_BOAT);
		entityStorageHolder = new EntityStorageHolder<>(this);
	}

	public StorageBoat(Level level) {
		this(ModEntities.STORAGE_BOAT.get(), level);
	}

	public StorageBoat(Level level, double x, double y, double z) {
		this(level);
		setPos(x, y, z);
		xo = x;
		yo = y;
		zo = z;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_STORAGE_ITEM, ItemStack.EMPTY);
		builder.define(DATA_WOOD_TYPE, WoodType.OAK);
	}

	@Override
	public ItemStack getStorageItem() {
		return entityData.get(DATA_STORAGE_ITEM);
	}

	@Override
	public void setStorageItem(ItemStack storageItem) {
		entityData.set(DATA_STORAGE_ITEM, storageItem.copy());
	}

	@Override
	public EntityStorageHolder<?> getStorageHolder() {
		return entityStorageHolder;
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource source, float amount) {
		return getStorageHolder().hurt(serverLevel, source, amount, super::hurtServer);
	}

	@Override
	public void destroy(ServerLevel serverLevel, DamageSource source) {
		kill(serverLevel);
		getStorageHolder().onDestroy();
	}

	@Override
	public ItemStack getDropStack(ItemStack storageItem) {
		ItemStack drop = getDropStack();
		drop.set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(storageItem));
		return drop;
	}

	private ItemStack getDropStack() {
		return StorageBoatItem.setWoodType(new ItemStack(ModItems.STORAGE_BOAT.get()), getWoodType());
	}

	@Override
	public ItemStack getPickResult() {
		ItemStack result = getDropStack();
		ItemStack storageItemCopy = getStorageItem().copy();
		storageItemCopy.remove(ModCoreDataComponents.STORAGE_UUID);
		result.set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(storageItemCopy));
		return result;
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput out) {
		super.addAdditionalSaveData(out);
		out.putChild("storageHolder", entityStorageHolder);
		out.store("woodType", WoodType.CODEC, getWoodType());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput in) {
		super.readAdditionalSaveData(in);
		in.child("storageHolder").ifPresent(entityStorageHolder::deserialize);
		entityData.set(DATA_WOOD_TYPE, in.read("woodType", WoodType.CODEC).orElse(WoodType.OAK));
	}

	@Override
	public void tick() {
		super.tick();
		entityStorageHolder.tick(this);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (key == DATA_STORAGE_ITEM && level().isClientSide()) {
			entityStorageHolder.onStorageItemSynced();
		}
	}

	private Component getWoodName(WoodType type) {
		return Component.translatable("wood_name." + SophisticatedStorage.MOD_ID + "." + type.name().toLowerCase(Locale.ROOT));
	}

	@Override
	protected Component getTypeName() {
		String boatDescId = isRaft() ? "storage_raft" : "storage_boat";
		return Component.translatable(StorageInMotionTranslationHelper.INSTANCE.translEntity(boatDescId), getWoodName(getWoodType()),
				getStorageItem().getHoverName());
	}

	public WoodType getWoodType() {
		return entityData.get(DATA_WOOD_TYPE);
	}

	public void setWoodType(WoodType woodType) {
		entityData.set(DATA_WOOD_TYPE, woodType);
	}

	public boolean isRaft() {
		return getWoodType() == WoodType.BAMBOO;
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		if (!player.isSecondaryUseActive()) {
			InteractionResult result = super.interact(player, hand);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}

		if (canAddPassenger(player) && !player.isSecondaryUseActive()) {
			return InteractionResult.PASS;
		} else {
			if (player instanceof ServerPlayer serverPlayer) {
				return getStorageHolder().openContainerMenu(serverPlayer);
			}
		}
		return InteractionResult.CONSUME;
	}

	@Override
	public InteractionResult interactWithContainerVehicle(Player player) {
		getStorageHolder().openMenu(player);
		return !player.level().isClientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
		return getStorageHolder().createMenu(id, player);
	}

	@Override
	public void openCustomInventoryScreen(Player player) {
		getStorageHolder().openContainerMenu(player);
	}

	@Override
	public void remove(RemovalReason pReason) {
		// overridden to prevent default boat logic from using container overrides to drop items when in some cases they are not supposed to be dropped
		setRemoved(pReason);
	}

	@Override
	public int getContainerSize() {
		return getStorageHolder().getStorageWrapper().getInventoryForInputOutput().getSlots();
	}

	@Override
	public NonNullList<ItemStack> getItemStacks() {
		return NonNullList.create();
	}

	@Override
	public void addChestVehicleSaveData(ValueOutput out) {
		getLootTable().ifPresent(lootTable -> {
			out.putString("LootTable", lootTable.location().toString());
			if (getContainerLootTableSeed() != 0L) {
				out.putLong("LootTableSeed", getContainerLootTableSeed());
			}
		});
	}

	@Override
	public void readChestVehicleSaveData(ValueInput in) {
		clearItemStacks();
		in.getString("LootTable").ifPresent(lootTable -> setContainerLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(lootTable))));
		in.getLong("LootTableSeed").ifPresent(this::setContainerLootTableSeed);
	}

	@Override
	public void chestVehicleDestroyed(DamageSource damageSource, ServerLevel level, Entity p_entity) {
		// noop
	}

	@Override
	public void clearChestVehicleContent() {
		unpackChestVehicleLootTable(null);
		InventoryHandler inventoryHandler = getStorageHolder().getStorageWrapper().getInventoryHandler();
		for (int slot = 0; slot < inventoryHandler.getSlots(); slot++) {
			inventoryHandler.setStackInSlot(slot, ItemStack.EMPTY);
		}
	}

	@Override
	public ItemStack removeChestVehicleItemNoUpdate(int slot) {
		ITrackedContentsItemHandler inventoryHandler = getStorageHolder().getStorageWrapper().getInventoryForInputOutput();
		return inventoryHandler.extractItem(slot, inventoryHandler.getStackInSlot(slot).getCount(), false);
	}

	@Override
	public ItemStack getChestVehicleItem(int slot) {
		return getStorageHolder().getStorageWrapper().getInventoryForInputOutput().getStackInSlot(slot);
	}

	@Override
	public ItemStack removeChestVehicleItem(int slot, int amount) {
		return getStorageHolder().getStorageWrapper().getInventoryForInputOutput().extractItem(slot, amount, false);
	}

	@Override
	public void setChestVehicleItem(int slot, ItemStack stack) {
		getStorageHolder().getStorageWrapper().getInventoryForInputOutput().setStackInSlot(slot, stack);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		return getStorageHolder().getStorageWrapper().getInventoryForInputOutput().isItemValid(slot, stack);
	}

	@Override
	public SlotAccess getChestVehicleSlot(int index) {
		return SlotAccess.NULL;
	}
}
