package dev.satherov.utilityvest.common.menu;


import dev.satherov.utilityvest.common.capabilities.UVVestCapability;
import dev.satherov.utilityvest.common.item.UVVestItem;
import dev.satherov.utilityvest.common.item.UVVestReference;
import dev.satherov.utilityvest.core.UVRegistry;
import dev.satherov.utilityvest.core.annotations.NothingNull;
import dev.satherov.utilityvest.core.lang.UVLanguage;

import net.neoforged.neoforge.items.SlotItemHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@NothingNull
public class UVFilterMenu extends UVVestMenu {
    
    public UVFilterMenu(int containerId, Inventory inventory, int rows, UVVestReference vestReference) {
        super(UVFilterMenu.getMenuProvider(rows), containerId, inventory, rows, vestReference, ItemStack.EMPTY);
    }
    
    public UVFilterMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf data, int rows) {
        super(
                UVFilterMenu.getMenuProvider(rows),
                containerId,
                inventory,
                rows,
                UVVestReference.read(data),
                ItemStack.OPTIONAL_STREAM_CODEC.decode(data)
        );
    }
    
    private static MenuType<?> getMenuProvider(int rows) {
        return switch (rows) {
            case 1 -> UVRegistry.FILTER_MENU_ONE.get();
            case 2 -> UVRegistry.FILTER_MENU_TWO.get();
            case 3 -> UVRegistry.FILTER_MENU_THREE.get();
            case 4 -> UVRegistry.FILTER_MENU_FOUR.get();
            case 5 -> UVRegistry.FILTER_MENU_FIVE.get();
            case 6 -> UVRegistry.FILTER_MENU_SIX.get();
            case 7 -> UVRegistry.FILTER_MENU_SEVEN.get();
            case 8 -> UVRegistry.FILTER_MENU_EIGHT.get();
            default -> throw new IllegalArgumentException("Invalid row count: " + rows);
        };
    }
    
    @Override
    protected void addVestSlots(Inventory inventory, UVVestCapability handler, int yOffset) {
        
        // Filter Slots
        for (int j = 0; j < this.rows; j++) {
            for (int k = 0; k < 9; k++) {
                this.addSlot(new SlotItemHandler(handler.filters, k + j * 9, 8 + k * 18, 18 + j * 18) {
                    
                    @Override
                    public int getMaxStackSize() {
                        return 1;
                    }
                    
                    @Override
                    public void onTake(Player player, ItemStack stack) {
                    }
                    
                    @Override
                    public ItemStack remove(int amount) {
                        return ItemStack.EMPTY;
                    }
                    
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                    
                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }
                    
                });
            }
        }
        
        super.addVestSlots(inventory, handler, yOffset);
    }
    
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
    
    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (slotId >= 0 && slotId < (this.rows * 9)) {
            final Slot slot = this.slots.get(slotId);
            
            switch (type) {
                case SWAP:
                case PICKUP:
                case PICKUP_ALL: {
                    final ItemStack carried = this.getCarried();
                    if (carried.getItem() instanceof UVVestItem) return;
                    
                    if (carried.isStackable() && carried.getCount() > 0) {
                        slot.set(carried.copy());
                    } else if (!carried.isStackable() && carried.getCount() > 0) {
                        try {
                            slot.set(carried.copy());
                        } catch (Exception e) {
                            slot.set(ItemStack.EMPTY);
                            player.closeContainer();
                            player.displayClientMessage(UVLanguage.ERROR_REJECTED.translate().withStyle(ChatFormatting.RED), true);
                            return;
                        }
                    } else if (slot.getItem().getCount() > 0) {
                        slot.set(ItemStack.EMPTY);
                    }
                    return;
                }
                default:
                    slot.set(ItemStack.EMPTY);
            }
        }
        
        super.clicked(slotId, button, type, player);
    }
}
