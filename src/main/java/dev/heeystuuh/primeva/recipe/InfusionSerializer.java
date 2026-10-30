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

public class InfusionSerializer implements RecipeSerializer<InfusionRecipe> {
    public static final MapCodec<InfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Ingredient.CODEC_NONEMPTY.fieldOf("center").forGetter(r -> r.center),
            Ingredient.CODEC_NONEMPTY.listOf().fieldOf("pedestals").forGetter(r -> r.pedestals),
            Codec.STRING.fieldOf("research").forGetter(r -> r.research),
            Codec.INT.optionalFieldOf("instability", 2).forGetter(r -> r.instability),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.result),
            Codec.INT.optionalFieldOf("tier", 1).forGetter(r -> r.tier),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("essences", java.util.Map.of()).forGetter(r -> r.rawEssences)
    ).apply(i, InfusionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> STREAM_CODEC = StreamCodec.of(
            (buf, r) -> {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.center);
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, r.pedestals);
                ByteBufCodecs.STRING_UTF8.encode(buf, r.research);
                ByteBufCodecs.VAR_INT.encode(buf, r.instability);
                ItemStack.STREAM_CODEC.encode(buf, r.result);
                ByteBufCodecs.VAR_INT.encode(buf, r.tier);
                buf.writeVarInt(r.rawEssences.size());
                r.rawEssences.forEach((k, v) -> {
                    buf.writeUtf(k);
                    buf.writeVarInt(v);
                });
            },
            buf -> {
                Ingredient c = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
                java.util.List<Ingredient> p = Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf);
                String res = ByteBufCodecs.STRING_UTF8.decode(buf);
                int ins = ByteBufCodecs.VAR_INT.decode(buf);
                ItemStack out = ItemStack.STREAM_CODEC.decode(buf);
                int t = ByteBufCodecs.VAR_INT.decode(buf);
                int n = buf.readVarInt();
                java.util.Map<String, Integer> ess = new java.util.HashMap<>();
                for (int i = 0; i < n; i++) ess.put(buf.readUtf(), buf.readVarInt());
                return new InfusionRecipe(c, p, res, ins, out, t, ess);
            });

    @Override
    public MapCodec<InfusionRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
