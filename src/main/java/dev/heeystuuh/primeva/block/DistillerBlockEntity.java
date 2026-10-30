package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.Essence;
import dev.heeystuuh.primeva.Miasma;
import dev.heeystuuh.primeva.ModBlockEntities;
import dev.heeystuuh.primeva.ModDataMaps;
import dev.heeystuuh.primeva.ModItems;
import dev.heeystuuh.primeva.item.PhialItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;

/** Destila itens em essências. Precisa de calor embaixo (fogo, lava, magma ou fogueira acesa). */
public class DistillerBlockEntity extends SyncedBlockEntity {
    public static final int TIME = 60;
    public static final int CAPACITY = 64;

    private ItemStack input = ItemStack.EMPTY;
    private final EnumMap<Essence, Integer> stored = new EnumMap<>(Essence.class);
    private int progress;

    public DistillerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISTILLER.get(), pos, state);
    }

    public static boolean heated(Level level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        if (below.is(Blocks.FIRE) || below.is(Blocks.SOUL_FIRE) || below.is(Blocks.LAVA) || below.is(Blocks.MAGMA_BLOCK)) return true;
        return below.is(BlockTags.CAMPFIRES) && below.getOptionalValue(CampfireBlock.LIT).orElse(false);
    }

    public Map<Essence, Integer> stored() {
        return stored;
    }

    private int get(Essence e) {
        return stored.getOrDefault(e, 0);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DistillerBlockEntity be) {
        if (level.getGameTime() % 10 == 0 && !be.stored.isEmpty() && EssenceNetwork.push(level, pos, be.stored, 4)) be.sync();
        if (be.input.isEmpty()) {
            be.progress = 0;
            return;
        }
        if (!heated(level, pos)) return;
        be.progress++;
        if (be.progress % 10 == 0 && level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 3, 0.2, 0.05, 0.2, 0.01);
        }
        if (be.progress < TIME) return;
        be.progress = 0;
        Map<Essence, Integer> ess = ModDataMaps.of(be.input.getItem());
        ess.forEach((e, n) -> be.stored.put(e, Math.min(CAPACITY, be.get(e) + n)));
        be.input.shrink(1);
        if (level.random.nextInt(4) == 0) Miasma.add(level, pos, 1);
        dev.heeystuuh.primeva.Discovery.near(level, pos, "do:distill");
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.6f, 1.2f);
        be.sync();
    }

    public void insert(Player player, ItemStack stack) {
        if (!input.isEmpty() && !ItemStack.isSameItemSameComponents(input, stack)) {
            player.displayClientMessage(Component.translatable("msg.primeva.distiller_busy").withStyle(ChatFormatting.RED), true);
            return;
        }
        int space = Math.min(stack.getCount(), 64 - input.getCount());
        if (space <= 0) return;
        if (input.isEmpty()) input = stack.copyWithCount(space);
        else input.grow(space);
        if (!player.getAbilities().instabuild) stack.shrink(space);
        level.playSound(null, worldPosition, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5f, 1.4f);
        sync();
        report(player);
    }

    public void fillPhial(Player player, ItemStack phials) {
        Essence best = null;
        for (Essence e : Essence.values()) if (get(e) >= PhialItem.AMOUNT && (best == null || get(e) > get(best))) best = e;
        if (best == null) {
            player.displayClientMessage(Component.translatable("msg.primeva.distiller_low").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        stored.put(best, get(best) - PhialItem.AMOUNT);
        if (!player.getAbilities().instabuild) phials.shrink(1);
        ItemStack full = new ItemStack(ModItems.phial(best));
        if (!player.getInventory().add(full)) player.drop(full, false);
        level.playSound(null, worldPosition, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1f, 1f);
        sync();
    }

    public void extract(Player player) {
        if (input.isEmpty()) return;
        if (!player.getInventory().add(input)) player.drop(input, false);
        input = ItemStack.EMPTY;
        progress = 0;
        sync();
    }

    public void report(Player player) {
        MutableComponent msg = Component.translatable("block.primeva.distiller").withStyle(ChatFormatting.GOLD).append(": ");
        boolean any = stored.values().stream().anyMatch(v -> v > 0);
        msg.append(any ? Essence.line(stored, "") : Component.translatable("msg.primeva.empty").withStyle(ChatFormatting.GRAY));
        if (!input.isEmpty()) msg.append(Component.literal("  [" + input.getCount() + "x ").withStyle(ChatFormatting.GRAY))
                .append(input.getHoverName().copy().withStyle(ChatFormatting.GRAY)).append(Component.literal("]").withStyle(ChatFormatting.GRAY));
        if (!heated(level, worldPosition)) msg.append(Component.translatable("msg.primeva.no_heat").withStyle(ChatFormatting.RED));
        player.displayClientMessage(msg, true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        putStack(tag, "input", input, registries);
        CompoundTag ess = new CompoundTag();
        stored.forEach((e, n) -> ess.putInt(e.id, n));
        tag.put("essences", ess);
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        input = getStack(tag, "input", registries);
        stored.clear();
        CompoundTag ess = tag.getCompound("essences");
        for (Essence e : Essence.values()) if (ess.contains(e.id)) stored.put(e, ess.getInt(e.id));
        progress = tag.getInt("progress");
    }
}
