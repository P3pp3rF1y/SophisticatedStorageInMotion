package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ChestBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.client.render.BarrelBakedModelBase;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class StorageBlockRenderer {
	public static void renderStorageBlock(float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, StorageBlockEntity renderBlockEntity) {
		BlockState state = renderBlockEntity.getBlockState();
		Minecraft minecraft = Minecraft.getInstance();
		if (renderBlockEntity instanceof BarrelBlockEntity barrel) {
			BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
			BakedModel bakedModel = blockRenderer.getBlockModel(barrel.getBlockState());
			ModelData modelData = BarrelBakedModelBase.getModelDataFromBlockEntity(barrel);
			BlockAndTintGetter wrappedLevel = new StaticBlockEntityTintGetter(minecraft.level, renderBlockEntity, packedLight); //TODO try to optimize not to create a new instance all the time, perhaps level keyed cache for these and then only setting blockentity in the render call
			for (RenderType renderType : bakedModel.getRenderTypes(state, RandomSource.create(42L), modelData)) {
				VertexConsumer vertexConsumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(renderType));
				RandomSource randomsource = RandomSource.create();
				randomsource.setSeed(42L);
				blockRenderer.getModelRenderer().tesselateWithoutAO(wrappedLevel, bakedModel, barrel.getBlockState(), BlockPos.ZERO, poseStack, vertexConsumer, false, randomsource, state.getSeed(BlockPos.ZERO), OverlayTexture.NO_OVERLAY, modelData, renderType);
			}
		}

		BlockEntityRenderer<StorageBlockEntity> renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(renderBlockEntity);
		if (renderer != null) {
			renderer.render(renderBlockEntity, partialTicks, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
		}
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

	public static void renderChestedHorseStorage(Class<? extends AbstractChestedHorse> chestedHorseClass, EntityRenderState entityRenderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight, @Nullable StorageBlockEntity renderBlockEntity) {
		if (renderBlockEntity == null) {
			return;
		}
		poseStack.pushPose();
		poseStack.mulPose(Axis.XP.rotationDegrees(180));

		Function<StorageBlockEntity, Vec3> offsetFunction = OFFSET_MAP.getOrDefault(chestedHorseClass, DEFAULT_OFFSET);
		if (offsetFunction != null) {
			poseStack.translate(offsetFunction.apply(renderBlockEntity).x, offsetFunction.apply(renderBlockEntity).y, offsetFunction.apply(renderBlockEntity).z);
		}

		renderStorageOnSide(chestedHorseClass, entityRenderState, poseStack, 90, 1, renderBlockEntity, packedLight, buffer, entityRenderState.partialTick);
		renderStorageOnSide(chestedHorseClass, entityRenderState, poseStack, 270, -1, renderBlockEntity, packedLight, buffer, entityRenderState.partialTick);
		poseStack.popPose();
	}

	private static void renderStorageOnSide(Class<? extends AbstractChestedHorse> chestedHorseClass, EntityRenderState entityRenderState, PoseStack poseStack, int storageRotation, float xOffsetMultiplier, StorageBlockEntity renderBlockEntity, int packedLight, MultiBufferSource buffer, float partialTick) {
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

		StorageBlockRenderer.renderStorageBlock(partialTick, poseStack, buffer, packedLight, renderBlockEntity);
		poseStack.popPose();
	}
}
