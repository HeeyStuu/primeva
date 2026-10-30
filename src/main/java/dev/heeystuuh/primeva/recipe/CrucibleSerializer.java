package dev.heeystuuh.primeva.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.HashMap;
import java.util.Map;

public class CrucibleSerializer implements RecipeSerializer<CrucibleRecipe> {
    public static final MapCodec<CrucibleRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("catalyst").forGetter(r -> r.catalyst),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("essences").forGetter(r -> r.raw),
            Codec.STRING.optionalFieldOf("research", "").forGetter(r -> r.research),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result)
    ).apply(i, CrucibleRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, r -> r.catalyst,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT), r -> new HashMap<>(r.raw),
            ByteBufCodecs.STRING_UTF8, r -> r.research,
            ItemStack.STREAM_CODEC, r -> r.result,
            (c, m, res, out) -> new CrucibleRecipe(c, (Map<String, Integer>) m, res, out));

    @Override
    public MapCodec<CrucibleRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CrucibleRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
