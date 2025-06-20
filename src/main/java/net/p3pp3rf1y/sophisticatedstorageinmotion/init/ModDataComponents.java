package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedcore.util.SimpleItemContent;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;

import java.util.function.Supplier;

public class ModDataComponents {
	private ModDataComponents() {
	}

	private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, SophisticatedStorageInMotion.MOD_ID);

	public static final Supplier<DataComponentType<SimpleItemContent>> STORAGE_ITEM = DATA_COMPONENT_TYPES.register("storage_item",
			() -> new DataComponentType.Builder<SimpleItemContent>().persistent(SimpleItemContent.CODEC).networkSynchronized(SimpleItemContent.STREAM_CODEC).build());

	public static void register(IEventBus modBus) {
		DATA_COMPONENT_TYPES.register(modBus);
	}
}
