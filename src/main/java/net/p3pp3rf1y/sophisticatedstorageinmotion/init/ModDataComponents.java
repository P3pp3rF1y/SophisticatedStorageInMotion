package net.p3pp3rf1y.sophisticatedstorageinmotion.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
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

	public static final Supplier<DataComponentType<Boolean>> LOCKED = DATA_COMPONENT_TYPES.register("locked",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final Supplier<DataComponentType<Boolean>> LOCK_VISIBLE = DATA_COMPONENT_TYPES.register("lock_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final Supplier<DataComponentType<Boolean>> UPGRADES_VISIBLE = DATA_COMPONENT_TYPES.register("upgrades_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final Supplier<DataComponentType<Boolean>> COUNTS_VISIBLE = DATA_COMPONENT_TYPES.register("counts_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static final Supplier<DataComponentType<Boolean>> FILL_LEVELS_VISIBLE = DATA_COMPONENT_TYPES.register("fill_levels_visible",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	public static void register(IEventBus modBus) {
		DATA_COMPONENT_TYPES.register(modBus);
	}
}
