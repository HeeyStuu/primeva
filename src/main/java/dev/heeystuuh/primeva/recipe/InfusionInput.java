package dev.heeystuuh.primeva.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

/** Item do centro do altar + itens dos pedestais ao redor. */
public record InfusionInput(ItemStack center, List<ItemStack> pedestals) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? center : pedestals.get(index - 1);
    }

    @Override
    public int size() {
        return pedestals.size() + 1;
    }
}
