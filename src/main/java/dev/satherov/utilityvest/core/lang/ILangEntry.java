package dev.satherov.utilityvest.core.lang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public interface ILangEntry {
    
    String getTranslationKey();
    
    default MutableComponent translate() {
        return Component.translatable(this.getTranslationKey());
    }
    
    default MutableComponent translate(Object... args) {
        return Component.translatable(this.getTranslationKey(), args);
    }
    
    default MutableComponent translateFormatted(ChatFormatting... formats) {
        return Component.translatable(this.getTranslationKey()).withStyle(formats);
    }
}
