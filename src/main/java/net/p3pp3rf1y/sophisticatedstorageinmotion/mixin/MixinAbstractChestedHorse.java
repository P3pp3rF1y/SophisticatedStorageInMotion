package net.p3pp3rf1y.sophisticatedstorageinmotion.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.StorageInMotionTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.EntityStorageHolder;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(AbstractChestedHorse.class)
public abstract class MixinAbstractChestedHorse extends AbstractHorse implements IMovingStorageEntity {
	@Shadow
	public abstract boolean hasChest();

	@Unique
	private static final String STORAGE_HOLDER_TAG = "storageHolder";
	@Unique
	private final EntityStorageHolder<MixinAbstractChestedHorse> entityStorageHolder = new EntityStorageHolder<>(this);
	@Unique
	private static final EntityDataAccessor<ItemStack> DATA_STORAGE_ITEM = SynchedEntityData.defineId(MixinAbstractChestedHorse.class, EntityDataSerializers.ITEM_STACK);

	protected MixinAbstractChestedHorse(EntityType<? extends AbstractChestedHorse> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Shadow
	protected abstract void playChestEquipsSound();

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void defineSynchedStorageItem(CallbackInfo ci) {
		entityData.define(DATA_STORAGE_ITEM, ItemStack.EMPTY);
	}

	@Inject(method = "dropEquipment", at = @At("TAIL"))
	private void dropStorageAndItsContents(CallbackInfo ci) {
		entityStorageHolder.onDestroy();
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void addStorageHolderSaveData(CompoundTag tag, CallbackInfo ci) {
		if (hasStorageItem()) {
			tag.put(STORAGE_HOLDER_TAG, entityStorageHolder.saveData());
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void readStorageHolderSaveData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains(STORAGE_HOLDER_TAG)) {
			entityStorageHolder.readData(tag.getCompound(STORAGE_HOLDER_TAG));
		}
	}

	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack stack = player.getItemInHand(hand);
		if (isTamed() && (stack.getItem() instanceof StorageBlockItem) && !hasStorageItem() && !hasChest()) {
			ItemStack stackCopy = stack.copyWithCount(1);
			entityStorageHolder.setStorageItemFrom(stackCopy, true);
			playChestEquipsSound();
			stack.shrink(1);
			cir.cancel();
			cir.setReturnValue(InteractionResult.sidedSuccess(level().isClientSide));
		} else if (isTamed() && hasStorageItem() && stack.is(Items.CHEST)) {
			cir.cancel();
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Override
	public List<Slot> instantiateExtraSlots() {
		if (canWearArmor()) {
			return List.of(new Slot(inventory, 1, 8, 36) {
				public boolean mayPlace(ItemStack stack) {
					return isArmor(stack);
				}
			});
		} else if (isSaddleable()) {
			return List.of(new Slot(inventory, 0, 0, 0) {
				public boolean mayPlace(ItemStack stack) {
					return stack.is(Items.SADDLE) && !this.hasItem() && isSaddleable();
				}
			});
		}

		return Collections.emptyList();
	}

	private boolean hasStorageItem() {
		return !getStorageItem().isEmpty();
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
	public ItemStack getDropStack(ItemStack storageItem) {
		NBTHelper.getUniqueId(storageItem, StorageWrapper.UUID_TAG).ifPresent(storageId-> MovingStorageData.moveToItemStorage(storageItem, storageId));
		return storageItem;
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (key == DATA_STORAGE_ITEM && level().isClientSide()) {
			entityStorageHolder.onStorageItemSynced();
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (hasStorageItem()) {
			entityStorageHolder.tick(this);
		}
	}

	@Override
	protected Component getTypeName() {
		if (hasStorageItem()) {
			return Component.translatable(StorageInMotionTranslationHelper.INSTANCE.translEntity("chested_horse_with_storage"), super.getTypeName(), getStorageItem().getHoverName());
		}
		return super.getTypeName();
	}
}
