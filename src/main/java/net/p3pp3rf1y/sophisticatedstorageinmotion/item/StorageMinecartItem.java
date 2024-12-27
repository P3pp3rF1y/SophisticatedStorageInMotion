package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageMinecart;

public class StorageMinecartItem extends MovingStorageItem {
	public StorageMinecartItem() {
		super(new Properties().stacksTo(1));
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
	public ItemStack getUncraftRemainingItem() {
		return new ItemStack(Items.MINECART);
	}
}
