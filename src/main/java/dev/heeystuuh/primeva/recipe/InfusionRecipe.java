package dev.heeystuuh.primeva.recipe;

import dev.heeystuuh.primeva.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Receita do Altar de Infusão (data/<ns>/recipe/*.json, type "primeva:infusion"):
 * { "center": ingrediente, "pedestals": [ingredientes], "research": "id", "instability": n, "tier": 1-3, "result": {"id":..,"count":..} }
 */
public class InfusionRecipe implements Recipe<InfusionInput> {
    public final Ingredient center;
    public final List<Ingredient> pedestals;
    public final String research;
    public final int instability;
    public final ItemStack result;
    /** 1 = altar simples, 2 = exige 4 Pilares de Contenção, 3 = exige 8 pilares + Catalisador Primordial sob o altar. */
    public final int tier;
    /** Essência drenada dos jarros durante o ritual (vazio = calculado pelo nível). */
    public final java.util.Map<String, Integer> rawEssences;

    public InfusionRecipe(Ingredient center, List<Ingredient> pedestals, String research, int instability, ItemStack result, int tier) {
        this(center, pedestals, research, instability, result, tier, java.util.Map.of());
    }

    public InfusionRecipe(Ingredient center, List<Ingredient> pedestals, String research, int instability, ItemStack result, int tier,
                          java.util.Map<String, Integer> rawEssences) {
        this.center = center;
        this.pedestals = pedestals;
        this.research = research;
        this.instability = instability;
        this.result = result;
        this.tier = tier;
        this.rawEssences = rawEssences;
    }

    /** Quanto de cada essência o ritual drena dos jarros próximos. */
    public java.util.Map<dev.heeystuuh.primeva.Essence, Integer> essenceDrain() {
        java.util.Map<dev.heeystuuh.primeva.Essence, Integer> out = new java.util.EnumMap<>(dev.heeystuuh.primeva.Essence.class);
        rawEssences.forEach((k, v) -> {
            dev.heeystuuh.primeva.Essence e = dev.heeystuuh.primeva.Essence.byId(k);
            if (e != null && v > 0) out.put(e, v);
        });
        if (!out.isEmpty()) return out;
        int base = tier >= 3 ? 64 : tier == 2 ? 24 : 8;
        for (Ingredient ing : pedestals) {
            for (ItemStack s : ing.getItems()) {
                if (s.getItem() instanceof dev.heeystuuh.primeva.item.PhialItem p) {
                    out.merge(p.essence, base, Integer::sum);
                    break;
                }
            }
        }
        if (out.isEmpty()) out.put(dev.heeystuuh.primeva.Essence.LUMEN, base);
        return out;
    }

    public static int pillarsNeeded(int tier) {
        return tier >= 3 ? 8 : tier == 2 ? 4 : 0;
    }

    /** Para cada ingrediente de pedestal, o índice do item que o satisfaz; null se não bate. */
    public int[] match(ItemStack centerItem, List<ItemStack> items) {
        if (!center.test(centerItem)) return null;
        if (items.size() < pedestals.size()) return null;
        int[] assign = new int[pedestals.size()];
        boolean[] used = new boolean[items.size()];
        return solve(0, items, used, assign) ? assign : null;
    }

    private boolean solve(int i, List<ItemStack> items, boolean[] used, int[] assign) {
        if (i == pedestals.size()) return true;
        for (int j = 0; j < items.size(); j++) {
            if (!used[j] && pedestals.get(i).test(items.get(j))) {
                used[j] = true;
                assign[i] = j;
                if (solve(i + 1, items, used, assign)) return true;
                used[j] = false;
            }
        }
        return false;
    }

    @Override
    public boolean matches(InfusionInput input, Level level) {
        return match(input.center(), input.pedestals()) != null;
    }

    @Override
    public ItemStack assemble(InfusionInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(center);
        list.addAll(pedestals);
        return list;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.INFUSION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.INFUSION_TYPE.get();
    }
}
