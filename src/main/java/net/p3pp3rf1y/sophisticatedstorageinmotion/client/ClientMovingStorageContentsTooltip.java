package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.p3pp3rf1y.sophisticatedcore.client.render.ClientStorageContentsTooltipBase;
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

	public void renderImage(Font font, int leftX, int topY, int width, int height, GuiGraphicsExtractor guiGraphics) {
		extractTooltip(MovingStorageItem.getMovingStorageWrapper(movingStorage), font, leftX, topY, guiGraphics);
	}

	public ClientMovingStorageContentsTooltip(MovingStorageItem.MovingStorageContentsTooltip tooltip) {
		movingStorage = tooltip.getMovingStorage();
	}

	@Override
	protected void sendInventorySyncRequest(UUID uuid) {
		ClientPacketDistributor.sendToServer(new RequestMovingStorageInventoryContentsPayload(uuid));
	}
}
