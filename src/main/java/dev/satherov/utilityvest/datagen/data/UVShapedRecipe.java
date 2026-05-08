package dev.satherov.utilityvest.datagen.data;

import dev.satherov.utilityvest.core.UVRegistry;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class UVShapedRecipe extends ShapedRecipe {
    
    public UVShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack center = input.getItem(1, 1);
        ItemStack result = this.getResultItem(registries).copy();
        result.copyFrom(center, UVRegistry.VEST_INVENTORY, UVRegistry.ITEM_INVENTORY, UVRegistry.FILTER_INVENTORY);
        return result;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer() {
        return UVRegistry.UPGRADE_SERIALIZER.get();
    }
    
    public static class Serializer implements RecipeSerializer<UVShapedRecipe> {
        public static final MapCodec<UVShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(
                p_340778_ -> p_340778_.group(
                                Codec.STRING.optionalFieldOf("group", "").forGetter(recipe -> recipe.getGroup()),
                                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(recipe -> recipe.category()),
                                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(p_311730_ -> p_311730_.getResultItem(null)),
                                Codec.BOOL.optionalFieldOf("show_notification", Boolean.valueOf(true)).forGetter(recipe -> recipe.showNotification())
                        )
                        .apply(p_340778_, UVShapedRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, UVShapedRecipe> STREAM_CODEC = StreamCodec.of(
                UVShapedRecipe.Serializer::toNetwork, UVShapedRecipe.Serializer::fromNetwork
        );
        
        private static UVShapedRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String s = buffer.readUtf();
            CraftingBookCategory craftingbookcategory = buffer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern shapedrecipepattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            ItemStack itemstack = ItemStack.STREAM_CODEC.decode(buffer);
            boolean flag = buffer.readBoolean();
            return new UVShapedRecipe(s, craftingbookcategory, shapedrecipepattern, itemstack, flag);
        }
        
        private static void toNetwork(RegistryFriendlyByteBuf buffer, UVShapedRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(null));
            buffer.writeBoolean(recipe.showNotification());
        }
        
        @Override
        public MapCodec<UVShapedRecipe> codec() {
            return Serializer.CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, UVShapedRecipe> streamCodec() {
            return Serializer.STREAM_CODEC;
        }
    }
}
