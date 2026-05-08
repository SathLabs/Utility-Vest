package dev.satherov.utilityvest.network;

import dev.satherov.utilityvest.UtilityVest;
import dev.satherov.utilityvest.common.item.UVVestItem;
import dev.satherov.utilityvest.common.item.UVVestReference;
import dev.satherov.utilityvest.core.lang.UVLanguage;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public record OpenVestPayload(boolean filter, UVVestReference vestReference) implements CustomPacketPayload {
    
    public static final StreamCodec<FriendlyByteBuf, OpenVestPayload> STREAM_CODEC =
            CustomPacketPayload.codec(OpenVestPayload::encode, OpenVestPayload::new);
    
    public static final Type<OpenVestPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(UtilityVest.MOD_ID, "open_vest_menu")
    );
    
    private OpenVestPayload(FriendlyByteBuf buf) {
        this(buf.readBoolean(), UVVestReference.read(buf));
    }
    
    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.filter);
        this.vestReference.write(buf);
    }
    
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return OpenVestPayload.TYPE;
    }
    
    public static class Handler {
        public static void handle(OpenVestPayload msg, IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                if (!ctx.flow().isServerbound() || !(ctx.player() instanceof ServerPlayer player)) {
                    return;
                }
                
                ItemStack stack = msg.vestReference.resolve(player);
                
                if (!stack.isEmpty() && stack.getItem() instanceof UVVestItem vest) {
                    UVVestReference boundReference = msg.vestReference.withVestId(UVVestItem.ensureVestId(stack));
                    if (msg.filter) {
                        vest.openFilterMenu(player, boundReference, stack);
                    } else {
                        vest.openInventoryMenu(player, boundReference, stack);
                    }
                }
                
            }).exceptionally(e -> {
                ctx.disconnect(UVLanguage.NETWORK_OPEN_MENU_FAILED.translate(e.getMessage()));
                return null;
            });
        }
    }
}
