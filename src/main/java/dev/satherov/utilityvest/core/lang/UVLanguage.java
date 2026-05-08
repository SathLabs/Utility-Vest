package dev.satherov.utilityvest.core.lang;

import dev.satherov.utilityvest.UtilityVest;

import net.minecraft.Util;

public enum UVLanguage implements ILangEntry {
    ITEM_GROUP("itemGroup.utilityvest", "Utility Vest"),
    
    NETWORK_SAVE_LOAD_FAILED("network", "save_load.failed", "Hotbar save / load payload failed: %s"),
    NETWORK_OPEN_MENU_FAILED("network", "open_menu.failed", "Opening vest menu failed: %s"),
    NETWORK_RESTOCK_FAILED("network", "restock.failed", "Restock payload failed: %s"),
    
    CONTAINER_UTILITY_VEST("container", "utility_vest", "Utility Vest"),
    CONTAINER_TOOL_VEST("container", "tool_vest", "Tool Vest"),
    CONTAINER_FILTERS("container", "filters", "Filters"),
    
    TOOLTIP_VEST_FILTER("tooltip", "vest.filter", "Press Sneak + %s to open the filter menu"),
    TOOLTIP_VEST_INVENTORY("tooltip", "vest.inventory", "Press %s to open the inventory menu"),
    TOOLTIP_VEST_HOTBAR("tooltip", "vest.hotbar", "Set filters via %s + a number key. Load filters via %s + a number key"),
    TOOLTIP_VEST_RADIAL("tooltip", "vest.radial", "Hold %s + a number key to open the quick-select menu"),
    TOOLTIP_VEST_RESTOCK("tooltip", "vest.restock", "Press %s to insert items matching your filters into the vest's inventory"),
    
    TOOLTIP_MENU_SWAP("tooltip", "menu.swap", "%s to swap with %s"),
    TOOLTIP_MENU_INSERT("tooltip", "menu.insert", "%s to insert %s into vest inventory"),
    TOOLTIP_MENU_CYCLE("tooltip", "menu.cycle", "%s and %s to cycle hotbars"),
    
    CHAT_SAVED("chat", "saved", "Saved Hotbar %s"),
    CHAT_LOADED("chat", "loaded", "Loaded Hotbar %s"),
    CHAT_RESTOCKED("chat", "restocked", "Restocked vest inventory"),
    
    KEY_CATEGORY("key", "category", "Utility Vest"),
    KEY_GUI("key", "gui", "Open Gui"),
    KEY_RADIAL("key", "radial", "Open Radial Menu"),
    KEY_RESTOCK("key", "restock", "Restock"),
    KEY_LOAD("key", "load", "Load"),
    KEY_SAVE("key", "save", "Save"),
    
    INPUT_WHEEL_UP("input", "wheel.up", "Scroll Up"),
    INPUT_WHEEL_DOWN("input", "wheel.down", "Scroll Down"),
    
    ERROR_REJECTED("error", "rejected", "Crashing entry! Rejected"),
    
    CONFIG_INVERT_RADIAL_SCROLL("utilityvest.configuration.invert_radial_scroll", "Invert Radial Scroll"),
    CONFIG_RADIAL_TOOLTIP("utilityvest.configuration.radial_tooltip", "Radial Tooltip"),
    CONFIG_RADIAL_STACK_COUNT("utilityvest.configuration.radial_stack_count", "Radial Stack Count"),
    CONFIG_REMEMBER_RADIAL_ROW("utilityvest.configuration.remember_radial_row", "Remember Radial Row"),
    ;
    
    private final String key;
    private final String value;
    
    UVLanguage(String type, String key, String value) {
        this(Util.makeDescriptionId(type, UtilityVest.rl(key)), value);
    }
    
    UVLanguage(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    @Override
    public String getTranslationKey() {
        return this.key;
    }
    
    public String getEnglishTranslation() {
        return this.value;
    }
}
