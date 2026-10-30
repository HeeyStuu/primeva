package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.*;
import dev.heeystuuh.primeva.item.PhialItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class EssenceJarBlockEntity extends SyncedBlockEntity {
    private Essence essence;
    private int amount;

    public EssenceJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENCE_JAR.get(), pos, state);
    }

    public static int capacity() {
        return Config.get(Config.JAR_CAPACITY);
    }

    public Essence essence() {
        return essence;
    }

    public int amount() {
        return amount;
    }

    public int signal() {
        return amount == 0 ? 0 : 1 + (int) (14f * amount / capacity());
    }

    /** Quanto deste tipo cabe. */
    public int space(Essence e) {
        if (essence != null && essence != e) return 0;
        return capacity() - amount;
    }

    public int insert(Essence e, int n) {
        int put = Math.min(n, space(e));
        if (put <= 0) return 0;
        essence = e;
        amount += put;
        sync();
        return put;
    }

    public boolean take(Essence e, int n) {
        if (essence != e || amount < n) return false;
        amount -= n;
        if (amount == 0) essence = null;
        sync();
        return true;
    }

    public void pour(Player p, ItemStack phial, Essence e) {
        if (space(e) < PhialItem.AMOUNT) {
            p.displayClientMessage(Component.translatable("msg.primeva.jar_full").withStyle(ChatFormatting.RED), true);
            return;
        }
        insert(e, PhialItem.AMOUNT);
        if (!p.getAbilities().instabuild) {
            phial.shrink(1);
            ItemStack empty = new ItemStack(ModItems.EMPTY_PHIAL.get());
            if (!p.getInventory().add(empty)) p.drop(empty, false);
        }
        level.playSound(null, worldPosition, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1f, 1f);
    }

    public void fill(Player p, ItemStack empty) {
        if (essence == null || amount < PhialItem.AMOUNT) {
            report(p);
            return;
        }
        Essence e = essence;
        take(e, PhialItem.AMOUNT);
        if (!p.getAbilities().instabuild) empty.shrink(1);
        ItemStack full = new ItemStack(ModItems.phial(e));
        if (!p.getInventory().add(full)) p.drop(full, false);
        level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1f, 1f);
    }

    public void vent(Player p) {
        if (amount > 0) Miasma.add(level, worldPosition, 1 + amount / 16);
        amount = 0;
        essence = null;
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6f, 1.4f);
        p.displayClientMessage(Component.translatable("msg.primeva.jar_vented").withStyle(ChatFormatting.GRAY), true);
        sync();
    }

    public void report(Player p) {
        Component msg = essence == null ? Component.translatable("msg.primeva.empty").withStyle(ChatFormatting.GRAY)
                : essence.displayName().copy().append(Component.literal(" " + amount + "/" + capacity()).withStyle(ChatFormatting.WHITE));
        p.displayClientMessage(Component.translatable("block.primeva.essence_jar").withStyle(ChatFormatting.GOLD).append(": ").append(msg), true);
    }

    /** O jarro quebrado guarda a essência no item (e devolve ao ser colocado de novo). */
    @Override
    protected void collectImplicitComponents(net.minecraft.core.component.DataComponentMap.Builder b) {
        super.collectImplicitComponents(b);
        if (essence != null && amount > 0) {
            CompoundTag t = new CompoundTag();
            t.putString("essence", essence.id);
            t.putInt("amount", amount);
            net.minecraft.world.level.block.entity.BlockEntity.addEntityType(t, getType());
            b.set(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.CustomData.of(t));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (essence != null) tag.putString("essence", essence.id);
        tag.putInt("amount", amount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        essence = tag.contains("essence") ? Essence.byId(tag.getString("essence")) : null;
        amount = tag.getInt("amount");
        if (essence == null) amount = 0;
    }
}
