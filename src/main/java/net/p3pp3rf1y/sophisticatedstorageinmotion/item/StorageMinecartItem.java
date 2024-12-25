package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.block.ITintableBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.block.ItemContentsStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.*;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageData;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;

import java.util.Optional;
import java.util.function.Consumer;

public class StorageMinecartItem extends ItemBase {
	public StorageMinecartItem() {
		super(new Properties().stacksTo(1));
	}

	public static ItemStack getStorageItem(ItemStack stack) {
		return stack.getOrDefault(ModDataComponents.STORAGE_ITEM, SimpleItemContent.EMPTY).copy();
	}

	@Override
	public void onCraftedPostProcess(ItemStack stack, Level level) {
		super.onCraftedPostProcess(stack, level);
		if (level.isClientSide()) {
			return;
		}
		ItemStack storageItem = getStorageItem(stack);
		if (storageItem.getItem() instanceof ShulkerBoxItem) {
			StackStorageWrapper shulkerStorageWrapper = StackStorageWrapper.fromStack(level.registryAccess(), storageItem);
			shulkerStorageWrapper.getContentsUuid().ifPresent(id -> {
				ItemContentsStorage itemContentsStorage = ItemContentsStorage.get();
				CompoundTag contentsNbt = itemContentsStorage.getOrCreateStorageContents(id).getCompound(StorageBlockEntity.STORAGE_WRAPPER_TAG);
				CompoundTag migratedContentsNbt = new CompoundTag();
				migratedContentsNbt.put(StorageWrapper.CONTENTS_TAG, contentsNbt.getCompound(StorageWrapper.CONTENTS_TAG));
				migratedContentsNbt.put(StorageWrapper.SETTINGS_TAG, contentsNbt.getCompound(StorageWrapper.SETTINGS_TAG));
				MovingStorageData.get(id).setContents(migratedContentsNbt);
				storageItem.set(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.of(contentsNbt.getCompound(StorageWrapper.RENDER_INFO_TAG)));
				setStorageItem(storageItem, stack);
				itemContentsStorage.removeStorageContents(id);
			});
			setStorageItem(storageItem, stack);
		}
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Config.COMMON.enabledItems.isItemEnabled(this)) {
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL_ITEM.get()), WoodType.SPRUCE)));
			ItemStack limitedIStack = new ItemStack(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get());
			if (limitedIStack.getItem() instanceof ITintableBlockItem tintableBlockItem) {
				tintableBlockItem.setMainColor(limitedIStack, DyeColor.YELLOW.getTextureDiffuseColor());
				tintableBlockItem.setAccentColor(limitedIStack, DyeColor.LIME.getTextureDiffuseColor());
			}
			itemConsumer.accept(createWithStorage(limitedIStack));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_COPPER_BARREL_2.get()), WoodType.BIRCH)));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_IRON_BARREL_3.get()), WoodType.ACACIA)));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.LIMITED_DIAMOND_BARREL_4.get()), WoodType.CRIMSON)));
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.NETHERITE_CHEST_ITEM.get()), WoodType.BAMBOO)));
			itemConsumer.accept(createWithStorage(new ItemStack(ModBlocks.IRON_SHULKER_BOX_ITEM.get())));
		}
	}


	private ItemStack createWithStorage(ItemStack storageStack) {
		ItemStack stack = new ItemStack(this);
		setStorageItem(storageStack, stack);
		return stack;
	}

	public static void setStorageItem(ItemStack storageItem, ItemStack movingStorageItem) {
		movingStorageItem.set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(storageItem));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos blockpos = context.getClickedPos();
		BlockState blockstate = level.getBlockState(blockpos);
		if (!blockstate.is(BlockTags.RAILS)) {
			return InteractionResult.FAIL;
		} else {
			ItemStack stack = context.getItemInHand();
			if (level instanceof ServerLevel serverlevel) {
				RailShape railshape = blockstate.getBlock() instanceof BaseRailBlock baseRailBlock ? baseRailBlock.getRailDirection(blockstate, level, blockpos, null) : RailShape.NORTH_SOUTH;
				double ascendingOffset = 0.0;
				if (railshape.isAscending()) {
					ascendingOffset = 0.5;
				}

				StorageMinecart minecart = new StorageMinecart(level, blockpos.getX() + 0.5, blockpos.getY() + 0.0625 + ascendingOffset, blockpos.getZ() + 0.5);
				minecart.getStorageHolder().setStorageItemFrom(stack);
				EntityType.createDefaultStackConfig(serverlevel, stack, context.getPlayer()).accept(minecart);

				serverlevel.addFreshEntity(minecart);
				serverlevel.gameEvent(GameEvent.ENTITY_PLACE, blockpos, GameEvent.Context.of(context.getPlayer(), serverlevel.getBlockState(blockpos.below())));
			}

			stack.shrink(1);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	@Override
	public Component getName(ItemStack stack) {
		SimpleItemContent storageItemContent = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContent != null ? Component.translatable(getDescriptionId(), storageItemContent.copy().getHoverName()) : super.getName(stack);
	}

	public static Optional<Item> getStorageItemType(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : Optional.of(storageItemContents.getItem());
	}

	public static Optional<WoodType> getStorageItemWoodType(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : WoodStorageBlockItem.getWoodType(storageItemContents);
	}

	public static Optional<Integer> getStorageItemMainColor(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : StorageBlockItem.getMainColorFromComponentHolder(storageItemContents);
	}

	public static Optional<Integer> getStorageItemAccentColor(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents == null ? Optional.empty() : StorageBlockItem.getAccentColorFromComponentHolder(storageItemContents);
	}

	public static boolean isStorageItemFlatTopBarrel(ItemStack stack) {
		SimpleItemContent storageItemContents = stack.get(ModDataComponents.STORAGE_ITEM);
		return storageItemContents != null && BarrelBlockItem.isFlatTop(storageItemContents);
	}
}
