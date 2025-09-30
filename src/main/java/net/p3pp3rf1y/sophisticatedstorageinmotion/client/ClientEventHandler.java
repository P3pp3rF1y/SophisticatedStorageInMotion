package net.p3pp3rf1y.sophisticatedstorageinmotion.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.p3pp3rf1y.sophisticatedcore.client.ItemInteractionHandler;
import net.p3pp3rf1y.sophisticatedcore.client.render.ItemInStorageHighlightRenderer;
import net.p3pp3rf1y.sophisticatedstorageinmotion.SophisticatedStorageInMotion;
import net.p3pp3rf1y.sophisticatedstorageinmotion.client.gui.PaintbrushMovingStorageOverlay;
import net.p3pp3rf1y.sophisticatedstorageinmotion.entity.IMovingStorageEntity;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModEntitiesClient;
import net.p3pp3rf1y.sophisticatedstorageinmotion.init.ModItems;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.MovingStorageItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageBoatItem;
import net.p3pp3rf1y.sophisticatedstorageinmotion.item.StorageMinecartItem;

import java.util.function.BiConsumer;

public class ClientEventHandler {
	private static final BiConsumer<LivingEntity, LivingEntityRenderState> STORAGE_HORSE_RENDER_STATE_MODIFIER = (entity, renderState) -> {
		if (!(entity instanceof AbstractChestedHorse chestedHorse) || !(entity instanceof IMovingStorageEntity movingStorage) || movingStorage.getStorageItem().isEmpty()) {
			return;
		}

		renderState.setRenderData(ContextKeys.HORSE_CLASS, chestedHorse.getClass());
		renderState.setRenderData(ContextKeys.RENDER_BLOCK_ENTITY, movingStorage.getStorageHolder().getRenderBlockEntity());
	};

	public static void registerHandlers(IEventBus modBus) {
		modBus.addListener(ClientEventHandler::registerOverlay);
		modBus.addListener(ClientEventHandler::registerTooltipComponent);
		modBus.addListener(ClientEventHandler::registerSpecialModelRenderers);
		modBus.addListener(ClientEventHandler::registerMovingStorageRenderStateModifiers);
		ModEntitiesClient.registerHandlers(modBus);

		IEventBus eventBus = NeoForge.EVENT_BUS;
		eventBus.addListener(ClientMovingStorageContentsTooltip::onWorldLoad);

		ItemInStorageHighlightRenderer.registerHighlightHandler(MovingStorageHighlightHandler.INSTANCE);
		ItemInteractionHandler.registerPayloadBuilder(MovingStorageItemActionPayloadBuilder.INSTANCE);
	}

	private static void registerMovingStorageRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
		event.registerEntityModifier(StorageBoatRenderer.class, StorageBoatRenderer.RENDER_STATE_MODIFIER);
		event.registerEntityModifier(StorageMinecartRenderer.class, StorageMinecartRenderer.RENDER_STATE_MODIFIER);
		event.registerEntityModifier((Class<EntityRenderer<LivingEntity, LivingEntityRenderState>>) (Class<?>) LivingEntityRenderer.class, STORAGE_HORSE_RENDER_STATE_MODIFIER);
	}

	private static void registerSpecialModelRenderers(RegisterSpecialModelRendererEvent event) {
		ModItems.ITEMS.getEntries().stream()
				.filter(i -> i.get() instanceof StorageMinecartItem)
				.forEach(i -> event.register(i.getId(), StorageMinecartItemRenderer.Unbaked.MAP_CODEC));

		ModItems.ITEMS.getEntries().stream()
				.filter(i -> i.get() instanceof StorageBoatItem)
				.forEach(b -> event.register(b.getId(), StorageBoatItemRenderer.Unbaked.MAP_CODEC));
	}

	private static void registerOverlay(RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.HOTBAR, ResourceLocation.fromNamespaceAndPath(SophisticatedStorageInMotion.MOD_ID, "paintbrush_moving_storage_info"), PaintbrushMovingStorageOverlay.HUD_PAINTBRUSH_INFO);
	}

	private static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(MovingStorageItem.MovingStorageContentsTooltip.class, ClientMovingStorageContentsTooltip::new);
	}
}
