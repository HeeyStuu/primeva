package dev.heeystuuh.primeva.block;

import net.minecraft.world.item.ItemStack;

/** Blocos que mostram um item flutuando em cima. */
public interface DisplayItem {
    ItemStack displayItem();

    double displayHeight();
}
