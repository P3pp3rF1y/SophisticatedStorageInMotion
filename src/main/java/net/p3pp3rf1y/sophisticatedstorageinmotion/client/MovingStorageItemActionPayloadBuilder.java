package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.client.ItemInteractionHandler;
import net.p3pp3rf1y.sophisticatedcore.client.render.IItemActionPayloadBuilder;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.MovingStorageItemActionPayloadHandler;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;

import java.util.List;
import java.util.Optional;

public class MovingStorageItemActionPayloadBuilder implements IItemActionPayloadBuilder<List<Integer>> {
	public static final MovingStorageItemActionPayloadBuilder INSTANCE = new MovingStorageItemActionPayloadBuilder();

	@Override
	public Identifier getPayloadHandlerId() {
		return MovingStorageItemActionPayloadHandler.ID;
	}

	@Override
	public Optional<List<Integer>> buildClientRequestData(Player player) {
		List<Integer> entityIds = player.level().getEntities(player,
				player.getBoundingBox().inflate(ItemInteractionHandler.INTERACTION_RANGE),
				e -> e instanceof IMovingStorageEntity && e.distanceTo(player) <= ItemInteractionHandler.INTERACTION_RANGE
		).stream().map(Entity::getId).toList();
		return entityIds.isEmpty() ? Optional.empty() : Optional.of(entityIds);
	}
}
