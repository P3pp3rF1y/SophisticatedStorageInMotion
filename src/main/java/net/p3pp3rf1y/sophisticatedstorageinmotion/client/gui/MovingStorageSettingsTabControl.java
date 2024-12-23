package net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui;

import net.p3pp3rf1y.sophisticatedcore.client.gui.Tab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedstorage.client.gui.StorageSettingsTabControl;
import net.p3pp3rf1y.sophisticatedstorageinmotion.common.gui.MovingStorageSettingsContainerMenu;

public class MovingStorageSettingsTabControl extends StorageSettingsTabControl {
	protected MovingStorageSettingsTabControl(MovingStorageSettingsScreen screen, Position position) {
		super(screen, position);
	}

	@Override
	protected Tab instantiateReturnBackTab() {
		return new BackToMovingStorageTab(new Position(x, getTopY()), screen.getMenu() instanceof MovingStorageSettingsContainerMenu menu ? menu.getEntityId() : -1);
	}
}
