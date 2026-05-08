package dev.satherov.utilityvest.network;

import dev.satherov.utilityvest.UtilityVest;
import dev.satherov.utilityvest.common.capabilities.UVVestCapability;
import dev.satherov.utilityvest.common.item.UVVestItem;
import dev.satherov.utilityvest.common.item.UVVestReference;
import dev.satherov.utilityvest.core.lang.UVLanguage;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record SaveLoadPayload(boolean save, int hotbarIndex, UVVestReference vestReference) implements CustomPacketPayload {
    
    public static final StreamCodec<FriendlyByteBuf, SaveLoadPayload> STREAM_CODEC =
            CustomPacketPayload.codec(SaveLoadPayload::encode, SaveLoadPayload::new);
    
    public static final Type<SaveLoadPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(UtilityVest.MOD_ID, "save_load")
    );
    
    private SaveLoadPayload(FriendlyByteBuf buf) {
        this(buf.readBoolean(), buf.readInt(), UVVestReference.read(buf));
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.save);
        buf.writeInt(this.hotbarIndex);
        this.vestReference.write(buf);
    }
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return SaveLoadPayload.TYPE;
    }
    
    public static class Handler {
        public static void handle(SaveLoadPayload msg, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (!ctx.flow().isServerbound() || !(ctx.player() instanceof ServerPlayer player)) {
                    return;
                }
                
                ItemStack vestStack = msg.vestReference.resolve(player);
                
                if (!vestStack.isEmpty() && vestStack.getItem() instanceof UVVestItem) {
                    UVVestItem.ensureVestId(vestStack);
                    IItemHandler handler = vestStack.getCapability(Capabilities.ItemHandler.ITEM);
                    
                    if (handler instanceof UVVestCapability capability) {
                        if (msg.save) {
                            capability.saveHotbar(player, msg.hotbarIndex);
                        } else {
                            capability.loadHotbar(player, msg.hotbarIndex);
                        }
                    }
                }
            }).exceptionally(e -> {
                ctx.disconnect(UVLanguage.NETWORK_SAVE_LOAD_FAILED.translate(e.getMessage()));
                return null;
            });
        }
    }
}
