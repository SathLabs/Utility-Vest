package dev.satherov.utilityvest.common.capabilities;

import dev.satherov.utilityvest.common.item.UVVestItem;
import dev.satherov.utilityvest.core.UVRegistry;
import dev.satherov.utilityvest.core.annotations.NothingNull;
import dev.satherov.utilityvest.core.lang.UVLanguage;

import net.neoforged.neoforge.items.ComponentItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@NothingNull
public class UVVestCapability implements IItemHandlerModifiable {
    
    public final ComponentItemHandler filters;
    public final ComponentItemHandler storage;
    
    public UVVestCapability(ItemStack stack, UVVestItem vest) {
        int maxBanks = vest.getMaxBanks();
        
        this.filters = new ComponentItemHandler(stack, UVRegistry.FILTER_INVENTORY.get(), maxBanks * 9);
        this.storage = new ComponentItemHandler(stack, UVRegistry.ITEM_INVENTORY.get(), maxBanks * 9);
    }
    
    @Override
    public int getSlots() {
        return this.storage.getSlots();
    }
    
    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        this.storage.setStackInSlot(slot, stack);
    }
    
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.storage.getStackInSlot(slot);
    }
    
    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.storage.insertItem(slot, stack, simulate);
    }
    
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.storage.extractItem(slot, amount, simulate);
    }
    
    @Override
    public int getSlotLimit(int slot) {
        return this.storage.getSlotLimit(slot);
    }
    
    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.storage.isItemValid(slot, stack);
    }
    
    public void saveHotbar(Player player, int index) {
        NonNullList<ItemStack> hotbar = this.getHotbar(player.getInventory());
        for (int i = 0; i < 9; i++) {
            this.filters.setStackInSlot(i + index * 9, hotbar.get(i).copy());
        }
        
        player.inventoryMenu.broadcastChanges();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
        }
        player.displayClientMessage(UVLanguage.CHAT_SAVED.translate(index).withStyle(ChatFormatting.GRAY), true);
    }
    
    public void loadHotbar(Player player, int index) {
        NonNullList<ItemStack> hotbar = this.getHotbar(player.getInventory());
        List<ItemStack> itemsToReturn = new ArrayList<>();
        
        for (ItemStack stack : hotbar) {
            if (this.matchFilter(stack)) {
                ItemStack overflow = this.insertWithOverflow(stack);
                if (!overflow.isEmpty()) {
                    itemsToReturn.add(overflow);
                }
            } else {
                itemsToReturn.add(stack);
            }
        }
        
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, ItemStack.EMPTY);
        }
        
        int slot = 0;
        for (ItemStack stack : this.getFilters(index)) {
            if (!stack.isEmpty()) {
                player.getInventory().setItem(slot, this.buildStackFromItems(stack, new ContainerItemHandler(this)));
            }
            slot++;
        }
        
        for (ItemStack stack : itemsToReturn) {
            
            ItemStack remaining = stack.copy();
            Inventory inv = player.getInventory();
            
            int match = inv.findSlotMatchingItem(stack);
            
            if (match != -1) {
                ItemStack inSlot = inv.getItem(match);
                int toInsert = Math.min(stack.getCount(), inSlot.getMaxStackSize() - inSlot.getCount());
                int remainingCount = stack.getCount() - toInsert;
                ItemStack matchCopy = stack.copyWithCount(toInsert);
                
                if (toInsert > 0) {
                    inv.add(match, matchCopy);
                }
                
                if (remainingCount > 0) {
                    remaining = stack.copyWithCount(remainingCount);
                }
            }
            
            for (int i = 9; i < inv.items.size(); i++) {
                if (inv.items.get(i).isEmpty()) {
                    inv.add(i, remaining);
                    remaining = ItemStack.EMPTY;
                    break;
                }
            }
            
            if (!remaining.isEmpty()) {
                remaining = this.insertWithOverflow(remaining);
            }
            
            if (!remaining.isEmpty()) {
                player.drop(remaining, false);
            }
        }
        
        
        player.inventoryMenu.broadcastChanges();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.broadcastChanges();
        }
        player.displayClientMessage(UVLanguage.CHAT_LOADED.translate(index).withStyle(ChatFormatting.GRAY), true);
    }
    
    public ItemStack swap(ItemStack stack, int idx) {
        if (idx == -1) {
            for (int i = 0; i < this.storage.getSlots(); i++) {
                ItemStack inSlot = this.storage.getStackInSlot(i);
                if (inSlot.isEmpty()) {
                    idx = i;
                    break;
                }
            }
        }
        if (idx == -1) return stack;
        ItemStack result = this.storage.getStackInSlot(idx);
        this.storage.setStackInSlot(idx, stack);
        return result;
    }
    
    public void collectItems(Player player) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.items.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            
            if (this.matchFilter(stack)) {
                inv.setItem(i, this.insertWithOverflow(stack));
            }
        }
        for (int i = 0; i < inv.offhand.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            
            if (this.matchFilter(stack)) {
                inv.setItem(i, this.insertWithOverflow(stack));
            }
        }
        player.displayClientMessage(UVLanguage.CHAT_RESTOCKED.translate().withStyle(ChatFormatting.GRAY), true);
    }
    
    private NonNullList<ItemStack> getHotbar(Inventory inv) {
        return NonNullList.of(ItemStack.EMPTY, inv.items.stream()
                .limit(9)
                .toArray(ItemStack[]::new));
    }
    
    public NonNullList<ItemStack> getFilters() {
        NonNullList<ItemStack> contents = NonNullList.withSize(this.filters.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < this.filters.getSlots(); i++) {
            contents.set(i, this.filters.getStackInSlot(i));
        }
        
        if (contents.isEmpty()) return contents;
        return NonNullList.of(ItemStack.EMPTY, contents.toArray(ItemStack[]::new));
    }
    
    public NonNullList<ItemStack> getFilters(int index) {
        NonNullList<ItemStack> results = NonNullList.withSize(9, ItemStack.EMPTY);
        int startIndex = index * 9;
        
        for (int i = 0; i < 9; i++) {
            if (startIndex + i < this.filters.getSlots()) {
                results.set(i, this.filters.getStackInSlot(startIndex + i));
            }
        }
        
        return results;
    }
    
    public NonNullList<ItemStack> getStorage() {
        NonNullList<ItemStack> contents = NonNullList.withSize(this.storage.getSlots(), ItemStack.EMPTY);
        for (int i = 0; i < this.storage.getSlots(); i++) {
            contents.set(i, this.storage.getStackInSlot(i));
        }
        
        if (contents.isEmpty()) {
            return NonNullList.withSize(this.storage.getSlots(), ItemStack.EMPTY);
        }
        return NonNullList.of(ItemStack.EMPTY, contents.toArray(ItemStack[]::new));
    }
    
    public NonNullList<ItemStack> getStorage(int index) {
        NonNullList<ItemStack> results = NonNullList.withSize(9, ItemStack.EMPTY);
        int startIndex = index * 9;
        
        for (int i = 0; i < 9; i++) {
            if (startIndex + i < this.storage.getSlots()) {
                results.set(i, this.storage.getStackInSlot(startIndex + i));
            }
        }
        
        return results;
    }
    
    public boolean matchFilter(ItemStack stack) {
        if (stack.is(UVRegistry.UTILITY_VEST_TAG)) return false;
        var filtersList = this.getFilters();
        if (filtersList.stream().allMatch(ItemStack::isEmpty)) return true;
        return filtersList.stream().anyMatch(f -> ItemStack.isSameItem(f, stack));
    }
    
    private ItemStack insertWithOverflow(ItemStack toInsert) {
        if (toInsert.isEmpty()) return ItemStack.EMPTY;
        ItemStack remainder = toInsert.copy();
        
        for (int i = 0; i < this.getSlots(); i++) {
            ItemStack inSlot = this.getStackInSlot(i);
            if (!inSlot.isEmpty() && ItemStack.isSameItem(inSlot, remainder)) {
                remainder = this.insertItem(i, remainder, false);
                if (remainder.isEmpty()) return ItemStack.EMPTY;
            }
        }
        
        for (int i = 0; i < this.getSlots(); i++) {
            if (remainder.isEmpty()) break;
            if (this.getStackInSlot(i).isEmpty()) {
                remainder = this.insertItem(i, remainder, false);
            }
        }
        
        return remainder;
    }
    
    
    private ItemStack buildStackFromItems(ItemStack template, IItemSourceHandler source) {
        if (template.isEmpty()) return ItemStack.EMPTY;
        
        ItemStack result = ItemStack.EMPTY;
        int neededAmount = template.getCount();
        
        for (int i = 0; i < source.getSize(); i++) {
            ItemStack storedItem = source.getStackInSlot(i);
            if (storedItem.isEmpty()) continue;
            
            if (ItemStack.isSameItem(template, storedItem)) {
                if (result.isEmpty()) {
                    result = storedItem.copy();
                    int extracted = source.extractItem(i, Math.min(storedItem.getCount(), neededAmount));
                    result.setCount(extracted);
                } else {
                    int remainingNeeded = neededAmount - result.getCount();
                    int extracted = source.extractItem(i, Math.min(storedItem.getCount(), remainingNeeded));
                    result.grow(extracted);
                }
                
                if (result.getCount() == neededAmount) {
                    return result;
                }
            }
        }
        
        return result;
    }
    
    private interface IItemSourceHandler {
        int getSize();
        
        ItemStack getStackInSlot(int slot);
        
        int extractItem(int slot, int amount);
    }
    
    private record ContainerItemHandler(UVVestCapability container) implements IItemSourceHandler {
        
        @Override
        public int getSize() {
            return this.container.getSlots();
        }
        
        @Override
        public ItemStack getStackInSlot(int slot) {
            return this.container.getStackInSlot(slot);
        }
        
        @Override
        public int extractItem(int slot, int amount) {
            return this.container.extractItem(slot, amount, false).getCount();
        }
    }
    
    private record PlayerInventoryHandler(Inventory inventory) implements IItemSourceHandler {
        
        @Override
        public int getSize() {
            return this.inventory.getContainerSize();
        }
        
        @Override
        public ItemStack getStackInSlot(int slot) {
            return this.inventory.getItem(slot);
        }
        
        @Override
        public int extractItem(int slot, int amount) {
            return this.inventory.removeItem(slot, amount).getCount();
        }
    }
}

