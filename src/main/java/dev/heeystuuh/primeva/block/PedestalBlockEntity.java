package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class PedestalBlockEntity extends SyncedBlockEntity implements DisplayItem {
    private ItemStack item = ItemStack.EMPTY;

    public PedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PEDESTAL.get(), pos, state);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack stack) {
        item = stack;
        sync();
    }

    @Override
    public ItemStack displayItem() {
        return item;
    }

    @Override
    public double displayHeight() {
        return 1.1;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        putStack(tag, "item", item, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        item = getStack(tag, "item", registries);
    }
}
