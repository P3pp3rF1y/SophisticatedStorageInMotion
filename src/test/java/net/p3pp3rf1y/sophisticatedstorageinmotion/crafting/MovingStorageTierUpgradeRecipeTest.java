package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.linkedstorage.LinkedStorageEndpointData;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Requires loaded NeoForge item components; covered by DevClient assert.movingStorageTierUpgradeRecipe matrix.")
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

			assertTrue(fixture.shaped().matches(fixture.input(), null));
			assertTrue(fixture.shapeless().matches(fixture.input(), null));
		}
	}

	@Test
	void secondaryLinkedMovingStorageCannotBeUpgradedByRecipe() {
		for (MovingStorageItem movingItem : List.of(ModItems.STORAGE_BOAT.get(), ModItems.STORAGE_MINECART.get())) {
			ItemStack storage = new ItemStack(ModBlocks.DIAMOND_CHEST_ITEM.get());
			storage.set(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT, new LinkedStorageEndpointData(UUID.randomUUID(), UUID.randomUUID()));
			RecipeFixture fixture = createRecipeFixture(movingItem, storage);

			assertFalse(fixture.shaped().matches(fixture.input(), null));
			assertFalse(fixture.shapeless().matches(fixture.input(), null));
			assertTrue(fixture.shaped().assemble(fixture.input()).isEmpty());
			assertTrue(fixture.shapeless().assemble(fixture.input()).isEmpty());
		}
	}

	@Test
	void primaryLinkedMovingStorageMatchesTierUpgradeRecipes() {
		for (MovingStorageItem movingItem : List.of(ModItems.STORAGE_BOAT.get(), ModItems.STORAGE_MINECART.get())) {
			ItemStack storage = new ItemStack(ModBlocks.DIAMOND_CHEST_ITEM.get());
			storage.set(ModCoreDataComponents.LINKED_STORAGE_ENDPOINT, new LinkedStorageEndpointData(UUID.randomUUID(), UUID.randomUUID()));
			storage.set(ModCoreDataComponents.LINKED_STORAGE_PRIMARY_ENDPOINT, true);
			RecipeFixture fixture = createRecipeFixture(movingItem, storage);

			assertTrue(fixture.shaped().matches(fixture.input(), null));
			assertTrue(fixture.shapeless().matches(fixture.input(), null));
		}
	}

	private static RecipeFixture createRecipeFixture(MovingStorageItem movingItem, ItemStack storage) {
		ItemStack moving = MovingStorageItem.createWithStorage(new ItemStack(movingItem), storage);
		CraftingInput input = CraftingInput.of(1, 1, List.of(moving));
		ItemStackTemplate upgradedStorage = ItemStackTemplate.fromNonEmptyStack(new ItemStack(ModBlocks.NETHERITE_CHEST_ITEM.get()));
		MovingStorageTierUpgradeShapedRecipe shaped = new MovingStorageTierUpgradeShapedRecipe(
				new ShapedRecipe(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
						new ShapedRecipePattern(1, 1, List.of(Optional.of(Ingredient.of(movingItem))), Optional.empty()),
						ItemStackTemplate.fromNonEmptyStack(new ItemStack(movingItem))),
				upgradedStorage);
		MovingStorageTierUpgradeShapelessRecipe shapeless = new MovingStorageTierUpgradeShapelessRecipe(
				new ShapelessRecipe(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
						ItemStackTemplate.fromNonEmptyStack(new ItemStack(movingItem)), List.of(Ingredient.of(movingItem))),
				upgradedStorage);
		return new RecipeFixture(input, shaped, shapeless);
	}

	private record RecipeFixture(CraftingInput input, MovingStorageTierUpgradeShapedRecipe shaped, MovingStorageTierUpgradeShapelessRecipe shapeless) {
	}
}
