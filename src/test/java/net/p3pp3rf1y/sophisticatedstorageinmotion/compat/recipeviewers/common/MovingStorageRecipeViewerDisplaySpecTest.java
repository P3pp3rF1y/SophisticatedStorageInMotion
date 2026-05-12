package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common;

import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.*;
import net.p3pp3rf1y.sophisticatedcore.util.RecipeHelper;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.subtypes.MovingStorageSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageFromStorageRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapedRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapelessRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("recipeViewerRegression")
class MovingStorageRecipeViewerDisplaySpecTest {
	@Test
	void tintedBarrelUsageShowsAssemblyRecipeAndPreservesTintInResult() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedBarrel = generatedTintStack(ModBlocks.BARREL_ITEM.get());

		List<CraftingDisplayVariant> usages = catalog.getCraftingUsagesFor(tintedBarrel).stream().flatMap(view -> view.variants().stream()).toList();

		CraftingDisplayVariant usage = usages.stream()
				.filter(variant -> ItemStack.isSameItemSameComponents(tintedBarrel, getSource(variant)))
				.findFirst()
				.orElseThrow();
		assertSameStack(tintedBarrel, getSource(usage));
		assertSameStack(tintedBarrel, MovingStorageItem.getStorageItem(usage.firstOutput()));
	}

	@Test
	void higherTierStorageBoatRecipeDoesNotDuplicateAcrossTierUpgradeVariants() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack basicBarrelBoat = movingStorage(woodStorageStack(ModBlocks.BARREL_ITEM.get()), WoodType.OAK);
		ItemStack ironBarrelBoat = movingStorage(woodStorageStack(ModBlocks.IRON_BARREL_ITEM.get()), WoodType.OAK);
		ItemStack copperBarrelBoat = movingStorage(woodStorageStack(ModBlocks.COPPER_BARREL_ITEM.get()), WoodType.OAK);

		List<CraftingDisplayVariant> recipes = catalog.getCraftingRecipesFor(ironBarrelBoat).stream().flatMap(view -> view.variants().stream()).toList();
		List<Item> sourceStorageItems = recipes.stream()
				.filter(MovingStorageRecipeViewerDisplaySpecTest::hasMovingStorageSource)
				.map(variant -> MovingStorageItem.getStorageItem(getSource(variant)).getItem())
				.distinct()
				.toList();

		assertEquals(2, sourceStorageItems.size());
		assertTrue(sourceStorageItems.contains(MovingStorageItem.getStorageItem(basicBarrelBoat).getItem()));
		assertTrue(sourceStorageItems.contains(MovingStorageItem.getStorageItem(copperBarrelBoat).getItem()));
	}

	@Test
	void storageBoatTierUpgradeUsageWorksForPreviousTierUpgradeResult() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack basicBarrelBoat = movingStorage(woodStorageStack(ModBlocks.BARREL_ITEM.get()), WoodType.OAK);
		ItemStack ironBarrelBoat = movingStorage(woodStorageStack(ModBlocks.IRON_BARREL_ITEM.get()), WoodType.OAK);
		ItemStack goldBarrelBoat = movingStorage(woodStorageStack(ModBlocks.GOLD_BARREL_ITEM.get()), WoodType.OAK);
		ItemStack previousTierResult = catalog.getCraftingRecipesFor(ironBarrelBoat).stream()
				.flatMap(view -> view.variants().stream())
				.filter(variant -> isSameMovingStorageItemAndBoat(basicBarrelBoat, getSource(variant)))
				.findFirst()
				.orElseThrow()
				.firstOutput();

		List<CraftingDisplayVariant> usages = catalog.getCraftingUsagesFor(previousTierResult).stream().flatMap(view -> view.variants().stream()).toList();

		assertEquals(1, usages.size());
		assertSameMovingStorage(previousTierResult, getSource(usages.getFirst()));
		assertSameMovingStorage(goldBarrelBoat, usages.getFirst().firstOutput());
	}

	@Test
	void storageBoatRecipeShowsAssemblyAndTierUpgradeRecipesForWoodStorage() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack goldBarrelBoat = movingStorage(woodStorageStack(ModBlocks.GOLD_BARREL_ITEM.get()), WoodType.OAK);
		ItemStack diamondBarrel = woodStorageStack(ModBlocks.DIAMOND_BARREL_ITEM.get());
		ItemStack diamondBarrelBoat = movingStorage(diamondBarrel, WoodType.OAK);

		List<CraftingDisplayVariant> recipes = catalog.getCraftingRecipesFor(diamondBarrelBoat).stream().flatMap(view -> view.variants().stream()).toList();
		List<CraftingDisplayVariant> assemblyRecipes = recipes.stream().filter(variant -> ItemStack.isSameItemSameComponents(diamondBarrel, getSource(variant))).toList();
		List<CraftingDisplayVariant> tierUpgradeRecipes = recipes.stream().filter(variant -> isSameMovingStorage(goldBarrelBoat, getSource(variant))).toList();

		assertEquals(1, assemblyRecipes.size());
		assertSameStack(diamondBarrel, getSource(assemblyRecipes.getFirst()));
		assertSameMovingStorage(diamondBarrelBoat, assemblyRecipes.getFirst().firstOutput());
		assertEquals(1, tierUpgradeRecipes.size());
		assertSameMovingStorage(goldBarrelBoat, getSource(tierUpgradeRecipes.getFirst()));
		assertSameMovingStorage(diamondBarrelBoat, tierUpgradeRecipes.getFirst().firstOutput());
	}

	@Test
	void storageBoatRecipeShowsAssemblyForFocusedTintedStorage() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedGoldBarrelBoat = movingStorage(tintedStack(ModBlocks.GOLD_BARREL_ITEM.get()), WoodType.OAK);
		ItemStack tintedDiamondBarrel = tintedStack(ModBlocks.DIAMOND_BARREL_ITEM.get());
		ItemStack tintedDiamondBarrelBoat = movingStorage(tintedDiamondBarrel, WoodType.OAK);

		List<CraftingDisplayVariant> recipes = catalog.getCraftingRecipesFor(tintedDiamondBarrelBoat).stream().flatMap(view -> view.variants().stream()).toList();
		List<CraftingDisplayVariant> assemblyRecipes = recipes.stream().filter(variant -> ItemStack.isSameItemSameComponents(tintedDiamondBarrel, getSource(variant))).toList();
		List<CraftingDisplayVariant> tierUpgradeRecipes = recipes.stream().filter(variant -> isSameMovingStorage(tintedGoldBarrelBoat, getSource(variant))).toList();

		assertEquals(1, assemblyRecipes.size());
		assertSameStack(tintedDiamondBarrel, getSource(assemblyRecipes.getFirst()));
		assertSameMovingStorage(tintedDiamondBarrelBoat, assemblyRecipes.getFirst().firstOutput());
		assertEquals(1, tierUpgradeRecipes.size());
		assertSameMovingStorage(tintedGoldBarrelBoat, getSource(tierUpgradeRecipes.getFirst()));
		assertSameMovingStorage(tintedDiamondBarrelBoat, tierUpgradeRecipes.getFirst().firstOutput());
	}

	@Test
	void tintedStorageBoatTierUpgradeUsagePreservesTintInNextTierResult() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedIronBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.IRON_BARREL_ITEM.get()), WoodType.OAK);
		ItemStack tintedGoldBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.GOLD_BARREL_ITEM.get()), WoodType.OAK);

		List<CraftingDisplayVariant> usages = catalog.getCraftingUsagesFor(tintedIronBarrelBoat).stream().flatMap(view -> view.variants().stream()).toList();

		assertEquals(1, usages.size());
		assertSameMovingStorage(tintedIronBarrelBoat, getSource(usages.getFirst()));
		assertSameMovingStorage(tintedGoldBarrelBoat, usages.getFirst().firstOutput());
	}

	@Test
	void tintedStorageBoatTierUpgradeRecipePreservesTintInSource() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedIronBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.IRON_BARREL_ITEM.get()), WoodType.OAK);
		ItemStack tintedGoldBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.GOLD_BARREL_ITEM.get()), WoodType.OAK);

		List<CraftingDisplayVariant> recipes = catalog.getCraftingRecipesFor(tintedGoldBarrelBoat).stream().flatMap(view -> view.variants().stream()).filter(MovingStorageRecipeViewerDisplaySpecTest::hasMovingStorageSource).toList();
		CraftingDisplayVariant recipe = recipes.stream()
				.filter(variant -> isSameMovingStorage(tintedIronBarrelBoat, getSource(variant)) && isSameMovingStorage(tintedGoldBarrelBoat, variant.firstOutput()))
				.findFirst()
				.orElseThrow();

		assertTrue(WoodStorageBlockItem.getWoodType(MovingStorageItem.getStorageItem(getSource(recipe))).isEmpty());
	}

	@Test
	void tintedStorageBoatTierUpgradeVariantsAreAvailableForStaticViewerRegistration() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedIronBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.IRON_BARREL_ITEM.get()), WoodType.OAK);
		ItemStack tintedGoldBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.GOLD_BARREL_ITEM.get()), WoodType.OAK);

		List<CraftingDisplayVariant> allDisplays = catalog.getCraftingSpecs().stream().flatMap(spec -> spec.getAllDisplays().stream()).filter(MovingStorageRecipeViewerDisplaySpecTest::hasMovingStorageSource).toList();

		assertTrue(allDisplays.stream().anyMatch(variant -> isSameMovingStorage(tintedIronBarrelBoat, getSource(variant)) && isSameMovingStorage(tintedGoldBarrelBoat, variant.firstOutput())));
	}

	@Test
	void tintedStorageMinecartTierUpgradeRecipePreservesNoWoodInSource() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedIronBarrelMinecart = movingStorageMinecart(singleColorTintStack(ModBlocks.IRON_BARREL_ITEM.get()));
		ItemStack tintedGoldBarrelMinecart = movingStorageMinecart(singleColorTintStack(ModBlocks.GOLD_BARREL_ITEM.get()));

		List<CraftingDisplayVariant> recipes = catalog.getCraftingRecipesFor(tintedGoldBarrelMinecart).stream().flatMap(view -> view.variants().stream()).filter(MovingStorageRecipeViewerDisplaySpecTest::hasMovingStorageSource).toList();
		CraftingDisplayVariant recipe = recipes.stream()
				.filter(variant -> isSameMovingStorage(tintedIronBarrelMinecart, getSource(variant)) && isSameMovingStorage(tintedGoldBarrelMinecart, variant.firstOutput()))
				.findFirst()
				.orElseThrow();

		assertTrue(WoodStorageBlockItem.getWoodType(MovingStorageItem.getStorageItem(getSource(recipe))).isEmpty());
	}

	private static IRecipeViewerDisplayCatalog createCatalog() {
		IRecipeViewerDisplayCatalog catalog = new RecipeViewerDisplayCatalog();
		try (TestRecipeResources.LoadedResources resources = TestRecipeResources.load(); MockedStatic<ClientRecipeHelper> clientRecipeHelper = Mockito.mockStatic(ClientRecipeHelper.class, Mockito.CALLS_REAL_METHODS)) {
			mockClientRecipeHelper(clientRecipeHelper, resources);
			MovingStorageSubtypeInterpreter subtypeInterpreter = new MovingStorageSubtypeInterpreter();
			IRecipeViewerDisplayContext context = stack -> stack.getItem() instanceof MovingStorageItem ? Optional.of(subtypeInterpreter) : Optional.empty();
			MovingStorageRecipeViewerDisplays.register(catalog, context);
		}
		return catalog;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void mockClientRecipeHelper(MockedStatic<ClientRecipeHelper> clientRecipeHelper, TestRecipeResources.LoadedResources resources) {
		RecipeManager recipeManager = resources.recipeManager();
		clientRecipeHelper.when(() -> ClientRecipeHelper.transformAllRecipeHoldersOfTypeIntoMultiple(Mockito.eq(RecipeType.CRAFTING), Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
			RecipeType recipeType = invocation.getArgument(0);
			Class recipeClass = invocation.getArgument(1);
			return ClientRecipeHelper.transformAllRecipeHoldersOfTypeIntoMultiple(recipeManager, recipeType, recipeClass, recipeHolder -> invocation.<java.util.function.Function<RecipeHolder, List>>getArgument(2).apply(normalizeRecipeHolder(recipeHolder, resources)));
		});
		clientRecipeHelper.when(() -> ClientRecipeHelper.assemble(Mockito.any(), Mockito.any())).thenAnswer(invocation -> assembleRecipe(invocation.getArgument(0), invocation.getArgument(1), resources));
		clientRecipeHelper.when(() -> ClientRecipeHelper.getResultItem(Mockito.any())).thenAnswer(invocation -> ClientRecipeHelper.getResultItem(invocation.getArgument(0), resources.registryLookup()));
	}

	private static ItemStack assembleRecipe(Recipe<CraftingInput> recipe, CraftingInput input, TestRecipeResources.LoadedResources resources) {
		if (recipe instanceof MovingStorageTierUpgradeShapedRecipe || recipe instanceof MovingStorageTierUpgradeShapelessRecipe) {
			ItemStack result = ClientRecipeHelper.getResultItem(recipe, resources.registryLookup()).copy();
			for (int slot = 0; slot < input.size(); slot++) {
				ItemStack slotStack = input.getItem(slot);
				if (slotStack.getItem() instanceof MovingStorageItem) {
					ItemStack sourceStorage = MovingStorageItem.getStorageItem(slotStack);
					ItemStack resultStorage = MovingStorageItem.getStorageItem(result);
					resultStorage.applyComponents(sourceStorage.getComponents());
					result.applyComponents(slotStack.getComponents());
					MovingStorageItem.setStorageItem(result, resultStorage);
					return result;
				}
			}
		}
		return ClientRecipeHelper.assemble(recipe, input, resources.registryLookup());
	}

	private static RecipeHolder<?> normalizeRecipeHolder(RecipeHolder<?> recipeHolder, TestRecipeResources.LoadedResources resources) {
		if (recipeHolder.value() instanceof MovingStorageFromStorageRecipe movingStorageFromStorageRecipe) {
			NonNullList<Ingredient> ingredients = NonNullList.createWithCapacity(movingStorageFromStorageRecipe.getIngredients().size());
			for (Ingredient ingredient : movingStorageFromStorageRecipe.getIngredients()) {
				ingredients.add(Ingredient.of(ingredient.items().map(ItemStack::new).map(ItemStack::getItem)));
			}
			ShapelessRecipe compose = new ShapelessRecipe(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""), ItemStackTemplate.fromNonEmptyStack(ClientRecipeHelper.getResultItem(movingStorageFromStorageRecipe, resources.registryLookup())), ingredients);
			return new RecipeHolder<>(recipeHolder.id(), new MovingStorageFromStorageRecipe(compose));
		}
		return recipeHolder;
	}

	private static ItemStack movingStorage(ItemStack storageStack, WoodType boatType) {
		return MovingStorageItem.createWithStorage(StorageBoatItem.setWoodType(new ItemStack(ModItems.STORAGE_BOAT.get()), boatType), storageStack);
	}

	private static ItemStack movingStorageMinecart(ItemStack storageStack) {
		return MovingStorageItem.createWithStorage(new ItemStack(ModItems.STORAGE_MINECART.get()), storageStack);
	}

	private static ItemStack woodStorageStack(Item item) {
		return WoodStorageBlockItem.setWoodType(new ItemStack(item), WoodType.ACACIA);
	}

	private static ItemStack generatedTintStack(Item item) {
		ItemStack stack = new ItemStack(item);
		if (item instanceof StorageBlockItem storageBlockItem) {
			storageBlockItem.setMainColor(stack, DyeColor.YELLOW.getTextureDiffuseColor());
			storageBlockItem.setAccentColor(stack, DyeColor.LIME.getTextureDiffuseColor());
		}
		return stack;
	}

	private static ItemStack singleColorTintStack(Item item) {
		ItemStack stack = new ItemStack(item);
		if (item instanceof StorageBlockItem storageBlockItem) {
			storageBlockItem.setMainColor(stack, DyeColor.YELLOW.getTextureDiffuseColor());
			storageBlockItem.setAccentColor(stack, DyeColor.YELLOW.getTextureDiffuseColor());
		}
		return stack;
	}

	private static ItemStack tintedStack(Item item) {
		ItemStack stack = new ItemStack(item);
		if (item instanceof StorageBlockItem storageBlockItem) {
			storageBlockItem.setMainColor(stack, 0x336699);
			storageBlockItem.setAccentColor(stack, 0x99CC33);
		}
		return stack;
	}

	private static ItemStack getSource(CraftingDisplayVariant variant) {
		return variant.inputs().stream().filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
	}

	private static boolean hasMovingStorageSource(CraftingDisplayVariant variant) {
		return getSource(variant).getItem() instanceof MovingStorageItem;
	}

	private static void assertSameStack(ItemStack expected, ItemStack actual) {
		assertTrue(ItemStack.isSameItemSameComponents(expected, actual), "Expected " + expected + " but got " + actual);
	}

	private static void assertSameMovingStorage(ItemStack expected, ItemStack actual) {
		assertTrue(isSameMovingStorage(expected, actual), "Expected " + expected + " but got " + actual);
	}

	private static boolean isSameMovingStorage(ItemStack expected, ItemStack actual) {
		if (!ItemStack.isSameItem(expected, actual)) {
			return false;
		}
		if (expected.getItem() instanceof StorageBoatItem && StorageBoatItem.getWoodType(expected) != StorageBoatItem.getWoodType(actual)) {
			return false;
		}
		ItemStack expectedStorage = MovingStorageItem.getStorageItem(expected);
		ItemStack actualStorage = MovingStorageItem.getStorageItem(actual);
		return ItemStack.isSameItem(expectedStorage, actualStorage)
				&& Objects.equals(StorageBlockItem.getMainColorFromComponentHolder(expectedStorage), StorageBlockItem.getMainColorFromComponentHolder(actualStorage))
				&& Objects.equals(StorageBlockItem.getAccentColorFromComponentHolder(expectedStorage), StorageBlockItem.getAccentColorFromComponentHolder(actualStorage));
	}

	private static boolean isSameMovingStorageItemAndBoat(ItemStack expected, ItemStack actual) {
		if (!ItemStack.isSameItem(expected, actual)) {
			return false;
		}
		if (expected.getItem() instanceof StorageBoatItem && StorageBoatItem.getWoodType(expected) != StorageBoatItem.getWoodType(actual)) {
			return false;
		}
		return ItemStack.isSameItem(MovingStorageItem.getStorageItem(expected), MovingStorageItem.getStorageItem(actual));
	}

	private final static class TestRecipeResources {
		private TestRecipeResources() {
		}

		private static LoadedResources load() {
			SharedConstants.tryDetectVersion();
			Bootstrap.bootStrap();

			ExecutorService backgroundExecutor = Executors.newFixedThreadPool(2);
			Executor gameExecutor = Runnable::run;
			try {
				PackRepository packRepository = ServerPacksSource.createVanillaTrustedRepository();
				WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, WorldDataConfiguration.DEFAULT, false, false);
				WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, PermissionSet.NO_PERMISSIONS);

				return WorldLoader.load(
						initConfig,
						context -> new WorldLoader.DataLoadOutput<>(UnitCookie.INSTANCE, context.datapackDimensions()),
						(resourceManager, resources, registries, cookie) -> new LoadedResources(resourceManager, resources, registries),
						backgroundExecutor,
						gameExecutor
				).join();
			} finally {
				backgroundExecutor.shutdown();
			}
		}

		private enum UnitCookie {
			INSTANCE
		}

		private record LoadedResources(CloseableResourceManager resourceManager, ReloadableServerResources serverResources, LayeredRegistryAccess<RegistryLayer> registries) implements AutoCloseable {
			private RecipeManager recipeManager() {
				return serverResources.getRecipeManager();
			}

			private net.minecraft.core.HolderLookup.Provider registryLookup() {
				return serverResources.getRegistryLookup();
			}

			@Override
			public void close() {
				resourceManager.close();
			}
		}
	}
}
