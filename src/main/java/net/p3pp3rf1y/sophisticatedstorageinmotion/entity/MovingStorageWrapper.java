package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SortBy;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.inventory.*;
import net.p3pp3rf1y.sophisticatedcore.renderdata.RenderInfo;
import net.p3pp3rf1y.sophisticatedcore.settings.SettingsHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.itemdisplay.ItemDisplaySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.settings.nosort.NoSortSettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.stack.StackUpgradeItem;
import net.p3pp3rf1y.sophisticatedcore.upgrades.voiding.VoidUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedcore.util.InventorySorter;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;
import net.p3pp3rf1y.sophisticatedstorage.Config;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorage.block.*;
import net.p3pp3rf1y.sophisticatedstorage.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorage.item.BarrelBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.item.ShulkerBoxItem;
import net.p3pp3rf1y.sophisticatedstorage.item.StorageBlockItem;
import net.p3pp3rf1y.sophisticatedstorage.settings.StorageSettingsHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class MovingStorageWrapper implements IStorageWrapper {
	public static final String SETTINGS_TAG = "settings";
	private final Runnable stackChangeHandler;
	private final ItemStack storageStack;
	private final Runnable contentsChangeHandler;

	@Nullable
	private InventoryHandler inventoryHandler = null;
	@Nullable
	private ContentsFilteredItemHandler contentsFilteredItemHandler = null;

	@Nullable
	private InventoryIOHandler inventoryIOHandler = null;
	@Nullable
	private UpgradeHandler upgradeHandler = null;

	@Nullable
	private SettingsHandler settingsHandler;
	private final RenderInfo renderInfo;

	private final Map<Class<? extends IUpgradeWrapper>, Consumer<? extends IUpgradeWrapper>> upgradeDefaultsHandlers = new HashMap<>();


	private MovingStorageWrapper(ItemStack storageStack, Runnable onContentsChanged, Runnable onStackChanged) {
		this.storageStack = storageStack;
		contentsChangeHandler = onContentsChanged;
		stackChangeHandler = onStackChanged;
		renderInfo = new MovingStorageRenderInfo(storageStack);

		if (EntityStorageHolder.isLimitedBarrel(storageStack)) {
			registerUpgradeDefaultsHandler(VoidUpgradeWrapper.class, LimitedBarrelBlockEntity.VOID_UPGRADE_VOIDING_OVERFLOW_OF_EVERYTHING_BY_DEFAULT);
		}
	}

	private static int getNumberOfDisplayItems(ItemStack stack) {
		return stack.getItem() instanceof BarrelBlockItem ? 4 : 1;
	}

	public static MovingStorageWrapper fromStack(ItemStack stack, Runnable onContentsChanged, Runnable onStackChanged) {
		MovingStorageWrapper movingStorageWrapper = StorageWrapperRepository.getStorageWrapper(stack, MovingStorageWrapper.class, s -> new MovingStorageWrapper(s, onContentsChanged, onStackChanged));
		UUID uuid = stack.get(ModCoreDataComponents.STORAGE_UUID);
		if (uuid != null) {
			movingStorageWrapper.setContentsUuid(uuid); //setting here because client side the uuid isn't in contentsnbt before this data is synced from server and it would create a new one otherwise
		}

		return movingStorageWrapper;
	}

	private UUID getNewUuid() {
		UUID newUuid = UUID.randomUUID();
		setContentsUuid(newUuid);
		return newUuid;
	}

	@Override
	public void setContentsChangeHandler(Runnable contentsChangeHandler) {
		//noop
	}

	@Override
	public int getNumberOfSlotRows() {
		int itemInventorySlots = getNumberOfInventorySlots();
		return (int) Math.ceil(itemInventorySlots <= 81 ? (double) itemInventorySlots / 9 : (double) itemInventorySlots / 12);
	}

	@Override
	public ITrackedContentsItemHandler getInventoryForUpgradeProcessing() {
		return getInventoryHandler();
	}

	@Override
	public InventoryHandler getInventoryHandler() {
		if (inventoryHandler == null) {
			initInventoryHandler();
		}
		return inventoryHandler;
	}

	private void initInventoryHandler() {
		inventoryHandler = new InventoryHandler(getNumberOfInventorySlots(), this, getContentsNbt(), contentsChangeHandler, StackUpgradeItem.getInventorySlotLimit(this), Config.SERVER.stackUpgrade) {
			@Override
			protected boolean isAllowed(ItemStack stack) {
				return isAllowedInStorage(stack);
			}
		};
		inventoryHandler.addListener(getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class)::itemChanged);
		inventoryHandler.setShouldInsertIntoEmpty(this::emptyInventorySlotsAcceptItems);
		inventoryHandler.onInit();
	}

	private boolean emptyInventorySlotsAcceptItems() {
		return !EntityStorageHolder.isLocked(storageStack) || allowsEmptySlotsMatchingItemInsertsWhenLocked();
	}

	private boolean allowsEmptySlotsMatchingItemInsertsWhenLocked() {
		return !EntityStorageHolder.isLimitedBarrel(storageStack);
	}

	public int getNumberOfInventorySlots() {
		Integer numberOfInventorySlots = storageStack.get(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS);
		if (numberOfInventorySlots != null) {
			return numberOfInventorySlots;
		}
		numberOfInventorySlots = getDefaultNumberOfInventorySlots(storageStack);
		storageStack.set(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, numberOfInventorySlots);
		stackChangeHandler.run();

		return numberOfInventorySlots;
	}

	@Override
	public ITrackedContentsItemHandler getInventoryForInputOutput() {
		if (EntityStorageHolder.isLocked(storageStack) && allowsEmptySlotsMatchingItemInsertsWhenLocked()) {
			if (contentsFilteredItemHandler == null) {
				contentsFilteredItemHandler = new ContentsFilteredItemHandler(this::getInventoryIOHandler, () -> getInventoryHandler().getSlotTracker(), () -> getSettingsHandler().getTypeCategory(MemorySettingsCategory.class));
			}
			return contentsFilteredItemHandler;
		}
		return getInventoryIOHandler();
	}

	private @NotNull ITrackedContentsItemHandler getInventoryIOHandler() {
		if (inventoryIOHandler == null) {
			inventoryIOHandler = new InventoryIOHandler(this);
		}
		return inventoryIOHandler.getFilteredItemHandler();
	}

	@Override
	public SettingsHandler getSettingsHandler() {
		if (settingsHandler == null) {
			if (getContentsUuid().isPresent()) {
				settingsHandler = new StorageSettingsHandler(getSettingsNbt(), contentsChangeHandler, this::getInventoryHandler, () -> renderInfo) {
					@Override
					protected int getNumberOfDisplayItems() {
						return MovingStorageWrapper.getNumberOfDisplayItems(storageStack);
					}

					@Override
					protected void saveCategoryNbt(CompoundTag settingsNbt, String categoryName, CompoundTag tag) {
						super.saveCategoryNbt(settingsNbt, categoryName, tag);
						contentsChangeHandler.run();
						if (categoryName.equals(ItemDisplaySettingsCategory.NAME)) {
							stackChangeHandler.run();
						}
					}
				};
			} else {
				settingsHandler = NoopStorageWrapper.INSTANCE.getSettingsHandler();
			}
		}
		return settingsHandler;
	}

	@Override
	public UpgradeHandler getUpgradeHandler() {
		if (upgradeHandler == null) {
			upgradeHandler = new UpgradeHandler(getNumberOfUpgradeSlots(), this, getContentsNbt(), contentsChangeHandler, () -> {
				if (inventoryHandler != null) {
					inventoryHandler.clearListeners();
					inventoryHandler.setBaseSlotLimit(StackUpgradeItem.getInventorySlotLimit(this));
				}
				getInventoryHandler().addListener(getSettingsHandler().getTypeCategory(ItemDisplaySettingsCategory.class)::itemChanged);
				inventoryIOHandler = null;
			}) {
				@Override
				public boolean isItemValid(int slot, ItemStack stack) {
					return super.isItemValid(slot, stack) && (stack.isEmpty() || SophisticatedStorage.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace()) || stack.is(ModItems.STORAGE_UPGRADE_TAG));
				}
			};
			upgradeDefaultsHandlers.forEach(this::registerUpgradeDefaultsHandlerInUpgradeHandler);
		}
		return upgradeHandler;
	}

	private <T extends IUpgradeWrapper> void registerUpgradeDefaultsHandlerInUpgradeHandler(Class<T> wrapperClass, Consumer<? extends IUpgradeWrapper> defaultsHandler) {
		//noinspection DataFlowIssue, unchecked - only called after upgradeHandler is initialized
		upgradeHandler.registerUpgradeDefaultsHandler(wrapperClass, (Consumer<T>) defaultsHandler);
	}

	public int getNumberOfUpgradeSlots() {
		@Nullable Integer numberOfUpgradeSlots = storageStack.get(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS);
		if (numberOfUpgradeSlots != null) {
			return numberOfUpgradeSlots;
		}
		numberOfUpgradeSlots = getDefaultNumberOfUpgradeSlots(storageStack);
		storageStack.set(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, numberOfUpgradeSlots);
		stackChangeHandler.run();

		return numberOfUpgradeSlots;
	}

	@Override
	public Optional<UUID> getContentsUuid() {
		return Optional.ofNullable(storageStack.get(ModCoreDataComponents.STORAGE_UUID));
	}

	private CompoundTag getSettingsNbt() {
		UUID storageId = getContentsUuid().orElseGet(this::getNewUuid);
		MovingStorageData storageData = MovingStorageData.get(storageId);
		CompoundTag baseContentsNbt = storageData.getContents();
		if (!baseContentsNbt.contains(SETTINGS_TAG)) {
			baseContentsNbt.put(SETTINGS_TAG, new CompoundTag());
			storageData.setContents(baseContentsNbt);
		}
		return baseContentsNbt.getCompound(SETTINGS_TAG);
	}

	private CompoundTag getContentsNbt() {
		UUID storageId = getContentsUuid().orElseGet(this::getNewUuid);
		MovingStorageData storageData = MovingStorageData.get(storageId);
		CompoundTag baseContentsNbt = storageData.getContents();
		if (!baseContentsNbt.contains(StorageWrapper.CONTENTS_TAG)) {
			baseContentsNbt.put(StorageWrapper.CONTENTS_TAG, new CompoundTag());
			storageData.setContents(baseContentsNbt);
		}
		return baseContentsNbt.getCompound(StorageWrapper.CONTENTS_TAG);
	}

	@Override
	public int getMainColor() {
		return StorageBlockItem.getMainColorFromComponentHolder(storageStack).orElse(-1);
	}

	@Override
	public int getAccentColor() {
		return StorageBlockItem.getAccentColorFromComponentHolder(storageStack).orElse(-1);
	}

	@Override
	public Optional<Integer> getOpenTabId() {
		return Optional.ofNullable(storageStack.get(ModCoreDataComponents.OPEN_TAB_ID));
	}

	@Override
	public void setOpenTabId(int openTabId) {
		storageStack.set(ModCoreDataComponents.OPEN_TAB_ID, openTabId);
		stackChangeHandler.run();
	}

	@Override
	public void removeOpenTabId() {
		storageStack.remove(ModCoreDataComponents.OPEN_TAB_ID);
		stackChangeHandler.run();
	}

	@Override
	public void setColors(int mainColor, int accentColor) {
		storageStack.set(ModCoreDataComponents.MAIN_COLOR, mainColor);
		storageStack.set(ModCoreDataComponents.ACCENT_COLOR, accentColor);
		stackChangeHandler.run();
	}

	@Override
	public void setSortBy(SortBy sortBy) {
		storageStack.set(ModCoreDataComponents.SORT_BY, sortBy);
		stackChangeHandler.run();
	}

	@Override
	public SortBy getSortBy() {
		return storageStack.getOrDefault(ModCoreDataComponents.SORT_BY, SortBy.NAME);
	}

	@Override
	public void sort() {
		Set<Integer> slotIndexesExcludedFromSort = new HashSet<>();
		slotIndexesExcludedFromSort.addAll(getSettingsHandler().getTypeCategory(NoSortSettingsCategory.class).getNoSortSlots());
		slotIndexesExcludedFromSort.addAll(getSettingsHandler().getTypeCategory(MemorySettingsCategory.class).getSlotIndexes());
		slotIndexesExcludedFromSort.addAll(getInventoryHandler().getNoSortSlots());
		InventorySorter.sortHandler(getInventoryHandler(), getComparator(), slotIndexesExcludedFromSort);
	}

	private Comparator<Map.Entry<ItemStackKey, Integer>> getComparator() {
		return switch (getSortBy()) {
			case COUNT -> InventorySorter.BY_COUNT;
			case TAGS -> InventorySorter.BY_TAGS;
			case NAME -> InventorySorter.BY_NAME;
			case MOD -> InventorySorter.BY_MOD;
		};
	}

	@Override
	public void onContentsNbtUpdated() {
		inventoryHandler = null;
		upgradeHandler = null;
		refreshInventoryForUpgradeProcessing();
	}

	@Override
	public void refreshInventoryForUpgradeProcessing() {
		refreshInventoryForInputOutput();
	}

	@Override
	public void refreshInventoryForInputOutput() {
		inventoryIOHandler = null;
	}

	@Override
	public void setPersistent(boolean persistent) {
		//noop
	}

	@Override
	public void fillWithLoot(Player playerEntity) {
		//noop
	}

	@Override
	public RenderInfo getRenderInfo() {
		return renderInfo;
	}

	@Override
	public void setColumnsTaken(int columnsTaken, boolean hasChanged) {
		//noop - would require a change if there ever was support for this in storage which is not a plan
	}

	@Override
	public int getColumnsTaken() {
		return 0;
	}

	public void setContentsUuid(UUID contentsUuid) {
		storageStack.set(ModCoreDataComponents.STORAGE_UUID, contentsUuid);
		onContentsNbtUpdated();
	}

	public static int getDefaultNumberOfInventorySlots(ItemStack storageStack) {
		return storageStack.getItem() instanceof BlockItemBase blockItem && blockItem.getBlock() instanceof IStorageBlock storageBlock ? storageBlock.getNumberOfInventorySlots() : 0;
	}

	public static int getDefaultNumberOfUpgradeSlots(ItemStack storageStack) {
		return storageStack.getItem() instanceof BlockItemBase blockItem && blockItem.getBlock() instanceof IStorageBlock storageBlock ? storageBlock.getNumberOfUpgradeSlots() : 0;
	}

	private boolean isAllowedInStorage(ItemStack stack) {
		if (!(storageStack.getItem() instanceof ShulkerBoxItem)) {
			return true;
		}

		Block block = Block.byItem(stack.getItem());
		return !(block instanceof ShulkerBoxBlock) && !(block instanceof net.minecraft.world.level.block.ShulkerBoxBlock) && !Config.SERVER.shulkerBoxDisallowedItems.isItemDisallowed(stack.getItem());
	}

	@Override
	public String getStorageType() {
		Item storageItem = storageStack.getItem();
		if (!(storageItem instanceof BlockItem blockItem)) {
			return "undefined";
		}

		if (blockItem.getBlock() instanceof ChestBlock) {
			return ChestBlockEntity.STORAGE_TYPE;
		} else if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
			return ShulkerBoxBlockEntity.STORAGE_TYPE;
		} else if (blockItem.getBlock() instanceof LimitedBarrelBlock) {
			return LimitedBarrelBlockEntity.STORAGE_TYPE;
		} else if (blockItem.getBlock() instanceof BarrelBlock) {
			return BarrelBlockEntity.STORAGE_TYPE;
		}

		return "undefined";
	}

	@Override
	public Component getDisplayName() {
		return storageStack.getDisplayName();
	}

	public void changeSize(int additionalInventorySlots, int additionalUpgradeSlots) {
		setNumberOfInventorySlots(getNumberOfInventorySlots() + additionalInventorySlots);
		getInventoryHandler().changeSlots(additionalInventorySlots);

		setNumberOfUpgradeSlots(getNumberOfUpgradeSlots() + additionalUpgradeSlots);
		getUpgradeHandler().increaseSize(additionalUpgradeSlots);
	}

	public void setNumberOfInventorySlots(int numberOfInventorySlots) {
		storageStack.set(ModCoreDataComponents.NUMBER_OF_INVENTORY_SLOTS, numberOfInventorySlots);
		stackChangeHandler.run();
	}

	public void setNumberOfUpgradeSlots(int numberOfUpgradeSlots) {
		storageStack.set(ModCoreDataComponents.NUMBER_OF_UPGRADE_SLOTS, numberOfUpgradeSlots);
		stackChangeHandler.run();
	}

	//TODO need this to be called for limited barrels when initialized - probably check for item in fromStack method here?
	public <T extends IUpgradeWrapper> void registerUpgradeDefaultsHandler(Class<T> upgradeClass, Consumer<T> defaultsHandler) {
		upgradeDefaultsHandlers.put(upgradeClass, defaultsHandler);
	}

	@Override
	public ItemStack getWrappedStorageStack() {
		return storageStack;
	}

	@Override
	public int getBaseStackSizeMultiplier() {
		return storageStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof IStorageBlock storageBlock ? storageBlock.getBaseStackSizeMultiplier() : 1;
	}

	private class MovingStorageRenderInfo extends RenderInfo {
		public MovingStorageRenderInfo(ItemStack storageStack) {
			super(() -> MovingStorageWrapper.this.stackChangeHandler, EntityStorageHolder.isLimitedBarrel(storageStack));
			deserialize();
		}

		@Override
		protected void serializeRenderInfo(CompoundTag renderInfo) {
			MovingStorageWrapper.this.storageStack.set(ModCoreDataComponents.RENDER_INFO_TAG, CustomData.of(renderInfo));
		}

		@Override
		protected Optional<CompoundTag> getRenderInfoTag() {
			return Optional.ofNullable(MovingStorageWrapper.this.storageStack.get(ModCoreDataComponents.RENDER_INFO_TAG.get())).map(CustomData::copyTag);			}
	}
}
