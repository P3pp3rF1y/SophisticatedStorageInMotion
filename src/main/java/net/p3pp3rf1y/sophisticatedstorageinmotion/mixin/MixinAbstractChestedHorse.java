package net.p3pp3rf1y.sophisticatedstorageinmotion.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.StorageInMotionTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.EntityStorageHolder;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IStorageItemAttachmentHolder;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntities;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Mixin(AbstractChestedHorse.class)
public abstract class MixinAbstractChestedHorse extends AbstractHorse implements IMovingStorageEntity, IStorageItemAttachmentHolder {
	@Shadow
	public abstract boolean hasChest();


	private static final Identifier SADDLE_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/saddle");
	private static final Identifier LLAMA_ARMOR_SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot/llama_armor");
	private static final String STORAGE_HOLDER = "storageHolder";
	@Unique
	private final EntityStorageHolder<MixinAbstractChestedHorse> entityStorageHolder = new EntityStorageHolder<>(this);
	private boolean storageItemSynced = false;

	protected MixinAbstractChestedHorse(EntityType<? extends AbstractChestedHorse> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Shadow
	protected abstract void playChestEquipsSound();

	@Inject(method = "dropEquipment", at = @At("TAIL"))
	private void dropStorageAndItsContents(ServerLevel serverLevel, CallbackInfo ci) {
		entityStorageHolder.onDestroy();
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void addStorageHolderSaveData(ValueOutput out, CallbackInfo ci) {
		if (hasStorageItem()) {
			out.putChild(STORAGE_HOLDER, entityStorageHolder);
		}
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void readStorageHolderSaveData(ValueInput in, CallbackInfo ci) {
		in.child(STORAGE_HOLDER).ifPresent(entityStorageHolder::deserialize);
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
		if (canUseSlot(EquipmentSlot.BODY) && (((AbstractChestedHorse) (Object) this).is(EntityTypeTags.CAN_WEAR_HORSE_ARMOR) || ((Object) this) instanceof Llama)) {
			Container container = createEquipmentSlotContainer(EquipmentSlot.BODY);
			return List.of(new ArmorSlot(container, this, EquipmentSlot.BODY, 0, 8, 36, ((Object) this) instanceof Llama ? LLAMA_ARMOR_SLOT_SPRITE : null) {
				public boolean mayPlace(ItemStack stack) {
					return isEquippableInSlot(stack, EquipmentSlot.BODY);
				}
			});
		} else if (canUseSlot(EquipmentSlot.SADDLE) && ((AbstractChestedHorse) (Object) this).is(EntityTypeTags.CAN_EQUIP_SADDLE)) {
			Container container = createEquipmentSlotContainer(EquipmentSlot.SADDLE);
			return List.of(new ArmorSlot(container, this, EquipmentSlot.SADDLE, 0, 0, 0, SADDLE_SLOT_SPRITE) {
				@Override
				public boolean mayPlace(ItemStack stack) {
					return stack.is(Items.SADDLE) && !hasItem() && canUseSlot(EquipmentSlot.SADDLE) && ((AbstractChestedHorse) (Object) this).is(EntityTypeTags.CAN_EQUIP_SADDLE);
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
		return getData(ModEntities.STORAGE_ITEM_ATTACHMENT);
	}

	@Override
	public void setStorageItem(ItemStack storageItem) {
		setData(ModEntities.STORAGE_ITEM_ATTACHMENT, storageItem);
	}

	@Override
	public EntityStorageHolder<?> getStorageHolder() {
		return entityStorageHolder;
	}

	@Override
	public ItemStack getDropStack(ItemStack storageItem) {
		@Nullable UUID storageId = storageItem.get(ModCoreDataComponents.STORAGE_UUID);
		if (storageId != null) {
			MovingStorageData.moveToItemStorage(level().registryAccess(), storageItem, storageId);
		}
		return storageItem;
	}

	@Override
	public void tick() {
		super.tick();
		if (storageItemSynced) {
			storageItemSynced = false;
			entityStorageHolder.onStorageItemSynced();
		}
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

	@Override
	public void markStorageItemSynced() {
		storageItemSynced = true;
	}
}
