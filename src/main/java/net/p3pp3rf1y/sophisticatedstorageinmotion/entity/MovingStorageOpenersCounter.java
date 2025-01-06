package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageContainerMenu;

import java.util.List;

public abstract class MovingStorageOpenersCounter {
	private static final int CHECK_TICK_DELAY = 5;
	private final Entity entity;
	private int openCount;
	private double maxInteractionRange;
	private long nextOpenRecheck;

	public MovingStorageOpenersCounter(Entity entity) {
		this.entity = entity;
		this.openCount = 0;
		this.maxInteractionRange = 0.0;
	}

	protected abstract void onOpen();

	protected abstract void onClose();

	private boolean isOwnContainer(Player player) {
		if (player.containerMenu instanceof MovingStorageContainerMenu<?> movingStorageContainerMenu) {
			return movingStorageContainerMenu.getStorageEntity().map(e -> e == entity).orElse(false);
		}
		return false;
	}

	public void incrementOpeners(Player player) {
		int i = openCount++;
		if (i == 0) {
			onOpen();
			entity.level().gameEvent(player, GameEvent.CONTAINER_OPEN, entity.blockPosition());
			nextOpenRecheck = entity.level().getGameTime() + CHECK_TICK_DELAY;
 		}

		maxInteractionRange = Math.max(5, maxInteractionRange);
	}

	public void decrementOpeners(Player player) {
		openCount--;
		if (openCount == 0) {
			onClose();
			entity.level().gameEvent(player, GameEvent.CONTAINER_CLOSE, entity.blockPosition());
			maxInteractionRange = 0.0;
		}
	}

	private List<Player> getPlayersWithContainerOpen(Level level, BlockPos pos) {
		double maxDistance = maxInteractionRange + 4.0;
		AABB aabb = new AABB(pos).inflate(maxDistance);
		return level.getEntities(EntityTypeTest.forClass(Player.class), aabb, this::isOwnContainer);
	}

	public void tick() {
		if (entity.level().getGameTime() > nextOpenRecheck && (entity.level().getGameTime() - nextOpenRecheck < 3 * CHECK_TICK_DELAY || openCount > 0)) {
			recheckOpeners();
		}
	}

	public void recheckOpeners() {
		Level level = entity.level();
		List<Player> playersWithStorageOpen = getPlayersWithContainerOpen(level, entity.blockPosition());
		maxInteractionRange = 5;

		int numberOfPlayers = playersWithStorageOpen.size();
		if (openCount != numberOfPlayers) {
			if (numberOfPlayers != 0 && openCount == 0) {
				onOpen();
				level.gameEvent(null, GameEvent.CONTAINER_OPEN, entity.blockPosition());
			} else if (numberOfPlayers == 0) {
				onClose();
				level.gameEvent(null, GameEvent.CONTAINER_CLOSE, entity.blockPosition());
			}

			openCount = numberOfPlayers;
		}

		if (numberOfPlayers > 0) {
			nextOpenRecheck = level.getGameTime() + CHECK_TICK_DELAY;
		}
	}

	public int getOpenerCount() {
		return openCount;
	}
}
