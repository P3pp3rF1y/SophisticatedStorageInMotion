package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.p3pp3rf1y.sophisticatedcore.client.render.BlockHighlightRenderHelper;
import net.p3pp3rf1y.sophisticatedcore.util.InventoryHelper;
import net.p3pp3rf1y.sophisticatedcore.util.VoxelOutliner;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.PaintbrushMovingStorageOverlay;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

public class ClientEventHandler {
	private static final int PAINTBRUSH_CAN_APPLY_HIGHLIGHT_COLOR = 0x69c53b;
	private static final int PAINTBRUSH_MISSING_ITEMS_HIGHLIGHT_COLOR = 0xc53b3b;

	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ClientEventHandler::registerClientExtensions);
		modBus.addListener(ClientEventHandler::registerOverlay);
		modBus.addListener(ClientEventHandler::registerTooltipComponent);

		IEventBus eventBus = NeoForge.EVENT_BUS;
		eventBus.addListener(ClientMovingStorageContentsTooltip::onWorldLoad);
		eventBus.addListener(ClientEventHandler::renderLevelStage);
	}

	private static void renderLevelStage(RenderLevelStageEvent event) {
		if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
			return;
		}

		renderPaintbrushEntityHighlight(event.getPoseStack(), event.getPartialTick().getGameTimeDeltaPartialTick(false), event.getCamera().getPosition());
	}

	private static void renderPaintbrushEntityHighlight(PoseStack poseStack, float partialTick, Vec3 cameraPos) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || mc.screen != null || mc.level == null || !(mc.hitResult instanceof EntityHitResult entityHitResult)
				|| !(entityHitResult.getEntity() instanceof IMovingStorageEntity)) {
			return;
		}

		InventoryHelper.getItemFromEitherHand(player, net.p3pp3rf1y.sophisticatedstorage.init.ModItems.PAINTBRUSH.get())
				.flatMap(paintbrush -> PaintbrushMovingStorageOverlay.getItemRequirementsFor(paintbrush, player, entityHitResult.getEntity()))
				.ifPresent(itemRequirements -> renderPaintbrushEntityHighlight(poseStack, partialTick, cameraPos, entityHitResult.getEntity(),
						itemRequirements.itemsMissing().isEmpty()));
	}

	private static void renderPaintbrushEntityHighlight(PoseStack poseStack, float partialTick, Vec3 cameraPos, Entity entity, boolean canApply) {
		AABB boundingBox = entity.getBoundingBox();
		double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
		double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
		double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
		int color = canApply ? PAINTBRUSH_CAN_APPLY_HIGHLIGHT_COLOR : PAINTBRUSH_MISSING_ITEMS_HIGHLIGHT_COLOR;
		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		poseStack.pushPose();
		poseStack.translate(x - cameraPos.x(), y - cameraPos.y(), z - cameraPos.z());
		BlockHighlightRenderHelper.renderThickEdges(poseStack, buffer, color, VoxelOutliner.edgesFromAABB(boundingBox), entity.getX(), entity.getY(),
				entity.getZ());
		poseStack.popPose();
	}

	private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
		event.registerItem(StorageMinecartItemRenderer.getItemRenderProperties(), ModItems.STORAGE_MINECART.get());
		event.registerItem(StorageBoatItemRenderer.getItemRenderProperties(), ModItems.STORAGE_BOAT.get());
	}

	private static void registerOverlay(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.HOTBAR,
				ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "paintbrush_moving_storage_info"),
				PaintbrushMovingStorageOverlay.HUD_PAINTBRUSH_INFO);
	}

	private static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(MovingStorageItem.MovingStorageContentsTooltip.class, ClientMovingStorageContentsTooltip::new);
	}
}
