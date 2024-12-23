package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StorageMinecart extends MinecartChest implements IMovingStorageEntity {
	private static final EntityDataAccessor<Optional<Component>> DATA_CUSTOM_NAME = SynchedEntityData.defineId(StorageMinecart.class, EntityDataSerializers.OPTIONAL_COMPONENT);
	static final EntityDataAccessor<ItemStack> DATA_STORAGE_ITEM = SynchedEntityData.defineId(StorageMinecart.class, EntityDataSerializers.ITEM_STACK);

	private final EntityStorageHolder<StorageMinecart> entityStorageHolder;

	public StorageMinecart(EntityType<StorageMinecart> entityType, Level level) {
		super(entityType, level);
		entityStorageHolder = new EntityStorageHolder<>(this);
	}

	private StorageMinecart(Level level) {
		this(ModEntities.STORAGE_MINECART.get(), level);
	}


	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_STORAGE_ITEM, ItemStack.EMPTY);
		builder.define(DATA_CUSTOM_NAME, Optional.empty());
	}

	@Override
	public ItemStack getStorageItem() {
		return this.entityData.get(DATA_STORAGE_ITEM);
	}

	@Override
	public void setStorageItem(ItemStack storageItem) {
		this.entityData.set(DATA_STORAGE_ITEM, storageItem.copy());
	}

	@Override
	public EntityStorageHolder<?> getStorageHolder() {
		return entityStorageHolder;
	}

	public StorageMinecart(Level level, double x, double y, double z) {
		this(level);
		this.setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}

	@Override
	public void destroy(DamageSource source) {
		this.kill();
		if (level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			ItemStack drop = new ItemStack(ModItems.STORAGE_MINECART.get());
			drop.set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(getStorageItem()));
			drop.set(DataComponents.CUSTOM_NAME, getCustomName());
			spawnAtLocation(drop);
			entityStorageHolder.dropAllItems();
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.put("storageHolder", entityStorageHolder.saveData(level().registryAccess()));
	}


	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityStorageHolder.readData(level().registryAccess(), tag.getCompound("storageHolder"));
	}

	@Override
	public void tick() {
		super.tick();
		entityStorageHolder.tick();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (key == DATA_STORAGE_ITEM && level().isClientSide()) {
			entityStorageHolder.onStorageItemSynced();
		}
	}

	@Override
	public void setCustomName(@Nullable Component customName) {
		entityData.set(DATA_CUSTOM_NAME, Optional.ofNullable(customName));
	}

	@Override
	public Component getCustomName() {
		return entityData.get(DATA_CUSTOM_NAME).orElseGet(() -> getStorageItem().getHoverName());
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		return getStorageHolder().openContainerMenu(player, this);
	}

	@Override
	public InteractionResult interactWithContainerVehicle(Player player) {
		return super.interactWithContainerVehicle(player);
	}
}
