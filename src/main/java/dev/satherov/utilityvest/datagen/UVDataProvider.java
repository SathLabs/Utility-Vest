package dev.satherov.utilityvest.datagen;

import dev.satherov.utilityvest.core.annotations.NothingNull;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@NothingNull
public class UVDataProvider implements DataProvider {
    
    private final List<DataProvider> subProviders = new ArrayList<>();
    
    public void addSubProvider(boolean include, DataProvider provider) {
        if (include) this.subProviders.add(provider);
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> list = new ArrayList<>();
        for (DataProvider provider : this.subProviders) list.add(provider.run(cachedOutput));
        return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
    }
    
    @Override
    public String getName() {
        return "Utility Vest Data Provider";
    }
}
