package net.p3pp3rf1y.sophisticatedstorageinmotion.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
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

import javax.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mixin(AbstractChestedHorse.class)
public abstract class MixinAbstractChestedHorse extends AbstractHorse implements IMovingStorageEntity {
	@Shadow
	public abstract boolean hasChest();

	private static final ResourceLocation SADDLE_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/saddle");
	private static final ResourceLocation LLAMA_ARMOR_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/llama_armor");
	private static final String STORAGE_HOLDER_TAG = "storageHolder";
	@Unique
	private final EntityStorageHolder<MixinAbstractChestedHorse> entityStorageHolder = new EntityStorageHolder<>(this);
	@Unique
	private static final EntityDataAccessor<ItemStack> DATA_STORAGE_ITEM = SynchedEntityData.defineId(MixinAbstractChestedHorse.class,
			EntityDataSerializers.ITEM_STACK);

	protected MixinAbstractChestedHorse(EntityType<? extends AbstractChestedHorse> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Shadow
	protected abstract void playChestEquipsSound();

	@Inject(method = "defineSynchedData", at = @At("TAIL"))
	private void defineSynchedStorageItem(SynchedEntityData.Builder builder, CallbackInfo ci) {
		builder.define(DATA_STORAGE_ITEM, ItemStack.EMPTY);
	}

	@Inject(method = "dropEquipment", at = @At("TAIL"))
	private void dropStorageAndItsContents(ServerLevel serverLevel, CallbackInfo ci) {
		entityStorageHolder.onDestroy();
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void addStorageHolderSaveData(CompoundTag tag, CallbackInfo ci) {
		if (hasStorageItem()) {
			tag.put(STORAGE_HOLDER_TAG, entityStorageHolder.saveData(level().registryAccess()));
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void readStorageHolderSaveData(CompoundTag tag, CallbackInfo ci) {
		if (tag.contains(STORAGE_HOLDER_TAG)) {
			entityStorageHolder.readData(level().registryAccess(), tag.getCompoundOrEmpty(STORAGE_HOLDER_TAG));
		}
	}

	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		ItemStack stack = player.getItemInHand(hand);
		if (isTamed() && (stack.getItem() instanceof StorageBlockItem) && !hasStorageItem() && !hasChest()) {
			ItemStack stackCopy = stack.copyWithCount(1);
			entityStorageHolder.setStorageItemFrom(stackCopy, true);
			playChestEquipsSound();
			stack.consume(1, player);
			cir.cancel();
			cir.setReturnValue(InteractionResult.SUCCESS);
		} else if (isTamed() && hasStorageItem() && stack.is(Items.CHEST)) {
			cir.cancel();
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}

	@Override
	public List<Slot> instantiateExtraSlots() {
		if (canUseSlot(EquipmentSlot.BODY) && (getType().is(EntityTypeTags.CAN_WEAR_HORSE_ARMOR) || ((Object) this) instanceof Llama)) {
			Container container = createEquipmentSlotContainer(EquipmentSlot.BODY);
			return List.of(new ArmorSlot(container, this, EquipmentSlot.BODY, 0, 8, 36, ((Object) this) instanceof Llama ? LLAMA_ARMOR_SLOT_SPRITE : null) {
				public boolean mayPlace(ItemStack stack) {
					return isEquippableInSlot(stack, EquipmentSlot.BODY);
				}
			});
		} else if (canUseSlot(EquipmentSlot.SADDLE) && getType().is(EntityTypeTags.CAN_EQUIP_SADDLE)) {
			Container container = createEquipmentSlotContainer(EquipmentSlot.SADDLE);
			return List.of(new ArmorSlot(container, this, EquipmentSlot.SADDLE, 0, 0, 0, SADDLE_SLOT_SPRITE) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return stack.is(Items.SADDLE) && !hasItem() && canUseSlot(EquipmentSlot.SADDLE) && getType().is(EntityTypeTags.CAN_EQUIP_SADDLE);
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
		@Nullable
		UUID storageId = storageItem.get(ModCoreDataComponents.STORAGE_UUID);
		if (storageId != null) {
			MovingStorageData.moveToItemStorage(storageItem, storageId);
		}
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
			return Component.translatable(StorageInMotionTranslationHelper.INSTANCE.translEntity("chested_horse_with_storage"), super.getTypeName(),
					getStorageItem().getHoverName());
		}
		return super.getTypeName();
	}
}
