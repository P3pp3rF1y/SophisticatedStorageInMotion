package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.client.render.BlockHighlightRenderHelper;
import net.p3pp3rf1y.sophisticatedcore.client.render.IClientHighlightHandler;
import net.p3pp3rf1y.sophisticatedcore.client.render.ItemInStorageHighlightRenderer;
import net.p3pp3rf1y.sophisticatedcore.util.Easing;
import net.p3pp3rf1y.sophisticatedcore.util.VoxelOutliner;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.MovingStorageItemActionPayloadHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;

import java.util.Collections;
import java.util.List;

public class MovingStorageHighlightHandler implements IClientHighlightHandler<List<Integer>> {
	private MovingStorageHighlightHandler() {
	}

	public static final MovingStorageHighlightHandler INSTANCE = new MovingStorageHighlightHandler();

	private List<Integer> highlightedStackEntityIds = Collections.emptyList();
	private List<Integer> highlightedItemEntityIds = Collections.emptyList();

	public void setHighlightedEntities(List<Integer> stackEntityIds, List<Integer> itemEntityIds) {
		highlightedStackEntityIds = stackEntityIds;
		highlightedItemEntityIds = itemEntityIds;
	}

	@Override
	public ResourceLocation getPayloadHandlerId() {
		return MovingStorageItemActionPayloadHandler.ID;
	}

	@Override
	public List<Integer> buildClientRequestData(Player player) {
		return player.level().getEntities(player,
				player.getBoundingBox().inflate(ItemInStorageHighlightRenderer.HIGHLIGHT_RANGE),
				e -> e instanceof IMovingStorageEntity && e.distanceTo(player) <= ItemInStorageHighlightRenderer.HIGHLIGHT_RANGE
		).stream().map(Entity::getId).toList();
	}

	@Override
	public void clearCache() {
		highlightedStackEntityIds = Collections.emptyList();
		highlightedItemEntityIds = Collections.emptyList();
	}

	@Override
	public void render(PoseStack poseStack, float partialTick, Vec3 cameraPos) {
		highlightedStackEntityIds.forEach(eId -> renderHighlightedEntity(poseStack, partialTick, cameraPos, eId, Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource(), ItemInStorageHighlightRenderer.MATCHING_STACK_HIGHLIGHT_COLOR));
		highlightedItemEntityIds.forEach(eId -> renderHighlightedEntity(poseStack, partialTick, cameraPos, eId, Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource(), ItemInStorageHighlightRenderer.MATCHING_ITEM_HIGHLIGHT_COLOR));
	}

	private static void renderHighlightedEntity(PoseStack poseStack, float partialTick, Vec3 cameraPos, int entityId, Minecraft mc, MultiBufferSource.BufferSource buffer, int color) {
		Entity entity = mc.level.getEntity(entityId);
		if (entity == null) {
			return;
		}

		AABB boundingBox = entity.getBoundingBox();
		double x = Mth.lerp(partialTick, entity.xOld, entity.getX());
		double y = Mth.lerp(partialTick, entity.yOld, entity.getY());
		double z = Mth.lerp(partialTick, entity.zOld, entity.getZ());
		poseStack.pushPose();
		double halfH = boundingBox.getYsize() * 0.5;
		poseStack.translate(x - cameraPos.x(), y - cameraPos.y(), z - cameraPos.z());
		poseStack.translate(0, halfH, 0);
		float scale = 1 + Easing.EASE_IN_OUT_CUBIC.ease((float) ItemInStorageHighlightRenderer.tri01(mc.level.getGameTime(), 15, partialTick)) * 0.05f;
		poseStack.scale(scale, scale, scale);
		poseStack.translate(0, -halfH, 0);
		BlockHighlightRenderHelper.renderThickEdges(poseStack, buffer, color, VoxelOutliner.edgesFromAABB(boundingBox), entity.getX(), entity.getY(), entity.getZ());
		poseStack.popPose();
	}
}
