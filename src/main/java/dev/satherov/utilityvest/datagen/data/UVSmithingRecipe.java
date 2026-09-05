package dev.satherov.utilityvest.datagen.data;

import dev.satherov.utilityvest.core.UVRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Extends the vanilla smithing recipe (rather than implementing SmithingRecipe from scratch) so that
 * recipe viewers (JEI, EMI, REI) and anything else that only knows how to read a genuine
 * SmithingTransformRecipe still recognize and display this upgrade correctly. Only assemble() is
 * overridden, to carry the vest's stored inventory over to the result instead of returning a fresh stack.
 */
public class UVSmithingRecipe extends SmithingTransformRecipe {

    private final Ingredient template;
    private final Ingredient base;
    private final Ingredient addition;
    private final ItemStack result;

    public UVSmithingRecipe(Ingredient template, Ingredient base, Ingredient addition, ItemStack result) {
        super(template, base, addition, result);
        this.template = template;
        this.base = base;
        this.addition = addition;
        this.result = result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ItemStack assemble(SmithingRecipeInput input, HolderLookup.Provider registries) {
        ItemStack result = this.getResultItem(registries).copy();
        result.copyFrom(input.base(), UVRegistry.VEST_INVENTORY, UVRegistry.ITEM_INVENTORY, UVRegistry.FILTER_INVENTORY);
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return UVRegistry.UPGRADE_SMITHING_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<UVSmithingRecipe> {
        public static final MapCodec<UVSmithingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                p_340778_ -> p_340778_.group(
                                Ingredient.CODEC.fieldOf("template").forGetter(recipe -> recipe.template),
                                Ingredient.CODEC.fieldOf("base").forGetter(recipe -> recipe.base),
                                Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> recipe.addition),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.result)
                        )
                        .apply(p_340778_, UVSmithingRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, UVSmithingRecipe> STREAM_CODEC = StreamCodec.of(
                UVSmithingRecipe.Serializer::toNetwork, UVSmithingRecipe.Serializer::fromNetwork
        );

        private static UVSmithingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            Ingredient template = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient base = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            Ingredient addition = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            return new UVSmithingRecipe(template, base, addition, result);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, UVSmithingRecipe recipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.template);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.base);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.addition);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.result);
        }

        @Override
        public MapCodec<UVSmithingRecipe> codec() {
            return Serializer.CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, UVSmithingRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }
    }
}
