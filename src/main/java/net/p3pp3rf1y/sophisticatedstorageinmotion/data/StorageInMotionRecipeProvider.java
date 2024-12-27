package net.p3pp3rf1y.sophisticatedstorageinmotion.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.world.item.Items;
import net.p3pp3rf1y.sophisticatedcore.crafting.ShapelessBasedRecipeBuilder;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageFromStorageRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.UncraftMovingStorageRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;

import java.util.concurrent.CompletableFuture;

public class StorageInMotionRecipeProvider extends RecipeProvider {
	public StorageInMotionRecipeProvider(DataGenerator generator, CompletableFuture<HolderLookup.Provider> registries) {
		super(generator.getPackOutput(), registries);
	}

	@Override
	protected void buildRecipes(RecipeOutput recipeOutput) {
		SpecialRecipeBuilder.special(UncraftMovingStorageRecipe::new).save(recipeOutput, SophisticatedStorageInMotion.getRegistryName("uncraft_moving_storage"));

		ShapelessBasedRecipeBuilder.shapeless(ModItems.STORAGE_MINECART.get(), MovingStorageFromStorageRecipe::new)
				.requires(Items.MINECART)
				.requires(ModBlocks.ALL_STORAGE_TAG)
				.unlockedBy("has_sophisticated_storage", has(ModBlocks.ALL_STORAGE_TAG))
				.save(recipeOutput);
	}
}
