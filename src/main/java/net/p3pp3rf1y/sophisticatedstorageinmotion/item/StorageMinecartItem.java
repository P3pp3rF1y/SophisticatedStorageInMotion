package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.util.ItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorage.block.ITintableBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModDataComponents;

import java.util.function.Consumer;

public class StorageMinecartItem extends ItemBase {
	public StorageMinecartItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public void addCreativeTabItems(Consumer<ItemStack> itemConsumer) {
		if (Config.COMMON.enabledItems.isItemEnabled(this)) {
			itemConsumer.accept(createWithStorage(WoodStorageBlockItem.setWoodType(new ItemStack(ModBlocks.BARREL_ITEM.get()), WoodType.SPRUCE)));
			ItemStack limitedIStack = new ItemStack(ModBlocks.LIMITED_GOLD_BARREL_1_ITEM.get());
			if (limitedIStack.getItem() instanceof ITintableBlockItem tintableBlockItem) {
				tintableBlockItem.setAccentColor(limitedIStack, 0xFF_EEEEEE);
				tintableBlockItem.setMainColor(limitedIStack, 0xFF_222222);
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
		stack.set(ModDataComponents.STORAGE_ITEM, SimpleItemContent.copyOf(storageStack));
		return stack;
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

}
