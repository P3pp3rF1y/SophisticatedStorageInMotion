package net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui;

import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TranslationHelper;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

public class StorageInMotionTranslationHelper extends TranslationHelper {
	public static final StorageInMotionTranslationHelper INSTANCE = new StorageInMotionTranslationHelper();
	private final String entityPrefix;

	public StorageInMotionTranslationHelper() {
		super(SophisticatedStorageInMotion.MOD_ID);
		String modId = SophisticatedStorageInMotion.MOD_ID;
		entityPrefix = "entity." + modId + ".";
	}

	public String translEntity(String entityName) {
		return entityPrefix + entityName;
	}
}
