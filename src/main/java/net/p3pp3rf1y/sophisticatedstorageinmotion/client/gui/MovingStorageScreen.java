package net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.player.Inventory;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.GuiHelper;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageTranslationHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;

public class MovingStorageScreen extends StorageScreenBase<MovingStorageContainerMenu<?>> {
	public static final int HORSE_VIEW_PADDING = 7;
	public static final int HORSE_VIEW_SIZE = 52;
	public static final int HORSE_WIDGET_WIDTH = HORSE_VIEW_SIZE + HORSE_VIEW_PADDING * 2 + 4 + 18;

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

	@Override
	protected void updateExtraSlotsPositions() {
		super.updateExtraSlotsPositions();

		getMenu().getStorageEntity().ifPresent(entity -> {
			if (entity instanceof AbstractChestedHorse) {
				getMenu().getExtraSlots().forEach(slot -> {
					if (slot.isActive()) {
						slot.x = inventoryLabelX - 90;
						slot.y = inventoryLabelY + 70;
					}

				});
			}
		});
	}

	@Override
	protected void extractBg(GuiGraphicsExtractor guiGraphics, float partialTicks, int mouseX, int mouseY) {
		getMenu().getStorageEntity().ifPresent(entity -> {
			if (entity instanceof AbstractChestedHorse horse) {
				int y = getHorseControlY();
				int x = getHorseControlX();
				int horseWidgetHeight = HORSE_VIEW_SIZE + HORSE_VIEW_PADDING * 2;

				GuiHelper.renderControlBackground(guiGraphics, x, y, HORSE_WIDGET_WIDTH, horseWidgetHeight, 128, 0, 128, 256);

				int entityViewX = x + HORSE_WIDGET_WIDTH - HORSE_VIEW_PADDING - HORSE_VIEW_SIZE;
				int entityViewY = y + HORSE_VIEW_PADDING;
				guiGraphics.fill(entityViewX, entityViewY, entityViewX + HORSE_VIEW_SIZE, entityViewY + HORSE_VIEW_SIZE, 0xFF_000000);
				InventoryScreen.extractEntityInInventoryFollowsMouse(guiGraphics, entityViewX, entityViewY, entityViewX + HORSE_VIEW_SIZE, entityViewY + HORSE_VIEW_SIZE, 17, 0.25F, mouseX, mouseY, horse);
			}
		});
		super.extractBg(guiGraphics, partialTicks, mouseX, mouseY);
	}

	private int getHorseControlY() {
		return getTopY() + inventoryLabelY + 28;
	}

	private int getHorseControlX() {
		return getLeftX() + inventoryLabelX - HORSE_WIDGET_WIDTH - 10;
	}

	public Rect2i getHorseControlRectangle() {
		return new Rect2i(getHorseControlX(), getHorseControlY(), 128, 256);
	}
}
