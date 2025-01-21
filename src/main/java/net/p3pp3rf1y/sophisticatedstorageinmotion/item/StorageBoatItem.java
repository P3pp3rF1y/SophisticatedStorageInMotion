package net.p3pp3rf1y.sophisticatedstorageinmotion.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.util.NonNullLazy;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;
import net.p3pp3rf1y.sophisticatedstorage.SophisticatedStorage;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.StorageBoatItemRenderer;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.EntityStorageHolder;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.StorageBoat;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StorageBoatItem extends MovingStorageItem {
	private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
	private static final String RAFT_DESCRIPTION_ID = "item." + SophisticatedStorageInMotion.MOD_ID + ".storage_raft";
	private static final String BOAT_TYPE_TAG = "boatType";
	public static final Map<Boat.Type, Supplier<Item>> SUPPORTED_BOAT_TYPES = Map.of(
			Boat.Type.ACACIA, () -> Items.ACACIA_BOAT,
			Boat.Type.BAMBOO, () -> Items.BAMBOO_RAFT,
			Boat.Type.BIRCH, () -> Items.BIRCH_BOAT,
			Boat.Type.CHERRY, () -> Items.CHERRY_BOAT,
			Boat.Type.DARK_OAK, () -> Items.DARK_OAK_BOAT,
			Boat.Type.JUNGLE, () -> Items.JUNGLE_BOAT,
			Boat.Type.MANGROVE, () -> Items.MANGROVE_BOAT,
			Boat.Type.OAK, () -> Items.OAK_BOAT,
			Boat.Type.SPRUCE, () -> Items.SPRUCE_BOAT
	);

	public StorageBoatItem() {
		super(new Properties().stacksTo(1));
	}

	@Override
	public ItemStack getUncraftRemainingItem(ItemStack input) {
		return new ItemStack(SUPPORTED_BOAT_TYPES.getOrDefault(getBoatType(input), () -> Items.OAK_BOAT).get());
	}

	@Override
	public List<ItemStack> getBaseMovingStorageItems() {
		return SUPPORTED_BOAT_TYPES.keySet().stream().map(type -> setBoatType(new ItemStack(this), type)).toList();
	}

	public static Boat.Type getBoatType(ItemStack boatStack) {
		return NBTHelper.getEnumConstant(boatStack, BOAT_TYPE_TAG, Boat.Type::byName).orElse(Boat.Type.OAK);
	}

	public static ItemStack setBoatType(ItemStack boatStack, Boat.Type type) {
		NBTHelper.setEnumConstant(boatStack, BOAT_TYPE_TAG, type);
		return boatStack;
	}

	@Override
	public Component getName(ItemStack stack) {
		return NBTHelper.getCompound(stack, EntityStorageHolder.STORAGE_ITEM_TAG).map(ItemStack::of)
				.<Component>map(storageItem -> {
					Boat.Type boatType = getBoatType(stack);
					String descriptionId = boatType == Boat.Type.BAMBOO ? RAFT_DESCRIPTION_ID : getDescriptionId();
					return Component.translatable(descriptionId, getWoodName(boatType), storageItem.getHoverName());
				}).orElse(super.getName(stack));
	}

	private Component getWoodName(Boat.Type type) {
		return Component.translatable("wood_name." + SophisticatedStorage.MOD_ID + "." + type.name().toLowerCase(Locale.ROOT));
	}

	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		HitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
		if (hitresult.getType() == HitResult.Type.MISS) {
			return InteractionResultHolder.pass(itemstack);
		} else {
			Vec3 playerViewVector = player.getViewVector(1.0F);
			List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(playerViewVector.scale(5)).inflate(1.0), ENTITY_PREDICATE);
			if (!list.isEmpty()) {
				Vec3 eyePosition = player.getEyePosition();
				for (Entity entity : list) {
					AABB aabb = entity.getBoundingBox().inflate(entity.getPickRadius());
					if (aabb.contains(eyePosition)) {
						return InteractionResultHolder.pass(itemstack);
					}
				}
			}

			if (hitresult.getType() == HitResult.Type.BLOCK) {
				Boat boat = createBoat(level, player, hitresult, itemstack);
				if (!level.noCollision(boat, boat.getBoundingBox())) {
					return InteractionResultHolder.fail(itemstack);
				} else {
					if (!level.isClientSide) {
						level.addFreshEntity(boat);
						level.gameEvent(player, GameEvent.ENTITY_PLACE, hitresult.getLocation());
						itemstack.shrink(1);
					}

					player.awardStat(Stats.ITEM_USED.get(this));
					return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
				}
			} else {
				return InteractionResultHolder.pass(itemstack);
			}
		}
	}

	private Boat createBoat(Level level, Player player, HitResult hitResult, ItemStack stack) {
		Vec3 vec3 = hitResult.getLocation();
		StorageBoat boat = new StorageBoat(level, vec3.x, vec3.y, vec3.z);
		EntityStorageHolder<?> storageHolder = boat.getStorageHolder();
		storageHolder.setStorageItemFrom(stack, true);
		storageHolder.onPlace();
		boat.setVariant(StorageBoatItem.getBoatType(stack));
		boat.setYRot(player.getYRot());
		if (level instanceof ServerLevel serverLevel) {
			EntityType.createDefaultStackConfig(serverLevel, stack, player).accept(boat);
		}
		return boat;
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept( new IClientItemExtensions() {
			private final NonNullLazy<BlockEntityWithoutLevelRenderer> ister = NonNullLazy.of(() -> new StorageBoatItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return ister.get();
			}
		});
	}
}
