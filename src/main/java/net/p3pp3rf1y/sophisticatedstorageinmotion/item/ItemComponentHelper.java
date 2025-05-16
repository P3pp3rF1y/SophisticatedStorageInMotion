package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedstorage.init.ModDataComponents;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Supplier;

public class ItemComponentHelper {
	private static final Set<Supplier<? extends DataComponentType<?>>> DROP_COMPONENTS = Set.of(
			ModDataComponents.WOOD_TYPE,
			ModCoreDataComponents.MAIN_COLOR,
			ModCoreDataComponents.ACCENT_COLOR,
			ModDataComponents.BARREL_MATERIALS,
			ModDataComponents.FLAT_TOP
	);

	public static ItemStack cleanUpStack(ItemStack stack) {
		ItemStack cleanedUpStack = stack.copy();
		for (Supplier<? extends DataComponentType<?>> componentType : DROP_COMPONENTS) {
			if (stack.has(componentType)) {
				setCompoment(cleanedUpStack, componentType, stack.get(componentType));
			}
		}
		return cleanedUpStack;
	}

	private static <T> void setCompoment(ItemStack stack, Supplier<? extends DataComponentType<?>> componentType, @Nullable T value) {
		stack.set((Supplier<DataComponentType<T>>) componentType, value);
	}
}
