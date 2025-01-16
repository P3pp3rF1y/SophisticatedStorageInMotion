package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltipBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.MovingStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.network.RequestMovingStorageInventoryContentsPayload;

import java.util.UUID;

public class ClientMovingStorageContentsTooltip extends ClientStorageContentsTooltipBase {
	private final ItemStack movingStorage;

	@SuppressWarnings("unused")
	//parameter needs to be there so that addListener logic would know which event this method listens to
	public static void onWorldLoad(LevelEvent.Load event) {
		refreshContents();
		lastRequestTime = 0;
	}

	@Override
	public void renderImage(Font font, int leftX, int topY, GuiGraphics guiGraphics) {
		renderTooltip(MovingStorageWrapper.fromStack(movingStorage, () -> {}, () -> {}), font, leftX, topY, guiGraphics);
	}

	public ClientMovingStorageContentsTooltip(MovingStorageItem.MovingStorageContentsTooltip tooltip) {
		movingStorage = tooltip.getMovingStorage();
	}

	@Override
	protected void sendInventorySyncRequest(UUID uuid) {
		PacketDistributor.sendToServer(new RequestMovingStorageInventoryContentsPayload(uuid));
	}
}
