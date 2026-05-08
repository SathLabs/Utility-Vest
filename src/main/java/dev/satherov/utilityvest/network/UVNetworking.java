package dev.satherov.utilityvest.network;

import dev.satherov.utilityvest.UtilityVest;
import dev.satherov.utilityvest.common.item.UVVestReference;

import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class UVNetworking {
    
    private UVNetworking() {
    }
    
    public static void registerPayload(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(UtilityVest.MOD_ID);
        
        registrar.playToServer(SaveLoadPayload.TYPE, SaveLoadPayload.STREAM_CODEC, SaveLoadPayload.Handler::handle);
        registrar.playToServer(OpenVestPayload.TYPE, OpenVestPayload.STREAM_CODEC, OpenVestPayload.Handler::handle);
        registrar.playToServer(RestockPayload.TYPE, RestockPayload.STREAM_CODEC, RestockPayload.Handler::handle);
        registrar.playBidirectional(SwapToolPayload.TYPE, SwapToolPayload.STREAM_CODEC, SwapToolPayload.Handler::handle);
    }
    
    public static void doSwap(Player player, boolean main, UVVestReference vestReference, ItemStack vest, int idx) {
        PacketDistributor.sendToServer(new SwapToolPayload(vestReference, main, idx));
        SwapToolPayload.Handler.execute(player, main, vest, idx);
    }
}
