package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
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
import net.minecraftforge.client.RenderTypeHelper;
import net.minecraftforge.client.model.data.ModelData;
import net.p3pp3rf1y.sophisticatedstorage.block.BarrelBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.ChestBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.client.render.BarrelBakedModelBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.EntityStorageHolder;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class StorageBlockRenderer {
	public static void renderStorageBlock(float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			StorageBlockEntity renderBlockEntity) {
		BlockState state = renderBlockEntity.getBlockState();
		Minecraft minecraft = Minecraft.getInstance();
		if (renderBlockEntity instanceof BarrelBlockEntity barrel) {
			BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
			BakedModel bakedModel = blockRenderer.getBlockModel(barrel.getBlockState());
			ModelData modelData = BarrelBakedModelBase.getModelDataFromBlockEntity(barrel);
			BlockAndTintGetter wrappedLevel = new StaticBlockEntityTintGetter(minecraft.level, renderBlockEntity, packedLight); // TODO try to optimize not to
																																// create a new instance all the
																																// time, perhaps level keyed
																																// cache for these and then only
																																// setting blockentity in the
																																// render call
			for (RenderType renderType : bakedModel.getRenderTypes(state, RandomSource.create(42L), modelData)) {
				VertexConsumer vertexConsumer = buffer.getBuffer(RenderTypeHelper.getEntityRenderType(renderType, false));
				RandomSource randomsource = RandomSource.create();
				randomsource.setSeed(42L);
				blockRenderer.getModelRenderer().tesselateWithoutAO(wrappedLevel, bakedModel, barrel.getBlockState(), BlockPos.ZERO, poseStack, vertexConsumer,
						false, randomsource, state.getSeed(BlockPos.ZERO), OverlayTexture.NO_OVERLAY, modelData, renderType);
			}
		}

		BlockEntityRenderer<StorageBlockEntity> renderer = minecraft.getBlockEntityRenderDispatcher().getRenderer(renderBlockEntity);
		if (renderer != null) {
			renderer.render(renderBlockEntity, partialTicks, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
		}
	}

	private static final Map<Class<? extends AbstractChestedHorse>, Function<StorageBlockEntity, Vec3>> OFFSET_MAP = new LinkedHashMap<>();

	static {
		OFFSET_MAP.put(Donkey.class,
				(renderBlockEntity) -> renderBlockEntity instanceof ChestBlockEntity ? new Vec3(0, -1.283, -0.515) : new Vec3(0, -1.34, -0.48));
		OFFSET_MAP.put(Mule.class,
				(renderBlockEntity) -> renderBlockEntity instanceof ChestBlockEntity ? new Vec3(0, -1.343, -0.515) : new Vec3(0, -1.40, -0.48));
		OFFSET_MAP.put(Llama.class, (renderBlockEntity) -> new Vec3(0, -1.5, -0.25));
		OFFSET_MAP.put(AbstractChestedHorse.class, (renderBlockEntity) -> new Vec3(0, -1.5, -0.25));
	}

	private static final Function<StorageBlockEntity, Vec3> DEFAULT_OFFSET = (
			renderBlockEntity) -> renderBlockEntity instanceof ChestBlockEntity ? new Vec3(0, -1.343, -0.515) : new Vec3(0, -1.40, -0.48);

	public static void renderChestedHorseStorage(float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			AbstractChestedHorse chestedHorse, IMovingStorageEntity movingStorage) {
		EntityStorageHolder<?> storageHolder = movingStorage.getStorageHolder();
		StorageBlockEntity renderBlockEntity = storageHolder.getRenderBlockEntity();
		if (renderBlockEntity == null) {
			return;
		}
		poseStack.pushPose();
		poseStack.mulPose(Axis.XP.rotationDegrees(180));

		Function<StorageBlockEntity, Vec3> offsetFunction = OFFSET_MAP.getOrDefault(chestedHorse.getClass(), DEFAULT_OFFSET);
		if (offsetFunction != null) {
			poseStack.translate(offsetFunction.apply(renderBlockEntity).x, offsetFunction.apply(renderBlockEntity).y,
					offsetFunction.apply(renderBlockEntity).z);
		}

		renderStorageOnSide(chestedHorse, poseStack, 90, 1, renderBlockEntity, packedLight, buffer, partialTicks);
		renderStorageOnSide(chestedHorse, poseStack, 270, -1, renderBlockEntity, packedLight, buffer, partialTicks);
		poseStack.popPose();
	}

	private static void renderStorageOnSide(AbstractChestedHorse chestedHorse, PoseStack poseStack, int storageRotation, float xOffsetMultiplier,
			StorageBlockEntity renderBlockEntity, int packedLight, MultiBufferSource buffer, float partialTick) {
		float halftWidth = chestedHorse.getBbWidth() / 2;
		poseStack.pushPose();

		float scale = 0.57f;
		poseStack.scale(scale, scale, scale);
		poseStack.mulPose(Axis.YN.rotationDegrees(storageRotation));
		float xOffset = halftWidth * 0.49f * xOffsetMultiplier;
		float sideOffset;
		if (chestedHorse instanceof Llama) {
			sideOffset = renderBlockEntity instanceof ChestBlockEntity ? 1 : 0.85f;
		} else {
			sideOffset = renderBlockEntity instanceof ChestBlockEntity ? 0.6f : 0.5f;
		}
		float zOffset = -halftWidth * sideOffset;
		double yOffset = renderBlockEntity instanceof ChestBlockEntity ? chestedHorse.getBbHeight() * 1.03f : chestedHorse.getBbHeight() * 1.01f;
		poseStack.translate(xOffset, yOffset, zOffset);
		if (!(renderBlockEntity instanceof ChestBlockEntity)) {
			poseStack.mulPose(Axis.XN.rotationDegrees(90));
		}
		poseStack.translate(-0.5, -0.5, -0.5);

		StorageBlockRenderer.renderStorageBlock(partialTick, poseStack, buffer, packedLight, renderBlockEntity);
		poseStack.popPose();
	}
}
