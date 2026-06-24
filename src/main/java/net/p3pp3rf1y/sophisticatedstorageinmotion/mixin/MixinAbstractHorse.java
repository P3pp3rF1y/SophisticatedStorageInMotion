package net.p3pp3rf1y.sophisticatedstorageinmotion.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractHorse.class)
public abstract class MixinAbstractHorse extends Animal {

	@Shadow
	public abstract boolean isTamed();

	protected MixinAbstractHorse(EntityType<? extends Animal> entityType, Level level) {
		super(entityType, level);
	}

	@Inject(method = "openCustomInventoryScreen", at = @At("HEAD"), cancellable = true)
	private void openStorageScreen(Player player, CallbackInfo ci) {
		if (!level().isClientSide && (!isVehicle() || hasPassenger(player)) && isTamed() && this instanceof IMovingStorageEntity movingStorage
				&& !movingStorage.getStorageItem().isEmpty()) {
			movingStorage.getStorageHolder().openContainerMenu(player);
			ci.cancel();
		}
	}
}
