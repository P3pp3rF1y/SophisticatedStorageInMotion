package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.data.PackOutput;
import net.p3pp3rf1y.sophisticatedcore.data.SophisticatedModelProvider;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.StorageBoatItemRenderer;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.StorageMinecartItemRenderer;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;

public class StorageInMotionModelProvider extends SophisticatedModelProvider {
	public StorageInMotionModelProvider(PackOutput output) {
		super(output, SophisticatedStorageInMotion.MOD_ID);
	}

	@Override
	protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
		itemModels.itemModelOutput.accept(ModItems.STORAGE_BOAT.get(), ItemModelUtils.specialModel(ModelLocationUtils.getModelLocation(ModItems.STORAGE_BOAT.get()), new StorageBoatItemRenderer.Unbaked()));
		itemModels.itemModelOutput.accept(ModItems.STORAGE_MINECART.get(), ItemModelUtils.specialModel(ModelLocationUtils.getModelLocation(ModItems.STORAGE_MINECART.get()), new StorageMinecartItemRenderer.Unbaked()));
	}
}
