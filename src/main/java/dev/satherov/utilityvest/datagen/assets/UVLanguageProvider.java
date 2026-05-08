package dev.satherov.utilityvest.datagen.assets;

import dev.satherov.utilityvest.UtilityVest;
import dev.satherov.utilityvest.core.UVRegistry;
import dev.satherov.utilityvest.core.lang.UVLanguage;

import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;

public class UVLanguageProvider extends LanguageProvider {
    
    public UVLanguageProvider(PackOutput output) {
        super(output, UtilityVest.MOD_ID, "en_us");
    }
    
    @Override
    protected void addTranslations() {
        for (UVLanguage lang : UVLanguage.values()) {
            this.add(lang.getTranslationKey(), lang.getEnglishTranslation());
        }
        
        for (DeferredHolder<Item, ? extends Item> holder : UVRegistry.ITEMS.getEntries()) {
            this.add(holder.get(), this.format(holder.getId().getPath()));
        }
    }
    
    private String format(String object) {
        String[] parts = object.split("_");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
        }
        return result.toString().trim();
    }
}
