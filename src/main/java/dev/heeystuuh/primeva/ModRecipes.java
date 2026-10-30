package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.recipe.CrucibleRecipe;
import dev.heeystuuh.primeva.recipe.CrucibleSerializer;
import dev.heeystuuh.primeva.recipe.InfusionRecipe;
import dev.heeystuuh.primeva.recipe.InfusionSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, PrimevaMod.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, PrimevaMod.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<InfusionRecipe>> INFUSION_TYPE =
            TYPES.register("infusion", () -> RecipeType.simple(PrimevaMod.id("infusion")));
    public static final DeferredHolder<RecipeSerializer<?>, InfusionSerializer> INFUSION_SERIALIZER =
            SERIALIZERS.register("infusion", InfusionSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CrucibleRecipe>> CRUCIBLE_TYPE =
            TYPES.register("crucible", () -> RecipeType.simple(PrimevaMod.id("crucible")));
    public static final DeferredHolder<RecipeSerializer<?>, CrucibleSerializer> CRUCIBLE_SERIALIZER =
            SERIALIZERS.register("crucible", CrucibleSerializer::new);

    private ModRecipes() {}
}
