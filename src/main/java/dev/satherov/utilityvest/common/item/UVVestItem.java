package dev.satherov.utilityvest.common.item;

import dev.satherov.utilityvest.client.input.UVKeybindManager;
import dev.satherov.utilityvest.common.menu.UVFilterMenu;
import dev.satherov.utilityvest.common.menu.UVInventoryMenu;
import dev.satherov.utilityvest.config.UVConfig;
import dev.satherov.utilityvest.core.UVRegistry;
import dev.satherov.utilityvest.core.annotations.NothingNull;
import dev.satherov.utilityvest.core.lang.UVLanguage;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NothingNull
public class UVVestItem extends Item {
    
    private final int maxBanks;
    
    public UVVestItem(Properties props, int maxBanks) {
        super(props.stacksTo(1));
        this.maxBanks = maxBanks;
    }
    
    public static UUID ensureVestId(ItemStack stack) {
        UUID vestId = stack.get(UVRegistry.VEST_ID);
        if (vestId != null) {
            return vestId;
        }
        
        UUID createdVestId = UUID.randomUUID();
        stack.set(UVRegistry.VEST_ID, createdVestId);
        return createdVestId;
    }
    
    public static Optional<UUID> getVestId(ItemStack stack) {
        return Optional.ofNullable(stack.get(UVRegistry.VEST_ID));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(UVLanguage.TOOLTIP_VEST_FILTER.translate(UVKeybindManager.GUI_KEY.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(UVLanguage.TOOLTIP_VEST_INVENTORY.translate(UVKeybindManager.GUI_KEY.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(UVLanguage.TOOLTIP_VEST_RESTOCK.translate(UVKeybindManager.RESTOCK.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(UVLanguage.TOOLTIP_VEST_RADIAL.translate(UVKeybindManager.RADIAL_KEY.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(UVLanguage.TOOLTIP_VEST_HOTBAR.translate(UVKeybindManager.SAVE.getKey().getDisplayName(), UVKeybindManager.LOAD.getKey().getDisplayName()).withStyle(ChatFormatting.GRAY));
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler != null) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                UVVestReference reference = UVVestReference.fromHand(hand, UVVestItem.ensureVestId(stack));
                if (player.isCrouching()) {
                    this.openFilterMenu(serverPlayer, reference, stack);
                } else {
                    this.openInventoryMenu(serverPlayer, reference, stack);
                }
            }
            return InteractionResultHolder.success(stack);
            
        }
        return super.use(level, player, hand);
    }
    
    public int getMaxBanks() {
        return this.maxBanks;
    }
    
    public MenuProvider getInventoryMenu(UVVestReference reference) {
        return new SimpleMenuProvider((id, inventory, player) -> new UVInventoryMenu(id, inventory, this.getMaxBanks(), reference), UVLanguage.CONTAINER_UTILITY_VEST.translate());
    }
    
    public MenuProvider getFilterMenu(UVVestReference reference) {
        return new SimpleMenuProvider((id, inventory, player) -> new UVFilterMenu(id, inventory, this.getMaxBanks(), reference), UVLanguage.CONTAINER_FILTERS.translate());
    }
    
    public void openInventoryMenu(ServerPlayer player, UVVestReference reference, ItemStack stack) {
        player.openMenu(this.getInventoryMenu(reference), buf -> this.writeMenuData(buf, reference, stack));
    }
    
    public void openFilterMenu(ServerPlayer player, UVVestReference reference, ItemStack stack) {
        player.openMenu(this.getFilterMenu(reference), buf -> this.writeMenuData(buf, reference, stack));
    }
    
    public int getLastOpenRow(ItemStack stack) {
        return UVConfig.RememberRadialRow ? stack.getOrDefault(UVRegistry.LAST_OPEN_ROW, 0) : 0;
    }
    
    public void setLastOpenRow(ItemStack stack, int row) {
        stack.set(UVRegistry.LAST_OPEN_ROW, row);
    }
    
    private void writeMenuData(RegistryFriendlyByteBuf buf, UVVestReference reference, ItemStack stack) {
        reference.write(buf);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack.copy());
    }
}
