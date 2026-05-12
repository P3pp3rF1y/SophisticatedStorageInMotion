package net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common;

import net.minecraft.SharedConstants;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.p3pp3rf1y.sophisticatedcore.compat.recipeviewers.common.*;
import net.p3pp3rf1y.sophisticatedcore.SophisticatedCore;
import net.p3pp3rf1y.sophisticatedcore.init.ModRecipes;
import net.p3pp3rf1y.sophisticatedcore.util.ColorHelper;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.init.ModBlocks;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.WoodStorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.compat.recipeviewers.common.subtypes.MovingStorageSubtypeInterpreter;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageFromStorageRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageIngredient;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapedRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.crafting.MovingStorageTierUpgradeShapelessRecipe;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.VanillaIngredientSerializer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.forgespi.language.IModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.GameData;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
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
				.filter(variant -> ItemStack.isSameItemSameTags(tintedBarrel, getSource(variant)))
				.findFirst()
				.orElseThrow();
		assertSameStack(tintedBarrel, getSource(usage));
		assertSameStack(tintedBarrel, MovingStorageItem.getStorageItem(usage.firstOutput()));
	}

	@Test
	void higherTierStorageBoatRecipeDoesNotDuplicateAcrossTierUpgradeVariants() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack basicBarrelBoat = movingStorage(woodStorageStack(ModBlocks.BARREL_ITEM.get()), Boat.Type.OAK);
		ItemStack ironBarrelBoat = movingStorage(woodStorageStack(ModBlocks.IRON_BARREL_ITEM.get()), Boat.Type.OAK);
		ItemStack copperBarrelBoat = movingStorage(woodStorageStack(ModBlocks.COPPER_BARREL_ITEM.get()), Boat.Type.OAK);

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
		ItemStack basicBarrelBoat = movingStorage(woodStorageStack(ModBlocks.BARREL_ITEM.get()), Boat.Type.OAK);
		ItemStack ironBarrelBoat = movingStorage(woodStorageStack(ModBlocks.IRON_BARREL_ITEM.get()), Boat.Type.OAK);
		ItemStack goldBarrelBoat = movingStorage(woodStorageStack(ModBlocks.GOLD_BARREL_ITEM.get()), Boat.Type.OAK);
		ItemStack previousTierResult = catalog.getCraftingRecipesFor(ironBarrelBoat).stream()
				.flatMap(view -> view.variants().stream())
				.filter(variant -> isSameMovingStorageItemAndBoat(basicBarrelBoat, getSource(variant)))
				.findFirst()
				.orElseThrow()
				.firstOutput();

		List<CraftingDisplayVariant> usages = catalog.getCraftingUsagesFor(previousTierResult).stream().flatMap(view -> view.variants().stream()).toList();

		assertEquals(1, usages.size());
		assertSameMovingStorage(previousTierResult, getSource(usages.get(0)));
		assertSameMovingStorage(goldBarrelBoat, usages.get(0).firstOutput());
	}

	@Test
	void storageBoatRecipeShowsAssemblyAndTierUpgradeRecipesForWoodStorage() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack goldBarrelBoat = movingStorage(woodStorageStack(ModBlocks.GOLD_BARREL_ITEM.get()), Boat.Type.OAK);
		ItemStack diamondBarrel = woodStorageStack(ModBlocks.DIAMOND_BARREL_ITEM.get());
		ItemStack diamondBarrelBoat = movingStorage(diamondBarrel, Boat.Type.OAK);

		List<CraftingDisplayVariant> recipes = catalog.getCraftingRecipesFor(diamondBarrelBoat).stream().flatMap(view -> view.variants().stream()).toList();
		List<CraftingDisplayVariant> assemblyRecipes = recipes.stream().filter(variant -> ItemStack.isSameItemSameTags(diamondBarrel, getSource(variant))).toList();
		List<CraftingDisplayVariant> tierUpgradeRecipes = recipes.stream().filter(variant -> isSameMovingStorage(goldBarrelBoat, getSource(variant))).toList();

		assertEquals(1, assemblyRecipes.size());
		assertSameStack(diamondBarrel, getSource(assemblyRecipes.get(0)));
		assertSameMovingStorage(diamondBarrelBoat, assemblyRecipes.get(0).firstOutput());
		assertEquals(1, tierUpgradeRecipes.size());
		assertSameMovingStorage(goldBarrelBoat, getSource(tierUpgradeRecipes.get(0)));
		assertSameMovingStorage(diamondBarrelBoat, tierUpgradeRecipes.get(0).firstOutput());
	}

	@Test
	void storageBoatRecipeShowsAssemblyForFocusedTintedStorage() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedGoldBarrelBoat = movingStorage(tintedStack(ModBlocks.GOLD_BARREL_ITEM.get()), Boat.Type.OAK);
		ItemStack tintedDiamondBarrel = tintedStack(ModBlocks.DIAMOND_BARREL_ITEM.get());
		ItemStack tintedDiamondBarrelBoat = movingStorage(tintedDiamondBarrel, Boat.Type.OAK);

		List<CraftingDisplayVariant> recipes = catalog.getCraftingRecipesFor(tintedDiamondBarrelBoat).stream().flatMap(view -> view.variants().stream()).toList();
		List<CraftingDisplayVariant> assemblyRecipes = recipes.stream().filter(variant -> ItemStack.isSameItemSameTags(tintedDiamondBarrel, getSource(variant))).toList();
		List<CraftingDisplayVariant> tierUpgradeRecipes = recipes.stream().filter(variant -> isSameMovingStorage(tintedGoldBarrelBoat, getSource(variant))).toList();

		assertEquals(1, assemblyRecipes.size());
		assertSameStack(tintedDiamondBarrel, getSource(assemblyRecipes.get(0)));
		assertSameMovingStorage(tintedDiamondBarrelBoat, assemblyRecipes.get(0).firstOutput());
		assertEquals(1, tierUpgradeRecipes.size());
		assertSameMovingStorage(tintedGoldBarrelBoat, getSource(tierUpgradeRecipes.get(0)));
		assertSameMovingStorage(tintedDiamondBarrelBoat, tierUpgradeRecipes.get(0).firstOutput());
	}

	@Test
	void tintedStorageBoatTierUpgradeUsagePreservesTintInNextTierResult() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedIronBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.IRON_BARREL_ITEM.get()), Boat.Type.OAK);
		ItemStack tintedGoldBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.GOLD_BARREL_ITEM.get()), Boat.Type.OAK);

		List<CraftingDisplayVariant> usages = catalog.getCraftingUsagesFor(tintedIronBarrelBoat).stream().flatMap(view -> view.variants().stream()).toList();

		assertEquals(1, usages.size());
		assertSameMovingStorage(tintedIronBarrelBoat, getSource(usages.get(0)));
		assertSameMovingStorage(tintedGoldBarrelBoat, usages.get(0).firstOutput());
	}

	@Test
	void tintedStorageBoatTierUpgradeRecipePreservesTintInSource() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedIronBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.IRON_BARREL_ITEM.get()), Boat.Type.OAK);
		ItemStack tintedGoldBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.GOLD_BARREL_ITEM.get()), Boat.Type.OAK);

		List<CraftingDisplayVariant> recipes = catalog.getCraftingRecipesFor(tintedGoldBarrelBoat).stream().flatMap(view -> view.variants().stream()).filter(MovingStorageRecipeViewerDisplaySpecTest::hasMovingStorageSource).toList();

		assertEquals(1, recipes.size());
		assertSameMovingStorage(tintedIronBarrelBoat, getSource(recipes.get(0)));
		assertTrue(WoodStorageBlockItem.getWoodType(MovingStorageItem.getStorageItem(getSource(recipes.get(0)))).isEmpty());
		assertSameMovingStorage(tintedGoldBarrelBoat, recipes.get(0).firstOutput());
	}

	@Test
	void tintedStorageBoatTierUpgradeVariantsAreAvailableForStaticViewerRegistration() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedIronBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.IRON_BARREL_ITEM.get()), Boat.Type.OAK);
		ItemStack tintedGoldBarrelBoat = movingStorage(singleColorTintStack(ModBlocks.GOLD_BARREL_ITEM.get()), Boat.Type.OAK);

		List<CraftingDisplayVariant> allDisplays = catalog.getCraftingSpecs().stream().flatMap(spec -> spec.getAllDisplays().stream()).filter(MovingStorageRecipeViewerDisplaySpecTest::hasMovingStorageSource).toList();

		assertTrue(allDisplays.stream().anyMatch(variant -> isSameMovingStorage(tintedIronBarrelBoat, getSource(variant)) && isSameMovingStorage(tintedGoldBarrelBoat, variant.firstOutput())));
	}

	@Test
	void tintedStorageMinecartTierUpgradeRecipePreservesNoWoodInSource() {
		IRecipeViewerDisplayCatalog catalog = createCatalog();
		ItemStack tintedIronBarrelMinecart = movingStorageMinecart(singleColorTintStack(ModBlocks.IRON_BARREL_ITEM.get()));
		ItemStack tintedGoldBarrelMinecart = movingStorageMinecart(singleColorTintStack(ModBlocks.GOLD_BARREL_ITEM.get()));

		List<CraftingDisplayVariant> recipes = catalog.getCraftingRecipesFor(tintedGoldBarrelMinecart).stream().flatMap(view -> view.variants().stream()).filter(MovingStorageRecipeViewerDisplaySpecTest::hasMovingStorageSource).toList();

		assertEquals(1, recipes.size());
		assertSameMovingStorage(tintedIronBarrelMinecart, getSource(recipes.get(0)));
		assertTrue(WoodStorageBlockItem.getWoodType(MovingStorageItem.getStorageItem(getSource(recipes.get(0)))).isEmpty());
		assertSameMovingStorage(tintedGoldBarrelMinecart, recipes.get(0).firstOutput());
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
		clientRecipeHelper.when(() -> ClientRecipeHelper.transformAllRecipesOfTypeIntoMultiple(Mockito.eq(RecipeType.CRAFTING), Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
			RecipeType recipeType = invocation.getArgument(0);
			Class recipeClass = invocation.getArgument(1);
			return ClientRecipeHelper.transformAllRecipesOfTypeIntoMultiple(recipeManager, recipeType, recipeClass, recipe -> invocation.<java.util.function.Function<Recipe, List>>getArgument(2).apply(normalizeRecipe(recipe, resources)));
		});
		clientRecipeHelper.when(() -> ClientRecipeHelper.assemble(Mockito.any(), Mockito.any())).thenAnswer(invocation -> assembleRecipe(invocation.getArgument(0), invocation.getArgument(1), resources));
		clientRecipeHelper.when(() -> ClientRecipeHelper.getResultItem(Mockito.any())).thenAnswer(invocation -> ClientRecipeHelper.getResultItem(invocation.getArgument(0), resources.registryLookup()));
	}

	private static ItemStack assembleRecipe(Recipe<CraftingContainer> recipe, CraftingContainer input, TestRecipeResources.LoadedResources resources) {
		if (recipe instanceof MovingStorageTierUpgradeShapedRecipe || recipe instanceof MovingStorageTierUpgradeShapelessRecipe) {
			ItemStack result = ClientRecipeHelper.getResultItem(recipe, resources.registryLookup()).copy();
			for (int slot = 0; slot < input.getContainerSize(); slot++) {
				ItemStack slotStack = input.getItem(slot);
				if (slotStack.getItem() instanceof MovingStorageItem) {
					ItemStack sourceStorage = MovingStorageItem.getStorageItem(slotStack);
					ItemStack resultStorage = MovingStorageItem.getStorageItem(result);
					if (sourceStorage.hasTag()) {
						resultStorage.setTag(sourceStorage.getTag().copy());
					}
					if (slotStack.hasTag()) {
						result.setTag(slotStack.getTag().copy());
					}
					MovingStorageItem.setStorageItem(result, resultStorage);
					return result;
				}
			}
		}
		return ClientRecipeHelper.assemble(recipe, input, resources.registryLookup());
	}

	private static Recipe<?> normalizeRecipe(Recipe<?> recipe, TestRecipeResources.LoadedResources resources) {
		if (recipe instanceof MovingStorageFromStorageRecipe movingStorageFromStorageRecipe) {
			NonNullList<Ingredient> ingredients = NonNullList.createWithCapacity(movingStorageFromStorageRecipe.getIngredients().size());
			for (Ingredient ingredient : movingStorageFromStorageRecipe.getIngredients()) {
				ingredients.add(Ingredient.of(ingredient.getItems()));
			}
			ShapelessRecipe compose = new ShapelessRecipe(recipe.getId(), movingStorageFromStorageRecipe.getGroup(), CraftingBookCategory.MISC, movingStorageFromStorageRecipe.getResultItem(resources.registryLookup()), ingredients);
			return new MovingStorageFromStorageRecipe(compose);
		}
		return recipe;
	}

	private static ItemStack movingStorage(ItemStack storageStack, Boat.Type boatType) {
		return MovingStorageItem.createWithStorage(StorageBoatItem.setBoatType(new ItemStack(ModItems.STORAGE_BOAT.get()), boatType), storageStack);
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
			storageBlockItem.setMainColor(stack, ColorHelper.getColor(DyeColor.YELLOW.getTextureDiffuseColors()));
			storageBlockItem.setAccentColor(stack, ColorHelper.getColor(DyeColor.LIME.getTextureDiffuseColors()));
		}
		return stack;
	}

	private static ItemStack singleColorTintStack(Item item) {
		ItemStack stack = new ItemStack(item);
		if (item instanceof StorageBlockItem storageBlockItem) {
			int color = ColorHelper.getColor(DyeColor.YELLOW.getTextureDiffuseColors());
			storageBlockItem.setMainColor(stack, color);
			storageBlockItem.setAccentColor(stack, color);
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
		assertTrue(ItemStack.isSameItemSameTags(expected, actual), "Expected " + expected + " but got " + actual);
	}

	private static void assertSameMovingStorage(ItemStack expected, ItemStack actual) {
		assertTrue(isSameMovingStorage(expected, actual), "Expected " + expected + " but got " + actual);
	}

	private static boolean isSameMovingStorage(ItemStack expected, ItemStack actual) {
		if (!ItemStack.isSameItem(expected, actual)) {
			return false;
		}
		if (expected.getItem() instanceof StorageBoatItem && StorageBoatItem.getBoatType(expected) != StorageBoatItem.getBoatType(actual)) {
			return false;
		}
		ItemStack expectedStorage = MovingStorageItem.getStorageItem(expected);
		ItemStack actualStorage = MovingStorageItem.getStorageItem(actual);
		return ItemStack.isSameItem(expectedStorage, actualStorage)
				&& Objects.equals(StorageBlockItem.getMainColorFromStack(expectedStorage), StorageBlockItem.getMainColorFromStack(actualStorage))
				&& Objects.equals(StorageBlockItem.getAccentColorFromStack(expectedStorage), StorageBlockItem.getAccentColorFromStack(actualStorage));
	}

	private static boolean isSameMovingStorageItemAndBoat(ItemStack expected, ItemStack actual) {
		if (!ItemStack.isSameItem(expected, actual)) {
			return false;
		}
		if (expected.getItem() instanceof StorageBoatItem && StorageBoatItem.getBoatType(expected) != StorageBoatItem.getBoatType(actual)) {
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
			ForgeTestModList.install(SophisticatedCore.MOD_ID, SophisticatedStorage.MOD_ID, SophisticatedStorageInMotion.MOD_ID);
			ForgeTestRegistries.installMovingStorage();

			ExecutorService backgroundExecutor = Executors.newFixedThreadPool(2);
			Executor gameExecutor = Runnable::run;
			try {
				PackRepository packRepository = new PackRepository(new ServerPacksSource());
				WorldLoader.PackConfig packConfig = new WorldLoader.PackConfig(packRepository, WorldDataConfiguration.DEFAULT, false, false);
				WorldLoader.InitConfig initConfig = new WorldLoader.InitConfig(packConfig, Commands.CommandSelection.INTEGRATED, 0);

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

		private final static class ForgeTestModList {
			private static final Map<String, Path> MERGED_RESOURCE_ROOTS = new HashMap<>();

			private ForgeTestModList() {
			}

			private static void install(String... modIds) {
				List<IModFileInfo> modFiles = new ArrayList<>();
				List<IModInfo> mods = new ArrayList<>();
				Map<String, Object> indexedMods = new HashMap<>();

				for (String modId : modIds) {
					Path moduleRoot = moduleRoot(modId);
					Path testResources = moduleRoot.resolve(Path.of("src", "test", "resources"));
					Path mainResources = moduleRoot.resolve(Path.of("src", "main", "resources"));
					Path generatedResources = moduleRoot.resolve(Path.of("src", "generated", "resources"));
					Path mergedResources = mergedResourceRoot(modId, moduleRoot, List.of(mainResources, generatedResources, testResources));

					IModInfo modInfo = proxy(IModInfo.class, (proxy, method, args) -> switch (method.getName()) {
						case "getModId", "getNamespace" -> modId;
						case "getDisplayName" -> modId;
						default -> defaultValue(method);
					});
					IModFile modFile = proxy(IModFile.class, (proxy, method, args) -> switch (method.getName()) {
						case "getModInfos" -> List.of(modInfo);
						case "getFileName" -> moduleRoot.getFileName().toString();
						case "getFilePath" -> mergedResources;
						case "findResource" -> findResource(mergedResources, (String[]) args[0]);
						default -> defaultValue(method);
					});
					IModFileInfo modFileInfo = proxy(IModFileInfo.class, (proxy, method, args) -> switch (method.getName()) {
						case "getFile" -> modFile;
						case "getMods" -> List.of(modInfo);
						case "requiredLanguageLoaders" -> List.of();
						default -> defaultValue(method);
					});

					modFiles.add(modFileInfo);
					mods.add(modInfo);
					indexedMods.put(modId, new Object());
				}

				ModList modList = ModList.of(List.<ModFile>of(), List.<ModInfo>of());
				setField(modList, "modFiles", modFiles);
				setField(modList, "sortedList", mods);
				setField(modList, "fileById", Map.of());
				setField(modList, "mods", List.of());
				setField(modList, "indexedMods", indexedMods);
			}

			private static Path moduleRoot(String modId) {
				String moduleName = switch (modId) {
					case SophisticatedCore.MOD_ID -> "SophisticatedCore";
					case SophisticatedStorage.MOD_ID -> "SophisticatedStorage";
					case SophisticatedStorageInMotion.MOD_ID -> "SophisticatedStorageInMotion";
					default -> throw new IllegalArgumentException("Unknown test mod " + modId);
				};
				Path workingDir = Path.of("").toAbsolutePath();
				if (workingDir.getFileName().toString().equals(moduleName)) {
					return workingDir;
				}
				Path child = workingDir.resolve(moduleName);
				if (Files.isDirectory(child)) {
					return child;
				}
				Path sibling = workingDir.getParent().resolve(moduleName);
				if (Files.isDirectory(sibling)) {
					return sibling;
				}
				throw new IllegalStateException("Unable to locate module " + moduleName + " from " + workingDir);
			}

			private static synchronized Path mergedResourceRoot(String modId, Path moduleRoot, List<Path> roots) {
				Path existingRoot = MERGED_RESOURCE_ROOTS.get(modId);
				if (existingRoot != null) {
					return existingRoot;
				}

				String workerId = System.getProperty("org.gradle.test.worker", "main");
				Path mergedResources = moduleRoot.resolve(Path.of("build", "recipe-viewer-test-resources", MovingStorageRecipeViewerDisplaySpecTest.class.getSimpleName() + "-" + workerId, modId));
				try {
					deleteRecursively(mergedResources);
					Files.createDirectories(mergedResources);
					for (Path root : roots) {
						copyResources(root, mergedResources);
					}
				} catch (IOException e) {
					throw new IllegalStateException("Unable to prepare merged test resources for " + modId, e);
				}
				MERGED_RESOURCE_ROOTS.put(modId, mergedResources);
				return mergedResources;
			}

			private static void copyResources(Path root, Path targetRoot) throws IOException {
				if (!Files.isDirectory(root)) {
					return;
				}
				try (Stream<Path> paths = Files.walk(root)) {
					paths.filter(Files::isRegularFile).forEach(source -> {
						Path target = targetRoot.resolve(root.relativize(source));
						try {
							Files.createDirectories(target.getParent());
							Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
				} catch (UncheckedIOException e) {
					throw e.getCause();
				}
			}

			private static void deleteRecursively(Path path) throws IOException {
				if (!Files.exists(path)) {
					return;
				}
				try (Stream<Path> paths = Files.walk(path)) {
					for (Path child : paths.sorted(Comparator.reverseOrder()).toList()) {
						Files.delete(child);
					}
				}
			}

			private static Path findResource(Path root, String[] path) {
				Path relativePath = Path.of(String.join("/", path));
				return root.resolve(relativePath);
			}

			@SuppressWarnings("unchecked")
			private static <T> T proxy(Class<T> type, InvocationHandler handler) {
				return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, (proxy, method, args) -> {
					if (method.getDeclaringClass() == Object.class) {
						return switch (method.getName()) {
							case "toString" -> type.getSimpleName() + " test proxy";
							case "hashCode" -> System.identityHashCode(proxy);
							case "equals" -> proxy == args[0];
							default -> defaultValue(method);
						};
					}
					return handler.invoke(proxy, method, args);
				});
			}

			private static Object defaultValue(Method method) {
				Class<?> returnType = method.getReturnType();
				if (returnType == boolean.class) {
					return false;
				}
				if (returnType == int.class) {
					return 0;
				}
				return null;
			}

			private static void setField(ModList modList, String name, Object value) {
				try {
					Field field = ModList.class.getDeclaredField(name);
					field.setAccessible(true);
					field.set(modList, value);
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException("Unable to initialize Forge test mod list", e);
				}
			}
		}

		private final static class ForgeTestRegistries {
			private static boolean installed;

			private ForgeTestRegistries() {
			}

			private static void installMovingStorage() {
				if (installed) {
					return;
				}
				installed = true;
				GameData.unfreezeData();

				registerDeferred(ModBlocks.class, "BLOCKS", ForgeRegistries.BLOCKS);
				registerDeferred(ModBlocks.class, "ITEMS", ForgeRegistries.ITEMS);
				registerDeferred(net.p3pp3rf1y.sophisticatedstorage.init.ModItems.class, "ITEMS", ForgeRegistries.ITEMS);
				registerDeferred(ModItems.class, "ITEMS", ForgeRegistries.ITEMS);
				registerDeferred(ModRecipes.class, "RECIPE_SERIALIZERS", ForgeRegistries.RECIPE_SERIALIZERS);
				registerDeferred(ModBlocks.class, "RECIPE_SERIALIZERS", ForgeRegistries.RECIPE_SERIALIZERS);
				registerDeferred(ModItems.class, "RECIPE_SERIALIZERS", ForgeRegistries.RECIPE_SERIALIZERS);
				registerAlwaysTrueCondition(SophisticatedCore.getRL("item_enabled"));
				registerAlwaysTrueCondition(SophisticatedStorage.getRL("drop_packed_disabled"));
				registerRecipeIngredient(new ResourceLocation("minecraft", "item"), VanillaIngredientSerializer.INSTANCE);
				registerRecipeIngredient(SophisticatedStorageInMotion.getRL("moving_storage"), MovingStorageIngredient.Serializer.INSTANCE);
			}

			private static <T> void registerDeferred(Class<?> owner, String fieldName, IForgeRegistry<T> registry) {
				try {
					unfreeze(registry);
					Field field = owner.getDeclaredField(fieldName);
					field.setAccessible(true);
					DeferredRegister<T> deferredRegister = (DeferredRegister<T>) field.get(null);
					Field entriesField = DeferredRegister.class.getDeclaredField("entries");
					entriesField.setAccessible(true);
					Map<RegistryObject<T>, java.util.function.Supplier<? extends T>> entries = (Map<RegistryObject<T>, java.util.function.Supplier<? extends T>>) entriesField.get(deferredRegister);

					for (Map.Entry<RegistryObject<T>, java.util.function.Supplier<? extends T>> entry : entries.entrySet()) {
						RegistryObject<T> registryObject = entry.getKey();
						T value;
						if (!registry.containsKey(registryObject.getId())) {
							value = entry.getValue().get();
							registry.register(registryObject.getId(), value);
						} else {
							value = registry.getValue(registryObject.getId());
						}
						registerVanillaRegistry(registry, registryObject.getId(), value);
						setField(registryObject, "value", value);
						setField(registryObject, "holder", registry.getHolder(registryObject.getId()).orElse(null));
					}
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException("Unable to initialize test registry " + owner.getName() + "." + fieldName, e);
				}
			}

			private static void unfreeze(IForgeRegistry<?> registry) throws ReflectiveOperationException {
				Method method = registry.getClass().getDeclaredMethod("unfreeze");
				method.setAccessible(true);
				method.invoke(registry);
			}

			private static <T> void registerVanillaRegistry(IForgeRegistry<T> forgeRegistry, ResourceLocation id, T value) {
				if (forgeRegistry == ForgeRegistries.ITEMS && !BuiltInRegistries.ITEM.containsKey(id)) {
					Registry.register(BuiltInRegistries.ITEM, id, (Item) value);
				} else if (forgeRegistry == ForgeRegistries.BLOCKS && !BuiltInRegistries.BLOCK.containsKey(id)) {
					Registry.register(BuiltInRegistries.BLOCK, id, (Block) value);
				} else if (forgeRegistry == ForgeRegistries.RECIPE_SERIALIZERS && !BuiltInRegistries.RECIPE_SERIALIZER.containsKey(id)) {
					Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, (RecipeSerializer<?>) value);
				}
			}

			private static void registerRecipeCondition(net.minecraftforge.common.crafting.conditions.IConditionSerializer<?> serializer) {
				try {
					CraftingHelper.register(serializer);
				} catch (IllegalStateException e) {
					if (!e.getMessage().startsWith("Duplicate recipe condition serializer:")) {
						throw e;
					}
				}
			}

			private static void registerAlwaysTrueCondition(ResourceLocation id) {
				registerRecipeCondition(new net.minecraftforge.common.crafting.conditions.IConditionSerializer<>() {
					@Override
					public void write(com.google.gson.JsonObject json, net.minecraftforge.common.crafting.conditions.ICondition value) {
					}

					@Override
					public net.minecraftforge.common.crafting.conditions.ICondition read(com.google.gson.JsonObject json) {
						return new net.minecraftforge.common.crafting.conditions.ICondition() {
							@Override
							public ResourceLocation getID() {
								return id;
							}

							@Override
							public boolean test(IContext context) {
								return true;
							}
						};
					}

					@Override
					public ResourceLocation getID() {
						return id;
					}
				});
			}

			private static void registerRecipeIngredient(ResourceLocation id, net.minecraftforge.common.crafting.IIngredientSerializer<?> serializer) {
				try {
					CraftingHelper.register(id, serializer);
				} catch (IllegalStateException e) {
					if (!e.getMessage().startsWith("Duplicate recipe ingredient serializer:")) {
						throw e;
					}
				}
			}

			private static void setField(Object target, String name, Object value) {
				try {
					Field field = target.getClass().getDeclaredField(name);
					field.setAccessible(true);
					field.set(target, value);
				} catch (ReflectiveOperationException e) {
					throw new IllegalStateException("Unable to set " + name + " on " + target, e);
				}
			}
		}

		private enum UnitCookie {
			INSTANCE
		}

		private record LoadedResources(CloseableResourceManager resourceManager, ReloadableServerResources serverResources, LayeredRegistryAccess<RegistryLayer> registries) implements AutoCloseable {
			private RecipeManager recipeManager() {
				return serverResources.getRecipeManager();
			}

			private RegistryAccess registryLookup() {
				return registries.compositeAccess();
			}

			@Override
			public void close() {
				resourceManager.close();
			}
		}
	}
}
