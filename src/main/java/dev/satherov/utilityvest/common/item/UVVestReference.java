package dev.satherov.utilityvest.common.item;

import dev.satherov.utilityvest.core.annotations.NothingNull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;
import java.util.UUID;

@NothingNull
public record UVVestReference(Source source, int slot, @Nullable UUID vestId) {
    
    private static final int NO_SLOT = -1;
    
    public static UVVestReference invalid() {
        return new UVVestReference(Source.INVENTORY, UVVestReference.NO_SLOT, null);
    }
    
    public static UVVestReference fromHand(InteractionHand hand, UUID vestId) {
        return new UVVestReference(hand == InteractionHand.MAIN_HAND ? Source.MAIN_HAND : Source.OFF_HAND, UVVestReference.NO_SLOT, vestId);
    }
    
    public static Optional<UVVestReference> findFirstAccessible(Player player, boolean checkHands) {
        if (checkHands && player.getMainHandItem().getItem() instanceof UVVestItem) {
            return Optional.of(UVVestReference.of(Source.MAIN_HAND, UVVestReference.NO_SLOT, player.getMainHandItem()));
        }
        
        if (checkHands && player.getOffhandItem().getItem() instanceof UVVestItem) {
            return Optional.of(UVVestReference.of(Source.OFF_HAND, UVVestReference.NO_SLOT, player.getOffhandItem()));
        }
        
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inv -> {
                    var handler = inv.getEquippedCurios();
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack stack = handler.getStackInSlot(i);
                        if (stack.getItem() instanceof UVVestItem) {
                            return Optional.of(UVVestReference.of(Source.CURIOS, i, stack));
                        }
                    }
                    return Optional.empty();
                });
    }
    
    public static UVVestReference read(FriendlyByteBuf buf) {
        Source source = buf.readEnum(Source.class);
        int slot = buf.readInt();
        UUID vestId = buf.readBoolean() ? buf.readUUID() : null;
        return new UVVestReference(source, slot, vestId);
    }
    
    private static UVVestReference of(Source source, int slot, ItemStack stack) {
        return new UVVestReference(source, slot, UVVestItem.getVestId(stack).orElse(null));
    }
    
    private static ItemStack findById(Player player, UUID vestId) {
        Inventory inventory = player.getInventory();
        
        ItemStack stack = UVVestReference.findMatchingStack(inventory.items, vestId);
        if (!stack.isEmpty()) return stack;
        
        stack = UVVestReference.findMatchingStack(inventory.armor, vestId);
        if (!stack.isEmpty()) return stack;
        
        stack = UVVestReference.findMatchingStack(inventory.offhand, vestId);
        if (!stack.isEmpty()) return stack;
        
        return CuriosApi.getCuriosInventory(player)
                .flatMap(inv -> {
                    var handler = inv.getEquippedCurios();
                    for (int i = 0; i < handler.getSlots(); i++) {
                        ItemStack curioStack = handler.getStackInSlot(i);
                        if (UVVestReference.matches(curioStack, vestId)) {
                            return Optional.of(curioStack);
                        }
                    }
                    return Optional.empty();
                })
                .orElse(ItemStack.EMPTY);
    }
    
    private static ItemStack findMatchingStack(Iterable<ItemStack> stacks, UUID vestId) {
        for (ItemStack stack : stacks) {
            if (UVVestReference.matches(stack, vestId)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    
    private static boolean matches(ItemStack stack, UUID vestId) {
        return !stack.isEmpty()
                && stack.getItem() instanceof UVVestItem
                && vestId.equals(UVVestItem.getVestId(stack).orElse(null));
    }
    
    public UVVestReference withVestId(UUID updatedVestId) {
        return new UVVestReference(this.source, this.slot, updatedVestId);
    }
    
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(this.source);
        buf.writeInt(this.slot);
        buf.writeBoolean(this.vestId != null);
        if (this.vestId != null) buf.writeUUID(this.vestId);
    }
    
    public ItemStack resolve(Player player) {
        ItemStack stack = this.resolveFromSource(player);
        if (this.matches(stack)) return stack;
        
        if (this.vestId == null) return ItemStack.EMPTY;
        return UVVestReference.findById(player, this.vestId);
    }
    
    private boolean matches(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof UVVestItem)) return false;
        return this.vestId == null || this.vestId.equals(UVVestItem.getVestId(stack).orElse(null));
    }
    
    private ItemStack resolveFromSource(Player player) {
        return switch (this.source) {
            case MAIN_HAND -> player.getMainHandItem();
            case OFF_HAND -> player.getOffhandItem();
            case INVENTORY -> {
                Inventory inventory = player.getInventory();
                yield this.slot >= 0 && this.slot < inventory.items.size() ? inventory.items.get(this.slot) : ItemStack.EMPTY;
            }
            case CURIOS -> CuriosApi.getCuriosInventory(player)
                    .map(ICuriosItemHandler::getEquippedCurios)
                    .filter(handler -> this.slot >= 0 && this.slot < handler.getSlots())
                    .map(handler -> handler.getStackInSlot(this.slot))
                    .orElse(ItemStack.EMPTY);
        };
    }
    
    public enum Source {
        MAIN_HAND,
        OFF_HAND,
        INVENTORY,
        CURIOS
    }
}
