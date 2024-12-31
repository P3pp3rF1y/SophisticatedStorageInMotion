package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.Items;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapelessBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;

import java.util.function.Consumer;

public class StorageInMotionRecipeProvider extends RecipeProvider {
	public StorageInMotionRecipeProvider(DataGenerator generator) {
		super(generator.getPackOutput());
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		SpecialRecipeBuilder.special(ModItems.UNCRAFT_MOVING_STORAGE_SERIALIZER.get()).save(consumer, SophisticatedStorageInMotion.getRegistryName("uncraft_moving_storage"));

		ShapelessBasedRecipeBuilder.shapeless(ModItems.STORAGE_MINECART.get(), ModItems.MOVING_STORAGE_FROM_STORAGE_SERIALIZER.get())
				.requires(Items.MINECART)
				.requires(ModBlocks.ALL_STORAGE_TAG)
				.unlockedBy("has_sophisticated_storage", has(ModBlocks.ALL_STORAGE_TAG))
				.save(consumer);
	}
}
