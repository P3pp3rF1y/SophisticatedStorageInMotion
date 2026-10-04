package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageItemClient;

import javax.annotation.Nullable;

public class MovingStorageItemClient {
	@Nullable
	public static TooltipComponent getTooltipImage(ItemStack stack) {
		ItemStack inner = MovingStorageItem.getStorageItem(stack);
		if (StorageBlockEntity.hasLinkedStorageEndpoint(inner)) {
			return StorageItemClient.getTooltipImage(inner);
		}
		Minecraft mc = Minecraft.getInstance();
		if (Screen.hasShiftDown() || (mc.player != null && !mc.player.containerMenu.getCarried().isEmpty())) {
			return new MovingStorageItem.MovingStorageContentsTooltip(stack);
		}
		return null;
	}
}
