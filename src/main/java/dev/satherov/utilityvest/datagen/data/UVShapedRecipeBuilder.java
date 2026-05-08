package dev.satherov.utilityvest.datagen.data;

import dev.satherov.utilityvest.core.annotations.NothingNull;
import dev.satherov.utilityvest.core.mixin.ShapedRecipeBuilderAccessor;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;

import java.util.Map;
import java.util.Objects;

@NothingNull
public class UVShapedRecipeBuilder extends ShapedRecipeBuilder {
    
    public UVShapedRecipeBuilder(RecipeCategory category, ItemLike result, int count) {
        super(category, result, count);
    }
    
    public static UVShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result) {
        return UVShapedRecipeBuilder.shaped(category, result, 1);
    }
    
    public static UVShapedRecipeBuilder shaped(RecipeCategory category, ItemLike result, int count) {
        return new UVShapedRecipeBuilder(category, result, count);
    }
    
    public UVShapedRecipeBuilder pattern(String pattern) {
        return (UVShapedRecipeBuilder) super.pattern(pattern);
    }
    
    public UVShapedRecipeBuilder define(Character symbol, TagKey<Item> tag) {
        return (UVShapedRecipeBuilder) super.define(symbol, tag);
    }
    
    public UVShapedRecipeBuilder define(Character symbol, ItemLike item) {
        return (UVShapedRecipeBuilder) super.define(symbol, item);
    }
    
    public UVShapedRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        return (UVShapedRecipeBuilder) super.unlockedBy(name, criterion);
    }
    
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        ShapedRecipeBuilderAccessor accessor = (ShapedRecipeBuilderAccessor) this;
        ShapedRecipePattern shapedrecipepattern = accessor.onEnsureValid(id);
        Advancement.Builder advancement$builder = recipeOutput.advancement().addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id)).rewards(AdvancementRewards.Builder.recipe(id)).requirements(AdvancementRequirements.Strategy.OR);
        Map<String, Criterion<?>> criteria = accessor.criteria();
        Objects.requireNonNull(advancement$builder);
        criteria.forEach(advancement$builder::addCriterion);
        UVShapedRecipe recipe = new UVShapedRecipe(Objects.requireNonNullElse(accessor.group(), ""), RecipeBuilder.determineBookCategory(accessor.category()), shapedrecipepattern, accessor.result(), accessor.showNotification());
        recipeOutput.accept(id, recipe, advancement$builder.build(id.withPrefix("recipes/" + accessor.category().getFolderName() + "/")));
    }
}
