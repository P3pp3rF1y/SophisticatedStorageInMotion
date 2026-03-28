package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ChestBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.client.render.BarrelBlockStateModelBase;
import net.p3pp3rf1y.sophisticatedstorage.client.render.RenderHelper;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StorageBlockRenderer {
	public static void submitStorageBlock(float partialTicks, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, StorageBlockEntity renderBlockEntity) {
		Minecraft minecraft = Minecraft.getInstance();
		BlockState state = renderBlockEntity.getBlockState();
		if (renderBlockEntity instanceof BarrelBlockEntity barrel && minecraft.level != null) {
			BlockStateModelSet blockModelSet = minecraft.getModelManager().getBlockStateModelSet();
			BlockStateModel blockStateModel = blockModelSet.get(state);
			if (blockStateModel instanceof BarrelBlockStateModelBase barrelModel) {
				barrelModel.setModelPropertiesFromBlockEntity(barrel);
			}
			StaticBlockEntityTintGetter wrappedLevel = new StaticBlockEntityTintGetter(minecraft.level, renderBlockEntity, packedLight);
			List<BlockStateModelPart> parts = new ArrayList<>();
			blockStateModel.collectParts(wrappedLevel, BlockPos.ZERO, state, RandomSource.create(42L), parts);
			submitModelParts(poseStack, submitNodeCollector, state, wrappedLevel, parts, packedLight);
		}

		BlockEntityRenderer<StorageBlockEntity, ? extends BlockEntityRenderState> renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(renderBlockEntity);
		if (renderer == null) {
			return;
		}
		submitBlockEntityRender(renderer, renderBlockEntity, partialTicks, poseStack, submitNodeCollector, packedLight);
	}

	private static <T extends BlockEntity, S extends BlockEntityRenderState> void submitBlockEntityRender(
			BlockEntityRenderer<T, S> renderer, T blockEntity, float partialTicks, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight) {
		S renderState = renderer.createRenderState();
		renderer.extractRenderState(blockEntity, renderState, partialTicks, Vec3.ZERO, null);
		renderState.lightCoords = packedLight;
		renderer.submit(renderState, poseStack, submitNodeCollector, RenderHelper.ZERO_POS_CAMERA_RENDER_STATE);
	}

	private static void submitModelParts(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, BlockState state, StaticBlockEntityTintGetter wrappedLevel, List<BlockStateModelPart> parts, int packedLight) {
		List<BakedQuad> cutoutQuads = new ArrayList<>();
		List<BakedQuad> translucentQuads = new ArrayList<>();
		for (BlockStateModelPart part : parts) {
			collectPartQuads(part, cutoutQuads, translucentQuads);
		}

		if (!cutoutQuads.isEmpty()) {
			submitQuads(poseStack, submitNodeCollector, state, wrappedLevel, packedLight, cutoutQuads, Sheets.cutoutBlockSheet());
		}
		if (!translucentQuads.isEmpty()) {
			submitQuads(poseStack, submitNodeCollector, state, wrappedLevel, packedLight, translucentQuads, Sheets.translucentBlockSheet());
		}
	}

	private static void collectPartQuads(BlockStateModelPart part, List<BakedQuad> cutoutQuads, List<BakedQuad> translucentQuads) {
		for (Direction direction : Direction.values()) {
			for (BakedQuad quad : part.getQuads(direction)) {
				(quad.materialInfo().layer().translucent() ? translucentQuads : cutoutQuads).add(quad);
			}
		}
		for (BakedQuad quad : part.getQuads(null)) {
			(quad.materialInfo().layer().translucent() ? translucentQuads : cutoutQuads).add(quad);
		}
	}

	private static void submitQuads(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, BlockState state, StaticBlockEntityTintGetter wrappedLevel, int packedLight, List<BakedQuad> quads, net.minecraft.client.renderer.rendertype.RenderType renderType) {
		submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
			QuadInstance quadInstance = new QuadInstance();
			quadInstance.setLightCoords(packedLight);
			quadInstance.setOverlayCoords(OverlayTexture.NO_OVERLAY);
			for (BakedQuad quad : quads) {
				int tintIndex = quad.materialInfo().tintIndex();
				quadInstance.setColor(getTintColor(state, wrappedLevel, tintIndex));
				vertexConsumer.putBakedQuad(pose, quad, quadInstance);
			}
		});
	}

	private static int getTintColor(BlockState state, StaticBlockEntityTintGetter wrappedLevel, int tintIndex) {
		if (tintIndex == -1) {
			return -1;
		}
		BlockTintSource tintSource = Minecraft.getInstance().getBlockColors().getTintSource(state, tintIndex);
		return tintSource == null ? -1 : tintSource.colorInWorld(state, wrappedLevel, BlockPos.ZERO);
	}

	private static final Map<Class<? extends AbstractChestedHorse>, Function<StorageBlockEntity, Vec3>> OFFSET_MAP = new LinkedHashMap<>();

	public static final Vec3 DONKEY_CHEST_OFFSET = new Vec3(0, -0.26, -0.1);

	public static final Vec3 DONKEY_OTHER_OFFSET = new Vec3(0, -0.325, -0.07);

	public static final Vec3 MULE_CHEST_OFFSET = new Vec3(0, -0.18, -0.13);

	public static final Vec3 MULE_OTHER_OFFSET = new Vec3(0, -0.24, -0.1);

	public static final Vec3 LLAMA_OTHER_OFFSET = new Vec3(0, -0.02, 0);

	static {
		OFFSET_MAP.put(Donkey.class, (renderBlockEntity) -> renderBlockEntity instanceof ChestBlockEntity ? DONKEY_CHEST_OFFSET : DONKEY_OTHER_OFFSET);
		OFFSET_MAP.put(Mule.class, (renderBlockEntity) -> renderBlockEntity instanceof ChestBlockEntity ? MULE_CHEST_OFFSET : MULE_OTHER_OFFSET);
		OFFSET_MAP.put(Llama.class, (renderBlockEntity) -> renderBlockEntity instanceof ChestBlockEntity ? Vec3.ZERO : LLAMA_OTHER_OFFSET);
		OFFSET_MAP.put(AbstractChestedHorse.class, (renderBlockEntity) -> MULE_OTHER_OFFSET);
	}

	private static final Function<StorageBlockEntity, Vec3> DEFAULT_OFFSET = (renderBlockEntity) -> renderBlockEntity instanceof ChestBlockEntity ? new Vec3(0, -1.343, -0.515) : new Vec3(0, -1.40, -0.48);

	public static void submitChestedHorseStorage(Class<? extends AbstractChestedHorse> chestedHorseClass, EntityRenderState entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight, @Nullable StorageBlockEntity renderBlockEntity) {
		if (renderBlockEntity == null) {
			return;
		}
		poseStack.pushPose();
		poseStack.mulPose(Axis.XP.rotationDegrees(180));

		Function<StorageBlockEntity, Vec3> offsetFunction = OFFSET_MAP.getOrDefault(chestedHorseClass, DEFAULT_OFFSET);
		if (offsetFunction != null) {
			poseStack.translate(offsetFunction.apply(renderBlockEntity).x, offsetFunction.apply(renderBlockEntity).y, offsetFunction.apply(renderBlockEntity).z);
		}

		submitStorageOnSide(chestedHorseClass, entityRenderState, poseStack, 90, 1, renderBlockEntity, packedLight, submitNodeCollector, entityRenderState.partialTick);
		submitStorageOnSide(chestedHorseClass, entityRenderState, poseStack, 270, -1, renderBlockEntity, packedLight, submitNodeCollector, entityRenderState.partialTick);
		poseStack.popPose();
	}

	private static void submitStorageOnSide(Class<? extends AbstractChestedHorse> chestedHorseClass, EntityRenderState entityRenderState, PoseStack poseStack, int storageRotation, float xOffsetMultiplier, StorageBlockEntity renderBlockEntity, int packedLight, SubmitNodeCollector submitNodeCollector, float partialTick) {
		float halfWidth = entityRenderState.boundingBoxWidth / 2;
		poseStack.pushPose();

		float scale = Llama.class.isAssignableFrom(chestedHorseClass) ? 0.57f : 0.5f;
		poseStack.scale(scale, scale, scale);
		poseStack.mulPose(Axis.YN.rotationDegrees(storageRotation));
		float xOffset = halfWidth * -0.49f * xOffsetMultiplier;
		float sideOffset;

		if (Llama.class.isAssignableFrom(chestedHorseClass)) {
			sideOffset = renderBlockEntity instanceof ChestBlockEntity ? 1 : 0.85f;
		} else {
			sideOffset = renderBlockEntity instanceof ChestBlockEntity ? 0.6f : 0.5f;
		}
		float zOffset = -halfWidth * sideOffset;
		double yOffset = -entityRenderState.boundingBoxHeight * 0.379f;
		poseStack.translate(xOffset, yOffset, zOffset);
		if (!(renderBlockEntity instanceof ChestBlockEntity)) {
			poseStack.mulPose(Axis.XN.rotationDegrees(90));
		}
		poseStack.translate(-0.5, -0.5, -0.5);

		StorageBlockRenderer.submitStorageBlock(partialTick, poseStack, submitNodeCollector, packedLight, renderBlockEntity);
		poseStack.popPose();
	}
}
