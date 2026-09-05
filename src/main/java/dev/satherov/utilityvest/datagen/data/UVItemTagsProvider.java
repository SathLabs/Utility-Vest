package dev.satherov.utilityvest.datagen.data;

import dev.satherov.utilityvest.core.UVRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class UVItemTagsProvider extends ItemTagsProvider {
    
    public UVItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, CompletableFuture.completedFuture(TagLookup.empty()));
    }
    
    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(UVRegistry.UTILITY_VEST_TAG)
                .add(UVRegistry.LEATHER_UTILITY_VEST.get())
                .add(UVRegistry.IRON_UTILITY_VEST.get())
                .add(UVRegistry.GOLD_UTILITY_VEST.get())
                .add(UVRegistry.DIAMOND_UTILITY_VEST.get())
                .add(UVRegistry.NETHERITE_UTILITY_VEST.get())
                .add(UVRegistry.ALLTHEMODIUM_UTILITY_VEST.get())
                .add(UVRegistry.VIBRANIUM_UTILITY_VEST.get())
                .add(UVRegistry.UNOBTAINIUM_UTILITY_VEST.get());
    }
}
