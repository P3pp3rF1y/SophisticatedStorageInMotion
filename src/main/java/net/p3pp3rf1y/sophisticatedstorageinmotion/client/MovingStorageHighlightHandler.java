package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.resources.Identifier;
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
import java.util.Optional;

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
	public Identifier getPayloadHandlerId() {
		return MovingStorageItemActionPayloadHandler.ID;
	}

	@Override
	public Optional<List<Integer>> buildClientRequestData(Player player) {
		List<Integer> entityIds = player.level().getEntities(player,
				player.getBoundingBox().inflate(ItemInStorageHighlightRenderer.HIGHLIGHT_RANGE),
				e -> e instanceof IMovingStorageEntity && e.distanceTo(player) <= ItemInStorageHighlightRenderer.HIGHLIGHT_RANGE
		).stream().map(Entity::getId).toList();
		return entityIds.isEmpty() ? Optional.empty() : Optional.of(entityIds);
	}

	@Override
	public void clearCache() {
		highlightedStackEntityIds = Collections.emptyList();
		highlightedItemEntityIds = Collections.emptyList();
	}

	@Override
	public void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, float partialTick, Vec3 cameraPos) {
		highlightedStackEntityIds.forEach(eId -> submitHighlightedEntity(submitNodeCollector, poseStack, partialTick, cameraPos, eId, Minecraft.getInstance(), ItemInStorageHighlightRenderer.MATCHING_STACK_HIGHLIGHT_COLOR));
		highlightedItemEntityIds.forEach(eId -> submitHighlightedEntity(submitNodeCollector, poseStack, partialTick, cameraPos, eId, Minecraft.getInstance(), ItemInStorageHighlightRenderer.MATCHING_ITEM_HIGHLIGHT_COLOR));
	}

	private static void submitHighlightedEntity(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, float partialTick, Vec3 cameraPos, int entityId, Minecraft mc, int color) {
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
		BlockHighlightRenderHelper.submitThickEdges(submitNodeCollector, poseStack, color, VoxelOutliner.edgesFromAABB(boundingBox), entity.getX(), entity.getY(), entity.getZ());
		poseStack.popPose();
	}
}
