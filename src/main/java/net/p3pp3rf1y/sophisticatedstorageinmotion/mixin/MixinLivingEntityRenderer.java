package net.p3pp3rf1y.sophisticatedstorageinmotion.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.ContextKeys;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.StorageBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer {
	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V"))
	private void submitSophisticatedStorage(LivingEntityRenderState renderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
			CameraRenderState cameraRenderState, CallbackInfo ci) {
		Class<? extends AbstractChestedHorse> horseClass = renderState.getRenderData(ContextKeys.HORSE_CLASS);
		if (horseClass == null) {
			return;
		}

		StorageBlockRenderer.submitChestedHorseStorage(horseClass, renderState, poseStack, submitNodeCollector, renderState.lightCoords,
				renderState.getRenderData(ContextKeys.RENDER_BLOCK_ENTITY));
	}
}
