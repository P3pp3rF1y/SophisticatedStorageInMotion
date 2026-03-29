package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageSavedData;
import net.p3pp3rf1y.sophisticatedcore.api.IUpgradeRenderer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SophisticatedMenuProvider;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.client.render.UpgradeRenderRegistry;
import net.p3pp3rf1y.sophisticatedcore.renderdata.IUpgradeRenderData;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.renderdata.UpgradeRenderDataType;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorage.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.entity.StorageHolderBase;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingLimitedBarrelContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.ItemComponentHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.MovingStorageOpennessPayload;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

import com.mojang.math.Axis;
import org.joml.Vector3f;

public class EntityStorageHolder<T extends Entity & IMovingStorageEntity> extends StorageHolderBase {
	private static final int AVERAGE_DROPPED_ITEM_ENTITY_STACK_SIZE = 20;
	private final T entity;

	@Nullable
	private StorageBlockEntity renderBlockEntity = null;

	public EntityStorageHolder(T entity) {
		super(!(entity instanceof AbstractChestedHorse));
		this.entity = entity;
	}

	@Override
	protected Entity getEntity() {
		return entity;
	}

	@Override
	protected boolean isOwnContainer(Player player) {
		if (player.containerMenu instanceof MovingStorageContainerMenu<?> movingStorageContainerMenu) {
			return movingStorageContainerMenu.getStorageEntity().map(e -> e == entity).orElse(false);
		}
		return false;
	}

	public void setStorageItemAndCustomNameFromMovingStorageStack(ItemStack movingStorageStack, boolean setupDefaults) {
		SimpleItemContent storageItemContents = movingStorageStack.get(ModDataComponents.STORAGE_ITEM.get());
		if (storageItemContents == null) {
			ItemStack barrel = new ItemStack(ModBlocks.BARREL_ITEM.get());
			WoodStorageBlockItem.setWoodType(barrel, WoodType.SPRUCE);
			setStorageItem(barrel);
		} else {
			ItemStack storageItem = storageItemContents.copy();
			setStorageItemFrom(storageItem, setupDefaults);
		}
		Component customName = movingStorageStack.get(DataComponents.CUSTOM_NAME);
		if (customName != null) {
			getEntity().setCustomName(customName);
		}
	}

	@Override
	protected float getUpgradeRenderYOffset() {
		if (entity instanceof AbstractChestedHorse) {
			return 1.2f;
		}

		return super.getUpgradeRenderYOffset();
	}

	@Override
	protected UnaryOperator<Vector3f> getUpgradeRenderPosition() {
		if (entity instanceof AbstractChestedHorse chestedHorse) {
			int side = (entity.tickCount / 20) % 2 == 0 ? 1 : -1;
			return vector -> getChestedHorseUpgradeRenderPoint(chestedHorse, side, vector);
		}

		return super.getUpgradeRenderPosition();
	}

	private Vector3f getChestedHorseUpgradeRenderPoint(AbstractChestedHorse chestedHorse, int side, Vector3f vector) {
		Vector3f point = new Vector3f(vector);
		point.rotate(Axis.YN.rotationDegrees(side > 0 ? 90.0F : -90.0F));
		point.add((float) (chestedHorse.getBbWidth() * (chestedHorse instanceof Llama ? 0.75F : 0.62F)) * side,
				(float) (chestedHorse.getBbHeight() * (chestedHorse instanceof Llama ? 0.62F : 0.55F)),
				chestedHorse instanceof Llama ? -0.08F : -0.05F);
		point.rotate(Axis.YN.rotationDegrees(chestedHorse.yBodyRot - 180.0F));
		point.add(chestedHorse.position().toVector3f());
		return point;
	}

	public void setStorageItemFrom(ItemStack storageItem, boolean setupDefaults) {
		setStorageItem(storageItem);
		if (setupDefaults && MovingStorageWrapper.isLimitedBarrel(storageItem)) {
			LimitedBarrelBlockEntity.setFixedSettings(getStorageWrapper(), getStorageWrapper() instanceof MovingStorageWrapper movingStorageWrapper ? movingStorageWrapper.getNumberOfInventorySlots() : getStorageWrapper().getInventoryHandler().getSlots());
			LimitedBarrelBlock.setupDefaultSettings(getStorageWrapper());
		}
	}


	public CompoundTag saveData(HolderLookup.Provider registries) {
		CompoundTag ret = new CompoundTag();
		ItemStack storageItem = entity.getStorageItem();
		if (!storageItem.isEmpty()) {
			ret.put("storageItem", storageItem.save(registries, new CompoundTag()));
		}
		return ret;
	}

	public void readData(HolderLookup.Provider registries, CompoundTag tag) {
		if (tag.contains("storageItem")) {
			setStorageItem(ItemStack.parseOptional(registries, tag.getCompound("storageItem")));
		}
	}

	private void setRenderBlockEntity(StorageBlockEntity storageBlockEntity) {
		renderBlockEntity = storageBlockEntity;
	}

	@Override
	protected void setSyncedStorageStack(ItemStack storageStack) {
		entity.setStorageItem(storageStack);
	}

	@Override
	protected ItemStack getSyncedStorageStack() {
		return entity.getStorageItem();
	}

	@Override
	protected IStorageSavedData getStorageData(UUID storageId) {
		return MovingStorageData.get(storageId);
	}

	@Override
	public boolean isLocked(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.LOCKED, false);
	}

	@Nullable
	@Override
	protected Level getLevel() {
		return entity.level();
	}

	@Override
	protected Vec3 getPosition() {
		return entity.position();
	}

	@Override
	protected CustomPacketPayload createOpennessPayload() {
		return new MovingStorageOpennessPayload(entity.getId(), isOpen());
	}

	@Override
	@Nullable
	protected StorageBlockEntity retrieveRenderBlockEntity() {
		ItemStack storageItem = entity.getStorageItem();
		if (renderBlockEntity == null) {
			if (storageItem.getItem() instanceof BlockItem blockItem) {
				if (blockItem.getBlock() instanceof ChestBlock) {
					renderBlockEntity = new ChestBlockEntity(BlockPos.ZERO, blockItem.getBlock().defaultBlockState());
				} else if (blockItem.getBlock() instanceof LimitedBarrelBlock) {
					renderBlockEntity = new LimitedBarrelBlockEntity(BlockPos.ZERO,
							blockItem.getBlock().defaultBlockState()
									.setValue(LimitedBarrelBlock.HORIZONTAL_FACING, Direction.NORTH)
									.setValue(LimitedBarrelBlock.VERTICAL_FACING, VerticalFacing.UP)
					);
				} else if (blockItem.getBlock() instanceof BarrelBlock) {
					renderBlockEntity = new BarrelBlockEntity(BlockPos.ZERO,
							blockItem.getBlock().defaultBlockState()
									.setValue(BarrelBlock.FACING, Direction.UP)
					);
				} else if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
					renderBlockEntity = new ShulkerBoxBlockEntity(BlockPos.ZERO, blockItem.getBlock().defaultBlockState());
				}
			}

			if (renderBlockEntity == null) {
				renderBlockEntity = new ChestBlockEntity(BlockPos.ZERO, ModBlocks.CHEST.get().defaultBlockState());
			}
			setRenderBlockEntity(renderBlockEntity);
			renderBlockEntity.setLevel(entity.level());
		}
		return renderBlockEntity;
	}

	public void onDestroy() {
		if (entity.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
			if (Config.COMMON.dropPacked.get()) {
				pack();
			}
			ItemStack storageItem = entity.getStorageItem();
			if (!isShulkerBox() && !isPacked(storageItem)) {
				dropAllItems();

				if (storageItem.has(ModCoreDataComponents.STORAGE_UUID)) {
					MovingStorageData.get(storageItem.get(ModCoreDataComponents.STORAGE_UUID)).removeStorageContents();
				}
				storageItem = ItemComponentHelper.cleanUpStack(storageItem);
			}
			ItemStack drop = entity.getDropStack(storageItem);
			if (entity.hasCustomName()) {
				drop.set(DataComponents.CUSTOM_NAME, entity.getCustomName());
			}
			entity.spawnAtLocation(drop);
		}
	}

	private void dropAllItems() {
		InventoryHelper.dropItems(getStorageWrapper().getInventoryHandler(), entity.level(), entity.position().x(), entity.position().y(), entity.position().z());
		InventoryHelper.dropItems(getStorageWrapper().getUpgradeHandler(), entity.level(), entity.position().x(), entity.position().y(), entity.position().z());
	}

	@Override
	protected void setLocked(boolean locked) {
		entity.getStorageItem().set(ModDataComponents.LOCKED, locked);
	}

	public boolean pack() {
		if (isShulkerBox() || isPacked(entity.getStorageItem())) {
			return false;
		}

		ItemStack storageItem = entity.getStorageItem();
		WoodStorageBlockItem.setPacked(storageItem, true);
		setStorageItem(storageItem);

		return true;
	}

	public void onPlace() {
		if (isPacked(entity.getStorageItem())) {
			ItemStack storageItem = entity.getStorageItem();
			WoodStorageBlockItem.setPacked(storageItem, false);
			setStorageItem(storageItem);
		}
	}

	private boolean canBeHurtByWithFeedback(DamageSource source) {
		if (Config.COMMON.dropPacked.get() || isPacked() || !(source.getEntity() instanceof Player player)) {
			return true;
		}

		if (player.isCrouching() || isShulkerBox()) {
			return true;
		}

		AtomicInteger droppedItemEntityCount = new AtomicInteger(0);
		InventoryHelper.iterate(getStorageWrapper().getInventoryHandler(), (slot, stack) -> {
			if (stack.isEmpty()) {
				return;
			}
			droppedItemEntityCount.addAndGet((int) Math.ceil(stack.getCount() / (double) Math.min(stack.getMaxStackSize(), AVERAGE_DROPPED_ITEM_ENTITY_STACK_SIZE)));
		});

		if (droppedItemEntityCount.get() <= Config.SERVER.tooManyItemEntityDrops.get()) {
			return true;
		}

		ItemBase packingTapeItem = ModItems.PACKING_TAPE.get();
		Component packingTapeItemName = packingTapeItem.getName(new ItemStack(packingTapeItem)).copy().withStyle(ChatFormatting.GREEN);
		player.sendSystemMessage(StorageTranslationHelper.INSTANCE.translStatusMessage("too_many_item_entity_drops",
				entity.getName().copy().withStyle(ChatFormatting.GREEN),
				Component.literal(String.valueOf(droppedItemEntityCount.get())).withStyle(ChatFormatting.RED),
				packingTapeItemName)
		);
		return false;
	}

	public boolean hurt(DamageSource source, float amount, BiFunction<DamageSource, Float, Boolean> superHurt) {
		if (canBeHurtByWithFeedback(source) && superHurt.apply(source, amount)) {
			if (source.getEntity() instanceof Player player && player.getAbilities().instabuild && entity.isRemoved()) {
				dropAllItems();
			}
			return true;
		}
		return false;
	}

	@Override
	protected AABB getPickupBoundingBox() {
		return getEntity().getBoundingBox().inflate(0.2D);
	}

	@Override
	protected void openMenu(Player player) {
		player.openMenu(new SophisticatedMenuProvider((w, p, pl) -> createMenu(w, pl), entity.getName(), false), buffer -> buffer.writeInt(entity.getId()));
	}

	public MovingStorageContainerMenu<? extends Entity> createMenu(int id, Player pl) {
		if (MovingStorageWrapper.isLimitedBarrel(entity.getStorageItem())) {
			return new MovingLimitedBarrelContainerMenu<>(id, pl, entity.getId());
		} else {
			return new MovingStorageContainerMenu<>(id, pl, entity.getId());
		}
	}

	@Override
	protected void playSound(SoundEvent sound) {
		entity.level().playSound(null, entity, sound, SoundSource.BLOCKS, 0.5F, entity.level().random.nextFloat() * 0.1F + 0.9F);
	}

	@Override
	protected void refreshRenderBlockEntity() {
		renderBlockEntity = null;
	}
}
