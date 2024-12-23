package net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;

public class MovingStorageScreen extends StorageScreenBase<MovingStorageContainerMenu<?>> {
	public static MovingStorageScreen constructScreen(MovingStorageContainerMenu<?> screenContainer, Inventory inv, Component title) {
		return new MovingStorageScreen(screenContainer, inv, title);
	}

	protected MovingStorageScreen(MovingStorageContainerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	protected String getStorageSettingsTabTooltip() {
		return StorageTranslationHelper.INSTANCE.translGui("settings.tooltip");
	}
}
