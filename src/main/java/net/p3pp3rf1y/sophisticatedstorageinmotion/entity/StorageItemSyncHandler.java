package net.p3pp3rf1y.sophisticatedstorageinmotion.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;

public class StorageItemSyncHandler implements AttachmentSyncHandler<ItemStack> {
	@Override
	public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf, ItemStack stack, boolean initialSync) {
		ItemStack.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, stack);
	}

	@Override
	@Nullable
	public ItemStack read(IAttachmentHolder attachmentHolder, RegistryFriendlyByteBuf registryFriendlyByteBuf, @Nullable ItemStack stack) {
		if (attachmentHolder instanceof IStorageItemAttachmentHolder holder) {
			holder.markStorageItemSynced();
		}

		return ItemStack.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
	}
}
