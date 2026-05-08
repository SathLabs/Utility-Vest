package dev.satherov.utilityvest.common.menu;


import dev.satherov.utilityvest.common.capabilities.UVVestCapability;
import dev.satherov.utilityvest.common.item.UVVestReference;
import dev.satherov.utilityvest.core.annotations.NothingNull;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

@NothingNull
public abstract class UVVestMenu extends AbstractContainerMenu {
    
    protected final int rows;
    protected final UVVestReference vestReference;
    
    public UVVestMenu(MenuType<?> menuType, int containerId, Inventory inventory, int rows, UVVestReference vestReference, @Nullable ItemStack fallbackVestStack) {
        super(menuType, containerId);
        final Player player = inventory.player;
        this.vestReference = vestReference;
        
        ItemStack stack = vestReference.resolve(player);
        if (stack.isEmpty() && fallbackVestStack != null && !fallbackVestStack.isEmpty()) {
            stack = fallbackVestStack;
        }
        this.rows = rows;
        if (stack.isEmpty()) {
            player.closeContainer();
            return;
        }
        
        
        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (!(handler instanceof UVVestCapability cap)) {
            player.closeContainer();
            return;
        }
        
        int yOffset = (rows - 4) * 18;
        this.addVestSlots(inventory, cap, yOffset);
    }
    
    protected void addVestSlots(Inventory inventory, UVVestCapability handler, int yOffset) {
        InvWrapper wrapper = new InvWrapper(inventory);
        
        // Player Inventory
        for (int inv = 0; inv < 3; inv++) {
            for (int j1 = 0; j1 < 9; j1++) {
                this.addSlot(new SlotItemHandler(wrapper, j1 + inv * 9 + 9, 8 + j1 * 18, 103 + inv * 18 + yOffset));
            }
        }
        
        // Hotbar
        for (int hotbar = 0; hotbar < 9; hotbar++) {
            this.addSlot(new SlotItemHandler(wrapper, hotbar, 8 + hotbar * 18, 161 + yOffset));
        }
    }
    
    public int getRows() {
        return this.rows;
    }
    
    @Override
    public boolean stillValid(Player player) {
        return player.isAlive() && !this.vestReference.resolve(player).isEmpty();
    }
    
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.PICKUP && slotId >= 0 && slotId < (this.rows * 9)) {
            final Slot slot = this.slots.get(slotId);
            final ClickAction action = button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
            final ItemStack stack = slot.getItem();
            final ItemStack carried = this.getCarried();
            final SlotAccess carriedAccess = this.createAccess();
            
            if (CommonHooks.onItemStackedOn(carried, stack, slot, action, player, carriedAccess)) {
                slot.setChanged();
                this.broadcastChanges();
                return;
            }
            
            if (!carried.isEmpty() && carried.overrideStackedOnOther(slot, action, player)) {
                slot.setChanged();
                this.broadcastChanges();
                return;
            }
            
            if (!stack.isEmpty() && stack.overrideOtherStackedOnMe(carried, slot, action, player, carriedAccess)) {
                slot.set(stack.copy());
                slot.setChanged();
                this.broadcastChanges();
                return;
            }
        }
        
        super.clicked(slotId, button, clickType, player);
    }
    
    private SlotAccess createAccess() {
        return new SlotAccess() {
            @Override
            public ItemStack get() {
                return UVVestMenu.this.getCarried();
            }
            
            @Override
            public boolean set(ItemStack stack) {
                UVVestMenu.this.setCarried(stack);
                return true;
            }
        };
    }
}