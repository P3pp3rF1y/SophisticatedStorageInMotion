package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.SharedConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MovingStorageTierUpgradeRecipeTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void unlinkedMovingStorageMatchesTierUpgradeRecipes() {
		for (MovingStorageItem movingItem : List.of(ModItems.STORAGE_BOAT.get(), ModItems.STORAGE_MINECART.get())) {
			RecipeFixture fixture = createRecipeFixture(movingItem, new ItemStack(ModBlocks.DIAMOND_CHEST_ITEM.get()));

			boolean shapedMatches = fixture.shaped().matches(fixture.input(), null);
			boolean shapelessMatches = fixture.shapeless().matches(fixture.input(), null);

			assertTrue(shapedMatches);
			assertTrue(shapelessMatches);
		}
	}

	@Test
	void secondaryLinkedMovingStorageCannotBeUpgradedByRecipe() {
		for (MovingStorageItem movingItem : List.of(ModItems.STORAGE_BOAT.get(), ModItems.STORAGE_MINECART.get())) {
			ItemStack storage = new ItemStack(ModBlocks.DIAMOND_CHEST_ITEM.get());
			storage.set(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT, new LinkedStorageEndpointData(UUID.randomUUID(), UUID.randomUUID()));
			RecipeFixture fixture = createRecipeFixture(movingItem, storage);

			boolean shapedMatches = fixture.shaped().matches(fixture.input(), null);
			boolean shapelessMatches = fixture.shapeless().matches(fixture.input(), null);
			ItemStack shapedResult = fixture.shaped().assemble(fixture.input(), null);
			ItemStack shapelessResult = fixture.shapeless().assemble(fixture.input(), null);

			assertFalse(shapedMatches);
			assertFalse(shapelessMatches);
			assertTrue(shapedResult.isEmpty());
			assertTrue(shapelessResult.isEmpty());
		}
	}

	@Test
	void primaryLinkedMovingStorageMatchesTierUpgradeRecipes() {
		for (MovingStorageItem movingItem : List.of(ModItems.STORAGE_BOAT.get(), ModItems.STORAGE_MINECART.get())) {
			ItemStack storage = new ItemStack(ModBlocks.DIAMOND_CHEST_ITEM.get());
			storage.set(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT, new LinkedStorageEndpointData(UUID.randomUUID(), UUID.randomUUID()));
			storage.set(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT, true);
			RecipeFixture fixture = createRecipeFixture(movingItem, storage);

			boolean shapedMatches = fixture.shaped().matches(fixture.input(), null);
			boolean shapelessMatches = fixture.shapeless().matches(fixture.input(), null);

			assertTrue(shapedMatches);
			assertTrue(shapelessMatches);
		}
	}

	private static RecipeFixture createRecipeFixture(MovingStorageItem movingItem, ItemStack storage) {
		ItemStack moving = MovingStorageItem.createWithStorage(new ItemStack(movingItem), storage);
		CraftingInput input = CraftingInput.of(1, 1, List.of(moving));
		MovingStorageTierUpgradeShapedRecipe shaped = new MovingStorageTierUpgradeShapedRecipe(new ShapedRecipe("", CraftingBookCategory.MISC,
				new ShapedRecipePattern(1, 1, NonNullList.of(Ingredient.EMPTY, Ingredient.of(movingItem)), Optional.empty()),
				MovingStorageItem.createWithStorage(new ItemStack(movingItem), new ItemStack(ModBlocks.NETHERITE_CHEST_ITEM.get()))));
		MovingStorageTierUpgradeShapelessRecipe shapeless = new MovingStorageTierUpgradeShapelessRecipe(new ShapelessRecipe("", CraftingBookCategory.MISC,
				MovingStorageItem.createWithStorage(new ItemStack(movingItem), new ItemStack(ModBlocks.NETHERITE_CHEST_ITEM.get())),
				NonNullList.of(Ingredient.EMPTY, Ingredient.of(movingItem))));
		return new RecipeFixture(input, shaped, shapeless);
	}

	private record RecipeFixture(CraftingInput input, MovingStorageTierUpgradeShapedRecipe shaped, MovingStorageTierUpgradeShapelessRecipe shapeless) {
	}
}
