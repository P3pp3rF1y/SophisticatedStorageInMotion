package net.p3pp3rf1y.sophisticatedstorageinmotion.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.p3pp3rf1y.sophisticatedcore.util.BlockItemBase;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class MovingStorageIngredient implements ICustomIngredient {
	public static final MapCodec<MovingStorageIngredient> CODEC = RecordCodecBuilder
			.mapCodec(
					instance -> instance
							.group(Item.CODEC.fieldOf("moving_storage_item").forGetter(ingredient -> ingredient.movingStorageItem),
									Item.CODEC.fieldOf("storage_item").forGetter(ingredient -> ingredient.storageItem))
							.apply(instance, MovingStorageIngredient::new));
	private final Holder<Item> movingStorageItem;
	private final Holder<Item> storageItem;
	private final List<ItemStack> movingStorages;

	private MovingStorageIngredient(Holder<Item> movingStorageItem, Holder<Item> storageItem) {
		this.movingStorageItem = movingStorageItem;
		this.storageItem = storageItem;
		List<ItemStack> storageItemCreativeTabItems = new ArrayList<>();
		if (storageItem.value() instanceof BlockItemBase itemBase) {
			itemBase.addCreativeTabItems(storageItemCreativeTabItems::add);
		}
		movingStorages = new ArrayList<>();
		storageItemCreativeTabItems.forEach(storageItemStack -> {
			ItemStack movingStorageStack = new ItemStack(movingStorageItem);
			MovingStorageItem.setStorageItem(movingStorageStack, storageItemStack);
			movingStorages.add(movingStorageStack);
		});
	}

	public static MovingStorageIngredient of(Holder<Item> movingStorageItem, Item storageItem) {
		return new MovingStorageIngredient(movingStorageItem, BuiltInRegistries.ITEM.get(BuiltInRegistries.ITEM.getKey(storageItem)).orElseThrow());
	}

	public List<ItemStack> getMovingStorages() {
		return movingStorages.stream().map(ItemStack::copy).toList();
	}

	@Override
	public boolean test(ItemStack itemStack) {
		return itemStack.getItem() == movingStorageItem.value() && MovingStorageItem.getStorageItem(itemStack).getItem() == storageItem.value();
	}

	@Override
	public Stream<Holder<Item>> items() {
		return Stream.of(movingStorageItem);
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IngredientType<?> getType() {
		return ModItems.MOVING_STORAGE_INGREDIENT_TYPE.get();
	}

	@Override
	public SlotDisplay display() {
		return new SlotDisplay.Composite(movingStorages.stream().map(SlotDisplay.ItemStackSlotDisplay::new).map(SlotDisplay.class::cast).toList());
	}
}
