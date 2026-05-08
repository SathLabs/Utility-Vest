package dev.satherov.utilityvest.datagen.data;

import dev.satherov.utilityvest.core.UVRegistry;
import dev.satherov.utilityvest.core.annotations.NothingNull;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.concurrent.CompletableFuture;

@NothingNull
public class UVRecipeProvider extends RecipeProvider {
    
    public UVRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }
    
    public void vest(RecipeOutput recipeOutput, DeferredHolder<Item, ? extends Item> vest, TagKey<Item> tag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, vest.get())
                .pattern("a a")
                .pattern("aba")
                .pattern("ccc")
                .define('a', Tags.Items.LEATHERS)
                .define('b', Tags.Items.CHESTS)
                .define('c', tag)
                .unlockedBy(String.format("has_%s", tag.location().getPath()), RecipeProvider.has(tag))
                .save(recipeOutput, vest.getId());
    }
    
    public void upgrade(RecipeOutput recipeOutput, DeferredHolder<Item, ? extends Item> vest, TagKey<Item> tag, DeferredHolder<Item, ? extends Item> before) {
        UVShapedRecipeBuilder.shaped(RecipeCategory.MISC, vest.get())
                .pattern(" a ")
                .pattern("aba")
                .pattern(" a ")
                .define('a', tag)
                .define('b', before.get())
                .unlockedBy(String.format("has_%s", before.getId().getPath()), RecipeProvider.has(before.get()))
                .save(recipeOutput, vest.getId());
    }
    
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        
        this.vest(recipeOutput, UVRegistry.LEATHER_UTILITY_VEST, Tags.Items.LEATHERS);
        this.upgrade(recipeOutput, UVRegistry.IRON_UTILITY_VEST, Tags.Items.INGOTS_IRON, UVRegistry.LEATHER_UTILITY_VEST);
        this.upgrade(recipeOutput, UVRegistry.GOLD_UTILITY_VEST, Tags.Items.INGOTS_GOLD, UVRegistry.IRON_UTILITY_VEST);
        this.upgrade(recipeOutput, UVRegistry.DIAMOND_UTILITY_VEST, Tags.Items.GEMS_DIAMOND, UVRegistry.GOLD_UTILITY_VEST);
        this.upgrade(recipeOutput, UVRegistry.NETHERITE_UTILITY_VEST, Tags.Items.INGOTS_NETHERITE, UVRegistry.DIAMOND_UTILITY_VEST);
    }
}
