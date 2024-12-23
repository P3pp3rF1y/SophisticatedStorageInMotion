package net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.LimitedBarrelScreen;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;

public class MovingLimitedBarrelScreen extends MovingStorageScreen {
	public static final int STORAGE_SLOTS_HEIGHT = 82;

	public MovingLimitedBarrelScreen(MovingStorageContainerMenu<?> menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	protected void drawSlotBg(GuiGraphics guiGraphics, int x, int y, int visibleSlotsCount) {
		LimitedBarrelScreen.drawSlotBg(this, guiGraphics, x, y, getMenu().getNumberOfStorageInventorySlots());
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.renderLabels(guiGraphics, mouseX, mouseY);
		LimitedBarrelScreen.renderBars(font, imageWidth, getMenu(), guiGraphics, getMenu()::getSlotFillPercentage);
	}

	@Override
	protected int getStorageInventoryHeight(int displayableNumberOfRows) {
		return STORAGE_SLOTS_HEIGHT;
	}

	@Override
	protected void updateStorageSlotsPositions() {
		LimitedBarrelScreen.updateSlotPositions(getMenu(), getMenu().getNumberOfStorageInventorySlots(), imageWidth);
	}

	@Override
	protected boolean shouldShowSortButtons() {
		return false;
	}

	@Override
	protected void addSearchBox() {
		// No search box for limited barrels
	}
}
