package dev.satherov.utilityvest.datagen.data;

import dev.satherov.utilityvest.core.annotations.NothingNull;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.LinkedHashMap;
import java.util.Map;

@NothingNull
public class UVSmithingRecipeBuilder {

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final RecipeCategory category;
    private final ItemStack result;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    public UVSmithingRecipeBuilder(Ingredient template, Ingredient base, Ingredient addition, RecipeCategory category, ItemLike result) {
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.category = category;
        this.result = new ItemStack(result);
    }

    public static UVSmithingRecipeBuilder smithing(Ingredient template, Ingredient base, Ingredient addition, RecipeCategory category, ItemLike result) {
        return new UVSmithingRecipeBuilder(template, base, addition, category, result);
    }

    public UVSmithingRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
        Advancement.Builder advancement$builder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement$builder::addCriterion);
        UVSmithingRecipe recipe = new UVSmithingRecipe(this.template, this.base, this.addition, this.result);
        recipeOutput.accept(id, recipe, advancement$builder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }
}
