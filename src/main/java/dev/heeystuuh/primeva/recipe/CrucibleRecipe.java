package dev.heeystuuh.primeva.recipe;

import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.EnumMap;
import java.util.Map;

/**
 * Receita do Cadinho Arcano (type "primeva:crucible"):
 * { "catalyst": ingrediente, "essences": {"terra": 4, ...}, "research": "id", "result": {"id":..} }
 */
public class CrucibleRecipe implements Recipe<SingleRecipeInput> {
    public final Ingredient catalyst;
    public final Map<Essence, Integer> essences;
    public final Map<String, Integer> raw;
    public final String research;
    public final ItemStack result;

    public CrucibleRecipe(Ingredient catalyst, Map<String, Integer> raw, String research, ItemStack result) {
        this.catalyst = catalyst;
        this.raw = raw;
        this.research = research;
        this.result = result;
        this.essences = new EnumMap<>(Essence.class);
        raw.forEach((k, v) -> {
            Essence e = Essence.byId(k);
            if (e != null && v > 0) essences.put(e, v);
        });
    }

    public boolean enough(Map<Essence, Integer> stored) {
        for (Map.Entry<Essence, Integer> e : essences.entrySet()) if (stored.getOrDefault(e.getKey(), 0) < e.getValue()) return false;
        return true;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return catalyst.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int w, int h) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, catalyst);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CRUCIBLE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.CRUCIBLE_TYPE.get();
    }
}
